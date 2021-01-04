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

import static org.openhab.binding.accuweather.internal.AccuweatherBindingConstants.*;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.accuweather.internal.interfaces.Cache;
import org.openhab.binding.accuweather.internal.util.api.AccuweatherStation;
import org.openhab.binding.accuweather.internal.util.api.client.HttpClient;
import org.openhab.binding.accuweather.internal.util.api.client.ObjectMapper;
import org.openhab.binding.accuweather.internal.util.cache.InMemoryCache;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AccuweatherHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Alvaro Denis <denisacostaq@gmail.com> - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.accuweather", service = ThingHandlerFactory.class)
public class AccuweatherHandlerFactory extends BaseThingHandlerFactory {
    Logger logger = LoggerFactory.getLogger(AccuweatherHandlerFactory.class);

    // Bridge
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections
            .unmodifiableSet(Stream.of(UID_BRIDGE, UID_STATION).collect(Collectors.toSet()));

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (UID_BRIDGE.equals(thingTypeUID)) {
            ObjectMapper mapper = new ObjectMapper();
            Cache cache = new InMemoryCache();
            HttpClient httpClient = new HttpClient();
            AccuweatherStation accuweatherStation = new AccuweatherStation(cache, mapper, httpClient);
            return new AccuweatherBridgeHandler((Bridge) thing, accuweatherStation);
        } else if (UID_STATION.equals(thingTypeUID)) {
            return new AccuweatherStationHandler(thing);
        }
        return null;
    }
}
