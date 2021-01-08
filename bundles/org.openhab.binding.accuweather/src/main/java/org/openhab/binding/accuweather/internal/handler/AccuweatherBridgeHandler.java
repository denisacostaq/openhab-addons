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

import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.accuweather.internal.config.AccuweatherBridgeConfiguration;
import org.openhab.binding.accuweather.internal.interfaces.AccuweatherHttpApiClient;
import org.openhab.core.thing.*;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.osgi.service.component.annotations.Reference;
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
    private final AccuweatherHttpApiClient accuweatherHttpApiClient;

    private @Nullable AccuweatherBridgeConfiguration config;

    private String apiKey = "";

    public AccuweatherBridgeHandler(Bridge bridge, final @Reference AccuweatherHttpApiClient accuweatherHttpApiClient) {
        super(bridge);
        this.accuweatherHttpApiClient = accuweatherHttpApiClient;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void initialize() {
        config = getConfigAs(AccuweatherBridgeConfiguration.class);
        updateStatus(ThingStatus.UNKNOWN);
        scheduler.execute(() -> {
            if (!hasRequiredFields()) {
                setThingOfflineWithConfError("some required config fields are missing");
                return;
            }
            if (accuweatherHttpApiClient.verifyHttpApiKey(apiKey)) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                // FIXME(denisacostaq@gmail.com): fix this handling, retry policy
                setThingOfflineWithCommError("unable to validate accuweather.com API Key");
            }
        });
    }

    @Override
    public void childHandlerInitialized(ThingHandler childHandler, Thing childThing) {
        super.childHandlerInitialized(childHandler, childThing);
        if (logger.isTraceEnabled() || UID_STATION.equals(childThing.getThingTypeUID())) {
            AccuweatherStationHandler accuweatherStationHandler = (AccuweatherStationHandler) childHandler;
            Thing thing = accuweatherStationHandler.getThing();
            logger.trace("chield handler initialized for bridge, thing type {}, thing {}", thing.getThingTypeUID(),
                    thing.getUID());
        }
    }

    private boolean hasRequiredFields() {
        return hasApiKey();
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

    public void setThingOfflineWithCommError(@Nullable String statusDescription) {
        if (!Objects.isNull(statusDescription)) {
            logger.warn("{}", statusDescription);
        }
        final String status = !Objects.isNull(statusDescription) ? statusDescription : "null";
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, status);
    }

    public void setThingOfflineWithConfError(@Nullable String statusDescription) {
        if (!Objects.isNull(statusDescription)) {
            logger.warn("{}", statusDescription);
        }
        final String status = !Objects.isNull(statusDescription) ? statusDescription : "null";
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR, status);
    }
}
