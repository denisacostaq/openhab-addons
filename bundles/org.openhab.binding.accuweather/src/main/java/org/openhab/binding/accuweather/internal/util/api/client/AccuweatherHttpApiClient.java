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

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;
import org.openhab.binding.accuweather.internal.exceptions.HttpErrorResponseException;
import org.openhab.binding.accuweather.internal.exceptions.RemoteErrorResponseException;
import org.openhab.binding.accuweather.internal.interfaces.Cache;
import org.openhab.binding.accuweather.internal.interfaces.GeoInfo;
import org.openhab.binding.accuweather.internal.interfaces.ObjectMapper;
import org.openhab.binding.accuweather.internal.model.pojo.AdministrativeArea;
import org.openhab.binding.accuweather.internal.model.pojo.CitySearchResult;
import org.openhab.binding.accuweather.internal.model.pojo.CurrentConditions;
import org.openhab.core.i18n.LocationProvider;
import org.openhab.core.library.types.PointType;
import org.osgi.service.component.annotations.Reference;
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
        implements org.openhab.binding.accuweather.internal.interfaces.AccuweatherHttpApiClient, GeoInfo {
    private final Logger logger = LoggerFactory.getLogger(AccuweatherHttpApiClient.class);

    private final LocationProvider locationProvider;
    private final HttpClientRawInterface httpClientRaw;
    private final Cache cache;
    private final ObjectMapper mapper;
    private final GeoInfo geoInfo;
    String apiKey = "";

    public AccuweatherHttpApiClient(final @Reference LocationProvider locationProvider,
            final @Reference HttpClientRawInterface httpClientRaw, final @Reference ObjectMapper mapper,
            final @Reference Cache cache) {
        this.locationProvider = locationProvider;
        this.httpClientRaw = httpClientRaw;
        this.mapper = mapper;
        this.cache = cache;
        this.geoInfo = this;
    }

    @Override
    @Nullable // FIXME(denisacostaq@gmail.com): remove
    public List<AdministrativeArea> getAdminAreas(String countryDomainName) throws RemoteErrorResponseException {
        String key = adminAreasCacheKey(countryDomainName);
        // FIXME(denisacostaq@gmail.com): consider expired here, priority of null vs rate vs cache
        String adminAreas = (String) cache.getValue(key);
        boolean notFoundInCache = StringUtils.isEmpty(adminAreas);
        List<AdministrativeArea> adminAreasModel;
        if (notFoundInCache) {
            logger.trace("invalid cache value, getting a new one");
            try {
                adminAreas = httpClientRaw.getAdminAreas(countryDomainName, this.apiKey);
            } catch (HttpErrorResponseException e) {
                logger.warn("unable to get admin areas, details:\n{}", e.toString());
                throw new RemoteErrorResponseException(e);
            }
        } else {
            logger.trace("using previous value from cache");
        }
        adminAreasModel = mapper.deserializeAdminAreasResult(adminAreas);
        if (notFoundInCache) {
            cache.setValue(key, adminAreas);
        }
        logger.trace("getting {} admin areas for country code {}", adminAreasModel.size(), countryDomainName);
        return adminAreasModel;
    }

    @Override
    public List<CitySearchResult> citySearch(AdministrativeArea adminCode, CitySearchResult cityQuery)
            throws RemoteErrorResponseException {
        String key = citySearchCacheKey(adminCode, cityQuery);
        // FIXME(denisacostaq@gmail.com): consider expired here, priority of null vs rate vs cache
        String citySearch = (String) cache.getValue(key);
        boolean notFoundInCache = StringUtils.isEmpty(citySearch);
        List<CitySearchResult> citySearchResults;
        if (notFoundInCache) {
            logger.trace("invalid cache value, getting a new one");
            try {
                citySearch = httpClientRaw.citySearch(adminCode.countryID, adminCode.iD, cityQuery.englishName,
                        this.apiKey);
            } catch (HttpErrorResponseException e) {
                logger.warn("unable to get cities, details:\n{}", e.toString());
                throw new RemoteErrorResponseException(e);
            }
        } else {
            logger.trace("using previous value from cache");
        }
        citySearchResults = mapper.deserializeCitySearchResult(citySearch);
        if (notFoundInCache) {
            cache.setValue(key, citySearch);
        }
        logger.trace("getting {} cities for country code {}, admin code {} and city name {}", citySearchResults.size(),
                adminCode.countryID, adminCode.iD, cityQuery.englishName);
        return citySearchResults;
    }

    @Override
    public CurrentConditions currentConditions(CitySearchResult city) throws RemoteErrorResponseException {
        String key = currentConditionsCacheKey(city);
        String currentConditions = (String) cache.getValue(key);
        boolean notFoundInCache = StringUtils.isEmpty(currentConditions);
        // FIXME(denisacostaq@gmail.com): consider expired here, priority of null vs rate vs cache
        CurrentConditions currentConditionsModel;
        if (notFoundInCache) {
            logger.trace("invalid cache value, getting a new one");
            try {
                currentConditions = httpClientRaw.getCurrentConditions(city.key, this.apiKey);
            } catch (HttpErrorResponseException e) {
                logger.warn("unable to get current conditions, details:\n{}", e.toString());
                throw new RemoteErrorResponseException(e);
            }
        } else {
            logger.trace("using previous value from cache");
        }
        currentConditionsModel = mapper.deserializeCurrentConditions(currentConditions);
        if (notFoundInCache) {
            cache.setValue(key, currentConditions);
        }
        logger.trace("getting current conditions {} for city key {}", currentConditions, city.englishName);
        return currentConditionsModel;
    }

    @Override
    // FIXME(denisacostaq@gmail.com): duplicate code
    public List<CitySearchResult> getNeighborsCities(CitySearchResult city) throws RemoteErrorResponseException {
        if (StringUtils.isEmpty(city.key)) {
            logger.info("can not get neighbors of city without city key");
            return new ArrayList<>();
        }
        String key = neighborsCitiesCacheKey(city);
        // FIXME(denisacostaq@gmail.com): consider expired here, priority of null vs rate vs cache
        String citySearch = (String) cache.getValue(key);
        boolean notFoundInCache = StringUtils.isEmpty(citySearch);
        List<CitySearchResult> citySearchResults;
        if (notFoundInCache) {
            logger.trace("invalid cache value, getting a new one");
            try {
                citySearch = httpClientRaw.neighborsCities(city.key, this.apiKey);
            } catch (HttpErrorResponseException e) {
                logger.warn("unable to get neighbor cities, details:\n{}", e.toString());
                throw new RemoteErrorResponseException(e);
            }
        } else {
            logger.trace("using previous value from cache");
        }
        citySearchResults = mapper.deserializeCitySearchResult(citySearch);
        if (notFoundInCache) {
            cache.setValue(key, citySearch);
        }
        logger.trace("getting {} neighbor cities for city name", city.englishName);
        return citySearchResults;
    }

    public boolean verifyHttpApiKey(String apiKey) throws RemoteErrorResponseException {
        String countryCode = geoInfo.getCountryDomainName(locationProvider.getLocation());
        if (StringUtils.isEmpty(countryCode)) {
            // NOTE(denisacostaq@gmail.com): this is required even if there is not location provider available
            countryCode = "BG";
        }
        String oldApiKey = this.apiKey;
        this.apiKey = apiKey;
        List<AdministrativeArea> adminAreas = null;
        try {
            adminAreas = getAdminAreas(countryCode);
        } catch (RemoteErrorResponseException e) {
            logger.debug("Exception trying to get admin areas for country code {}: {}", countryCode, e.getMessage());
            throw e;
        } finally {
            if (Objects.isNull(adminAreas) || adminAreas.isEmpty()) {
                this.apiKey = oldApiKey;
            }
        }
        return !(Objects.isNull(adminAreas) || adminAreas.isEmpty());
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

    /**********************************************************************************/
    /****************************** GeoInfo *************************/
    /**********************************************************************************/

    private CitySearchResult citySearchByCoordinates(@Nullable PointType location) throws RemoteErrorResponseException {
        Float latitude = location.getLatitude().floatValue();
        Float longitude = location.getLongitude().floatValue();
        String key = geoPositionSearchCacheKey(latitude, longitude);
        // FIXME(denisacostaq@gmail.com): consider expired here, priority of null vs rate vs cache
        String citySearch = (String) cache.getValue(key);
        boolean notFoundInCache = StringUtils.isEmpty(citySearch);
        CitySearchResult citySearchResult;
        if (notFoundInCache) {
            logger.trace("invalid cache value, getting a new one");
            try {
                citySearch = httpClientRaw.geoPositionSearch(latitude, longitude, this.apiKey);
            } catch (HttpErrorResponseException e) {
                logger.warn("unable to get cities from coordinates, details:\n{}", e.toString());
                throw new RemoteErrorResponseException(e);
            }
        } else {
            logger.trace("using previous value from cache");
        }
        citySearchResult = mapper.deserializeSingleCitySearchResult(citySearch);
        if (notFoundInCache) {
            cache.setValue(key, citySearch);
        }
        logger.trace("getting {} city for latitude {} and longitude {}", citySearchResult.englishName,
                location.getLatitude().floatValue(), location.getLongitude().floatValue());
        return citySearchResult;
    }

    @Override
    public String getCityName(@Nullable PointType location) throws RemoteErrorResponseException {
        return citySearchByCoordinates(location).englishName;
    }

    @Override
    public String getCountryName(@Nullable PointType location) throws RemoteErrorResponseException {
        return citySearchByCoordinates(location).country.englishName;
    }

    @Override
    public String getCountryDomainName(@Nullable PointType location) throws RemoteErrorResponseException {
        return citySearchByCoordinates(location).country.iD;
    }

    @Override
    public String getAdministrativeArea(@Nullable PointType location) throws RemoteErrorResponseException {
        return citySearchByCoordinates(location).administrativeArea.englishName;
    }

    private String geoPositionSearchCacheKey(@NotNull Float latitude, @NonNull Float longitude) {
        return String.format("%s/%f/%f", this.apiKey, latitude, longitude);
    }
}
