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
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

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
        final var entity = context.entity();
        final long availableXp = entity instanceof Player player ? getAvailableExperiencePoints(player) : 0;

        final int requestedXp = xp.apply(context).mapLeft(Coercion.NON_NEGATIVE_INT).orThrow();
        final int aggregateCost = context.aggregate(AGGREGATE_KEY, () -> 0, it -> it + requestedXp);
        if (aggregateCost > availableXp) {
            return Either.right(new ExperiencePointsCostInformation(availableXp, requestedXp));
        }

        context.consume(AGGREGATE_KEY, cost -> {
            if (entity instanceof Player player) {
                player.giveExperiencePoints(-cost);
            }
        });
        return Either.left(new ExperiencePointsCostInformation(availableXp, requestedXp));
    }

    @Override
    public Identifier identifier() {
        return IDENTIFIER;
    }

    private static long getAvailableExperiencePoints(Player player) {
        final long xpForLevel = getCumulativeXpNeededForLevel(player.experienceLevel);
        final long xpForProgress = (int) Math.floor(player.experienceProgress * player.getXpNeededForNextLevel());
        return xpForLevel + xpForProgress;
    }

    private static long getXpNeededForNextLevel(int level) {
        if (level >= 30) {
            return 112 + (level - 30) * 9L;
        }
        return level >= 15 ? 37 + (level - 15) * 5L : 7 + level * 2L;
    }

    private static long getCumulativeXpNeededForLevel(int targetLevel) {
        long currentCumulativeXp = 0;
        for (int level = 0; level < targetLevel; level++) {
            currentCumulativeXp += getXpNeededForNextLevel(level);
        }
        return currentCumulativeXp;
    }
}
