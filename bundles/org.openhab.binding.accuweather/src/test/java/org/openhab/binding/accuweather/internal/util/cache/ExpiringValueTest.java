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

package org.openhab.binding.accuweather.internal.util.cache;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.time.Instant;
import java.util.Calendar;
import java.util.Date;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openhab.binding.accuweather.internal.interfaces.cache.ExpiringValue;

import generator.Generator;

/**
 * The {@link ExpiringValueTest} have the unit tests for {@link ExpiringValue}.
 *
 * @author Alvaro Denis <denisacostaq@gmail.com> - Initial contribution
 */
class ExpiringValueTest {

    @BeforeAll
    static void beforeAll() {
        new ExpiringValueImpl<>(new Date(Instant.now().toEpochMilli()), "");
    }

    @Test
    void expiresAt() {
        Date expiresAt = new Date(Instant.now().toEpochMilli());
        final ExpiringValueImpl<String> expiringValue = new ExpiringValueImpl<>(expiresAt, "val");

        // When
        Date result = expiringValue.expiresAt();

        // Then
        assertThat(result, is(notNullValue()));
        assertThat(result, is(equalTo(expiresAt)));
    }

    @Test
    void stillValidAfterShouldBeFalse() {
        Date expiresAt = new Date(Instant.now().toEpochMilli());
        final ExpiringValueImpl<String> expiringValue = new ExpiringValueImpl<>(expiresAt, "val");

        // When
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(expiresAt);
        calendar.add(Calendar.SECOND, 10);
        Date relative = calendar.getTime();
        boolean result = expiringValue.stillValid(relative);

        // Then
        assertThat(result, is(notNullValue()));
        assertThat(result, is(equalTo(false)));
    }

    @Test
    void stillValidBeforeShouldBeTrue() {
        Date expiresAt = new Date(Instant.now().toEpochMilli());
        final ExpiringValueImpl<String> expiringValue = new ExpiringValueImpl<>(expiresAt, "val");

        // When
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(expiresAt);
        calendar.add(Calendar.SECOND, -10);
        Date relative = calendar.getTime();
        boolean result = expiringValue.stillValid(relative);

        // Then
        assertThat(result, is(notNullValue()));
        assertThat(result, is(equalTo(true)));
    }

    @Test
    void stillValidSameShouldBeFalse() {
        Date expiresAt = new Date(Instant.now().toEpochMilli());
        final ExpiringValueImpl<String> expiringValue = new ExpiringValueImpl<>(expiresAt, "val");

        // When
        boolean result = expiringValue.stillValid(expiresAt);

        // Then
        assertThat(result, is(notNullValue()));
        assertThat(result, is(equalTo(false)));
    }

    @Test
    void stillValidNullShouldBeFalse() {
        Date now = new Date(Instant.now().toEpochMilli());
        final ExpiringValueImpl<String> expiringValue = new ExpiringValueImpl<>(null, "val");

        // When
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.SECOND, -10);
        Date relativeBefore = calendar.getTime();
        calendar.setTime(now);
        calendar.add(Calendar.SECOND, 10);
        Date relativeAfter = calendar.getTime();
        boolean resultBefore = expiringValue.stillValid(relativeBefore);
        boolean resultSame = expiringValue.stillValid(now);
        boolean resultAfter = expiringValue.stillValid(relativeAfter);

        // Then
        assertThat(resultBefore, is(notNullValue()));
        assertThat(resultBefore, is(equalTo(false)));
        assertThat(resultSame, is(notNullValue()));
        assertThat(resultSame, is(equalTo(false)));
        assertThat(resultAfter, is(notNullValue()));
        assertThat(resultAfter, is(equalTo(false)));
    }

    @Test
    void value() {
        String val = Generator.getRandomString();
        final ExpiringValueImpl<String> expiringValue = new ExpiringValueImpl<>(new Date(Instant.now().toEpochMilli()),
                val);

        // When
        String result = expiringValue.value();

        // Then
        assertThat(result, is(notNullValue()));
        assertThat(result, is(equalTo(val)));
    }
}
