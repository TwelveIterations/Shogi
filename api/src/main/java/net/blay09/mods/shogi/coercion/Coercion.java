package net.blay09.mods.shogi.coercion;

import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;
import com.mojang.datafixers.util.Either;
import net.blay09.mods.shogi.util.ShogiDuration;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * Built-in coercion helpers for converting resolved values into common types.
 */
public final class Coercion {

    private Coercion() {
    }

    /**
     * Coerces lists/json arrays to their first element and leaves other values unchanged.
     */
    public static final Function<Object, Object> FIRST = input -> switch (input) {
        case List<?> list when !list.isEmpty() -> list.getFirst();
        case List<?> _ -> Either.right(new CoercionException(input, "Cannot retrieve first element of empty list"));
        case JsonArray array when !array.isEmpty() -> array.get(0);
        case JsonArray _ -> Either.right(new CoercionException(input, "Cannot retrieve first element of empty json array"));
        default -> input;
    };

    /**
     * Coerces lists/json arrays to their last element and leaves other values unchanged.
     */
    public static final Function<Object, Object> LAST = input -> switch (input) {
        case List<?> list when !list.isEmpty() -> list.getLast();
        case List<?> _ -> Either.right(new CoercionException(input, "Cannot retrieve last element of empty list"));
        case JsonArray array when !array.isEmpty() -> array.get(array.size() - 1);
        case JsonArray _ -> Either.right(new CoercionException(input, "Cannot retrieve last element of empty json array"));
        default -> input;
    };

    /**
     * Wraps non-list values into a single-element list.
     */
    public static final Function<Object, List<?>> LIST = input -> switch (input) {
        case List<?> list -> list;
        default -> List.of(input);
    };

    /**
     * Coerces values to an integer, reading the last list/array element when applicable.
     */
    public static final Function<Object, Integer> INT = LAST.andThen(input -> switch (input) {
        case Integer intValue -> intValue;
        case Number numberValue -> numberValue.intValue();
        case JsonPrimitive jsonElement when jsonElement.isNumber() -> jsonElement.getAsInt();
        case JsonPrimitive jsonElement when jsonElement.isString() -> Integer.parseInt(jsonElement.getAsString());
        case JsonPrimitive jsonElement when jsonElement.isBoolean() -> jsonElement.getAsBoolean() ? 1 : 0;
        default -> Integer.parseInt(Objects.toString(input));
    });

    /**
     * Coerces values to a non-negative integer.
     */
    public static final Function<Object, Integer> NON_NEGATIVE_INT = input -> Math.max(0, INT.apply(input));

    /**
     * Coerces values to a duration in seconds, reading the last list/array element when applicable.
     */
    public static final Function<Object, Duration> DURATION = LAST.andThen(input -> switch (input) {
        case Duration duration -> duration.isNegative() ? Duration.ZERO : duration;
        case Number numberValue -> Duration.ofMillis(Mth.floor(numberValue.doubleValue() * 1000));
        case JsonPrimitive jsonElement when jsonElement.isNumber() -> Duration.ofMillis(Mth.floor(jsonElement.getAsDouble() * 1000));
        case JsonPrimitive jsonElement when jsonElement.isString() -> ShogiDuration.parse(jsonElement.getAsString());
        default -> ShogiDuration.parse(Objects.toString(input));
    });

    /**
     * Coerces values to a float, reading the last list/array element when applicable.
     */
    public static final Function<Object, Float> FLOAT = LAST.andThen(input -> switch (input) {
        case Float floatValue -> floatValue;
        case Number numberValue -> numberValue.floatValue();
        case JsonPrimitive jsonElement when jsonElement.isNumber() -> jsonElement.getAsFloat();
        case JsonPrimitive jsonElement when jsonElement.isString() -> Float.parseFloat(jsonElement.getAsString());
        case JsonPrimitive jsonElement when jsonElement.isBoolean() -> jsonElement.getAsBoolean() ? 1f : 0f;
        default -> Float.parseFloat(Objects.toString(input));
    });

    /**
     * Coerces values to a boolean, reading the last list/array element when applicable.
     */
    public static final Function<Object, Boolean> BOOLEAN = LAST.andThen(input -> switch (input) {
        case Boolean booleanInput -> booleanInput;
        case JsonPrimitive jsonElement when jsonElement.isBoolean() -> jsonElement.getAsBoolean();
        case JsonPrimitive jsonElement when jsonElement.isNumber() -> jsonElement.getAsInt() == 1;
        case JsonPrimitive jsonElement when jsonElement.isString() -> jsonElement.getAsBoolean();
        default -> Boolean.parseBoolean(Objects.toString(input));
    });

    /**
     * Coerces values to a string, reading the last list/array element when applicable.
     */
    public static final Function<Object, String> STRING = LAST.andThen(input -> switch (input) {
        case JsonPrimitive jsonElement -> jsonElement.getAsString();
        default -> Objects.toString(input);
    });

    /**
     * Coerces values to a literal chat component using {@link #STRING}.
     */
    public static final Function<Object, Component> COMPONENT = input -> Component.literal(STRING.apply(input));

    /**
     * Creates a coercion that requires values to be assignable to the given class.
     *
     * @param clazz target class
     * @param <T> target type
     * @return a coercion function enforcing the runtime type
     * @throws CoercionException if the value is not assignable to {@code clazz}
     */
    @SuppressWarnings("unchecked")
    public static <T> Function<Object, T> toClass(Class<T> clazz) {
        return input -> {
            if (!clazz.isAssignableFrom(input.getClass())) {
                throw new CoercionException(input, "Expected " + clazz.getSimpleName() + " but got " + input.getClass().getSimpleName());
            }
            return (T) input;
        };
    }

}
