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
package org.openhab.binding.accuweather.internal.handler;

import static org.openhab.binding.accuweather.internal.AccuweatherBindingConstants.UID_STATION;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.accuweather.internal.config.AccuweatherConfiguration;
import org.openhab.binding.accuweather.internal.util.api.AccuweatherStation;
import org.openhab.core.thing.*;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AccuweatherBridgeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Alvaro Denis <denisacostaq@gmail.com> - Initial contribution
 */
@NonNullByDefault
public class AccuweatherBridgeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(AccuweatherBridgeHandler.class);

    private @Nullable AccuweatherConfiguration config;

    private String apiKey = "";
    private String countryCode = "";
    private Integer adminCode = 0;
    private String cityName = "";
    private AccuweatherStation accuweatherStation;

    public AccuweatherBridgeHandler(Bridge bridge, AccuweatherStation accuweatherStation) {
        super(bridge);
        this.accuweatherStation = accuweatherStation;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void initialize() {
        config = getConfigAs(AccuweatherConfiguration.class);
        updateStatus(ThingStatus.UNKNOWN);
        scheduler.execute(() -> {
            if (!hasRequiredFields()) {
                setThingOfflineWithConfError("some required config fields are missing");
                return;
            }
            accuweatherStation.setHttpApiKey(apiKey);
            accuweatherStation.setCountryCode(countryCode);
            accuweatherStation.setAdminCode(adminCode);
            accuweatherStation.setCityName(cityName);
            if (accuweatherStation.resolveHttpCityKey()) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                setThingOfflineWithCommError("unable to get city key for the configured parameters");
            }
        });
    }

    @Override
    public void childHandlerInitialized(ThingHandler childHandler, Thing childThing) {
        super.childHandlerInitialized(childHandler, childThing);
        if (UID_STATION.equals(childThing.getThingTypeUID())) {
            AccuweatherStationHandler accuweatherStationHandler = (AccuweatherStationHandler) childHandler;
            accuweatherStationHandler.setAccuweatherStation(accuweatherStation);
        }
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
        cityName = configLocationName;
        return true;
    }

    public void setThingOfflineWithCommError(@Nullable String statusDescription) {
        String status = statusDescription != null ? statusDescription : "null";
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, status);
    }

    public void setThingOfflineWithConfError(@Nullable String statusDescription) {
        String status = statusDescription != null ? statusDescription : "null";
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR, status);
    }
}
