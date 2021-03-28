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
import java.io.FileReader;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.binding.accuweather.internal.model.pojo.AdministrativeArea;
import org.openhab.binding.accuweather.internal.model.pojo.CitySearchResult;
import org.openhab.binding.accuweather.internal.model.pojo.CurrentConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

/**
 * The {@link DataSource} read the golden files transforming them in POJO objects.
 *
 * @author Alvaro Denis <denisacostaq@gmail.com> - Initial contribution
 */
public class DataSource {
    private final Logger logger = LoggerFactory.getLogger(DataSource.class);
    private static DataSource self = null;
    private CurrentConditions currentConditions = null;
    private List<CitySearchResult> cityNeighborsByLocationKey = null;
    private CitySearchResult cityByGeopossitionSearch = null;
    private List<AdministrativeArea> administrativeAreas = null;
    private List<CitySearchResult> citySearchResultsNarrowedByCountryCodeAndAdminCode = null;

    private DataSource() {
    }

    public static DataSource getInstance() throws FileNotFoundException {
        if (Objects.isNull(self)) {
            self = new DataSource();
            self.init();
        }
        return self;
    }

    public CurrentConditions getCurrentConditions() {
        return currentConditions;
    }

    public List<CitySearchResult> getCityNeighborsByLocationKey() {
        return cityNeighborsByLocationKey;
    }

    public CitySearchResult getCityByGeopossitionSearch() {
        return cityByGeopossitionSearch;
    }

    public List<AdministrativeArea> getAdministrativeAreas() {
        return administrativeAreas;
    }

    public List<CitySearchResult> getCitySearchResultsNarrowedByCountryCodeAndAdminCode() {
        return citySearchResultsNarrowedByCountryCodeAndAdminCode;
    }

    private void init() throws FileNotFoundException {
        currentConditions = loadCurrentConditions();
        cityNeighborsByLocationKey = loadCityNeighborsByLocationKey();
        cityByGeopossitionSearch = loadCityByGeopossitionSearch();
        administrativeAreas = loadAdministrativeAreas();
        citySearchResultsNarrowedByCountryCodeAndAdminCode = loadCitySearchResultsNarrowedByCountryCodeAndAdminCode();
    }

    private Object loadFile(String fileName, Class cl) throws FileNotFoundException {
        URL currentConditionsPath = DataSource.class.getResource("/OH-INF/golden-files/" + fileName);
        Gson gson = new Gson();
        JsonReader reader = new JsonReader(new FileReader(currentConditionsPath.getFile()));
        Object data = gson.fromJson(reader, cl);
        return data;
    }

    private CurrentConditions loadCurrentConditions() throws FileNotFoundException {
        return (CurrentConditions) loadFile("Current_Conditions.json", CurrentConditions.class);
    }

    private @NonNull List<CitySearchResult> loadCityNeighborsByLocationKey() throws FileNotFoundException {
        return Arrays
                .asList((CitySearchResult[]) loadFile("City_NeighborsbylocationKey.json", CitySearchResult[].class));
    }

    private CitySearchResult loadCityByGeopossitionSearch() throws FileNotFoundException {
        return (CitySearchResult) loadFile("Geoposition_Search.json", CitySearchResult.class);
    }

    private @NonNull List<AdministrativeArea> loadAdministrativeAreas() throws FileNotFoundException {
        return Arrays.asList((AdministrativeArea[]) loadFile("Admin_Area_List.json", AdministrativeArea[].class));
    }

    private @NonNull List<CitySearchResult> loadCitySearchResultsNarrowedByCountryCodeAndAdminCode()
            throws FileNotFoundException {
        return Arrays
                .asList((CitySearchResult[]) loadFile("City_Search_results_narrowed_by_countryCode_and_adminCode_.json",
                        CitySearchResult[].class));
    }
}
