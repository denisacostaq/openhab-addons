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
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.openhab.binding.accuweather.internal.handler.AccuweatherBridgeHandler;
import org.openhab.binding.accuweather.internal.interfaces.ObjectMapper;
import org.openhab.binding.accuweather.internal.model.pojo.AdministrativeArea;
import org.openhab.binding.accuweather.internal.model.pojo.CitySearchResult;
import org.openhab.binding.accuweather.internal.model.pojo.CurrentConditions;
import org.openhab.binding.accuweather.internal.model.pojo.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link AccuweatherBridgeHandler} is responsible for deserializing json responses from accuweather.com
 *
 * @author Alvaro Denis <denisacostaq@gmail.com> - Initial contribution
 */
public class ObjectMapperJson implements ObjectMapper {
    private final Logger logger = LoggerFactory.getLogger(ObjectMapperJson.class);
    private final Gson gson = new Gson();

    @Override
    public List<CitySearchResult> deserializeCitySearchResult(String json) {
        try {
            return Arrays.stream(gson.fromJson(json, CitySearchResult[].class)).collect(Collectors.toList());
        } catch (JsonSyntaxException e) {
            logger.debug("Got JsonSyntaxException: {}", e.getMessage());
            // FIXME(denisacostaq@gmail.com): setThingOfflineWithCommError(e.getMessage(), "Error parsing json
            // response");
            // rescheduleValidateKeysJob();
        }
        return null;
    }

    @Override
    public CitySearchResult deserializeSingleCitySearchResult(String json) {
        try {
            return gson.fromJson(json, CitySearchResult.class);
        } catch (JsonSyntaxException e) {
            logger.debug("Got JsonSyntaxException: {}", e.getMessage());
            // FIXME(denisacostaq@gmail.com): setThingOfflineWithCommError(e.getMessage(), "Error parsing json
            // response");
            // rescheduleValidateKeysJob();
        }
        return null;
    }

    @Override
    public List<AdministrativeArea> deserializeAdminAreasResult(String json) {
        try {
            return Arrays.stream(gson.fromJson(json, AdministrativeArea[].class)).collect(Collectors.toList());
        } catch (JsonSyntaxException e) {
            logger.debug("Got JsonSyntaxException: {}", e.getMessage());
            // FIXME(denisacostaq@gmail.com): setThingOfflineWithCommError(e.getMessage(), "Error parsing json
            // response");
        }
        return null;
    }

    @Override
    public CurrentConditions deserializeCurrentConditions(String json) {
        try {
            CurrentConditions[] currentConditions = gson.fromJson(json, CurrentConditions[].class);
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

    @Override
    public ErrorResponse deserializeErrorResponse(String json) {
        try {
            return gson.fromJson(json, ErrorResponse.class);
        } catch (JsonSyntaxException e) {
            logger.debug("Got JsonSyntaxException: {}", e.getMessage());
            // FIXME(denisacostaq@gmail.com): setThingOfflineWithCommError(e.getMessage(), "Error parsing json
            // response");
            // rescheduleValidateKeysJob();
        }
        return null;
    }

    @Override
    public boolean canDeserializeContentType(String ct) {
        return StringUtils.equals(ct, "application/json");
    }

    @Override
    public String getCityKey(String json) {
        List<CitySearchResult> citySearchResults = deserializeCitySearchResult(json);
        if (citySearchResults.isEmpty()) {
            return "";
        }
        return citySearchResults.get(0).key;
    }

    @Override
    public Double getTemperature(String json) {
        CurrentConditions currentConditions = deserializeCurrentConditions(json);
        if (currentConditions == null || currentConditions.temperature == null
                || currentConditions.temperature.metric == null) {
            return null;
        }
        return currentConditions.temperature.metric.value;
    }

    @Override
    public Boolean hasPrecipitation(String json) {
        CurrentConditions currentConditions = deserializeCurrentConditions(json);
        if (currentConditions == null) {
            return null;
        }
        return currentConditions.hasPrecipitation;
    }
}
