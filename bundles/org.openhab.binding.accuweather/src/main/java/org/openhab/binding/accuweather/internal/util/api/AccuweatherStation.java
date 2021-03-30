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

package org.openhab.binding.accuweather.internal.util.api;

import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.accuweather.internal.interfaces.AccuweatherHttpApiClient;
import org.openhab.binding.accuweather.internal.interfaces.WeatherStation;
import org.openhab.binding.accuweather.internal.model.pojo.AdministrativeArea;
import org.openhab.binding.accuweather.internal.model.pojo.CitySearchResult;
import org.openhab.binding.accuweather.internal.model.pojo.CurrentConditions;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AccuweatherStation} is the WeatherStation implementation for https://www.accuweather.com/
 *
 * @author Alvaro Denis <denisacostaq@gmail.com> - Initial contribution
 */
@NonNullByDefault
public class AccuweatherStation<HttpRespT, CacheValT, E extends Throwable>
        implements WeatherStation<HttpRespT, CacheValT, E> {
    Logger logger = LoggerFactory.getLogger(AccuweatherStation.class);
    private @Nullable AccuweatherHttpApiClient<HttpRespT, CacheValT, E> httpClient;
    private String cityKey = "";
    private String countryCode = "";
    private Integer adminCode = 0;
    private String cityName = "";

    public void setHttpClient(final @Reference AccuweatherHttpApiClient<HttpRespT, CacheValT, E> httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public boolean verifyStationConfigParams(String countryCode, Integer adminCode, String cityName) throws E {
        String oldCountryCode = this.countryCode;
        Integer oldAdminCode = this.adminCode;
        String oldCityName = this.cityName;
        String oldCityKey = this.cityKey;
        this.countryCode = countryCode;
        this.adminCode = adminCode;
        this.cityName = cityName;
        CurrentConditions currentConditions = null;
        try {
            List<CitySearchResult> cities = httpClient.citySearch(
                    new AdministrativeArea(String.valueOf(adminCode), countryCode), new CitySearchResult("", cityName));
            if (cities.size() == 1) {
                this.cityKey = cities.get(0).key;
                currentConditions = currentConditions();
            } else if (cities.isEmpty()) {
                logger.debug("Unable to get any city for country code {}, admin code {}, city name {}", countryCode,
                        adminCode, cityName);
            } else {
                logger.debug(
                        "getting more than one cities for country code {}, admin code {} and city name {} looks suspicious",
                        countryCode, adminCode, cityName);
            }
        } catch (Throwable exc) {
            // FIXME(denisacostaq@gmail.com): no cast
            E e = (E) exc;
            logger.warn("unable to validate {} config params {}", AccuweatherStation.class.getSimpleName(),
                    e.getMessage());
            throw exc;
        } finally {
            if (Objects.isNull(currentConditions)) {
                this.countryCode = oldCountryCode;
                this.adminCode = oldAdminCode;
                this.cityName = oldCityName;
                this.cityKey = oldCityKey;
            }
        }
        return !Objects.isNull(currentConditions);
    }

    @Nullable // FIXME(denisacostaq@gmail.com): remove
    @Override
    public CurrentConditions currentConditions() throws E {
        return httpClient.currentConditions(new CitySearchResult(this.cityKey, cityName));
    }
}
