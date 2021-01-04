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

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.openhab.binding.accuweather.internal.api.AccuweatherStation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AccuweatherDataSource} is responsible for pulling data through
 * the http API .from accuweather.com
 *
 * @author Mark Hilbush - Initial contribution
 */
public class AccuweatherDataSource {
    private final Logger logger = LoggerFactory.getLogger(AccuweatherDataSource.class);
    private static ScheduledExecutorService scheduler;
    private AccuweatherStation accuweatherStation;

    public AccuweatherDataSource(ScheduledExecutorService scheduledExecutorService,
            AccuweatherStation accuweatherStation) {
        this.scheduler = scheduledExecutorService;
        this.accuweatherStation = accuweatherStation;
    }

    /*
     * Start the event listener for the Ambient Weather real-time API
     */
    public void start(Consumer<Float> callback) {
        logger.warn("AccuweatherClient: Start pooling");
        this.scheduler.scheduleAtFixedRate(() -> {
            callback.accept(accuweatherStation.getTemperature());
        }, 3, 3, TimeUnit.SECONDS);
    }
}
