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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.accuweather.internal.util.api.AccuweatherStation;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
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
    private @Nullable AccuweatherStation accuweatherStation;

    /**
     * Creates a new instance of this class for the {@link Thing}.
     *
     * @param thing the thing that should be handled, not null
     */
    public AccuweatherStationHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);
        scheduler.execute(() -> {
            // FIXME(denisacostaq@gmail.com): updateStatus(bridge.getStatus());
            updateStatus(ThingStatus.ONLINE);
            new AccuweatherDataSource(scheduler, accuweatherStation).start((temp) -> {
                logger.warn("temp {}", temp);
                if (temp == null) {
                    updateStatus(ThingStatus.OFFLINE);
                } else {
                    // TODO(denisacostaq@gmail.com): optimize querying the current status
                    updateStatus(ThingStatus.ONLINE);
                    updateState(CH_TEMPERATURE, new DecimalType(temp));
                }
            });
        });
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (CH_TEMPERATURE.equals(channelUID.getId())) {
            if (command instanceof RefreshType) {
                // TODO: handle data refresh
            }

            // TODO: handle command

            // Note: if communication with thing fails for some reason,
            // indicate that by setting the status with detail information:
            // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
            // "Could not control device at IP address x.x.x.x");
        }
    }

    public void setAccuweatherStation(AccuweatherStation accuweatherStation) {
        this.accuweatherStation = accuweatherStation;
    }
}
