package net.blay09.mods.shogi.effect;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
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
        return Codec.either(scope.getEffectCodec(), ExtraCodecs.JSON).xmap(
                either -> either.map(effect -> effect, ConstantEffect::new),
                Either::left
        );
    }
}
