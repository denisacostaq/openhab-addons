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

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;
import org.openhab.binding.accuweather.internal.interfaces.Cache;
import org.openhab.binding.accuweather.internal.model.pojo.AdministrativeArea;
import org.openhab.binding.accuweather.internal.model.pojo.CitySearchResult;
import org.openhab.binding.accuweather.internal.model.pojo.CurrentConditions;

/**
 * The {@link AccuweatherHttpApiClient} is responsible for preparing the requests to HttpClientRawInterface
 * and deserialize the responses to the models. Some cache handling is done here too
 *
 * @author Alvaro Denis <denisacostaq@gmail.com> - Initial contribution
 */
@NonNullByDefault
public class AccuweatherHttpApiClient
        implements org.openhab.binding.accuweather.internal.interfaces.AccuweatherHttpApiClient {

    private static final String TEMPEATURE_KEY = "11ba66f6-7e84-424e-97e8-f362756d4ed4";
    private static final String HAS_PRESIPITACION_KEY = "ae781ae5-618d-4c6b-b6f2-f064b8706a26";

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
        return null;
    }

    @Override
    public List<CitySearchResult> citySearch(AdministrativeArea adminCode, CitySearchResult cityQuery) {
        String key = citySearchCacheKey(adminCode, cityQuery);
        String citySearch = (String) cache.getValue(key);
        // FIXME(denisacostaq@gmail.com): consider expired here, priority of null vs rate vs cache
        if (Objects.isNull(citySearch)) {
            String resp = httpClientRaw.citySearch(adminCode.countryID, adminCode.iD, cityQuery.englishName,
                    this.apiKey);
            citySearch = resp;
            cache.setValue(key, citySearch);
        }
        return Arrays.asList(mapper.deserializeCitySearchResult(citySearch));
    }

    @Override
    public CurrentConditions currentConditions(CitySearchResult city) {
        String key = currentConditionsCacheKey(city);
        String currentConditions = (String) cache.getValue(key);
        // FIXME(denisacostaq@gmail.com): consider expired here, priority of null vs rate vs cache
        if (Objects.isNull(currentConditions)) {
            String resp = httpClientRaw.getCurrentConditions(city.key, this.apiKey);
            currentConditions = resp;
            cache.setValue(key, currentConditions);
        }
        return mapper.deserializeCurrentConditions(currentConditions);
    }

    @Override
    @Nullable // FIXME(denisacostaq@gmail.com): remove
    public List<CitySearchResult> getNeighborsCities(CitySearchResult cityQuery) {
        return null;
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
        return String.format("%s/%s", this.apiKey, city.key);
    }

    private String citySearchCacheKey(@NotNull AdministrativeArea adminCode, @NotNull CitySearchResult cityQuery) {
        return String.format("%s/%s/%s/%s", this.apiKey, adminCode.countryID, adminCode.iD, cityQuery.englishName);
    }
}
