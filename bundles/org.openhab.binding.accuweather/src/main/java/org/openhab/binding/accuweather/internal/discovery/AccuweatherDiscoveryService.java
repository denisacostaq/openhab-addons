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
package org.openhab.binding.accuweather.internal.discovery;

import static org.openhab.binding.accuweather.internal.AccuweatherBindingConstants.*;

import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.accuweather.internal.interfaces.GeoInfo;
import org.openhab.binding.accuweather.internal.model.pojo.AdministrativeArea;
import org.openhab.binding.accuweather.internal.model.pojo.CitySearchResult;
import org.openhab.binding.accuweather.internal.model.pojo.GeoPosition;
import org.openhab.binding.accuweather.internal.util.api.GeoInfoImpl;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.i18n.LocationProvider;
import org.openhab.core.library.types.PointType;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AccuweatherDiscoveryService} creates things based on the detected location.
 *
 * @author Alvaro Denis <denisacostaq@gmail.com> - Initial contribution
 */
@NonNullByDefault
public class AccuweatherDiscoveryService extends AbstractDiscoveryService {
    private final Logger logger = LoggerFactory.getLogger(AccuweatherDiscoveryService.class);

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(UID_STATION);
    private static final long STATIONS_CACHE_MILLIS = TimeUnit.HOURS.toMillis(12);
    private static final int DISCOVER_TIMEOUT_SECONDS = 5;
    private static final int LOCATION_CHANGED_CHECK_INTERVAL_SECONDS = 60;

    private @Nullable ScheduledFuture<?> discoveryJob;
    private GeoInfo geoInfo = new GeoInfoImpl();
    private @Nullable PointType previousLocation;
    private @NonNullByDefault({}) LocationProvider locationProvider;

    /**
     * Creates a {@link AccuweatherDiscoveryService} with immediately enabled background discovery.
     */
    public AccuweatherDiscoveryService(final @Reference LocationProvider locationProvider) {
        super(SUPPORTED_THING_TYPES, DISCOVER_TIMEOUT_SECONDS, true);
        this.locationProvider = locationProvider;
    }

    @Override
    protected void startScan() {
        logger.trace("starting Accuweather scan");
        PointType location = locationProvider.getLocation();
        // TODO(denisacostaq@gmail.com): if (location == null) {}
        createResults(location);
    }

    @Override
    protected void startBackgroundDiscovery() {
        logger.trace("starting background scan from Accuweather");
        if (discoveryJob == null) {
            discoveryJob = scheduler.scheduleWithFixedDelay(() -> {
                PointType currentLocation = locationProvider.getLocation();
                if (!Objects.equals(currentLocation, previousLocation)) {
                    logger.debug("Location has been changed from {} to {}: Creating new discovery results",
                            previousLocation, currentLocation);
                    logger.warn("currentLocation {}", currentLocation.toFullString());
                    createResults(currentLocation);
                    previousLocation = currentLocation;
                }
            }, 0, LOCATION_CHANGED_CHECK_INTERVAL_SECONDS, TimeUnit.SECONDS);
            logger.debug("Scheduled FMI Weather location-changed discovery job every {} seconds",
                    LOCATION_CHANGED_CHECK_INTERVAL_SECONDS);
        }
    }

    @Override
    public void activate(@Nullable Map<String, Object> configProperties) {
        super.activate(configProperties);
    }

    @Override
    public void deactivate() {
        super.deactivate();
    }

    public void createResults(@Nullable PointType location) {
        createStationsFromLocation(location);
    }

    private void createStationsFromLocation(@Nullable PointType location) {
        String cityName = geoInfo.getCityName(location);
        String countryCode = geoInfo.getCountryDomainName(location);
        String administrativeAreaName = geoInfo.getAdministrativeArea(location);
        // AccuweatherHttpApiClient httpApiClient;
        List<AdministrativeArea> adminAreas = new ArrayList<>();
        // httpApiClient.getAdminAreas(countryCode)
        // .stream().filter((aa) -> {
        // return StringUtils.equals(aa.englishName, administrativeAreaName);
        // }).collect(Collectors.toList());
        adminAreas.add(new AdministrativeArea("22", countryCode));
        AdministrativeArea administrativeArea = adminAreas.get(0);
        // httpApiClient.citySearch(administrativeArea, new CitySearchResult("", cityName));
        List<CitySearchResult> filteredStations = new LinkedList<>();
        Set.of(new CitySearchResult(adminAreas.get(0).iD, cityName,
                new GeoPosition(location.getLatitude().doubleValue(), location.getLongitude().doubleValue())))
                .forEach(station -> {
                    DiscoveryResult discoveryResult = DiscoveryResultBuilder
                            .create(new ThingUID(UID_STATION,
                                    cleanId(String.format("station_%s_%s", station.key, station.englishName))))
                            .withLabel(String.format("Accuweather observation for %s", station.englishName))
                            .withProperty("BindingConstants.ADMIN_AREA", station.key)
                            .withRepresentationProperty("BindingConstants.ADMIN_AREA").build();
                    thingDiscovered(discoveryResult);
                });
    }

    private static String cleanId(String id) {
        return id.replace("ä", "a").replace("ö", "o").replace("å", "a").replace("Ä", "A").replace("Ö", "O")
                .replace("Å", "a").replaceAll("[^a-zA-Z0-9_]", "_");
    }

    @Override
    protected void stopBackgroundDiscovery() {
        logger.debug("Stopping Accuweather background discovery");
        ScheduledFuture<?> discoveryJob = this.discoveryJob;
        if (discoveryJob != null) {
            if (discoveryJob.cancel(true)) {
                this.discoveryJob = null;
                logger.debug("Stopped Accuweather background discovery");
            }
        }
    }
}
