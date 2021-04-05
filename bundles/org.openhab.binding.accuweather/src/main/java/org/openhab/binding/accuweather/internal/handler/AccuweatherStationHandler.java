/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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

import static org.openhab.binding.accuweather.internal.AccuweatherBindingConstants.*;

import java.time.*;
import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.measure.Quantity;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.accuweather.internal.config.AccuweatherStationConfiguration;
import org.openhab.binding.accuweather.internal.exceptions.RemoteErrorResponseException;
import org.openhab.binding.accuweather.internal.interfaces.WeatherStation;
import org.openhab.binding.accuweather.internal.model.mapper.ModelTranslator;
import org.openhab.binding.accuweather.internal.model.view.CurrentConditions;
import org.openhab.binding.accuweather.internal.util.api.AccuweatherStation;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.*;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
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
public class AccuweatherStationHandler<HttpRespT, CacheValT, E extends Throwable> extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(AccuweatherStationHandler.class);
    private @Nullable AccuweatherStationConfiguration config;
    private final WeatherStation<HttpRespT, CacheValT, E> weatherStation;
    private @Nullable ScheduledFuture<?> poolingJob;
    private String countryCode = "";
    private Integer adminCode = 0;
    private String cityName = "";
    private static final Duration STATION_VALIDATION_DELAY = Duration.ofMillis(3000);

    /**
     * Creates a new instance of this class for the {@link Thing}.
     *
     * @param thing the thing that should be handled, not null
     */
    public AccuweatherStationHandler(Thing thing,
            final @Reference WeatherStation<HttpRespT, CacheValT, E> weatherStation) {
        super(thing);
        this.weatherStation = weatherStation;
    }

    @Override
    public void initialize() {
        config = getConfigAs(AccuweatherStationConfiguration.class);
        updateStatus(ThingStatus.UNKNOWN);
        this.scheduleValidateStationParams(Duration.ZERO);
    }

    @Override
    public void dispose() {
        super.dispose();
        this.cancelPoolingJob();
    }

    private CurrentConditions getCurrentConditions() {
        org.openhab.binding.accuweather.internal.model.pojo.CurrentConditions currentConditions = null;
        try {
            currentConditions = weatherStation.currentConditions();
        } catch (Throwable exc) {
            // FIXME(denisacostaq@gmail.com): no cast
            RemoteErrorResponseException e = (RemoteErrorResponseException) exc;
            // TODO(denisacostaq@gmail.com):
            // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
            logger.warn("unable to get current conditions, details: {}", e.getMessage());
        }
        return ModelTranslator.currentConditions(currentConditions);
    }

    private void refreshChanel(ChannelUID channelUID) {
        CurrentConditions currentConditions = getCurrentConditions();
        switch (channelUID.getIdWithoutGroup()) {
            case CH_TEMPERATURE:
                setChannelValue(channelUID, currentConditions.getTemperature());
                break;
            case CH_OBSERVATION_TIME:
                setChannelValue(channelUID, currentConditions.getLocalObservationDateTime());
                break;
            case CH_PRECIPITATION_TYPE:
                setChannelValue(channelUID, currentConditions.getPrecipitationType());
                break;
            case CH_WEATHER_TEXT:
                setChannelValue(channelUID, currentConditions.getWeatherText());
                break;
            case CH_WEATHER_ICON:
                setChannelValue(channelUID, currentConditions.getWeatherIcon());
                break;
            default:
                logger.trace("channel UID {} not handled in refresh", channelUID.toString());
                break;
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            refreshChanel(channelUID);
        }
    }

    /**
     * this function have a retry policy for key validation
     * 
     * @param delay wait a moment before trying to validate the cache
     */
    private void scheduleValidateStationParams(Duration delay) {
        logger.trace("validating API Key in {} seconds", delay.toSeconds());
        scheduler.schedule(() -> {
            if (!ThingStatus.ONLINE.equals(getBridge().getStatus())) {
                setThingOfflineWithCommError("bridge is offline");
                this.scheduleValidateStationParams(STATION_VALIDATION_DELAY);
                return;
            }
            if (!hasRequiredFields()) {
                setThingOfflineWithConfError("some required config fields are missing");
                return;
            }
            final String genericErrMsg = "unable to validate station config params";
            try {
                // FIXME(denisacostaq@gmail.com): Do not cast
                ((AccuweatherStation<HttpRespT, CacheValT, E>) weatherStation).setHttpClient(
                        ((AccuweatherBridgeHandler) (getBridge().getHandler())).getAccuweatherHttpApiClient());
                if (weatherStation.verifyStationConfigParams(countryCode, adminCode, cityName)) {
                    updateStatus(ThingStatus.ONLINE);
                    poolingJob = new AccuweatherDataSource(scheduler, weatherStation).start((currentConditions) -> {
                       updateAllChannels(currentConditions);
                    }, () -> {
                        this.cancelPoolingJob();
                    });
                } else {
                    setThingOfflineWithCommError("unable to validate configured parameters");
                }
            } catch (Throwable exc) {
                // FIXME(denisacostaq@gmail.com): no cast, and/or use E
                RemoteErrorResponseException e = (RemoteErrorResponseException) exc;
                if (Objects.equals(e.status(), RemoteErrorResponseException.StatusType.BAD_SERVER)) {
                    logger.debug("remote server error, rescheduling station config parameters validation in {} seconds",
                            STATION_VALIDATION_DELAY.toSeconds());
                    setThingOfflineWithCommError(genericErrMsg);
                    this.scheduleValidateStationParams(STATION_VALIDATION_DELAY);
                } else if (Objects.equals(e.status(), RemoteErrorResponseException.StatusType.BAD_CREDENTIALS)) {
                    logger.debug("Invalid API Key for accuweather.com, control flow should not reach this point");
                    setThingOfflineWithCommError("The provided accuweather.com API key looks invalid, please check it");
                } else {
                    // FIXME(denisacostaq@gmail.com): consider max rate reached
                    logger.debug("Invalid state, please contact the developer");
                }
            }
        }, delay.toMillis(), TimeUnit.MILLISECONDS);
    }

    private void updateAllChannels(CurrentConditions currentConditions) {
        setChannelValue(new ChannelUID(this.getThing().getUID(), CHG_CURRENT, CH_TEMPERATURE),
                currentConditions.getTemperature());
        setChannelValue(new ChannelUID(this.getThing().getUID(), CHG_CURRENT,CH_REAL_FEEL_TEMPERATURE),
                currentConditions.getRealFeelTemperature());
        setChannelValue(new ChannelUID(this.getThing().getUID(), CHG_CURRENT, CH_REAL_FEEL_TEMPERATURE_SHADE),
                currentConditions.getRealFeelTemperatureShade());
        setChannelValue(new ChannelUID(this.getThing().getUID(), CHG_CURRENT, CH_OBSERVATION_TIME),
                currentConditions.getLocalObservationDateTime());
        setChannelValue(new ChannelUID(this.getThing().getUID(), CHG_CURRENT, CH_PRECIPITATION_TYPE),
                currentConditions.getPrecipitationType());
        setWeatherText(new ChannelUID(this.getThing().getUID(), CHG_CURRENT, CH_WEATHER_TEXT),
                currentConditions.getWeatherText());
        setChannelValue(new ChannelUID(this.getThing().getUID(), CHG_CURRENT, CH_WEATHER_ICON),
                currentConditions.getWeatherIcon());
    }

    private void cancelPoolingJob() {
        if (!Objects.isNull(this.poolingJob) && !this.poolingJob.isCancelled()) {
            logger.trace("cancelling the background poller");
            this.poolingJob.cancel(false);
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
        if (Objects.isNull(configAdminCode) || configAdminCode == 0) {
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

    private void setChannelValue(ChannelUID channelUID, @Nullable Float val) {
        State state = Objects.isNull(val) ? null : new DecimalType(val);
        setChannelValue(channelUID, state);
    }

    private void setChannelValue(ChannelUID channelUID, @Nullable Integer val) {
        State state = Objects.isNull(val) ? null : new DecimalType(val);
        setChannelValue(channelUID, state);
    }

    private void setChannelValue(ChannelUID channelUID, @Nullable State state) {
        if (!Objects.isNull(state)) {
            updateState(channelUID.getId(), state);
        } else {
            updateState(channelUID.getId(), UnDefType.NULL);
        }
    }

    private void setChannelValue(ChannelUID channelUID, @Nullable Date date) {
        State state = Objects.isNull(date) ? null
                : new DateTimeType(ZonedDateTime.ofInstant(date.toInstant(), ZoneOffset.UTC)
                        .withZoneSameInstant(ZoneId.systemDefault()));
        setChannelValue(channelUID, state);
    }

    private void setChannelValue(ChannelUID channelUID, @Nullable String val) {
        if (!StringUtils.isBlank(val)) {
            updateState(channelUID.getId(), new StringType(val));
        }
    }

    private void setWeatherText(ChannelUID channelUID, String weatherText) {
        logger.warn("updateState(channelUID.getId(), new StringType(weatherText)) {}, {}", channelUID.getId(),
                weatherText);
        updateState(channelUID.getId(), StringType.valueOf(weatherText));
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
