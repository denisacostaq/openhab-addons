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

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import javax.validation.constraints.NotNull;

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
public class AccuweatherDataSource {
    private final Logger logger = LoggerFactory.getLogger(AccuweatherDataSource.class);
    private static ScheduledExecutorService scheduler;
    private final WeatherStation weatherStation;

    public AccuweatherDataSource(ScheduledExecutorService scheduledExecutorService,
            final @Reference WeatherStation weatherStation) {
        this.scheduler = scheduledExecutorService;
        this.weatherStation = weatherStation;
    }

    /*
     * Start the event listener for the Ambient Weather real-time API
     */
    @NotNull
    public ScheduledFuture<?> start(Consumer<Float> callback) {
        logger.warn("AccuweatherClient: Start pooling");
        return this.scheduler.scheduleAtFixedRate(() -> {
            callback.accept(weatherStation.getTemperature());
        }, 3, 3, TimeUnit.SECONDS);
    }
}