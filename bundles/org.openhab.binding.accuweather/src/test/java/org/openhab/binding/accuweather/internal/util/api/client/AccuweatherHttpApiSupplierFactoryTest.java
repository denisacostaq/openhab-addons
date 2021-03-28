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

import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.openhab.binding.accuweather.internal.exceptions.HttpErrorResponseException;
import org.openhab.binding.accuweather.internal.exceptions.RemoteErrorResponseException;
import org.openhab.binding.accuweather.internal.interfaces.AccuweatherHttpApiSupplierFactoryInterface;
import org.openhab.binding.accuweather.internal.interfaces.HttpClientRawInterface;
import org.openhab.binding.accuweather.internal.interfaces.ObjectMapper;
import org.openhab.binding.accuweather.internal.interfaces.cache.ExpiringValue;
import org.openhab.binding.accuweather.internal.interfaces.cache.ThrowingSupplier;
import org.openhab.binding.accuweather.internal.model.pojo.*;
import org.openhab.binding.accuweather.internal.util.cache.ExpiringValueImpl;

/**
 * The {@link AccuweatherHttpApiSupplierFactoryTest} have the unit tests for {@link AccuweatherHttpApiSupplierFactory}.
 *
 * @author Alvaro Denis <denisacostaq@gmail.com> - Initial contribution
 */
class AccuweatherHttpApiSupplierFactoryTest {
    private @Mock HttpClientRawInterface<ExpiringValue<String>, @NonNull RemoteErrorResponseException> httpClientRaw;
    private @Mock ObjectMapper mapper;
    private AccuweatherHttpApiSupplierFactoryInterface<String, Object, @NonNull RemoteErrorResponseException> httpApiSupplierFactory;

    private void initMocks() {
        httpClientRaw = Mockito.mock(HttpClientRawInterface.class);
        mapper = Mockito.mock(ObjectMapper.class);
    }

    @BeforeEach
    void setUp() {
        initMocks();
        httpApiSupplierFactory = new AccuweatherHttpApiSupplierFactory<>(httpClientRaw, mapper);
    }

    @Test
    void getAdminAreasSupplier() throws RemoteErrorResponseException, HttpErrorResponseException {
        // Giving
        final String adminAreaId = "22";
        final String countryId = "BG";
        final String apiKey = "api key";
        Mockito.when(httpClientRaw.getAdminAreas(Mockito.eq(countryId), Mockito.eq(apiKey)))
                .thenReturn(new ExpiringValueImpl<>(null, "a"));
        Mockito.when(mapper.deserializeAdminAreasResult("a"))
                .thenReturn(Arrays.asList(new AdministrativeArea(adminAreaId, countryId)));
        ThrowingSupplier<@Nullable ExpiringValue<Object>, @NonNull RemoteErrorResponseException> supplier = httpApiSupplierFactory
                .getAdminAreasSupplier(countryId, apiKey);

        // When
        @Nullable
        ExpiringValue<Object> result = supplier.get();

        // Then
        assertThat(result, is(notNullValue()));
        assertThat(result.expiresAt(), is(equalTo(null)));
        assertThat(result.value(), is(notNullValue()));
        assertThat(result.value(), is(instanceOf(List.class)));
        assertThat(((List<Object>) (result.value())).size(), is(equalTo(1)));
        assertThat(((List<Object>) (result.value())).get(0), is(instanceOf(AdministrativeArea.class)));
        assertThat(((List<AdministrativeArea>) (result.value())).get(0).iD, is(equalTo(adminAreaId)));
        assertThat(((List<AdministrativeArea>) (result.value())).get(0).countryID, is(equalTo(countryId)));
    }

    @Test
    void getCitySearchSupplier() throws HttpErrorResponseException, RemoteErrorResponseException {
        // Giving
        final String cityKey = "city key";
        final String apiKey = "api key";
        final AdministrativeArea adminAreaQuery = new AdministrativeArea("22", "BG");
        final CitySearchResult cityQuery = new CitySearchResult("", "city name");
        Mockito.when(httpClientRaw.citySearch(Mockito.eq(adminAreaQuery.countryID), Mockito.eq(adminAreaQuery.iD),
                Mockito.eq(cityQuery.englishName), Mockito.eq(apiKey))).thenReturn(new ExpiringValueImpl<>(null, "a"));
        Mockito.when(mapper.deserializeCitySearchResult("a"))
                .thenReturn(Arrays.asList(new CitySearchResult(cityKey, cityQuery.englishName)));
        ThrowingSupplier<@Nullable ExpiringValue<Object>, @NonNull RemoteErrorResponseException> supplier = httpApiSupplierFactory
                .getCitySearchSupplier(adminAreaQuery, cityQuery, apiKey);

        // When
        @Nullable
        ExpiringValue<Object> result = supplier.get();

        // Then
        assertThat(result, is(notNullValue()));
        assertThat(result.expiresAt(), is(equalTo(null)));
        assertThat(result.value(), is(notNullValue()));
        assertThat(result.value(), is(instanceOf(List.class)));
        assertThat(((List<Object>) (result.value())).size(), is(equalTo(1)));
        assertThat(((List<Object>) (result.value())).get(0), is(instanceOf(CitySearchResult.class)));
        assertThat(((List<CitySearchResult>) (result.value())).get(0).key, is(equalTo(cityKey)));
        assertThat(((List<CitySearchResult>) (result.value())).get(0).englishName, is(equalTo(cityQuery.englishName)));
    }

