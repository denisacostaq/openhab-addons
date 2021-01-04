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
package org.openhab.binding.accuweather.internal;

import static org.openhab.binding.accuweather.internal.AccuweatherBindingConstants.*;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.accuweather.internal.model.pojo.CitySearchResult;
import org.openhab.core.io.net.http.HttpUtil;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link AccuweatherHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Alvaro Denis <denisacostaq@gmail.com> - Initial contribution
 */
@NonNullByDefault
public class AccuweatherHandler extends BaseBridgeHandler {
    // URL to retrieve device list from Ambient Weather
    private static final String LOCATIONS_URL = "http://dataservice.accuweather.com/locations/v1/cities/%COUNTRY_CODE%/%ADMIN_CODE%/search?apikey=%API_KEY%&q=%LOCATION_NAME%";

    // Timeout of the call to the Ambient Weather devices API
    public static final int DEVICES_API_TIMEOUT = 20000;

    // Time to wait after failed key validation
    public static final long KEY_VALIDATION_DELAY = 60L;

    private final Logger logger = LoggerFactory.getLogger(AccuweatherHandler.class);
    private final Gson gson = new Gson();

    private @Nullable AccuweatherConfiguration config;

    private String apiKey = "";
    private String countryCode = "";
    private Integer adminCode = 0;
    private String locationName = "";

    public AccuweatherHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void initialize() {
        config = getConfigAs(AccuweatherConfiguration.class);

        // TODO: Initialize the handler.
        // The framework requires you to return from this method quickly. Also, before leaving this method a thing
        // status from one of ONLINE, OFFLINE or UNKNOWN must be set. This might already be the real thing status in
        // case you can decide it directly.
        // In case you can not decide the thing status directly (e.g. for long running connection handshake using WAN
        // access or similar) you should set status UNKNOWN here and then decide the real status asynchronously in the
        // background.

        // set the thing status to UNKNOWN temporarily and let the background task decide for the real status.
        // the framework is then able to reuse the resources from the thing handler initialization.
        // we set this upfront to reliably check status updates in unit tests.
        updateStatus(ThingStatus.UNKNOWN);

        // Example for background initialization:
        scheduler.execute(() -> {
            String locationKey = getCityKey();
            if (!StringUtils.isEmpty(locationKey)) {
                logger.trace("locationKey {}", locationKey);
                updateStatus(ThingStatus.ONLINE);
                // listener.start(applicationKey, apiKey, gson);
            } else {
                updateStatus(ThingStatus.OFFLINE);
            }
        });

        // These logging types should be primarily used by bindings
        // logger.trace("Example trace message");
        // logger.debug("Example debug message");
        // logger.warn("Example warn message");

        // Note: When initialization can NOT be done set the status with more details for further
        // analysis. See also class ThingStatusDetail for all available status details.
        // Add a description to give user information to understand why thing does not work as expected. E.g.
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
        // "Can not access device as username and/or password are invalid");
    }

    private boolean hasRequiredFields() {
        return hasApiKey() && hasCountryCode() && hasAdminCode() && hasLocationName();
    }

    /*
     * Check if an APY key has been provided in the thing config
     */
    private boolean hasApiKey() {
        String configApiKey = config.apiKey;
        if (StringUtils.isEmpty(configApiKey)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Missing API key");
            return false;
        }
        apiKey = configApiKey;
        return true;
    }

    private boolean hasCountryCode() {
        String configCountryCode = config.countryCode;
        if (StringUtils.isEmpty(configCountryCode)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Missing country code");
            return false;
        }
        countryCode = configCountryCode;
        return true;
    }

    private boolean hasAdminCode() {
        Integer configAdminCode = config.adminCode;
        if (configAdminCode == null || configAdminCode == 0) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Missing administration code");
            return false;
        }
        adminCode = configAdminCode;
        return true;
    }

    private boolean hasLocationName() {
        String configLocationName = config.locationName;
        if (StringUtils.isEmpty(configLocationName)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Missing location name");
            return false;
        }
        locationName = configLocationName;
        return true;
    }

    /**
     *
     * @return the city key
     */
    public String getCityKey() {
        if (!hasRequiredFields()) {
            return "";
        }
        logger.debug("Validating API key through getting cities API");
        try {
            // Query locations from Accuweather
            String url = LOCATIONS_URL.replace("%COUNTRY_CODE%", countryCode)
                    .replace("%ADMIN_CODE%", adminCode.toString()).replace("%API_KEY%", apiKey)
                    .replace("%LOCATION_NAME%", locationName);
            // FIXME(denisacostaq@gmail.com): Use the builded url instead
            url = "http://localhost:8000/City_Search_results_narrowed_by_countryCode_and_adminCode_.json";
            logger.debug(
                    "Bridge: Querying City Search (results narrowed by countryCode and adminCode Accuweather service");
            String response = HttpUtil.executeUrl("GET", url, DEVICES_API_TIMEOUT);
            logger.trace("Bridge: Response = {}", response);
            // Got a response so the keys are good
            CitySearchResult[] cities = gson.fromJson(response, CitySearchResult[].class);
            logger.trace("Bridge: API key is valid with");
            if (cities.length > 0) {
                if (cities.length > 1) {
                    logger.warn("Expected a single result for locations but got {}", cities.length);
                }
                CitySearchResult city = cities[0];
                return city.key;
            }
        } catch (IOException e) {
            // executeUrl throws IOException when it gets a Not Authorized (401) response
            logger.debug("Bridge: Got IOException: {}", e.getMessage());
            setThingOfflineWithCommError(e.getMessage(), "Invalid API or application key");
            // rescheduleValidateKeysJob();
        } catch (IllegalArgumentException e) {
            logger.debug("Bridge: Got IllegalArgumentException: {}", e.getMessage());
            setThingOfflineWithCommError(e.getMessage(), "Unable to get devices");
            // rescheduleValidateKeysJob();
        } catch (JsonSyntaxException e) {
            logger.debug("Bridge: Got JsonSyntaxException: {}", e.getMessage());
            setThingOfflineWithCommError(e.getMessage(), "Error parsing json response");
            // rescheduleValidateKeysJob();
        }
        logger.warn("Unable to get location Key (id)");
        return "";
    }

    public void setThingOfflineWithCommError(@Nullable String errorDetail, @Nullable String statusDescription) {
        String status = statusDescription != null ? statusDescription : "null";
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, status);
    }
}
