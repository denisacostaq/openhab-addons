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

import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link AccuweatherDataSource} is responsible for establishing
 * a socket.io connection with accuweather.com, subscribing/unsubscribing
 * to data events, receiving data events through the real-time socket.io API,
 * and for routing the data events to a weather station thing handler for processing.
 *
 * @author Mark Hilbush - Initial contribution
 */
public class AccuweatherDataSource {
    // URL used to get the realtime event stream
    private static final String REALTIME_URL = "https://api.ambientweather.net/?api=1&applicationKey=%APPKEY%";

    // JSON used to subscribe or unsubscribe from weather data events
    private static final String SUB_UNSUB_JSON = "{ apiKeys: [ '%APIKEY%' ] }";

    private final Logger logger = LoggerFactory.getLogger(AccuweatherDataSource.class);

    private String apiKey;

    private String applicationKey;

    private Gson gson;
    private static ScheduledExecutorService scheduler;

    public AccuweatherDataSource(ScheduledExecutorService scheduledExecutorService, String applicationKey,
            String apiKey, Gson gson) {
        this.applicationKey = applicationKey;
        this.apiKey = apiKey;
        this.gson = gson;
        this.scheduler = scheduledExecutorService;
    }

    /*
     * Start the event listener for the Ambient Weather real-time API
     */
    public void start(Consumer<Float> callback) {
        logger.warn("AccuweatherClient: Start pooling");
        this.scheduler.scheduleAtFixedRate(() -> {
            callback.accept(new Random().nextFloat());
        }, 0, 5, TimeUnit.SECONDS);
    }

    /*
     * Stop the event listener for the Ambient Weather real-time API.
     */
    public void stop() {
        logger.debug("Listener: Event listener stopping");
    }
}
