package net.blay09.mods.shogi.common.effect.player;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.MapCodec;
import net.blay09.mods.shogi.context.ShogiContext;
import net.blay09.mods.shogi.effect.ShogiEffect;
import net.minecraft.resources.Identifier;

public class Dismount implements ShogiEffect<Boolean> {

    public static final Identifier IDENTIFIER = Identifier.fromNamespaceAndPath("shogi", "dismount");
    private static final Dismount INSTANCE = new Dismount();
    public static final MapCodec<Dismount> MAP_CODEC = MapCodec.unit(INSTANCE);

    @Override
    public Either<Boolean, ?> apply(ShogiContext context) {
        final var player = context.requirePlayer();
        context.execute(IDENTIFIER, player::stopRiding);
        return Either.left(true);
    }

    @Override
    public Identifier identifier() {
        return IDENTIFIER;
    }

}
