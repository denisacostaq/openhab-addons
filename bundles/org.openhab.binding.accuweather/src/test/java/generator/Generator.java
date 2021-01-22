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

package generator;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import org.openhab.binding.accuweather.internal.model.pojo.AdministrativeArea;
import org.openhab.binding.accuweather.internal.model.pojo.CitySearchResult;
import org.openhab.binding.accuweather.internal.model.pojo.CurrentConditions;

/**
 * The {@link Generator} generate random Data for the tests
 *
 * @author Alvaro Denis <denisacostaq@gmail.com> - Initial contribution
 */
public class Generator {
    public static String getRandomString() {
        return UUID.randomUUID().toString();
    }

    public static boolean getRandomBool() {
        return new Random().nextBoolean();
    }

    private static List<AdministrativeArea> getAdminAreas() throws FileNotFoundException {
        return DataSource.getInstance().getAdministrativeAreas();
    }

    public static AdministrativeArea getRandomAdminArea() throws FileNotFoundException {
        List<AdministrativeArea> administrativeAreas = getAdminAreas();
        // TODO(denisacostaq@gmail.com): The implementations should look like this, so more data is required
        // administrativeAreas.get(getRandomIndex(Collections.singletonList(administrativeAreas)));
        return administrativeAreas.stream().filter(administrativeArea -> "22".equals(administrativeArea.iD)).findFirst()
                .get();
    }

    public static CurrentConditions getRandomCurrentConditions(CitySearchResult city) throws FileNotFoundException {
        assert "51097".equals(city.key);
        return DataSource.getInstance().getCurrentConditions();
    }

    public static List<CitySearchResult> getCityNeighborsByLocationKey(CitySearchResult query)
            throws FileNotFoundException {
        assert "51097".equals(query.key);
        return DataSource.getInstance().getCityNeighborsByLocationKey();
    }

    public static CitySearchResult getCityByGeopossitionSearch() throws FileNotFoundException {
        return DataSource.getInstance().getCityByGeopossitionSearch();
    }

    public static List<CitySearchResult> getCitySearchResultsNarrowedByCountryCodeAndAdminCode(
            AdministrativeArea narrowAdminArea, CitySearchResult query) throws FileNotFoundException {
        assert "BG".equals(narrowAdminArea.countryID);
        assert "22".equals(narrowAdminArea.iD);
        assert "Sofia".equals(query.englishName);
        return DataSource.getInstance().getCitySearchResultsNarrowedByCountryCodeAndAdminCode();
    }

    private static int getRandomUint(int min, int max) {
        assert min >= 0;
        assert max > min;
        return min + new Random().nextInt(max - min);
    }

    private static int getRandomIndex(@NotNull List<Object> lst) {
        return getRandomUint(0, lst.size() - 1);
    }
}
