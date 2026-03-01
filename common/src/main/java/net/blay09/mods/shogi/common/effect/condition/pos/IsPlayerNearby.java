package net.blay09.mods.shogi.common.effect.condition.pos;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.blay09.mods.shogi.effect.ShogiEffect;
import net.blay09.mods.shogi.context.ShogiContext;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

public record IsPlayerNearby(float distance, int min) implements ShogiEffect<Boolean> {
    public static final Identifier IDENTIFIER = Identifier.fromNamespaceAndPath("shogi", "is_player_nearby");
    public static final MapCodec<IsPlayerNearby> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.FLOAT.fieldOf("distance").forGetter(IsPlayerNearby::distance),
            Codec.INT.fieldOf("min").orElse(1).forGetter(IsPlayerNearby::min)
    ).apply(instance, IsPlayerNearby::new));

    @Override
    public Identifier identifier() {
        return IDENTIFIER;
    }

    @Override
    public Either<? extends Boolean, ?> apply(ShogiContext context) {
        final var level = context.requireLevel();
        final var pos = context.requireBlockPos();
        final var entities = level.getEntitiesOfClass(Player.class, AABB.ofSize(pos.getCenter(), distance, distance, distance));
        return Either.left(entities.size() >= min);
    }
}
