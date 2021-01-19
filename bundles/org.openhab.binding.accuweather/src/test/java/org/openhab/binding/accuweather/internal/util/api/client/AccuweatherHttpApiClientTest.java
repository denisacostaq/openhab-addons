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

package org.openhab.binding.accuweather.internal.util.api.client;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.openhab.binding.accuweather.internal.exceptions.RemoteErrorResponseException;
import org.openhab.binding.accuweather.internal.interfaces.GeoInfo;
import org.openhab.binding.accuweather.internal.interfaces.ObjectMapper;
import org.openhab.binding.accuweather.internal.interfaces.cache.ExpiringCacheMapInterface;
import org.openhab.binding.accuweather.internal.interfaces.cache.ExpiringValue;
import org.openhab.binding.accuweather.internal.model.pojo.*;
import org.openhab.core.i18n.LocationProvider;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.PointType;

/**
 * The {@link AccuweatherHttpApiClientTest} have the unit tests for {@link AccuweatherHttpApiClient}.
 *
 * @author Alvaro Denis <denisacostaq@gmail.com> - Initial contribution
 */
class AccuweatherHttpApiClientTest {
    private @Mock LocationProvider locationProvider;
    private @Mock HttpClientRawInterface<ExpiringValue<String>, @NonNull RemoteErrorResponseException> httpClientRaw;
    private @Mock ObjectMapper mapper;
    private @Mock ExpiringCacheMapInterface<String, Object, @NonNull RemoteErrorResponseException> cache;

    org.openhab.binding.accuweather.internal.interfaces.AccuweatherHttpApiClient<String, Object, @NonNull RemoteErrorResponseException> accuweatherHttpApiClient;

    private void initMocks() {
        locationProvider = Mockito.mock(LocationProvider.class);
        httpClientRaw = Mockito.mock(HttpClientRawInterface.class);
        mapper = Mockito.mock(ObjectMapper.class);
        cache = Mockito.mock(ExpiringCacheMapInterface.class);
    }

