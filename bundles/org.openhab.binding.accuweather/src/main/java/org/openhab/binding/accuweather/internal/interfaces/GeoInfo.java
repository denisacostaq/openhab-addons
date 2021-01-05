package org.openhab.binding.accuweather.internal.interfaces;

import java.math.BigDecimal;

public interface GeoInfo {
    String getCityName(BigDecimal latitude, BigDecimal longitude);
    String getCountryName(BigDecimal latitude, BigDecimal longitude);
    String getCountryDomainName(BigDecimal latitude, BigDecimal longitude);
}
