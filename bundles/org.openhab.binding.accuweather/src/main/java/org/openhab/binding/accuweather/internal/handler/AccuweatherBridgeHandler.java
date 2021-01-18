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

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.accuweather.internal.config.AccuweatherBridgeConfiguration;
import org.openhab.binding.accuweather.internal.discovery.AccuweatherDiscoveryService;
import org.openhab.binding.accuweather.internal.exceptions.RemoteErrorResponseException;
import org.openhab.binding.accuweather.internal.interfaces.AccuweatherHttpApiClient;
import org.openhab.binding.accuweather.internal.interfaces.cache.ExpiringCacheMapInterface;
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
    private final AccuweatherDiscoveryService discoveryService;
    private final ExpiringCacheMapInterface<String, Object, RemoteErrorResponseException> cache;
    private static final Duration KEY_VALIDATION_DELAY = Duration.ofMillis(3000);

    private @Nullable AccuweatherBridgeConfiguration config;

    private String apiKey = "";

    public AccuweatherBridgeHandler(Bridge bridge, final @Reference AccuweatherHttpApiClient accuweatherHttpApiClient,
            final @Reference AccuweatherDiscoveryService discoveryService,
            final @Reference ExpiringCacheMapInterface<String, Object, RemoteErrorResponseException> cache) {
        super(bridge);
        this.accuweatherHttpApiClient = accuweatherHttpApiClient;
        this.discoveryService = discoveryService;
        this.cache = cache;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void initialize() {
        config = getConfigAs(AccuweatherBridgeConfiguration.class);
        updateStatus(ThingStatus.UNKNOWN);
        if (!hasRequiredFields()) {
            setThingOfflineWithConfError("some required config fields are missing");
            return;
        }
        scheduleValidateApiKey(Duration.ZERO);
    }

    @Override
    public void childHandlerInitialized(ThingHandler childHandler, Thing childThing) {
        super.childHandlerInitialized(childHandler, childThing);
        if (logger.isTraceEnabled() || UID_STATION.equals(childThing.getThingTypeUID())) {
            AccuweatherStationHandler accuweatherStationHandler = (AccuweatherStationHandler) childHandler;
            Thing thing = accuweatherStationHandler.getThing();
            logger.trace("child handler initialized for bridge, thing type {}, thing {}", thing.getThingTypeUID(),
                    thing.getUID());
        }
    }

    /**
     * this function have a retry policy for key validation
     * 
     * @param delay wait a moment before trying to validate the cache
     */
    private void scheduleValidateApiKey(Duration delay) {
        scheduler.schedule(() -> {
            final String genericErrMsg = "unable to validate accuweather.com API Key";
            try {
                if (accuweatherHttpApiClient.verifyHttpApiKey(apiKey)) {
                    updateStatus(ThingStatus.ONLINE);
                    if (true) { // TODO(denisacostaq@gmail.com): Check if should be called multiple times
                        discoveryService.setBridgeUID(this.getThing().getUID());
                        discoveryService.activate(null);
                    }
                } else {
                    setThingOfflineWithCommError(genericErrMsg);
                }
            } catch (Throwable exc) {
                // FIXME(denisacostaq@gmail.com): cast to template
                if (exc instanceof RemoteErrorResponseException) {
                    RemoteErrorResponseException e = (RemoteErrorResponseException) exc;
                    if (Objects.equals(e.status(), RemoteErrorResponseException.StatusType.BAD_SERVER)) {
                        logger.debug("remote server error, rescheduling key validation in {} seconds",
                                KEY_VALIDATION_DELAY.toSeconds());
                        setThingOfflineWithCommError(genericErrMsg);
                        // this.scheduleValidateApiKey(KEY_VALIDATION_DELAY);
                    } else if (Objects.equals(e.status(), RemoteErrorResponseException.StatusType.BAD_CREDENTIALS)) {
                        logger.debug("Invalid API Key for accuweather.com");
                        setThingOfflineWithCommError(
                                "The provided accuweather.com API key looks invalid, please check it");
                    } else {
                        // FIXME(denisacostaq@gmail.com): consider max rate reached
                        logger.debug("Invalid state, please contact the developer");
                    }
                } else {
                    logger.warn("unexpected error: {}", exc.getMessage());
                }
            }
        }, delay.toMillis(), TimeUnit.MILLISECONDS);
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
        if (!StringUtils.equals(apiKey, configApiKey)) {
            cache.clear();
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

    public AccuweatherHttpApiClient getAccuweatherHttpApiClient() {
        return accuweatherHttpApiClient;
    }
}
