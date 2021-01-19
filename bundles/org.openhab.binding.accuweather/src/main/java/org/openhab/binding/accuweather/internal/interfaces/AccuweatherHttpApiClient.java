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

package org.openhab.binding.accuweather.internal.interfaces;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.accuweather.internal.model.pojo.AdministrativeArea;
import org.openhab.binding.accuweather.internal.model.pojo.CitySearchResult;
import org.openhab.binding.accuweather.internal.model.pojo.CurrentConditions;

/**
 * The {@link AccuweatherHttpApiClient} is responsible for preparing the requests to HttpClientRawInterface
 * and deserialize the responses to the models.
 *
 * @author Alvaro Denis <denisacostaq@gmail.com> - Initial contribution
 */
@NonNullByDefault
public interface AccuweatherHttpApiClient<HttpRespT, CacheValT, E extends Throwable> {
    @Nullable // FIXME(denisacostaq@gmail.com): remove
    List<AdministrativeArea> getAdminAreas(String countryDomainName) throws E;

    @Nullable // FIXME(denisacostaq@gmail.com): remove
    List<CitySearchResult> citySearch(AdministrativeArea adminCode, CitySearchResult cityQuery) throws E;

    @Nullable // FIXME(denisacostaq@gmail.com): remove
    CurrentConditions currentConditions(CitySearchResult city) throws E;

    List<CitySearchResult> getNeighborsCities(CitySearchResult cityQuery) throws E;

    boolean verifyHttpApiKey(String apiKey) throws E;
}
