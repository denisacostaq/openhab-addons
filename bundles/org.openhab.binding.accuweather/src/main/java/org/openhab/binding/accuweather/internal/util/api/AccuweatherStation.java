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

package org.openhab.binding.accuweather.internal.util.api;

import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.accuweather.internal.interfaces.AccuweatherHttpApiClient;
import org.openhab.binding.accuweather.internal.interfaces.WeatherStation;
import org.openhab.binding.accuweather.internal.model.pojo.AdministrativeArea;
import org.openhab.binding.accuweather.internal.model.pojo.CitySearchResult;
import org.openhab.binding.accuweather.internal.model.pojo.CurrentConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AccuweatherStation} is the WeatherStation implementation for https://www.accuweather.com/
 *
 * @author Alvaro Denis <denisacostaq@gmail.com> - Initial contribution
 */
@NonNullByDefault
public class AccuweatherStation implements WeatherStation {
    Logger logger = LoggerFactory.getLogger(AccuweatherStation.class);
    private AccuweatherHttpApiClient httpClient;
    private String cityKey = "";
    private String countryCode = "";
    private Integer adminCode = 0;
    private String cityName = "";

    public AccuweatherStation(AccuweatherHttpApiClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    @Nullable // FIXME(denisacostaq@gmail.com): remove this
    public Float getTemperature() {
        CitySearchResult city = new CitySearchResult(cityKey, "");
        CurrentConditions currentConditions = httpClient.currentConditions(city);
        if (currentConditions == null || currentConditions.temperature == null
                || currentConditions.temperature.metric == null) {
            return null;
        }
        return Double.valueOf(currentConditions.temperature.metric.value).floatValue();
    }

    @Override
    @Nullable // FIXME(denisacostaq@gmail.com): remove this
    public Float getHumidity() {
        return null;
    }

    @Override
    @Nullable // FIXME(denisacostaq@gmail.com): remove this
    public Date getCurrentTime() {
        return null;
    }

    @Override
    public Boolean hasPrecipitation() {
        CitySearchResult city = new CitySearchResult(cityKey, "");
        CurrentConditions currentConditions = httpClient.currentConditions(city);
        return currentConditions.hasPrecipitation;
    }

    public boolean resolveHttpCityKey() {
        if (StringUtils.isEmpty(this.cityKey)) {
            logger.debug("Validating API key through getting cities API");
            AdministrativeArea aa = new AdministrativeArea(String.valueOf(adminCode), countryCode);
            CitySearchResult csr = new CitySearchResult("", cityName);
            List<CitySearchResult> cities = httpClient.citySearch(aa, csr);
            if (Objects.isNull(cities) || cities.size() != 1) {
                logger.warn("expected 1 city but got {}", Objects.isNull(cities) ? "null" : cities.size());
                return false;
            }
            String cityKey = cities.get(0).key;
            if (StringUtils.isEmpty(cityKey)) {
                return false;
            }
            // Got a response so the keys are good
            this.setCityKey(cityKey);
            return true;
        }
        return false;
    }

    public void setHttpApiKey(String apiKey) {
        this.httpClient.setApiKey(apiKey);
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public void setAdminCode(Integer adminCode) {
        this.adminCode = adminCode;
    }

    public void setCityName(String locationName) {
        this.cityName = locationName;
    }

    public void setCityKey(String cityKey) {
        this.cityKey = cityKey;
    }
}
