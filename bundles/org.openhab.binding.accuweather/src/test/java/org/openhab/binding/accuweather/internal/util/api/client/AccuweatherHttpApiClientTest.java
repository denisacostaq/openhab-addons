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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.NotImplementedException;
import org.eclipse.jdt.annotation.NonNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.openhab.binding.accuweather.internal.exceptions.HttpErrorResponseException;
import org.openhab.binding.accuweather.internal.exceptions.RemoteErrorResponseException;
import org.openhab.binding.accuweather.internal.interfaces.ObjectMapper;
import org.openhab.binding.accuweather.internal.interfaces.cache.ExpiringCacheMapInterface;
import org.openhab.binding.accuweather.internal.interfaces.cache.ExpiringValue;
import org.openhab.binding.accuweather.internal.model.pojo.AdministrativeArea;
import org.openhab.core.i18n.LocationProvider;

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
    void getAdminAreas() throws RemoteErrorResponseException, HttpErrorResponseException {
        // Giving
        final String adminAreaId = "22";
        final String countryId = "BG";
        List<AdministrativeArea> adminAreasModel = Arrays.asList(new AdministrativeArea(adminAreaId, countryId));
        when(cache.putIfAbsentAndGet(Mockito.any(), Mockito.any())).thenReturn(adminAreasModel);

        // When
        List<AdministrativeArea> result = accuweatherHttpApiClient.getAdminAreas("BG");

        // Then
        assertThat(result.isEmpty(), is(false));
        assertThat(result.get(0).iD, is(equalTo(adminAreaId)));
        assertThat(result.get(0).countryID, is(equalTo(countryId)));
    }

    @Test
    void citySearch() {
        throw new NotImplementedException("implement citySearch test");
    }

    @Test
    void currentConditions() {
        throw new NotImplementedException("implement currentConditions test");
    }

    @Test
    void getNeighborsCities() {
        throw new NotImplementedException("implement getNeighborsCities test");
    }

    @Test
    void verifyHttpApiKey() {
        throw new NotImplementedException("implement verifyHttpApiKey test");
    }

    @Test
    void getCityName() {
        throw new NotImplementedException("implement getCityName test");
    }

    @Test
    void getCountryDomainName() {
        throw new NotImplementedException("implement getCountryDomainName test");
    }

    @Test
    void getAdministrativeArea() {
        throw new NotImplementedException("implement getAdministrativeArea test");
    }
}
