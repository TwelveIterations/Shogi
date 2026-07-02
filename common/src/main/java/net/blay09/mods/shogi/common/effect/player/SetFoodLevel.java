package net.blay09.mods.shogi.common.effect.player;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.blay09.mods.shogi.coercion.Coercion;
import net.blay09.mods.shogi.context.ShogiContext;
import net.blay09.mods.shogi.effect.EffectArgumentCodecs;
import net.blay09.mods.shogi.effect.ShogiEffect;
import net.blay09.mods.shogi.scope.ShogiScope;
import net.minecraft.resources.ResourceLocation;

public record SetFoodLevel(ShogiEffect<?> foodLevel) implements ShogiEffect<Integer> {

    public static final ResourceLocation IDENTIFIER = ResourceLocation.fromNamespaceAndPath("shogi", "set_food_level");

    public static MapCodec<SetFoodLevel> mapCodec(ShogiScope scope) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
                EffectArgumentCodecs.effectOrConstant(scope).fieldOf("food_level").forGetter(SetFoodLevel::foodLevel)
        ).apply(instance, SetFoodLevel::new));
    }

    @Override
    public Either<Integer, Object> apply(ShogiContext context) {
        final var player = context.requirePlayer();
        final int foodLevelAmount = foodLevel.apply(context).mapLeft(Coercion.NON_NEGATIVE_INT).orThrow();
        final int previousFoodLevel = player.getFoodData().getFoodLevel();
        context.execute(IDENTIFIER, () -> player.getFoodData().setFoodLevel(foodLevelAmount));
        return Either.left(previousFoodLevel);
    }

    @Override
    public ResourceLocation identifier() {
        return IDENTIFIER;
    }
}
