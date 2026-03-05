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

public record ExperiencePointsCost(ShogiEffect<?> xp) implements ShogiEffect<ExperiencePointsCostInformation> {

    public static final Identifier IDENTIFIER = Identifier.fromNamespaceAndPath("shogi", "xp_points_cost");
    public static final AggregateKey<Integer> AGGREGATE_KEY = AggregateKey.of(IDENTIFIER);

    public static MapCodec<ExperiencePointsCost> mapCodec(ShogiScope scope) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
                EffectArgumentCodecs.effectOrConstant(scope).fieldOf("xp").forGetter(ExperiencePointsCost::xp)
        ).apply(instance, ExperiencePointsCost::new));
    }

    @Override
    public Either<ExperiencePointsCostInformation, Object> apply(ShogiContext context) {
        final var player = context.requirePlayer();

        final var requestedXp = xp.apply(context).mapLeft(Coercion.NON_NEGATIVE_INT).orThrow();
        final int aggregateCost = context.aggregate(AGGREGATE_KEY, () -> 0, it -> it + requestedXp);
        if (aggregateCost > player.totalExperience) {
            return Either.right(new ExperiencePointsCostInformation(player.totalExperience, requestedXp));
        }

        context.consume(AGGREGATE_KEY, cost -> player.giveExperiencePoints(-cost));
        return Either.left(new ExperiencePointsCostInformation(player.totalExperience, requestedXp));
    }

    @Override
    public Identifier identifier() {
        return IDENTIFIER;
    }
}
