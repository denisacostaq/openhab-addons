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

import static org.openhab.binding.accuweather.internal.AccuweatherBindingConstants.CH_TEMPERATURE;

import java.util.concurrent.ScheduledFuture;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.accuweather.internal.config.AccuweatherStationConfiguration;
import org.openhab.binding.accuweather.internal.interfaces.WeatherStation;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AccuweatherBridgeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Alvaro Denis <denisacostaq@gmail.com> - Initial contribution
 */
@NonNullByDefault
public class AccuweatherStationHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(AccuweatherStationHandler.class);
    private @Nullable AccuweatherStationConfiguration config;
    private @Nullable WeatherStation weatherStation;
    private @Nullable ScheduledFuture<?> poolingJob;
    private String countryCode = "";
    private Integer adminCode = 0;
    private String cityName = "";

    /**
     * Creates a new instance of this class for the {@link Thing}.
     *
     * @param thing the thing that should be handled, not null
     */
    public AccuweatherStationHandler(Thing thing, WeatherStation weatherStation) {
        super(thing);
        this.weatherStation = weatherStation;
    }

    @Override
    public void initialize() {
        config = getConfigAs(AccuweatherStationConfiguration.class);
        updateStatus(ThingStatus.UNKNOWN);
        scheduler.execute(() -> {
            updateStatus(getBridge().getStatus());
            if (!hasRequiredFields()) {
                setThingOfflineWithConfError("some required config fields are missing");
                return;
            }
            if (weatherStation.verifyStationConfigParams(countryCode, adminCode, cityName)) {
                updateStatus(ThingStatus.ONLINE);
                poolingJob = new AccuweatherDataSource(scheduler, weatherStation).start((temp) -> {
                    setTemperature(temp);
                });
            } else {
                // FIXME(denisacostaq@gmail.com): fix this handling
                setThingOfflineWithCommError("unable to get city key for the configured parameters");
            }
        });
    }

    @Override
    public void dispose() {
        super.dispose();
        this.poolingJob.cancel(true);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (CH_TEMPERATURE.equals(channelUID.getId())) {
            if (command instanceof RefreshType) {
                logger.warn("if (command instanceof RefreshType) {");
                // TODO: handle data refresh
                setTemperature(weatherStation.getTemperature());
            }

            // TODO: handle command

            // Note: if communication with thing fails for some reason,
            // indicate that by setting the status with detail information:
            // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
            // "Could not control device at IP address x.x.x.x");
        }
    }

    private boolean hasRequiredFields() {
        return hasCountryCode() && hasAdminCode() && hasLocationName();
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

    private void setTemperature(@Nullable Float temp) {
        if (temp == null) {
            updateStatus(ThingStatus.OFFLINE);
        } else {
            // TODO(denisacostaq@gmail.com): optimize querying the current status
            updateStatus(ThingStatus.ONLINE);
            updateState(CH_TEMPERATURE, new DecimalType(temp));
        }
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
