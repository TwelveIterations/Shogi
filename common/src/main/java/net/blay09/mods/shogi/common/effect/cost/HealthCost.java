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
import net.minecraft.world.entity.LivingEntity;

public record HealthCost(ShogiEffect<?> health) implements ShogiEffect<HealthCostInformation> {

    public static final Identifier IDENTIFIER = Identifier.fromNamespaceAndPath("shogi", "health_cost");
    public static final AggregateKey<Float> AGGREGATE_KEY = AggregateKey.of(IDENTIFIER);

    public static MapCodec<HealthCost> mapCodec(ShogiScope scope) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
                EffectArgumentCodecs.effectOrConstant(scope).fieldOf("health").forGetter(HealthCost::health)
        ).apply(instance, HealthCost::new));
    }

    @Override
    public Either<HealthCostInformation, Object> apply(ShogiContext context) {
        final var entity = context.entity();
        final float availableHealth = entity instanceof LivingEntity livingEntity ? livingEntity.getHealth() : 0f;

        final float requestedHealth = Math.max(0f, health.apply(context).mapLeft(Coercion.FLOAT).orThrow());
        final float aggregateCost = context.aggregate(AGGREGATE_KEY, () -> 0f, it -> it + requestedHealth);
        if (aggregateCost > 0f && aggregateCost >= availableHealth) {
            return Either.right(new HealthCostInformation(availableHealth, requestedHealth));
        }

        context.consume(AGGREGATE_KEY, cost -> {
            if (entity instanceof LivingEntity livingEntity) {
                livingEntity.setHealth(Math.max(1f, livingEntity.getHealth() - cost));
            }
        });
        return Either.left(new HealthCostInformation(availableHealth, requestedHealth));
    }

    @Override
    public Identifier identifier() {
        return IDENTIFIER;
    }
}
