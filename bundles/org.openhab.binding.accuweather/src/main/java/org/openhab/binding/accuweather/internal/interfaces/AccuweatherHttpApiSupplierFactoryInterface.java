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

package org.openhab.binding.accuweather.internal.interfaces;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.accuweather.internal.interfaces.cache.ExpiringValue;
import org.openhab.binding.accuweather.internal.interfaces.cache.ThrowingSupplier;
import org.openhab.binding.accuweather.internal.model.pojo.AdministrativeArea;
import org.openhab.binding.accuweather.internal.model.pojo.CitySearchResult;

/**
 * The {@link AccuweatherHttpApiSupplierFactoryInterface} is responsible for creating suppliers for
 * {@link org.openhab.binding.accuweather.internal.interfaces.AccuweatherHttpApiClient} interface
 *
 * @author Alvaro Denis <denisacostaq@gmail.com> - Initial contribution
 */
@NonNullByDefault
public interface AccuweatherHttpApiSupplierFactoryInterface<HttpRespT, CacheValT, CacheExcT extends Throwable> {
    ThrowingSupplier<@Nullable ExpiringValue<CacheValT>, CacheExcT> getAdminAreasSupplier(String countryDomainName,
            String apiKey) throws CacheExcT;

    ThrowingSupplier<@Nullable ExpiringValue<CacheValT>, CacheExcT> getCitySearchSupplier(AdministrativeArea adminCode,
            CitySearchResult cityQuery, String apiKey);

    ThrowingSupplier<@Nullable ExpiringValue<CacheValT>, CacheExcT> getCurrentConditionsSupplier(CitySearchResult city,
            String apiKey);

    ThrowingSupplier<@Nullable ExpiringValue<CacheValT>, CacheExcT> getNeighborsCitiesSupplier(CitySearchResult city,
            String apiKey);

    ThrowingSupplier<@Nullable ExpiringValue<CacheValT>, CacheExcT> getCitySearchByCoordinatesSupplier(
            @Nullable Float latitude, @Nullable Float longitude, String apiKey);
}
