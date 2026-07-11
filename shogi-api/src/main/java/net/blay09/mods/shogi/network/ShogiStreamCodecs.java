package net.blay09.mods.shogi.network;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Either;
import net.blay09.mods.shogi.effect.ConstantEffect;
import net.blay09.mods.shogi.effect.EmptyEffect;
import net.blay09.mods.shogi.effect.ShogiEmpty;
import net.blay09.mods.shogi.effect.failure.ShogiDeferred;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Registry and dispatch codec for synchronizing dynamic Shogi payload objects.
 */
public final class ShogiStreamCodecs {
    private static final Logger logger = LoggerFactory.getLogger(ShogiStreamCodecs.class);
    private static final Object UNKNOWN_VALUE = new Object();

    private record Entry(Identifier id, Class<?> type, StreamCodec<RegistryFriendlyByteBuf, Object> codec) {
    }

    private static final Map<Class<?>, Entry> byClass = new LinkedHashMap<>();
    private static final Map<Identifier, Entry> byIdentifier = new LinkedHashMap<>();

    private static final StreamCodec<RegistryFriendlyByteBuf, Object> UNKNOWN_CODEC = StreamCodec.unit(UNKNOWN_VALUE);
    private static final StreamCodec<RegistryFriendlyByteBuf, Identifier> IDENTIFIER_STREAM_CODEC = Identifier.STREAM_CODEC.cast();
    private static final StreamCodec<RegistryFriendlyByteBuf, Throwable> THROWABLE_CODEC = StreamCodec.unit(new Exception());
    private static final StreamCodec<RegistryFriendlyByteBuf, ShogiDeferred> DEFERRED_CODEC = StreamCodec.unit(ShogiDeferred.INSTANCE);
    private static final StreamCodec<RegistryFriendlyByteBuf, ShogiEmpty> EMPTY_CODEC = StreamCodec.unit(EmptyEffect.INSTANCE);
    private static final StreamCodec<RegistryFriendlyByteBuf, JsonElement> JSON_CODEC = ByteBufCodecs.lenientJson(Short.MAX_VALUE).cast();

    /**
     * Dynamic object stream codec that dispatches by registered runtime type identifier.
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, Object> STREAM_CODEC = IDENTIFIER_STREAM_CODEC.dispatch(
            ShogiStreamCodecs::identifierForValue,
            ShogiStreamCodecs::codecForIdentifier
    );

    /**
     * List codec for dynamic payload values.
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, List<Object>> LIST_STREAM_CODEC = STREAM_CODEC.apply(ByteBufCodecs.list());

    static {
        register(id("int"), Integer.class, ByteBufCodecs.VAR_INT.cast());
        register(id("float"), Float.class, ByteBufCodecs.FLOAT.cast());
        register(id("bool"), Boolean.class, ByteBufCodecs.BOOL.cast());
        register(id("string"), String.class, ByteBufCodecs.STRING_UTF8.cast());
        register(id("list"), List.class, LIST_STREAM_CODEC);
        register(id("json"), JsonElement.class, JSON_CODEC.cast());
        register(id("component"), Component.class, ComponentSerialization.STREAM_CODEC);
        register(id("throwable"), Throwable.class, THROWABLE_CODEC);
        register(id("deferred"), ShogiDeferred.class, DEFERRED_CODEC);
        register(id("empty"), ShogiEmpty.class, EMPTY_CODEC);
        register(id("constant_effect"), ConstantEffect.class, StreamCodec.composite(
                JSON_CODEC,
                ConstantEffect::value,
                ConstantEffect::new
        ));
    }

    private ShogiStreamCodecs() {
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath("shogi", path);
    }

    /**
     * Registers a runtime type and stream codec for dynamic payload synchronization.
     *
     * @param typeId network type identifier
     * @param type runtime class handled by the codec
     * @param codec codec used for encoding/decoding payload values
     * @param <T> payload type
     * @throws IllegalArgumentException if the class or identifier was already registered
     */
    public static synchronized <T> void register(Identifier typeId, Class<? super T> type, StreamCodec<RegistryFriendlyByteBuf, T> codec) {
        if (byClass.containsKey(type)) {
            throw new IllegalArgumentException("A synced payload codec is already registered for class " + type.getName());
        }
        if (byIdentifier.containsKey(typeId)) {
            throw new IllegalArgumentException("A synced payload codec is already registered for identifier " + typeId);
        }

        @SuppressWarnings("unchecked")
        final var erasedCodec = (StreamCodec<RegistryFriendlyByteBuf, Object>) codec;
        final var entry = new Entry(typeId, type, erasedCodec);
        byClass.put(type, entry);
        byIdentifier.put(typeId, entry);
    }

    /**
     * Returns the dynamic object codec used for synchronized payload values.
     *
     * @return dynamic object stream codec
     */
    public static StreamCodec<RegistryFriendlyByteBuf, Object> dynamicObjectCodec() {
        return STREAM_CODEC;
    }

    /**
     * Returns whether both sides of the given either payload can currently be encoded.
     *
     * @param payload payload to inspect
     * @return {@code true} when all contained values are encodable
     */
    public static boolean canEncodeEither(Either<?, ?> payload) {
        return payload.map(ShogiStreamCodecs::canEncodeObject, ShogiStreamCodecs::canEncodeObject);
    }

    /**
     * Returns whether the payload contains unknown placeholder values from decode fallbacks.
     *
     * @param payload payload to inspect
     * @return {@code true} when any value is unknown
     */
    public static boolean containsUnknown(Either<?, ?> payload) {
        return payload.map(ShogiStreamCodecs::containsUnknownObject, ShogiStreamCodecs::containsUnknownObject);
    }

    private static boolean containsUnknownObject(Object value) {
        if (value == UNKNOWN_VALUE) {
            return true;
        }

        if (value instanceof List<?> list) {
            for (final var element : list) {
                if (containsUnknownObject(element)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean canEncodeObject(@Nullable Object value) {
        if (value == null) {
            return false;
        }

        if (value instanceof List<?> list) {
            for (final var element : list) {
                if (!canEncodeObject(element)) {
                    return false;
                }
            }
        }

        return findEntryForClass(value.getClass()) != null;
    }

    @Nullable
    private static Entry findEntryForClass(Class<?> valueClass) {
        final Entry exactMatch = byClass.get(valueClass);
        if (exactMatch != null) {
            return exactMatch;
        }

        for (final var entry : byClass.entrySet()) {
            if (entry.getKey().isAssignableFrom(valueClass)) {
                return entry.getValue();
            }
        }
        return null;
    }

    private static Identifier identifierForValue(Object value) {
        final Entry entry = findEntryForClass(value.getClass());
        if (entry == null) {
            throw new IllegalArgumentException("No synced payload codec registered for class: " + value.getClass().getName());
        }
        return entry.id();
    }

    private static StreamCodec<RegistryFriendlyByteBuf, Object> codecForIdentifier(Identifier typeId) {
        final Entry entry = byIdentifier.get(typeId);
        if (entry == null) {
            logger.warn("Ignoring synced payload with unknown type id '{}'", typeId);
            return UNKNOWN_CODEC;
        }
        return entry.codec();
    }
}
