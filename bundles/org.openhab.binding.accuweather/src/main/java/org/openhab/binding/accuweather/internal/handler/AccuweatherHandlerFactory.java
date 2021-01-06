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

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.accuweather.internal.discovery.AccuweatherDiscoveryService;
import org.openhab.binding.accuweather.internal.interfaces.Cache;
import org.openhab.binding.accuweather.internal.interfaces.GeoInfo;
import org.openhab.binding.accuweather.internal.util.api.AccuweatherStation;
import org.openhab.binding.accuweather.internal.util.api.GeoInfoImpl;
import org.openhab.binding.accuweather.internal.util.api.client.AccuweatherHttpApiClient;
import org.openhab.binding.accuweather.internal.util.api.client.HttpClient;
import org.openhab.binding.accuweather.internal.util.api.client.ObjectMapper;
import org.openhab.binding.accuweather.internal.util.cache.InMemoryCache;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.LocationProvider;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.i18n.UnitProvider;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
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
    private final Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();

    // Bridge
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections
            .unmodifiableSet(Stream.of(UID_BRIDGE, UID_STATION).collect(Collectors.toSet()));

    private final LocaleProvider localeProvider;
    private final LocationProvider locationProvider;
    private final UnitProvider unitProvider;
    private final TimeZoneProvider timeZoneProvider;
    private final GeoInfo geoInfo = new GeoInfoImpl();
    private final ObjectMapper mapper = new ObjectMapper();
    private final Cache cache = new InMemoryCache();
    private final HttpClient httpClient = new HttpClient();
    private final AccuweatherHttpApiClient httpApiClient;

    @Activate
    public AccuweatherHandlerFactory(final @Reference LocaleProvider localeProvider,
            final @Reference LocationProvider locationProvider, final @Reference UnitProvider unitProvider,
            final @Reference TimeZoneProvider timeZoneProvider) {
        this.localeProvider = localeProvider;
        this.locationProvider = locationProvider;
        this.unitProvider = unitProvider;
        this.timeZoneProvider = timeZoneProvider;
        this.httpApiClient = new AccuweatherHttpApiClient(geoInfo, locationProvider, httpClient, mapper, cache);
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (UID_BRIDGE.equals(thingTypeUID)) {
            BaseBridgeHandler handler = new AccuweatherBridgeHandler((Bridge) thing, httpApiClient);
            registerDiscoveryService(handler.getThing().getUID(), httpApiClient);
            return handler;
        } else if (UID_STATION.equals(thingTypeUID)) {
            AccuweatherStation accuweatherStation = new AccuweatherStation(httpApiClient);
            return new AccuweatherStationHandler(thing, accuweatherStation);
        }
        return null;
    }

    @Override
    protected void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof AccuweatherBridgeHandler) {
            unregisterDiscoveryService(thingHandler.getThing().getUID());
        }
    }

    private synchronized void registerDiscoveryService(ThingUID bridgeUID,
            org.openhab.binding.accuweather.internal.interfaces.AccuweatherHttpApiClient httpApiClient) {
        logger.trace("registering {}", this.getClass().getName());
        AccuweatherDiscoveryService discoveryService = new AccuweatherDiscoveryService(locationProvider, httpApiClient,
                geoInfo);
        discoveryService.activate(null);
        discoveryServiceRegs.put(bridgeUID,
                bundleContext.registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<>()));
    }

    private synchronized void unregisterDiscoveryService(ThingUID bridgeUID) {
        logger.trace("unregistering {}", this.getClass().getName());
        ServiceRegistration<?> serviceReg = discoveryServiceRegs.remove(bridgeUID);
        if (serviceReg != null) {
            AccuweatherDiscoveryService service = (AccuweatherDiscoveryService) bundleContext
                    .getService(serviceReg.getReference());
            serviceReg.unregister();
            if (service != null) {
                service.deactivate();
            }
        }
    }
}
