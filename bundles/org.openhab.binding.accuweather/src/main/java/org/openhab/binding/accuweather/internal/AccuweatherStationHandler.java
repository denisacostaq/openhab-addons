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

import static org.openhab.binding.accuweather.internal.AccuweatherBindingConstants.CH_TEMPERATURE;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.accuweather.internal.model.pojo.CurrentConditions;
import org.openhab.core.io.net.http.HttpUtil;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
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
public class AccuweatherStationHandler extends BaseThingHandler {
    private static final String LOCATIONS_URL = "http://dataservice.accuweather.com/currentconditions/v1/%CITY_KEY%?apikey=%API_KEY%";
    private final Logger logger = LoggerFactory.getLogger(AccuweatherStationHandler.class);
    private final Gson gson = new Gson();
    private String cityKey;
    private String apiKey;

    /**
     * Creates a new instance of this class for the {@link Thing}.
     *
     * @param thing the thing that should be handled, not null
     */
    public AccuweatherStationHandler(Thing thing, String cityKey, String apiKey) {
        super(thing);
        this.cityKey = cityKey;
        this.apiKey = apiKey;
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);
        scheduler.execute(() -> {
            // FIXME(denisacostaq@gmail.com): updateStatus(bridge.getStatus());
            updateStatus(ThingStatus.ONLINE);
            new AccuweatherDataSource(scheduler, "", "", null).start((temp) -> {
                temp = (float) getCurrentConditions();
                updateState(CH_TEMPERATURE, new DecimalType(temp));
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

    // FIXME(denisacostaq@gmail.com): duplicate code
    public double getCurrentConditions() {
        logger.debug("Getting API key through getting cities API");
        try {
            // Query locations from Accuweather
            String url = LOCATIONS_URL.replace("%CITY_KEY%", cityKey).replace("%API_KEY%", apiKey);
            // FIXME(denisacostaq@gmail.com): Use the builded url instead
            url = "http://localhost:8000/Current_Conditions.json";
            logger.debug(
                    "Bridge: Querying City Search (results narrowed by countryCode and adminCode Accuweather service");
            int DEVICES_API_TIMEOUT = 60;
            String response = HttpUtil.executeUrl("GET", url, DEVICES_API_TIMEOUT);
            logger.trace("Bridge: Response = {}", response);
            // Got a response so the keys are good
            CurrentConditions[] currentConditions = gson.fromJson(response, CurrentConditions[].class);
            logger.trace("Bridge: API key is valid with");
            if (currentConditions.length > 0) {
                if (currentConditions.length > 1) {
                    logger.warn("Expected a single result for current conditions but got {}", currentConditions.length);
                }
                CurrentConditions currentCondition = currentConditions[0];
                return currentCondition.temperature.metric.value;
            }
        } catch (IOException e) {
            // executeUrl throws IOException when it gets a Not Authorized (401) response
            logger.debug("Bridge: Got IOException: {}", e.getMessage());
            // FIXME(denisacostaq@gmail.com): setThingOfflineWithCommError(e.getMessage(), "Invalid API or application key");
            // rescheduleValidateKeysJob();
        } catch (IllegalArgumentException e) {
            logger.debug("Bridge: Got IllegalArgumentException: {}", e.getMessage());
            // FIXME(denisacostaq@gmail.com): setThingOfflineWithCommError(e.getMessage(), "Unable to get devices");
            // rescheduleValidateKeysJob();
        } catch (JsonSyntaxException e) {
            logger.debug("Bridge: Got JsonSyntaxException: {}", e.getMessage());
            // FIXME(denisacostaq@gmail.com): setThingOfflineWithCommError(e.getMessage(), "Error parsing json response");
            // rescheduleValidateKeysJob();
        }
        logger.warn("Unable to get location Key (id)");
        return 0;
    }
}
