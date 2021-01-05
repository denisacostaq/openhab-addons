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

import org.openhab.binding.accuweather.internal.handler.AccuweatherBridgeHandler;
import org.openhab.binding.accuweather.internal.model.pojo.CitySearchResult;
import org.openhab.binding.accuweather.internal.model.pojo.CurrentConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link AccuweatherBridgeHandler} is responsible for deserializing responses from accuweather.com
 *
 * @author Alvaro Denis <denisacostaq@gmail.com> - Initial contribution
 */
public class ObjectMapper {
    private final Logger logger = LoggerFactory.getLogger(ObjectMapper.class);
    private final Gson gson = new Gson();

    public CitySearchResult deserializeCitySearchResult(String json) {
        try {
            CitySearchResult[] cities = gson.fromJson(json, CitySearchResult[].class);
            logger.trace("Bridge: API key is valid with");
            if (cities.length > 0) {
                if (cities.length > 1) {
                    logger.warn("Expected a single result for city search but got {}", cities.length);
                }
                return cities[0];
            }
        } catch (JsonSyntaxException e) {
            logger.debug("Got JsonSyntaxException: {}", e.getMessage());
            // FIXME(denisacostaq@gmail.com): setThingOfflineWithCommError(e.getMessage(), "Error parsing json
            // response");
            // rescheduleValidateKeysJob();
        }
        return null;
    }

    public CurrentConditions deserializeCurrentConditions(String json) {
        try {
            CurrentConditions[] currentConditions = gson.fromJson(json, CurrentConditions[].class);
            logger.trace("Bridge: API key is valid with");
            if (currentConditions.length > 0) {
                if (currentConditions.length > 1) {
                    logger.warn("Expected a single result for current conditions but got {}", currentConditions.length);
                }
                return currentConditions[0];
            }
        } catch (JsonSyntaxException e) {
            logger.debug("Got JsonSyntaxException: {}", e.getMessage());
            // FIXME(denisacostaq@gmail.com): setThingOfflineWithCommError(e.getMessage(), "Error parsing json
            // response");
            // rescheduleValidateKeysJob();
        }
        return null;
    }

    public String getCityKey(String json) {
        CitySearchResult citySearchResult = deserializeCitySearchResult(json);
        if (citySearchResult == null) {
            return "";
        }
        return citySearchResult.key;
    }

    public Double getTemperature(String json) {
        CurrentConditions currentConditions = deserializeCurrentConditions(json);
        if (currentConditions == null || currentConditions.temperature == null
                || currentConditions.temperature.metric == null) {
            return null;
        }
        return currentConditions.temperature.metric.value;
    }

    public Boolean hasPrecipitation(String json) {
        CurrentConditions currentConditions = deserializeCurrentConditions(json);
        if (currentConditions == null) {
            return null;
        }
        return currentConditions.hasPrecipitation;
    }
}
