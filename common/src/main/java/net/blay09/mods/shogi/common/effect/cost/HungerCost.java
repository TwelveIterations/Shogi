package net.blay09.mods.shogi.common.effect.cost;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.blay09.mods.shogi.coercion.Coercion;
import net.blay09.mods.shogi.context.ShogiContext;
import net.blay09.mods.shogi.context.executor.aggregate.AggregateKey;
import net.blay09.mods.shogi.effect.EffectArgumentCodecs;
import net.blay09.mods.shogi.effect.ShogiEffect;
import net.blay09.mods.shogi.scope.ShogiScope;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public record HungerCost(ShogiEffect<?> hunger) implements ShogiEffect<HungerCostInformation> {

    public static final ResourceLocation IDENTIFIER = ResourceLocation.fromNamespaceAndPath("shogi", "hunger_cost");
    public static final AggregateKey<Integer> AGGREGATE_KEY = AggregateKey.of(IDENTIFIER);

    public static MapCodec<HungerCost> mapCodec(ShogiScope scope) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
                EffectArgumentCodecs.effectOrConstant(scope).fieldOf("hunger").forGetter(HungerCost::hunger)
        ).apply(instance, HungerCost::new));
    }

    @Override
    public Either<HungerCostInformation, Object> apply(ShogiContext context) {
        final var entity = context.entity();
        final int availableHunger = entity instanceof Player player ? player.getFoodData().getFoodLevel() : 0;

        final int requestedHunger = hunger.apply(context).mapLeft(Coercion.NON_NEGATIVE_INT).orThrow();
        final int aggregateCost = context.aggregate(AGGREGATE_KEY, () -> 0, it -> it + requestedHunger);
        if (aggregateCost > availableHunger) {
            return Either.right(new HungerCostInformation(availableHunger, requestedHunger));
        }

        context.consume(AGGREGATE_KEY, cost -> {
            if (entity instanceof Player player) {
                final var foodData = player.getFoodData();
                foodData.setFoodLevel(foodData.getFoodLevel() - cost);
            }
        });
        return Either.left(new HungerCostInformation(availableHunger, requestedHunger));
    }

    @Override
    public ResourceLocation identifier() {
        return IDENTIFIER;
    }
}
