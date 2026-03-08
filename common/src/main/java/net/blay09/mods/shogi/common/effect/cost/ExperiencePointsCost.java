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
        final var player = context.requirePlayer();
        final int availableXp = getAvailableExperiencePoints(player);

        final var requestedXp = xp.apply(context).mapLeft(Coercion.NON_NEGATIVE_INT).orThrow();
        final int aggregateCost = context.aggregate(AGGREGATE_KEY, () -> 0, it -> it + requestedXp);
        if (aggregateCost > availableXp) {
            return Either.right(new ExperiencePointsCostInformation(availableXp, requestedXp));
        }

        context.consume(AGGREGATE_KEY, cost -> player.giveExperiencePoints(-cost));
        return Either.left(new ExperiencePointsCostInformation(availableXp, requestedXp));
    }

    @Override
    public Identifier identifier() {
        return IDENTIFIER;
    }

    private static int getAvailableExperiencePoints(Player player) {
        final int xpForLevel = getCumulativeXpNeededForLevel(player.experienceLevel);
        final int xpForProgress = (int) Math.floor(player.experienceProgress * player.getXpNeededForNextLevel());
        return xpForLevel + xpForProgress;
    }

    private static int getXpNeededForNextLevel(int level) {
        if (level >= 30) {
            return 112 + (level - 30) * 9;
        }
        return level >= 15 ? 37 + (level - 15) * 5 : 7 + level * 2;
    }

    private static int getCumulativeXpNeededForLevel(int targetLevel) {
        int currentCumulativeXp = 0;
        for (int level = 0; level < targetLevel; level++) {
            currentCumulativeXp += getXpNeededForNextLevel(level);
        }
        return currentCumulativeXp;
    }
}
