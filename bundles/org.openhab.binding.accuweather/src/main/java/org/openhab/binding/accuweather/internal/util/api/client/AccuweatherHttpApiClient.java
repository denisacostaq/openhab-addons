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
import org.openhab.binding.accuweather.internal.interfaces.GeoInfo;
import org.openhab.binding.accuweather.internal.interfaces.ObjectMapper;
import org.openhab.binding.accuweather.internal.interfaces.cache.ExpiringCacheMapInterface;
import org.openhab.binding.accuweather.internal.interfaces.cache.ExpiringValue;
import org.openhab.binding.accuweather.internal.interfaces.cache.ThrowingSupplier;
import org.openhab.binding.accuweather.internal.model.pojo.AdministrativeArea;
import org.openhab.binding.accuweather.internal.model.pojo.CitySearchResult;
import org.openhab.binding.accuweather.internal.model.pojo.CurrentConditions;
import org.openhab.binding.accuweather.internal.util.cache.*;
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
public class AccuweatherHttpApiClient<HttpRespT, CacheValT, CacheExcT extends Throwable> implements
        org.openhab.binding.accuweather.internal.interfaces.AccuweatherHttpApiClient<HttpRespT, CacheValT, CacheExcT>,
        GeoInfo<CacheExcT> {
    private final Logger logger = LoggerFactory.getLogger(AccuweatherHttpApiClient.class);

    private final LocationProvider locationProvider;
    private final HttpClientRawInterface<ExpiringValue<HttpRespT>, CacheExcT> httpClientRaw;
    private final ExpiringCacheMapInterface<String, CacheValT, CacheExcT> cache;
    private final ObjectMapper mapper;
    private final GeoInfo<CacheExcT> geoInfo;
    String apiKey = "";

    public AccuweatherHttpApiClient(final @Reference LocationProvider locationProvider,
            final @Reference HttpClientRawInterface<ExpiringValue<HttpRespT>, CacheExcT> httpClientRaw,
            final @Reference ObjectMapper mapper,
            final @Reference ExpiringCacheMapInterface<String, CacheValT, CacheExcT> cache) {
        this.locationProvider = locationProvider;
        this.httpClientRaw = httpClientRaw;
        this.mapper = mapper;
        this.cache = cache;
        this.geoInfo = this;
    }

    private ThrowingSupplier<@Nullable ExpiringValue<CacheValT>, CacheExcT> getAdminAreasSupplier(
            String countryDomainName) throws CacheExcT {
        logger.trace("Null cache value, setting up admin areas supplier.");
        return () -> {
            try {
                ExpiringValue<HttpRespT> adminAreas = httpClientRaw.getAdminAreas(countryDomainName, this.apiKey);
                HttpRespT vj = adminAreas.value();
                // FIXME(denisacostaq@gmail.com): vj should be deduced as string
                final List<AdministrativeArea> adminAreasModel = mapper.deserializeAdminAreasResult((String) vj);
                if (Objects.isNull(adminAreasModel)) {
                    // FIXME(denisacostaq@gmail.com): no cast
                    throw (CacheExcT) new RemoteErrorResponseException(
                            RemoteErrorResponseException.StatusType.BAD_CLIENT, "Unable to decode response");
                }
                return new ExpiringValueImpl<>(adminAreas.expiresAt(), (CacheValT) adminAreasModel);
            } catch (HttpErrorResponseException e) {
                logger.warn("Got HttpErrorResponseException, unable to get admin areas, details:\n{}", e.toString());
                // FIXME(denisacostaq@gmail.com): no cast
                throw (CacheExcT) new RemoteErrorResponseException(e);
            }
        };
    }

    @Override
    @Nullable // FIXME(denisacostaq@gmail.com): remove
    public List<AdministrativeArea> getAdminAreas(String countryDomainName) throws CacheExcT {
        String key = adminAreasCacheKey(countryDomainName);
        // FIXME(denisacostaq@gmail.com): consider expired here, priority of null vs rate vs cache
        CacheValT adminAreas = cache.get(key);
        if (Objects.isNull(adminAreas)) {
            cache.put(key, getAdminAreasSupplier(countryDomainName));
            adminAreas = cache.get(key);
        } else {
            logger.trace("using previous value from cache");
        }
        List<AdministrativeArea> adminAreasModel = (List<AdministrativeArea>) adminAreas;
        logger.trace("getting {} admin areas for country code {}", adminAreasModel.size(), countryDomainName);
        return adminAreasModel;
    }

    private ThrowingSupplier<@Nullable ExpiringValue<CacheValT>, CacheExcT> getCitySearchSupplier(
            AdministrativeArea adminCode, CitySearchResult cityQuery) {
        logger.trace("Null cache value, setting up city search supplier.");
        return () -> {
            try {
                ExpiringValue<HttpRespT> citySearch = httpClientRaw.citySearch(adminCode.countryID, adminCode.iD,
                        cityQuery.englishName, this.apiKey);
                HttpRespT vj = citySearch.value();
                // FIXME(denisacostaq@gmail.com): vj should be deduced as string
                final List<CitySearchResult> citySearchModel = mapper.deserializeCitySearchResult((String) vj);
                if (Objects.isNull(citySearchModel)) {
                    // FIXME(denisacostaq@gmail.com): no cast
                    throw (CacheExcT) new RemoteErrorResponseException(
                            RemoteErrorResponseException.StatusType.BAD_CLIENT, "Unable to decode response");
                }
                return new ExpiringValueImpl<>(citySearch.expiresAt(), (CacheValT) citySearchModel);
            } catch (HttpErrorResponseException e) {
                logger.warn(
                        "Got HttpErrorResponseException|RemoteErrorResponseException, unable to get admin areas, details:\n{}",
                        e.toString());
                // FIXME(denisacostaq@gmail.com): no cast
                throw (CacheExcT) new RemoteErrorResponseException(e);
            }
        };
    }

    @Override
    @Nullable // FIXME(denisacostaq@gmail.com): remove
    public List<CitySearchResult> citySearch(AdministrativeArea adminCode, CitySearchResult cityQuery)
            throws CacheExcT {
        String key = citySearchCacheKey(adminCode, cityQuery);
        // FIXME(denisacostaq@gmail.com): consider expired here, priority of null vs rate vs cache
        CacheValT citySearch = cache.get(key);
        if (Objects.isNull(citySearch)) {
            cache.put(key, getCitySearchSupplier(adminCode, cityQuery));
            citySearch = cache.get(key);
        } else {
            logger.trace("using previous value from cache");
        }
        List<CitySearchResult> citySearchModel = (List<CitySearchResult>) citySearch;
        logger.trace("getting {} cities for country code {}, admin code {} and city name {}", citySearchModel.size(),
                adminCode.countryID, adminCode.iD, cityQuery.englishName);
        return citySearchModel;
    }

    private ThrowingSupplier<@Nullable ExpiringValue<CacheValT>, CacheExcT> getCurrentConditionsSupplier(
            CitySearchResult city) {
        logger.trace("Null cache value, setting up current conditions supplier.");
        return () -> {
            try {
                ExpiringValue<HttpRespT> currentConditions = httpClientRaw.getCurrentConditions(city.key, this.apiKey);
                HttpRespT vj = currentConditions.value();
                // FIXME(denisacostaq@gmail.com): vj should be deduced as string
                final CurrentConditions currentConditionsModel = mapper.deserializeCurrentConditions((String) vj);
                if (Objects.isNull(currentConditionsModel)) {
                    // FIXME(denisacostaq@gmail.com): no cast
                    throw (CacheExcT) new RemoteErrorResponseException(
                            RemoteErrorResponseException.StatusType.BAD_CLIENT, "Unable to decode response");
                }
                return new ExpiringValueImpl<>(currentConditions.expiresAt(), (CacheValT) currentConditionsModel);
            } catch (HttpErrorResponseException e) {
                logger.warn("Got HttpErrorResponseException, unable to get current conditions, details:\n{}",
                        e.toString());
                // FIXME(denisacostaq@gmail.com): no cast
                throw (CacheExcT) new RemoteErrorResponseException(e);
            }
        };
    }

    @Override
    @Nullable // FIXME(denisacostaq@gmail.com): remove
    public CurrentConditions currentConditions(CitySearchResult city) throws CacheExcT {
        String key = currentConditionsCacheKey(city);
        // FIXME(denisacostaq@gmail.com): consider expired here, priority of null vs rate vs cache
        CacheValT currentConditions = cache.get(key);
        if (Objects.isNull(currentConditions)) {
            cache.put(key, getCurrentConditionsSupplier(city));
            currentConditions = cache.get(key);
        } else {
            logger.trace("using previous value from cache");
        }
        CurrentConditions currentConditionsModel = (CurrentConditions) currentConditions;
        logger.trace("getting current conditions {} for city {}", currentConditions, city.englishName);
        return currentConditionsModel;
    }

    private ThrowingSupplier<@Nullable ExpiringValue<CacheValT>, CacheExcT> getNeighborsCitiesSupplier(
            CitySearchResult city) {
        logger.trace("Null cache value, setting up neighbor cities supplier.");
        return () -> {
            try {
                ExpiringValue<HttpRespT> neighborsCities = httpClientRaw.neighborsCities(city.key, this.apiKey);
                HttpRespT vj = neighborsCities.value();
                // FIXME(denisacostaq@gmail.com): vj should be deduced as string
                final List<CitySearchResult> currentConditionsModel = mapper.deserializeCitySearchResult((String) vj);
                if (Objects.isNull(currentConditionsModel)) {
                    // FIXME(denisacostaq@gmail.com): no cast
                    throw (CacheExcT) new RemoteErrorResponseException(
                            RemoteErrorResponseException.StatusType.BAD_CLIENT, "Unable to decode response");
                }
                return new ExpiringValueImpl<>(neighborsCities.expiresAt(), (CacheValT) currentConditionsModel);
            } catch (HttpErrorResponseException e) {
                logger.warn("Got HttpErrorResponseException, unable to get neighbor cities, details:\n{}",
                        e.toString());
                // FIXME(denisacostaq@gmail.com): no cast
                throw (CacheExcT) new RemoteErrorResponseException(e);
            }
        };
    }

    @Override
    // FIXME(denisacostaq@gmail.com): duplicate code
    public List<CitySearchResult> getNeighborsCities(CitySearchResult city) throws CacheExcT {
        if (StringUtils.isEmpty(city.key)) {
            logger.info("can not get neighbors of city without city key");
            return new ArrayList<>();
        }
        String key = neighborsCitiesCacheKey(city);
        // FIXME(denisacostaq@gmail.com): consider expired here, priority of null vs rate vs cache
        CacheValT neighborsCities = cache.get(key);
        if (Objects.isNull(neighborsCities)) {
            cache.put(key, getNeighborsCitiesSupplier(city));
            neighborsCities = cache.get(key);
        } else {
            logger.trace("using previous value from cache");
        }
        List<CitySearchResult> neighborsCitiesModel = new ArrayList<>();
        ((List<CitySearchResult>) neighborsCities).stream()
                .forEach(citySearchResult -> neighborsCitiesModel.add(citySearchResult));
        logger.trace("getting {} neighbor cities for city name", city.englishName);
        return neighborsCitiesModel;
    }

    public boolean verifyHttpApiKey(String apiKey) throws CacheExcT {
        String oldApiKey = this.apiKey;
        this.apiKey = apiKey;
        String countryCode = geoInfo.getCountryDomainName(locationProvider.getLocation());
        if (StringUtils.isEmpty(countryCode)) {
            // NOTE(denisacostaq@gmail.com): this is required even if there is not location provider available
            countryCode = "BG";
        }
        List<AdministrativeArea> adminAreas = null;
        try {
            adminAreas = getAdminAreas(countryCode);
        } catch (Throwable exc) {
            // FIXME(denisacostaq@gmail.com): no cast
            CacheExcT e = (CacheExcT) exc;
            logger.debug("Exception trying to get admin areas for country code {}: {}", countryCode, e.getMessage());
            // FIXME(denisacostaq@gmail.com): Enable this throw (CacheExcT)e;
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

    private ThrowingSupplier<@Nullable ExpiringValue<CacheValT>, CacheExcT> getCitySearchByCoordinatesSupplier(
            @Nullable Float latitude, @Nullable Float longitude) {
        logger.trace("Null cache value, setting up city search by coordinates supplier.");
        return () -> {
            try {
                ExpiringValue<HttpRespT> citySearch = httpClientRaw.geoPositionSearch(latitude, longitude, this.apiKey);
                HttpRespT vj = citySearch.value();
                // FIXME(denisacostaq@gmail.com): vj should be deduced as string
                final CitySearchResult citySearchModel = mapper.deserializeSingleCitySearchResult((String) vj);
                if (Objects.isNull(citySearchModel)) {
                    throw (CacheExcT) new RemoteErrorResponseException(
                            RemoteErrorResponseException.StatusType.BAD_CLIENT, "Unable to decode response");
                }
                return new ExpiringValueImpl<>(citySearch.expiresAt(), (CacheValT) citySearchModel);
            } catch (HttpErrorResponseException e) {
                logger.warn("Got HttpErrorResponseException, unable to get city by coordinates, details:\n{}",
                        e.toString());
                throw (CacheExcT) new RemoteErrorResponseException(e);
            }
        };
    }

    private CitySearchResult citySearchByCoordinates(@Nullable PointType location) throws CacheExcT {
        Float latitude = location.getLatitude().floatValue();
        Float longitude = location.getLongitude().floatValue();
        String key = geoPositionSearchCacheKey(latitude, longitude);
        // FIXME(denisacostaq@gmail.com): consider expired here, priority of null vs rate vs cache
        CacheValT citySearchResult = cache.get(key);
        if (Objects.isNull(citySearchResult)) {
            cache.put(key, getCitySearchByCoordinatesSupplier(latitude, longitude));
            citySearchResult = cache.get(key);
        } else {
            logger.trace("using previous value from cache");
        }
        CitySearchResult citySearchResultModel = (CitySearchResult) citySearchResult;
        logger.trace("getting {} city for latitude {} and longitude {}", citySearchResultModel.englishName,
                location.getLatitude().floatValue(), location.getLongitude().floatValue());
        return citySearchResultModel;
    }

    @Override
    public String getCityName(@Nullable PointType location) throws CacheExcT {
        return citySearchByCoordinates(location).englishName;
    }

    @Override
    public String getCountryName(@Nullable PointType location) throws CacheExcT {
        return citySearchByCoordinates(location).country.englishName;
    }

    @Override
    public String getCountryDomainName(@Nullable PointType location) throws CacheExcT {
        return citySearchByCoordinates(location).country.iD;
    }

    @Override
    public String getAdministrativeArea(@Nullable PointType location) throws CacheExcT {
        return citySearchByCoordinates(location).administrativeArea.englishName;
    }

    private String geoPositionSearchCacheKey(@NotNull Float latitude, @NonNull Float longitude) {
        return String.format("%s/%f/%f", this.apiKey, latitude, longitude);
    }
}
