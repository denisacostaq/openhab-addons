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
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.measure.Quantity;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.accuweather.internal.config.AccuweatherStationConfiguration;
import org.openhab.binding.accuweather.internal.exceptions.RemoteErrorResponseException;
import org.openhab.binding.accuweather.internal.interfaces.WeatherStation;
import org.openhab.binding.accuweather.internal.util.api.AccuweatherStation;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.*;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
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

    private void refreshChanel(ChannelUID channelUID) {
        switch (channelUID.getIdWithoutGroup()) {
            case CH_TEMPERATURE:
                try {
                    setTemperature(channelUID, weatherStation.getTemperature());
                } catch (Throwable exc) {
                    // FIXME(denisacostaq@gmail.com): no cast
                    RemoteErrorResponseException e = (RemoteErrorResponseException) exc;
                    // TODO(denisacostaq@gmail.com):
                    // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    logger.warn("unable to get temperature, details: {}", e.getMessage());
                }
                break;
            case CH_OBSERVATION_TIME:
                try {
                    setObservationTime(channelUID, weatherStation.getCurrentTime());
                } catch (Throwable exc) {
                    // FIXME(denisacostaq@gmail.com): no cast
                    E e = (E) exc;
                    // TODO(denisacostaq@gmail.com):
                    // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    logger.warn("unable to get temperature, details: {}", e.getMessage());
                }
                break;
            case CH_PRECIPITATION_TYPE:
                try {
                    setPrecipitationType(channelUID, weatherStation.getPrecipitationType());
                } catch (Throwable exc) {
                    // FIXME(denisacostaq@gmail.com): no cast
                    E e = (E) exc;
                    // TODO(denisacostaq@gmail.com):
                    // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    logger.warn("unable to get precipitation type, details: {}", e.getMessage());
                }
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
                    poolingJob = new AccuweatherDataSource(scheduler, weatherStation).start((temp, date) -> {
                        ChannelUID chTemp = new ChannelUID(this.getThing().getUID(), CHG_CURRENT, CH_TEMPERATURE);
                        ChannelUID chDate = new ChannelUID(this.getThing().getUID(), CHG_CURRENT, CH_OBSERVATION_TIME);
                        setTemperature(chTemp, temp);
                        setObservationTime(chDate, date);
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

    private void setTemperature(ChannelUID channelUID, @Nullable Float temp) {
        // TODO(denisacostaq@gmail.com): change to be based on exceptions
        if (Objects.isNull(temp)) {
            updateStatus(ThingStatus.OFFLINE);
        } else {
            // TODO(denisacostaq@gmail.com): optimize querying the current status
            updateStatus(ThingStatus.ONLINE);
            updateState(channelUID.getId(), new DecimalType(temp));
        }
    }

    private void setObservationTime(ChannelUID channelUID, @Nullable Date date) {
        // TODO(denisacostaq@gmail.com): change to be based on exceptions
        if (Objects.isNull(date)) {
            updateStatus(ThingStatus.OFFLINE);
        } else {
            DateTimeType dateTimeType = new DateTimeType(ZonedDateTime.ofInstant(date.toInstant(), ZoneOffset.UTC)
                    .withZoneSameInstant(ZoneId.systemDefault()));
            updateState(channelUID.getId(), dateTimeType);
        }
    }

    private void setPrecipitationType(ChannelUID channelUID, @Nullable String precipitationType) {
        // TODO(denisacostaq@gmail.com): change to be based on exceptions
        if (Objects.isNull(precipitationType)) {
            updateStatus(ThingStatus.OFFLINE);
        } else {
            updateState(channelUID.getId(), new StringType(precipitationType));
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
