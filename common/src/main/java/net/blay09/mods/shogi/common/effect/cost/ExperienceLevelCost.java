package net.blay09.mods.shogi.common.effect.cost;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.blay09.mods.shogi.context.executor.aggregate.AggregateKey;
import net.blay09.mods.shogi.effect.ShogiEffect;
import net.blay09.mods.shogi.context.ShogiContext;
import net.blay09.mods.shogi.effect.EffectArgumentCodecs;
import net.blay09.mods.shogi.scope.ShogiScope;
import net.blay09.mods.shogi.coercion.Coercion;
import net.minecraft.resources.Identifier;

public record ExperienceLevelCost(ShogiEffect<?> level) implements ShogiEffect<Boolean> {

    public static final Identifier IDENTIFIER = Identifier.fromNamespaceAndPath("shogi", "xp_level_cost");
    public static final AggregateKey<Integer> AGGREGATE_KEY = AggregateKey.of(IDENTIFIER);

    public static MapCodec<ExperienceLevelCost> mapCodec(ShogiScope scope) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
                EffectArgumentCodecs.effectOrConstant(scope).fieldOf("level").forGetter(ExperienceLevelCost::level)
        ).apply(instance, ExperienceLevelCost::new));
    }

    @Override
    public Either<Boolean, Object> apply(ShogiContext context) {
        final var player = context.requirePlayer();

        final int requestedLevels = level.apply(context).mapLeft(Coercion.NON_NEGATIVE_INT).orThrow();
        final int aggregateCost = context.aggregate(AGGREGATE_KEY, () -> 0, it -> it + requestedLevels);
        if (aggregateCost > player.experienceLevel) {
            return Either.right(new ExperienceLevelCostFailure(player.experienceLevel, requestedLevels));
        }

        context.consume(AGGREGATE_KEY, cost -> player.giveExperienceLevels(-cost));
        return Either.left(true);
    }

    @Override
    public Identifier identifier() {
        return IDENTIFIER;
    }
}
