package net.blay09.mods.shogi.common.effect.compose;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.blay09.mods.shogi.common.context.aggregate.DeferredEffectExecutor;
import net.blay09.mods.shogi.common.context.aggregate.ShogiAggregateContext;
import net.blay09.mods.shogi.context.ShogiContext;
import net.blay09.mods.shogi.effect.ShogiEffect;
import net.blay09.mods.shogi.effect.ShogiEmpty;
import net.blay09.mods.shogi.effect.failure.ShogiFatalFailure;
import net.blay09.mods.shogi.scope.ShogiScope;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

public record AggregateEffect(List<ShogiEffect<?>> effects) implements ShogiEffect<List<Object>> {

    public static final Identifier IDENTIFIER = Identifier.fromNamespaceAndPath("shogi", "aggregate");

    public static MapCodec<AggregateEffect> mapCodec(ShogiScope scope) {
        return RecordCodecBuilder.mapCodec(builder -> builder.group(
                scope.getEffectCodec().listOf().fieldOf("effects").forGetter(it -> it.effects)
        ).apply(builder, AggregateEffect::new));
    }

    @Override
    public Identifier identifier() {
        return IDENTIFIER;
    }


    @Override
    public Either<List<Object>, ?> apply(ShogiContext context) {
        if (effects.isEmpty()) {
            return Either.left(List.of());
        }

        final var aggregateContext = new ShogiAggregateContext(context);
        final var successes = new ArrayList<>();
        final var failures = new ArrayList<>();
        for (final var rule : effects) {
            final var result = rule.apply(aggregateContext);
            // Empty results are ignored to allow for fallback further up the line
            final var success = result.left().orElse(null);
            if (success != null && !(success instanceof ShogiEmpty)) {
                successes.add(success);
            }
            final var failure = result.right().orElse(null);
            if (failure != null && !(failure instanceof ShogiEmpty)) {
                failures.add(failure);
            }
            // Fatal failures and exceptions stop aggregates immediately and return only themselves
            if (failure instanceof ShogiFatalFailure || failure instanceof Throwable) {
                return Either.right(failure);
            }
        }

        if (failures.isEmpty()) {
            if (aggregateContext.executor() instanceof DeferredEffectExecutor executor) {
                executor.execute();
            }
            return Either.left(successes);
        } else {
            return Either.right(failures);
        }
    }
}
