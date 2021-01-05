package org.openhab.binding.accuweather.internal.util.api;

import org.openhab.binding.accuweather.internal.interfaces.GeoInfo;

import java.math.BigDecimal;

public class GeoInfoImpl implements GeoInfo {
    @Override
    public String getCityName(BigDecimal latitude, BigDecimal longitude) {
        // FIXME(denisacostaq@gmail.com): Use an external API
        return "Sofia";
    }

    @Override
    public String getCountryName(BigDecimal latitude, BigDecimal longitude) {
        // FIXME(denisacostaq@gmail.com): Use an external API
        return "Bulgaria";
    }

    @Override
    public String getCountryDomainName(BigDecimal latitude, BigDecimal longitude) {
        // FIXME(denisacostaq@gmail.com): Use an external API
        return "bg";
    }
}
