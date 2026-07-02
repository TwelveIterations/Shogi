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

public record ExperienceLevelCost(ShogiEffect<?> level) implements ShogiEffect<ExperienceLevelCostInformation> {

    public static final ResourceLocation IDENTIFIER = ResourceLocation.fromNamespaceAndPath("shogi", "xp_level_cost");
    public static final AggregateKey<Integer> AGGREGATE_KEY = AggregateKey.of(IDENTIFIER);

    public static MapCodec<ExperienceLevelCost> mapCodec(ShogiScope scope) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
                EffectArgumentCodecs.effectOrConstant(scope).fieldOf("level").forGetter(ExperienceLevelCost::level)
        ).apply(instance, ExperienceLevelCost::new));
    }

    @Override
    public Either<ExperienceLevelCostInformation, Object> apply(ShogiContext context) {
        final var entity = context.entity();
        final int availableLevels = entity instanceof Player player ? player.experienceLevel : 0;

        final int requestedLevels = level.apply(context).mapLeft(Coercion.NON_NEGATIVE_INT).orThrow();
        final int aggregateCost = context.aggregate(AGGREGATE_KEY, () -> 0, it -> it + requestedLevels);
        if (aggregateCost > availableLevels) {
            return Either.right(new ExperienceLevelCostInformation(availableLevels, requestedLevels));
        }

        context.consume(AGGREGATE_KEY, cost -> {
            if (entity instanceof Player player) {
                player.giveExperienceLevels(-cost);
            }
        });
        return Either.left(new ExperienceLevelCostInformation(availableLevels, requestedLevels));
    }

    @Override
    public ResourceLocation identifier() {
        return IDENTIFIER;
    }
}
