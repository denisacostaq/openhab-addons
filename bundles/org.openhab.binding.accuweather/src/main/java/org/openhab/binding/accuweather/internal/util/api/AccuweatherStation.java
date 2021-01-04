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

import org.apache.commons.lang.StringUtils;
import org.openhab.binding.accuweather.internal.interfaces.Cache;
import org.openhab.binding.accuweather.internal.interfaces.WeatherStation;
import org.openhab.binding.accuweather.internal.util.api.client.HttpClient;
import org.openhab.binding.accuweather.internal.util.api.client.ObjectMapper;

/**
 * The {@link AccuweatherStation} is the WeatherStation implementation for https://www.accuweather.com/
 *
 * @author Alvaro Denis <denisacostaq@gmail.com> - Initial contribution
 */
public class AccuweatherStation implements WeatherStation {
    private static final String TEMPEATURE_KEY = "11ba66f6-7e84-424e-97e8-f362756d4ed4";
    private static final String HAS_PRESIPITACION_KEY = "ae781ae5-618d-4c6b-b6f2-f064b8706a26";
    private Cache cache;
    private ObjectMapper mapper;
    private HttpClient httpClient;

    public AccuweatherStation(Cache cache, ObjectMapper mapper, HttpClient httpClient) {
        this.cache = cache;
        this.mapper = mapper;
        this.httpClient = httpClient;
    }

    @Override
    public Float getTemperature() {
        Float temp = (Float) cache.getValue(TEMPEATURE_KEY);
        if (temp == null) {
            String json = httpClient.getCurrentConditions();
            temp = mapper.getTemperature(json).floatValue();
            cache.setValue(TEMPEATURE_KEY, temp);
        }
        return temp;
    }

    @Override
    public Float getHumidity() {
        return null;
    }

    @Override
    public Date getCurrentTime() {
        return null;
    }

    @Override
    public Boolean hasPrecipitation() {
        Boolean hasPrecip = (Boolean) cache.getValue(HAS_PRESIPITACION_KEY);
        if (hasPrecip == null) {
            String json = httpClient.getCurrentConditions();
            hasPrecip = mapper.hasPrecipitation(json);
            cache.setValue(HAS_PRESIPITACION_KEY, hasPrecip);
        }
        return hasPrecip;
    }

    public boolean resolveHttpCityKey() {
        if (StringUtils.isEmpty(httpClient.getCityKey())) {
            String json = httpClient.resolveCityKey();
            String cityKey = mapper.getCityKey(json);
            if (StringUtils.isEmpty(cityKey)) {
                return false;
            }
            httpClient.setCityKey(cityKey);
            return true;
        }
        return false;
    }

    public void setHttpApiKey(String apiKey) {
        this.httpClient.setApiKey(apiKey);
    }

    public void setHttpCountryCode(String countryCode) {
        this.httpClient.setCountryCode(countryCode);
    }

    public void setHttpAdminCode(Integer adminCode) {
        this.httpClient.setAdminCode(adminCode);
    }

    public void setLocationName(String locationName) {
        this.httpClient.setLocationName(locationName);
    }

    public void setCityKey(String cityKey) {
        this.httpClient.setCityKey(cityKey);
    }
}
