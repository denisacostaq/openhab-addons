/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.openhab.binding.accuweather.internal.util.api.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;
import org.openhab.binding.accuweather.internal.interfaces.Cache;
import org.openhab.binding.accuweather.internal.model.pojo.AdministrativeArea;
import org.openhab.binding.accuweather.internal.model.pojo.CitySearchResult;
import org.openhab.binding.accuweather.internal.model.pojo.CurrentConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AccuweatherHttpApiClient} is responsible for preparing the requests to HttpClientRawInterface
 * and deserialize the responses to the models. Some cache handling is done here too
 *
 * @author Alvaro Denis <denisacostaq@gmail.com> - Initial contribution
 */
@NonNullByDefault
public class AccuweatherHttpApiClient
        implements org.openhab.binding.accuweather.internal.interfaces.AccuweatherHttpApiClient {
    private final Logger logger = LoggerFactory.getLogger(AccuweatherHttpApiClient.class);

    HttpClientRawInterface httpClientRaw;
    Cache cache;
    ObjectMapper mapper;
    String apiKey = "";

    public AccuweatherHttpApiClient(HttpClientRawInterface httpClientRaw, ObjectMapper mapper, Cache cache) {
        this.httpClientRaw = httpClientRaw;
        this.mapper = mapper;
        this.cache = cache;
    }

    @Override
    @Nullable // FIXME(denisacostaq@gmail.com): remove
    public List<AdministrativeArea> getAdminAreas(String countryDomainName) {
        String key = adminAreasCacheKey(countryDomainName);
        // FIXME(denisacostaq@gmail.com): consider expired here, priority of null vs rate vs cache
        String adminAreas = (String) cache.getValue(key);
        boolean foundInCache = Objects.isNull(adminAreas);
        List<AdministrativeArea> adminAreasModel;
        logger.warn("foundInCache {}", foundInCache);
        if (foundInCache) {
            logger.debug("invalid cache value, getting a new one");
            String resp = httpClientRaw.getAdminAreas(countryDomainName, this.apiKey);
            adminAreasModel = mapper.deserializeAdminAreasResult(resp);
            cache.setValue(key, resp);
        } else {
            logger.debug("using previous value from cache");
            adminAreasModel = mapper.deserializeAdminAreasResult(adminAreas);
        }
        logger.debug("getting {} admin areas for country code {}", adminAreasModel.size(), countryDomainName);
        return adminAreasModel;
    }

    @Override
    public List<CitySearchResult> citySearch(AdministrativeArea adminCode, CitySearchResult cityQuery) {
        String key = citySearchCacheKey(adminCode, cityQuery);
        // FIXME(denisacostaq@gmail.com): consider expired here, priority of null vs rate vs cache
        String citySearch = (String) cache.getValue(key);
        boolean foundInCache = Objects.isNull(citySearch);
        List<CitySearchResult> citySearchResults;
        if (foundInCache) {
            logger.debug("invalid cache value, getting a new one");
            String resp = httpClientRaw.citySearch(adminCode.countryID, adminCode.iD, cityQuery.englishName,
                    this.apiKey);
            citySearchResults = mapper.deserializeCitySearchResult(resp);
            cache.setValue(key, resp);
        } else {
            logger.debug("using previous value from cache");
            citySearchResults = mapper.deserializeCitySearchResult(citySearch);
        }
        logger.debug("getting {} cities for country code {}, admin code {} and city name {}", citySearchResults.size(),
                adminCode.countryID, adminCode.iD, cityQuery.englishName);
        return citySearchResults;
    }

    @Override
    public CurrentConditions currentConditions(CitySearchResult city) {
        String key = currentConditionsCacheKey(city);
        String currentConditions = (String) cache.getValue(key);
        // FIXME(denisacostaq@gmail.com): consider expired here, priority of null vs rate vs cache
        boolean foundInCache = Objects.isNull(currentConditions);
        CurrentConditions currentConditionsModel;
        if (foundInCache) {
            logger.debug("invalid cache value, getting a new one");
            String resp = httpClientRaw.getCurrentConditions(city.key, this.apiKey);
            currentConditionsModel = mapper.deserializeCurrentConditions(resp);
            cache.setValue(key, resp);
        } else {
            logger.debug("using previous value from cache");
            currentConditionsModel = mapper.deserializeCurrentConditions(currentConditions);
        }
        logger.debug("getting current conditions {} for city key {}", currentConditions, city.englishName);
        return currentConditionsModel;
    }

    @Override
    @Nullable // FIXME(denisacostaq@gmail.com): remove
    public List<CitySearchResult> getNeighborsCities(@Nullable CitySearchResult city) {
        if (Objects.isNull(city)) {
            logger.info("can not get neighbors of null city");
            return new ArrayList<>();
        }
        String key = neighborsCitiesCacheKey(city);
        // FIXME(denisacostaq@gmail.com): consider expired here, priority of null vs rate vs cache
        String citySearch = (String) cache.getValue(key);
        List<CitySearchResult> citySearchResults;
        boolean foundInCache = Objects.isNull(citySearch);
        if (foundInCache) {
            logger.debug("invalid cache value, getting a new one");
            String resp = httpClientRaw.neighborsCities(city.key, this.apiKey);
            citySearchResults = mapper.deserializeCitySearchResult(resp);
            cache.setValue(key, resp);
        } else {
            logger.debug("using previous value from cache");
            citySearchResults = mapper.deserializeCitySearchResult(citySearch);
        }
        logger.debug("getting {} neighbor cities for city name", city.englishName);
        return citySearchResults;
    }

    @Override
    public String getApiKey() {
        return this.apiKey;
    }

    @Override
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    private String currentConditionsCacheKey(@NotNull CitySearchResult city) {
        return String.format("%s/%s/currentConditionsCacheKey", this.apiKey, city.key);
    }

    private String neighborsCitiesCacheKey(@Nullable CitySearchResult city) {
        return String.format("%s/%s/neighborsCitiesCacheKey", this.apiKey, city.key);
    }

    private String citySearchCacheKey(@NotNull AdministrativeArea adminCode, @NotNull CitySearchResult cityQuery) {
        return String.format("%s/%s/%s/%s", this.apiKey, adminCode.countryID, adminCode.iD, cityQuery.englishName);
    }

    private String adminAreasCacheKey(@NotNull String countryDomainName) {
        return String.format("%s/%s", this.apiKey, countryDomainName);
    }
}
