package net.blay09.mods.shogi.common.effect.condition.entity;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.MapCodec;
import net.blay09.mods.shogi.common.platform.ShogiRuntimeSpi;
import net.blay09.mods.shogi.effect.ShogiEffect;
import net.blay09.mods.shogi.context.ShogiContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class IsPlayer implements ShogiEffect<Boolean> {

    public static final ResourceLocation IDENTIFIER = ResourceLocation.fromNamespaceAndPath("shogi", "is_player");
    private static final IsPlayer INSTANCE = new IsPlayer();
    public static final MapCodec<IsPlayer> MAP_CODEC = MapCodec.unit(INSTANCE);

    @Override
    public Either<Boolean, Throwable> apply(ShogiContext context) {
        final var entity = context.requireEntity();
        return Either.left(entity instanceof Player player && !ShogiRuntimeSpi.get().isFakePlayer(player));
    }

    @Override
    public ResourceLocation identifier() {
        return IDENTIFIER;
    }

}
