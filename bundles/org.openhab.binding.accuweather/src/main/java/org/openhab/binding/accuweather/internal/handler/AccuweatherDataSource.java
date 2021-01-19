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

import java.util.Date;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

import javax.validation.constraints.NotNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.accuweather.internal.exceptions.RemoteErrorResponseException;
import org.openhab.binding.accuweather.internal.interfaces.WeatherStation;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AccuweatherDataSource} is responsible for pulling data through
 * the http API .from accuweather.com
 *
 * @author Alvaro Denis <denisacostaq@gmail.com> - Initial contribution
 */
@NonNullByDefault
public class AccuweatherDataSource {
    public interface Command {
        void call();
    }

    private final Logger logger = LoggerFactory.getLogger(AccuweatherDataSource.class);
    private final ScheduledExecutorService scheduler;
    private final WeatherStation weatherStation;

    public AccuweatherDataSource(final @Reference ScheduledExecutorService scheduledExecutorService,
            final @Reference WeatherStation weatherStation) {
        this.scheduler = scheduledExecutorService;
        this.weatherStation = weatherStation;
    }

    /*
     * Start the event listener for the Ambient Weather real-time API
     */
    @NotNull
    public ScheduledFuture<?> start(BiConsumer<@Nullable Float, @Nullable Date> callback, Command cancel) {
        logger.debug("AccuweatherClient: Start pooling");
        return this.scheduler.scheduleAtFixedRate(() -> {
            try {
                callback.accept(weatherStation.getTemperature(), weatherStation.getCurrentTime());
            } catch (Throwable exc) {
                // FIXME(denisacostaq@gmail.com): no cast
                RemoteErrorResponseException e = (RemoteErrorResponseException) exc;
                logger.warn("unable to get temperature, details: {}", e.getMessage());
                switch (e.status()) {
                    case BAD_CREDENTIALS:
                    case BAD_PERMISSIONS:
                    case BAD_RESOURCE:
                        cancel.call();
                }
            }
        }, 3, 3, TimeUnit.SECONDS);
    }
}
