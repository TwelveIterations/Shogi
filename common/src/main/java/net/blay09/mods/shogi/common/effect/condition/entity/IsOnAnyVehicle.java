package net.blay09.mods.shogi.common.effect.condition.entity;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.MapCodec;
import net.blay09.mods.shogi.context.ShogiContext;
import net.blay09.mods.shogi.effect.ShogiEffect;
import net.minecraft.resources.Identifier;

import static net.blay09.mods.shogi.common.ShogiCommon.id;

public class IsOnAnyVehicle implements ShogiEffect<Boolean> {
    public static final Identifier IDENTIFIER = id("is_on_any_vehicle");
    private static final IsOnAnyVehicle INSTANCE = new IsOnAnyVehicle();
    public static final MapCodec<IsOnAnyVehicle> MAP_CODEC = MapCodec.unit(INSTANCE);

    @Override
    public Identifier identifier() {
        return IDENTIFIER;
    }

    @Override
    public Either<? extends Boolean, ?> apply(ShogiContext context) {
        final var entity = context.requireEntity();
        return Either.left(entity.getVehicle() != null);
    }
}
