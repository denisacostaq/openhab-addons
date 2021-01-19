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
package org.openhab.binding.accuweather.internal.discovery;

import static org.openhab.binding.accuweather.internal.AccuweatherBindingConstants.*;
import static org.openhab.binding.accuweather.internal.config.AccuweatherStationConfiguration.*;

import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.accuweather.internal.interfaces.AccuweatherHttpApiClient;
import org.openhab.binding.accuweather.internal.interfaces.GeoInfo;
import org.openhab.binding.accuweather.internal.model.pojo.AdministrativeArea;
import org.openhab.binding.accuweather.internal.model.pojo.CitySearchResult;
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
public class AccuweatherDiscoveryService<HttpRespT, CacheValT, E extends Throwable> extends AbstractDiscoveryService {
    private final Logger logger = LoggerFactory.getLogger(AccuweatherDiscoveryService.class);

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(UID_STATION);
    private static final long STATIONS_CACHE_MILLIS = TimeUnit.HOURS.toMillis(12);
    private static final int DISCOVER_TIMEOUT_SECONDS = 5;
    private static final int LOCATION_CHANGED_CHECK_INTERVAL_SECONDS = 60;

    private @Nullable ScheduledFuture<?> discoveryJob;
    private final GeoInfo<E> geoInfo;
    private @Nullable PointType previousLocation;
    private final LocationProvider locationProvider;
    private final AccuweatherHttpApiClient<HttpRespT, CacheValT, E> httpApiClient;
    private ThingUID bridgeUID = new ThingUID("Contact the developer about this error".split(" "));

    /**
     * Creates a {@link AccuweatherDiscoveryService} with immediately enabled background discovery.
     */
    public AccuweatherDiscoveryService(final @Reference LocationProvider locationProvider,
            final @Reference AccuweatherHttpApiClient<HttpRespT, CacheValT, E> httpApiClient,
            final @Reference GeoInfo<E> geoInfo) {
        super(SUPPORTED_THING_TYPES, DISCOVER_TIMEOUT_SECONDS, true);
        this.locationProvider = locationProvider;
        this.httpApiClient = httpApiClient;
        this.geoInfo = geoInfo;
    }

    public void setBridgeUID(final @Reference ThingUID bridgeUID) {
        this.bridgeUID = bridgeUID;
    }

    @Override
    protected void startScan() {
        logger.trace("starting Accuweather scan");
        PointType location = locationProvider.getLocation();
        if (Objects.isNull(location)) {
            logger.warn("unable to discover stations for null location");
            return;
        }
        try {
            createResults(location);
        } catch (Throwable exc) {
            // FIXME(denisacostaq@gmail.com): no cast
            E e = (E) exc;
            logger.warn("unable to discover stations for location {}, detail: {}", location.toFullString(),
                    e.getMessage());
        }
    }

    @Override
    protected void startBackgroundDiscovery() {
        logger.trace("starting Accuweather background scan");
        if (discoveryJob == null) {
            discoveryJob = scheduler.scheduleWithFixedDelay(() -> {
                PointType currentLocation = locationProvider.getLocation();
                if (!Objects.equals(currentLocation, previousLocation)) {
                    logger.debug("Location has been changed from {} to {}: Creating new discovery results",
                            previousLocation, currentLocation);
                    this.startScan();
                    previousLocation = currentLocation;
                }
            }, 0, LOCATION_CHANGED_CHECK_INTERVAL_SECONDS, TimeUnit.SECONDS);
            logger.debug("Scheduled Accuweather location-changed discovery job every {} seconds",
                    LOCATION_CHANGED_CHECK_INTERVAL_SECONDS);
        }
    }

    @Override
    public void activate(@Nullable Map<String, Object> configProperties) {
        logger.trace("Starting discovery service {}...", this.getClass().getSimpleName());
        super.activate(configProperties);
    }

    @Override
    public void deactivate() {
        super.deactivate();
    }

    public void createResults(@Nullable PointType location) throws E {
        createStationsFromLocation(location);
    }

    private CitySearchResult getCityFromLocation(@Nullable PointType location) throws E {
        String cityName = geoInfo.getCityName(location);
        String countryCode = geoInfo.getCountryDomainName(location);
        String administrativeAreaName = geoInfo.getAdministrativeAreaName(location);
        List<AdministrativeArea> adminAreas = httpApiClient.getAdminAreas(countryCode).stream()
                .filter(aa -> StringUtils.equals(aa.englishName, administrativeAreaName)).collect(Collectors.toList());
        if (adminAreas.size() != 1) {
            if (adminAreas.isEmpty()) {
                logger.warn("unable to get any administrative area");
                return new CitySearchResult();
            }
            logger.debug("getting more than one administrative area for name {} and country code {} looks suspicious",
                    adminAreas, countryCode);
        }
        List<CitySearchResult> citySearchResults = httpApiClient.citySearch(adminAreas.get(0),
                new CitySearchResult("", cityName));
        if (adminAreas.size() != 1) {
            if (adminAreas.isEmpty()) {
                logger.warn("unable to get any city");
                return new CitySearchResult();
            }
            logger.debug(
                    "getting more than one cities for country code {}, admin code {} and city name {} looks suspicious",
                    adminAreas.get(0).countryID, adminAreas.get(0).iD, cityName);
        }
        return citySearchResults.get(0);
    }

    private void createStationsFromLocation(@Nullable PointType location) throws E {
        if (Objects.isNull(location)) {
            logger.info("can not create stations of null location");
            return;
        }
        CitySearchResult city = getCityFromLocation(location);
        if (StringUtils.isEmpty(city.key)) {
            logger.warn("can not determine city from location");
            return;
        }
        List<CitySearchResult> discoveredCities = new ArrayList<>();
        discoveredCities.add(city);
        discoveredCities.addAll(httpApiClient.getNeighborsCities(city));
        discoveredCities.forEach(neighborCity -> {
            DiscoveryResult discoveryResult = DiscoveryResultBuilder
                    .create(new ThingUID(UID_STATION, bridgeUID,
                            cleanId(String.format("station_%s_%s", neighborCity.englishName, neighborCity.key))))
                    .withLabel(String.format("Accuweather observation for %s", neighborCity.englishName))
                    .withProperty(countryCodeEntryNameInXml, neighborCity.country.iD)
                    .withProperty(adminCodeEntryNameInXml, neighborCity.administrativeArea.iD)
                    .withProperty(locationNameEntryNameInXml, neighborCity.englishName).withBridge(bridgeUID).build();
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
