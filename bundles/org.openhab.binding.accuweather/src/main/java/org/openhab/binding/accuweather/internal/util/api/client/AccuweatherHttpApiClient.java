/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.accuweather.internal.interfaces.AccuweatherHttpApiSupplierFactoryInterface;
import org.openhab.binding.accuweather.internal.interfaces.GeoInfo;
import org.openhab.binding.accuweather.internal.interfaces.cache.ExpiringCacheMapInterface;
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
public class AccuweatherHttpApiClient<HttpRespT, CacheValT, CacheExcT extends Throwable> implements
        org.openhab.binding.accuweather.internal.interfaces.AccuweatherHttpApiClient<HttpRespT, CacheValT, CacheExcT>,
        GeoInfo<CacheExcT> {
    private final Logger logger = LoggerFactory.getLogger(AccuweatherHttpApiClient.class);

    private final LocationProvider locationProvider;
    private final ExpiringCacheMapInterface<String, CacheValT, CacheExcT> cache;
    private final GeoInfo<CacheExcT> geoInfo;
    private final AccuweatherHttpApiSupplierFactoryInterface<HttpRespT, CacheValT, CacheExcT> supplierFactory;
    private String apiKey = "";

    public AccuweatherHttpApiClient(final @Reference LocationProvider locationProvider,
            final @Reference ExpiringCacheMapInterface<String, CacheValT, CacheExcT> cache,
            final @Reference AccuweatherHttpApiSupplierFactoryInterface<HttpRespT, CacheValT, CacheExcT> supplierFactory) {
        this.locationProvider = locationProvider;
        this.cache = cache;
        this.supplierFactory = supplierFactory;
        this.geoInfo = this;
    }

    @Override
    @Nullable // FIXME(denisacostaq@gmail.com): remove
    public List<AdministrativeArea> getAdminAreas(String countryDomainName) throws CacheExcT {
        String key = adminAreasCacheKey(countryDomainName);
        // FIXME(denisacostaq@gmail.com): consider expired here, priority of null vs rate vs cache
        CacheValT adminAreas = cache.putIfAbsentAndGet(key,
                supplierFactory.getAdminAreasSupplier(countryDomainName, this.apiKey));
        List<AdministrativeArea> adminAreasModel = (List<AdministrativeArea>) adminAreas;
        if (logger.isTraceEnabled()) {
            if (Objects.isNull(adminAreasModel)) {
                logger.trace("getting null admin areas for country code {}", countryDomainName);
            } else {
                logger.trace("getting {} admin areas for country code {}", adminAreasModel.size(), countryDomainName);
            }
        }
        return adminAreasModel;
    }

    @Override
    @Nullable // FIXME(denisacostaq@gmail.com): remove
    public List<CitySearchResult> citySearch(AdministrativeArea adminCode, CitySearchResult cityQuery)
            throws CacheExcT {
        String key = citySearchCacheKey(adminCode, cityQuery);
        // FIXME(denisacostaq@gmail.com): consider expired here, priority of null vs rate vs cache
        CacheValT citySearch = cache.putIfAbsentAndGet(key,
                supplierFactory.getCitySearchSupplier(adminCode, cityQuery, this.apiKey));
        List<CitySearchResult> citySearchModel = (List<CitySearchResult>) citySearch;
        logger.trace("getting {} cities for country code {}, admin code {} and city name {}", citySearchModel.size(),
                adminCode.countryID, adminCode.iD, cityQuery.englishName);
        return citySearchModel;
    }

    @Override
    @Nullable // FIXME(denisacostaq@gmail.com): remove
    public CurrentConditions currentConditions(CitySearchResult city) throws CacheExcT {
        String key = currentConditionsCacheKey(city);
        // FIXME(denisacostaq@gmail.com): consider expired here, priority of null vs rate vs cache
        CacheValT currentConditions = cache.putIfAbsentAndGet(key,
                supplierFactory.getCurrentConditionsSupplier(city, this.apiKey));
        CurrentConditions currentConditionsModel = (CurrentConditions) currentConditions;
        logger.trace("getting current conditions {} for city {}", currentConditions, city.englishName);
        return currentConditionsModel;
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
        CacheValT neighborsCities = cache.putIfAbsentAndGet(key,
                supplierFactory.getNeighborsCitiesSupplier(city, this.apiKey));
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
        logger.debug("Validating API key through getting administrative areas for {}", countryCode);
        try {
            adminAreas = getAdminAreas(countryCode);
        } catch (Throwable exc) {
            // FIXME(denisacostaq@gmail.com): no cast
            CacheExcT e = (CacheExcT) exc;
            logger.debug("Exception trying to get admin areas for country code {}: {}", countryCode, e.getMessage());
            throw e;
        } finally {
            if (Objects.isNull(adminAreas) || adminAreas.isEmpty()) {
                this.apiKey = oldApiKey;
            }
        }
        return !(Objects.isNull(adminAreas) || adminAreas.isEmpty());
    }

    private String currentConditionsCacheKey(@NonNull CitySearchResult city) {
        return String.format("%s/%s/currentConditionsCacheKey", this.apiKey, city.key);
    }

    private String neighborsCitiesCacheKey(@Nullable CitySearchResult city) {
        return String.format("%s/%s/neighborsCitiesCacheKey", this.apiKey, city.key);
    }

    private String citySearchCacheKey(@NonNull AdministrativeArea adminCode, @NonNull CitySearchResult cityQuery) {
        return String.format("%s/%s/%s/%s", this.apiKey, adminCode.countryID, adminCode.iD, cityQuery.englishName);
    }

    private String adminAreasCacheKey(@NonNull String countryDomainName) {
        return String.format("%s/%s", this.apiKey, countryDomainName);
    }

    /**********************************************************************************/
    /****************************** GeoInfo *************************/
    /**********************************************************************************/

    private CitySearchResult citySearchByCoordinates(@Nullable PointType location) throws CacheExcT {
        Float latitude = location.getLatitude().floatValue();
        Float longitude = location.getLongitude().floatValue();
        String key = geoPositionSearchCacheKey(latitude, longitude);
        // FIXME(denisacostaq@gmail.com): consider expired here, priority of null vs rate vs cache
        CacheValT citySearchResult = cache.putIfAbsentAndGet(key,
                supplierFactory.getCitySearchByCoordinatesSupplier(latitude, longitude, this.apiKey));
        CitySearchResult citySearchResultModel = (CitySearchResult) citySearchResult;
        logger.trace("getting {} city for latitude {} and longitude {}", citySearchResultModel.englishName,
                location.getLatitude().floatValue(), location.getLongitude().floatValue());
        return citySearchResultModel;
    }

    @Override
    public String getCityName(@Nullable PointType location) throws CacheExcT {
        // FIXME(denisacostaq@gmail.com): handle null
        return citySearchByCoordinates(location).englishName;
    }

    @Override
    public String getCountryDomainName(@Nullable PointType location) throws CacheExcT {
        // FIXME(denisacostaq@gmail.com): handle null
        return citySearchByCoordinates(location).country.iD;
    }

    @Override
    public String getAdministrativeAreaName(@Nullable PointType location) throws CacheExcT {
        // FIXME(denisacostaq@gmail.com): handle null
        return citySearchByCoordinates(location).administrativeArea.englishName;
    }

    private String geoPositionSearchCacheKey(Float latitude, Float longitude) {
        // FIXME(denisacostaq@gmail.com): handle null
        return String.format("%s/%f/%f", this.apiKey, latitude, longitude);
    }
}
