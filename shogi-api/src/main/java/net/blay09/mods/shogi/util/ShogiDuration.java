package net.blay09.mods.shogi.util;

import net.minecraft.util.Mth;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

public class ShogiDuration {
    private static final Pattern SHORTHAND_TOKEN_PATTERN = Pattern.compile("([+-]?(?:\\d+(?:\\.\\d+)?|\\.\\d+))\\s*([smhdSMHD])");

    public static Duration parse(String input) {
        try {
            return Duration.parse(input);
        } catch (DateTimeParseException exception) {
            return parseShorthand(input, exception);
        }
    }

    private static Duration parseShorthand(String input, DateTimeParseException cause) {
        final String trimmed = input.trim();
        if (trimmed.isEmpty()) {
            throw invalidDuration(input, 0, cause);
        }

        final var matcher = SHORTHAND_TOKEN_PATTERN.matcher(trimmed);
        int cursor = 0;
        boolean hasToken = false;
        long totalMillis = 0L;
        while (cursor < trimmed.length()) {
            while (cursor < trimmed.length() && Character.isWhitespace(trimmed.charAt(cursor))) {
                cursor++;
            }
            if (cursor >= trimmed.length()) {
                break;
            }

            matcher.region(cursor, trimmed.length());
            if (!matcher.lookingAt()) {
                throw invalidDuration(input, cursor, cause);
            }

            final BigDecimal value;
            try {
                value = new BigDecimal(matcher.group(1));
            } catch (NumberFormatException exception) {
                throw invalidDuration(input, cursor, exception);
            }

            final char unit = Character.toLowerCase(matcher.group(2).charAt(0));
            final BigDecimal unitMillis = switch (unit) {
                case 's' -> BigDecimal.valueOf(1000L);
                case 'm' -> BigDecimal.valueOf(60_000L);
                case 'h' -> BigDecimal.valueOf(3_600_000L);
                case 'd' -> BigDecimal.valueOf(86_400_000L);
                default -> throw invalidDuration(input, cursor, cause);
            };

            final long tokenMillis;
            try {
                tokenMillis = value.multiply(unitMillis)
                        .setScale(0, RoundingMode.HALF_UP)
                        .longValueExact();
            } catch (ArithmeticException exception) {
                throw invalidDuration(input, cursor, exception);
            }

            try {
                totalMillis = Math.addExact(totalMillis, tokenMillis);
            } catch (ArithmeticException exception) {
                throw invalidDuration(input, cursor, exception);
            }

            hasToken = true;
            cursor = matcher.end();
        }

        if (!hasToken) {
            throw invalidDuration(input, 0, cause);
        }

        return Duration.ofMillis(totalMillis);
    }

    private static DateTimeParseException invalidDuration(String input, int index, Throwable cause) {
        return new DateTimeParseException("Text cannot be parsed to a Duration", input, index, cause);
    }

    public static int toTicks(Duration duration) {
        return toTicks(duration, 20);
    }

    public static int toTicks(Duration duration, int tps) {
        return Mth.floor(duration.toMillis() / 1000f * tps);
    }
}
