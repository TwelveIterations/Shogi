package net.blay09.mods.shogi.common.effect.compose;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.MapCodec;
import net.blay09.mods.shogi.context.ShogiContext;
import net.blay09.mods.shogi.effect.ShogiEffect;
import net.minecraft.resources.Identifier;

public class DefaultEffect implements ShogiEffect<Object> {
    public static final DefaultEffect INSTANCE = new DefaultEffect();
    public static final Identifier IDENTIFIER = Identifier.fromNamespaceAndPath("shogi", "default");
    public static final MapCodec<DefaultEffect> MAP_CODEC = MapCodec.unit(INSTANCE);

    @Override
    public Identifier identifier() {
        return IDENTIFIER;
    }

    @Override
    public Either<?, ?> apply(ShogiContext context) {
        return context.resolveDefault();
    }
}
