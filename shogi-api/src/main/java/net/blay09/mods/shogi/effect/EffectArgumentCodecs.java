package net.blay09.mods.shogi.effect;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.blay09.mods.shogi.scope.ShogiScope;
import net.minecraft.util.ExtraCodecs;

/**
 * Codec helpers for decoding effect arguments.
 */
public final class EffectArgumentCodecs {

    private EffectArgumentCodecs() {
    }

    /**
     * Creates a codec that accepts either a serialized effect or a constant JSON value.
     *
     * @param scope scope used to resolve registered effect codecs
     * @return codec producing a {@link ShogiEffect}
     */
    public static Codec<ShogiEffect<?>> effectOrConstant(ShogiScope scope) {
        return Codec.either(scope.getEffectCodec(), ExtraCodecs.JSON.validate(EffectArgumentCodecs::validateConstant))
                .xmap(
                        either -> either.map(effect -> effect, ConstantEffect::new),
                        Either::left
                );
    }

    private static DataResult<JsonElement> validateConstant(JsonElement json) {
        if (json instanceof JsonPrimitive) {
            return DataResult.success(json);
        }

        final var kind = json.isJsonArray() ? "array" : json.isJsonObject() ? "object" : json.isJsonNull() ? "null" : "value";
        return DataResult.error(() -> "Expected effect or scalar constant, got JSON " + kind);
    }
}
