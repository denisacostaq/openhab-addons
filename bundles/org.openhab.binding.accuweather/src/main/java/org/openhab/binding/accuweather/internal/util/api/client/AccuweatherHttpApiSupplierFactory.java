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

import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.accuweather.internal.exceptions.HttpErrorResponseException;
import org.openhab.binding.accuweather.internal.exceptions.RemoteErrorResponseException;
import org.openhab.binding.accuweather.internal.interfaces.ObjectMapper;
import org.openhab.binding.accuweather.internal.interfaces.cache.ExpiringValue;
import org.openhab.binding.accuweather.internal.interfaces.cache.ThrowingSupplier;
import org.openhab.binding.accuweather.internal.model.pojo.AdministrativeArea;
import org.openhab.binding.accuweather.internal.model.pojo.CitySearchResult;
import org.openhab.binding.accuweather.internal.model.pojo.CurrentConditions;
import org.openhab.binding.accuweather.internal.util.cache.ExpiringValueImpl;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AccuweatherHttpApiSupplierFactory} is responsible for creating suppliers for
 * {@link org.openhab.binding.accuweather.internal.util.api.client.AccuweatherHttpApiClient} implementation
 *
 * @author Alvaro Denis <denisacostaq@gmail.com> - Initial contribution
 */
@NonNullByDefault
public class AccuweatherHttpApiSupplierFactory<HttpRespT, CacheValT, CacheExcT extends Throwable>
        implements AccuweatherHttpApiSupplierFactoryInterface<HttpRespT, CacheValT, CacheExcT> {
    private final Logger logger = LoggerFactory.getLogger(AccuweatherHttpApiSupplierFactory.class);

    private final HttpClientRawInterface<ExpiringValue<HttpRespT>, CacheExcT> httpClientRaw;
    private final ObjectMapper mapper;

    public AccuweatherHttpApiSupplierFactory(
            final @Reference HttpClientRawInterface<ExpiringValue<HttpRespT>, CacheExcT> httpClientRaw,
            final @Reference ObjectMapper mapper) {
        this.httpClientRaw = httpClientRaw;
        this.mapper = mapper;
    }

    @Override
    public ThrowingSupplier<@Nullable ExpiringValue<CacheValT>, CacheExcT> getAdminAreasSupplier(
            final String countryDomainName, final String apiKey) throws CacheExcT {
        logger.trace("Null cache value, setting up admin areas supplier.");
        return () -> {
            try {
                ExpiringValue<HttpRespT> adminAreas = httpClientRaw.getAdminAreas(countryDomainName, apiKey);
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
    public ThrowingSupplier<@Nullable ExpiringValue<CacheValT>, CacheExcT> getCitySearchSupplier(
            AdministrativeArea adminCode, CitySearchResult cityQuery, String apiKey) {
        logger.trace("Null cache value, setting up city search supplier.");
        return () -> {
            try {
                ExpiringValue<HttpRespT> citySearch = httpClientRaw.citySearch(adminCode.countryID, adminCode.iD,
                        cityQuery.englishName, apiKey);
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
    public ThrowingSupplier<@Nullable ExpiringValue<CacheValT>, CacheExcT> getCurrentConditionsSupplier(
            CitySearchResult city, String apiKey) {
        logger.trace("Null cache value, setting up current conditions supplier.");
        return () -> {
            try {
                ExpiringValue<HttpRespT> currentConditions = httpClientRaw.getCurrentConditions(city.key, apiKey);
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
    public ThrowingSupplier<@Nullable ExpiringValue<CacheValT>, CacheExcT> getNeighborsCitiesSupplier(
            CitySearchResult city, String apiKey) {
        logger.trace("Null cache value, setting up neighbor cities supplier.");
        return () -> {
            try {
                ExpiringValue<HttpRespT> neighborsCities = httpClientRaw.neighborsCities(city.key, apiKey);
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
    public ThrowingSupplier<@Nullable ExpiringValue<CacheValT>, CacheExcT> getCitySearchByCoordinatesSupplier(
            @Nullable Float latitude, @Nullable Float longitude, String apiKey) {
        logger.trace("Null cache value, setting up city search by coordinates supplier.");
        return () -> {
            try {
                ExpiringValue<HttpRespT> citySearch = httpClientRaw.geoPositionSearch(latitude, longitude, apiKey);
                HttpRespT vj = citySearch.value();
                // FIXME(denisacostaq@gmail.com): vj should be deduced as string
                final CitySearchResult citySearchModel = mapper.deserializeSingleCitySearchResult((String) vj);
                if (Objects.isNull(citySearchModel)) {
                    throw (CacheExcT) new RemoteErrorResponseException(
                            RemoteErrorResponseException.StatusType.BAD_CLIENT, "Unable to decode response");
                }
                return new ExpiringValueImpl<>(citySearch.expiresAt(), (CacheValT) citySearchModel);
            } catch (Throwable exc) {
                if (exc instanceof RemoteErrorResponseException) {
                    CacheExcT e = (CacheExcT) exc;
                    logger.warn("Got HttpErrorResponseException, unable to get city by coordinates, details:\n{}",
                            e.getMessage());
                    throw e;
                } else {
                    logger.warn("Unexpected error, unable to get city by coordinates, details:\n{}", exc.getMessage());
                    return new ExpiringValueImpl<>(null, (CacheValT) new CitySearchResult());
                }
            }
        };
    }
}