    @BeforeEach
    void setUp() {
        initMocks();
        accuweatherHttpApiClient = new AccuweatherHttpApiClient(locationProvider, httpClientRaw, mapper, cache);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void getAdminAreas() throws RemoteErrorResponseException {
        // Giving
        final String adminAreaId = "22";
        final String countryId = "BG";
        when(cache.putIfAbsentAndGet(Mockito.any(), Mockito.any()))
                .thenReturn(Arrays.asList(new AdministrativeArea(adminAreaId, countryId)));

        // When
        List<AdministrativeArea> result = accuweatherHttpApiClient.getAdminAreas("BG");

        // Then
        assertThat(result, is(notNullValue()));
        assertThat(result.toArray(), is(not(emptyArray())));
        assertThat(result.get(0).iD, is(equalTo(adminAreaId)));
        assertThat(result.get(0).countryID, is(equalTo(countryId)));
    }

    @Test
    void citySearch() throws RemoteErrorResponseException {
        // Giving
        final String cityKey = "51097";
        final String cityName = "Sofia";
        when(cache.putIfAbsentAndGet(Mockito.any(), Mockito.any()))
                .thenReturn(Arrays.asList(new CitySearchResult(cityKey, cityName)));

        // When
        List<CitySearchResult> result = accuweatherHttpApiClient.citySearch(new AdministrativeArea(),
                new CitySearchResult());

        // Then
        assertThat(result, is(notNullValue()));
        assertThat(result.toArray(), is(not(emptyArray())));
        assertThat(result.get(0).key, is(equalTo(cityKey)));
        assertThat(result.get(0).englishName, is(equalTo(cityName)));
    }

    @Test
    void currentConditions() throws RemoteErrorResponseException {
        // Giving
        final Date localObservationDateTime = Date.from(Instant.now());
        final String weatherText = "Snowing";
        final boolean hasPrecipitation = true;
        final Object precipitationType = "Snow";
        final boolean isDayTime = true;
        final Temperature temperature = new Temperature(new Metric(-1, "C"));
        when(cache.putIfAbsentAndGet(Mockito.any(), Mockito.any())).thenReturn(new CurrentConditions(
                localObservationDateTime, weatherText, hasPrecipitation, precipitationType, isDayTime, temperature));

        // When
        CurrentConditions result = accuweatherHttpApiClient.currentConditions(new CitySearchResult());

        // Then
        assertThat(result.localObservationDateTime, is(equalTo(localObservationDateTime)));
        assertThat(result.weatherText, is(equalTo(weatherText)));
        assertThat(result.hasPrecipitation, is(equalTo(hasPrecipitation)));
        assertThat(result.precipitationType, is(equalTo(precipitationType)));
        assertThat(result.isDayTime, is(equalTo(isDayTime)));
        assertThat(result.temperature, is(equalTo(temperature)));
    }

    @Test
    void getNeighborsCities() throws RemoteErrorResponseException {
        // Giving
        final String city1Key = "51097";
        final String city1Name = "Sofia";
        final String city2Key = "46310";
        final String city2Name = "German";
        when(cache.putIfAbsentAndGet(Mockito.any(), Mockito.any())).thenReturn(
                Arrays.asList(new CitySearchResult(city1Key, city1Name), new CitySearchResult(city2Key, city2Name)));

        // When
        List<CitySearchResult> result = accuweatherHttpApiClient
                .getNeighborsCities(new CitySearchResult(city1Key, city1Name));

        // Then
        assertThat(result, is(notNullValue()));
        assertThat(result.toArray(), arrayWithSize(2));
        assertThat(result.get(0).key, is(equalTo(city1Key)));
        assertThat(result.get(0).englishName, is(equalTo(city1Name)));
        assertThat(result.get(1).key, is(equalTo(city2Key)));
        assertThat(result.get(1).englishName, is(equalTo(city2Name)));
    }

    @Test
    void getNeighborsCitiesEmptyCityKey() throws RemoteErrorResponseException {
        // Giving
        final String city1Key = "51097";
        final String city1Name = "Sofia";
        final String city2Key = "46310";
        final String city2Name = "German";
        when(cache.putIfAbsentAndGet(Mockito.any(), Mockito.any())).thenReturn(
                Arrays.asList(new CitySearchResult(city1Key, city1Name), new CitySearchResult(city2Key, city2Name)));

        // When
        List<CitySearchResult> result = accuweatherHttpApiClient.getNeighborsCities(new CitySearchResult());

        // Then
        assertThat(result, is(not(nullValue())));
        assertThat(result.toArray(), is(emptyArray()));
    }

    @Test
    void verifyHttpApiKey() throws RemoteErrorResponseException {
        // Giving
        final String apiKey = "api key";
        final String adminAreaId = "22";
        final String countryId = "BG";
        final BigDecimal latitude = BigDecimal.valueOf(1.454);
        final BigDecimal longitude = BigDecimal.valueOf(1.34343);
        when(locationProvider.getLocation())
                .thenReturn(new PointType(new DecimalType(latitude), new DecimalType(longitude)));
        when(cache.putIfAbsentAndGet(
                Mockito.eq(String.format("%s/%f/%f", apiKey, latitude.floatValue(), longitude.floatValue())),
                Mockito.any())).thenReturn(new CitySearchResult(new Country(countryId)));
        when(cache.putIfAbsentAndGet(Mockito.eq(String.format("%s/%s", apiKey, "BG")), Mockito.any()))
                .thenReturn(Arrays.asList(new AdministrativeArea(adminAreaId, countryId)));

        // When
        boolean result = accuweatherHttpApiClient.verifyHttpApiKey(apiKey);

        // Then
        assertThat(result, is(equalTo(true)));
    }

    @Test
    void verifyHttpApiKeyNullAdminAreas() throws RemoteErrorResponseException {
        // Giving
        final String apiKey = "api key";
        final String countryId = "BG";
        final BigDecimal latitude = BigDecimal.valueOf(1.454);
        final BigDecimal longitude = BigDecimal.valueOf(1.34343);
        when(locationProvider.getLocation())
                .thenReturn(new PointType(new DecimalType(latitude), new DecimalType(longitude)));
        when(cache.putIfAbsentAndGet(
                Mockito.eq(String.format("%s/%f/%f", apiKey, latitude.floatValue(), longitude.floatValue())),
                Mockito.any())).thenReturn(new CitySearchResult(new Country(countryId)));
        when(cache.putIfAbsentAndGet(Mockito.eq(String.format("%s/%s", apiKey, countryId)), Mockito.any()))
                .thenReturn(null);

        // When
        boolean result = accuweatherHttpApiClient.verifyHttpApiKey(apiKey);

        // Then
        assertThat(result, is(equalTo(false)));
    }

    @Test
    void verifyHttpApiKeyEmptyAdminAreas() throws RemoteErrorResponseException {
        // Giving
        final String apiKey = "api key";
        final String countryId = "BG";
        final BigDecimal latitude = BigDecimal.valueOf(1.454);
        final BigDecimal longitude = BigDecimal.valueOf(1.34343);
        when(locationProvider.getLocation())
                .thenReturn(new PointType(new DecimalType(latitude), new DecimalType(longitude)));
        when(cache.putIfAbsentAndGet(
                Mockito.eq(String.format("%s/%f/%f", apiKey, latitude.floatValue(), longitude.floatValue())),
                Mockito.any())).thenReturn(new CitySearchResult(new Country(countryId)));
        when(cache.putIfAbsentAndGet(Mockito.eq(String.format("%s/%s", apiKey, countryId)), Mockito.any()))
                .thenReturn(new ArrayList<>());

        // When
        boolean result = accuweatherHttpApiClient.verifyHttpApiKey(apiKey);

        // Then
        assertThat(result, is(equalTo(false)));
    }

    @Test
    void getCityName() throws Throwable {
        // Preconditions
        assertThat(accuweatherHttpApiClient, is(instanceOf(GeoInfo.class)));

        // Giving
        final String cityKey = "51097";
        final String cityName = "Sofia";
        when(cache.putIfAbsentAndGet(Mockito.any(), Mockito.any())).thenReturn(new CitySearchResult(cityKey, cityName));

        // When
        String result = ((GeoInfo) accuweatherHttpApiClient).getCityName(new PointType(
                new DecimalType(BigDecimal.valueOf(1.454)), new DecimalType(BigDecimal.valueOf(1.34343))));

        // Then
        assertThat(result, is(equalTo(cityName)));
    }

    @Test
    void getCountryDomainName() throws Throwable {
        // Preconditions
        assertThat(accuweatherHttpApiClient, is(instanceOf(GeoInfo.class)));

        // Giving
        final String countryId = "BG";
        when(cache.putIfAbsentAndGet(Mockito.any(), Mockito.any()))
                .thenReturn(new CitySearchResult(new Country(countryId)));

        // When
        String result = ((GeoInfo) accuweatherHttpApiClient).getCountryDomainName(new PointType(
                new DecimalType(BigDecimal.valueOf(1.454)), new DecimalType(BigDecimal.valueOf(1.34343))));

        // Then
        assertThat(result, is(equalTo(countryId)));
    }

    @Test
    void getAdministrativeArea() throws Throwable {
        // Preconditions
        assertThat(accuweatherHttpApiClient, is(instanceOf(GeoInfo.class)));

        // Giving
        final String adminAreaName = "Sofia";
        when(cache.putIfAbsentAndGet(Mockito.any(), Mockito.any()))
                .thenReturn(new CitySearchResult(new AdministrativeArea("", adminAreaName, "")));

        // When
        String result = ((GeoInfo) accuweatherHttpApiClient).getAdministrativeArea(new PointType(
                new DecimalType(BigDecimal.valueOf(1.454)), new DecimalType(BigDecimal.valueOf(1.34343))));

        // Then
        assertThat(result, is(equalTo(adminAreaName)));
    }
}
