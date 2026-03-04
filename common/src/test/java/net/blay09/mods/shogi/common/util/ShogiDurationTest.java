package net.blay09.mods.shogi.common.util;

import net.blay09.mods.shogi.util.ShogiDuration;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.format.DateTimeParseException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ShogiDurationTest {

    @Test
    void parsesIsoDuration() {
        assertEquals(Duration.ofSeconds(300), ShogiDuration.parse("PT300S"));
    }

    @Test
    void parsesSinglePartShorthand() {
        assertEquals(Duration.ofSeconds(300), ShogiDuration.parse("300s"));
        assertEquals(Duration.ofSeconds(300), ShogiDuration.parse("5m"));
        assertEquals(Duration.ofSeconds(7200), ShogiDuration.parse("2h"));
        assertEquals(Duration.ofDays(1), ShogiDuration.parse("1d"));
    }

    @Test
    void parsesDecimalShorthand() {
        assertEquals(Duration.ofMillis(1500), ShogiDuration.parse("1.5s"));
        assertEquals(Duration.ofSeconds(15), ShogiDuration.parse("0.25m"));
    }

    @Test
    void parsesCompoundShorthand() {
        assertEquals(Duration.ofSeconds(5400), ShogiDuration.parse("1h30m"));
        assertEquals(Duration.ofSeconds(5400), ShogiDuration.parse("30m1h"));
        assertEquals(Duration.ofSeconds(6300), ShogiDuration.parse("1h30m15m"));
        assertEquals(Duration.ofMillis(135_500), ShogiDuration.parse("2m15.5s"));
    }

    @Test
    void parsesCompoundWithWhitespaceAndCaseInsensitivity() {
        assertEquals(Duration.ofSeconds(5400), ShogiDuration.parse(" 1H 30m "));
        assertEquals(Duration.ofSeconds(9015), ShogiDuration.parse("2h 30m 15S"));
    }

    @Test
    void rejectsInvalidInputs() {
        assertThrows(DateTimeParseException.class, () -> ShogiDuration.parse("300"));
        assertThrows(DateTimeParseException.class, () -> ShogiDuration.parse("10w"));
        assertThrows(DateTimeParseException.class, () -> ShogiDuration.parse("1h-"));
        assertThrows(DateTimeParseException.class, () -> ShogiDuration.parse("foo"));
        assertThrows(DateTimeParseException.class, () -> ShogiDuration.parse("1h,30m"));
    }
}
