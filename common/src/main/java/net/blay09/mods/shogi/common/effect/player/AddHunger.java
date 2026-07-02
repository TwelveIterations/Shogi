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

public record AddHunger(ShogiEffect<?> hunger) implements ShogiEffect<Integer> {

    public static final ResourceLocation IDENTIFIER = ResourceLocation.fromNamespaceAndPath("shogi", "add_hunger");

    public static MapCodec<AddHunger> mapCodec(ShogiScope scope) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
                EffectArgumentCodecs.effectOrConstant(scope).fieldOf("hunger").forGetter(AddHunger::hunger)
        ).apply(instance, AddHunger::new));
    }

    @Override
    public Either<Integer, Object> apply(ShogiContext context) {
        final var player = context.requirePlayer();
        final int hungerAmount = hunger.apply(context).mapLeft(Coercion.INT).orThrow();
        final int previousHunger = player.getFoodData().getFoodLevel();
        context.execute(IDENTIFIER, () -> {
            final var foodData = player.getFoodData();
            foodData.setFoodLevel(foodData.getFoodLevel() - hungerAmount);
        });
        return Either.left(previousHunger);
    }

    @Override
    public ResourceLocation identifier() {
        return IDENTIFIER;
    }
}