    @Test
    void getCurrentConditionsSupplier() throws HttpErrorResponseException, RemoteErrorResponseException {
        // Giving
        final String cityKey = "city key";
        final String apiKey = "api key";
        final String weatherText = "Lloviendo";
        final String unitType = "C";
        final Double temVal = Double.valueOf(-1.121);
        final CitySearchResult cityQuery = new CitySearchResult(cityKey, "");
        Mockito.when(httpClientRaw.getCurrentConditions(Mockito.eq(cityQuery.key), Mockito.eq(apiKey)))
                .thenReturn(new ExpiringValueImpl<>(null, "a"));
        Mockito.when(mapper.deserializeCurrentConditions("a"))
                .thenReturn(new CurrentConditions(weatherText, false, new Temperature(new Metric(temVal, unitType))));
        ThrowingSupplier<@Nullable ExpiringValue<Object>, @NonNull RemoteErrorResponseException> supplier = httpApiSupplierFactory
                .getCurrentConditionsSupplier(cityQuery, apiKey);

        // When
        @Nullable
        ExpiringValue<Object> result = supplier.get();

        // Then
        assertThat(result, is(notNullValue()));
        assertThat(result.expiresAt(), is(equalTo(null)));
        assertThat(result.value(), is(notNullValue()));
        assertThat(result.value(), is(instanceOf(CurrentConditions.class)));
        assertThat(((CurrentConditions) (result.value())).weatherText, is(equalTo(weatherText)));
        assertThat(((CurrentConditions) (result.value())).isDayTime, is(equalTo(false)));
        assertThat(((CurrentConditions) (result.value())).temperature.metric.unit, is(equalTo(unitType)));
        assertThat(((CurrentConditions) (result.value())).temperature.metric.value, is(closeTo(temVal, 0.001)));
    }

    @Test
    void getNeighborsCitiesSupplier() throws HttpErrorResponseException, RemoteErrorResponseException {
        // Giving
        final String cityKey = "city key";
        final String apiKey = "api key";
        final CitySearchResult cityQuery = new CitySearchResult(cityKey, "");
        Mockito.when(httpClientRaw.neighborsCities(Mockito.eq(cityQuery.key), Mockito.eq(apiKey)))
                .thenReturn(new ExpiringValueImpl<>(null, "a"));
        Mockito.when(mapper.deserializeCitySearchResult("a"))
                .thenReturn(Arrays.asList(new CitySearchResult(cityKey, cityQuery.englishName)));
        ThrowingSupplier<@Nullable ExpiringValue<Object>, @NonNull RemoteErrorResponseException> supplier = httpApiSupplierFactory
                .getNeighborsCitiesSupplier(cityQuery, apiKey);

        // When
        @Nullable
        ExpiringValue<Object> result = supplier.get();

        // Then
        assertThat(result, is(notNullValue()));
        assertThat(result.expiresAt(), is(equalTo(null)));
        assertThat(result.value(), is(notNullValue()));
        assertThat(result.value(), is(instanceOf(List.class)));
        assertThat(((List<Object>) (result.value())).size(), is(equalTo(1)));
        assertThat(((List<Object>) (result.value())).get(0), is(instanceOf(CitySearchResult.class)));
        assertThat(((List<CitySearchResult>) (result.value())).get(0).key, is(equalTo(cityKey)));
        assertThat(((List<CitySearchResult>) (result.value())).get(0).englishName, is(equalTo(cityQuery.englishName)));
    }

    @Test
    void getCitySearchByCoordinatesSupplier() throws HttpErrorResponseException, RemoteErrorResponseException {
        // Giving
        final String cityKey = "city key";
        final String cityName = "city name";
        final String apiKey = "api key";
        final Float latitude = Float.valueOf(3.565f);
        final Float longitude = Float.valueOf(2.545f);
        Mockito.when(httpClientRaw.geoPositionSearch(Mockito.eq(latitude), Mockito.eq(longitude), Mockito.eq(apiKey)))
                .thenReturn(new ExpiringValueImpl<>(null, "a"));
        Mockito.when(mapper.deserializeSingleCitySearchResult("a")).thenReturn(new CitySearchResult(cityKey, cityName));
        ThrowingSupplier<@Nullable ExpiringValue<Object>, @NonNull RemoteErrorResponseException> supplier = httpApiSupplierFactory
                .getCitySearchByCoordinatesSupplier(latitude, longitude, apiKey);

        // When
        @Nullable
        ExpiringValue<Object> result = supplier.get();

        // Then
        assertThat(result, is(notNullValue()));
        assertThat(result.expiresAt(), is(equalTo(null)));
        assertThat(result.value(), is(notNullValue()));
        assertThat(result.value(), is(instanceOf(CitySearchResult.class)));
        assertThat(((CitySearchResult) (result.value())).key, is(equalTo(cityKey)));
        assertThat(((CitySearchResult) (result.value())).englishName, is(equalTo(cityName)));
    }
}
