package net.blay09.mods.shogi.common.effect.condition.context.player;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.blay09.mods.shogi.effect.ShogiEffect;
import net.blay09.mods.shogi.context.ShogiContext;
import net.blay09.mods.shogi.scope.ShogiScope;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;

import java.util.List;

public record AnyHand(ShogiEffect<?> condition) implements ShogiEffect<Boolean> {

    private static final InteractionHand[] HANDS = InteractionHand.values();
    public static final Identifier IDENTIFIER = Identifier.fromNamespaceAndPath("shogi", "any_hand");

    public static MapCodec<AnyHand> mapCodec(ShogiScope scope) {
        return RecordCodecBuilder.mapCodec(builder -> builder.group(
                scope.getEffectCodec().fieldOf("condition").forGetter(AnyHand::condition)
        ).apply(builder, AnyHand::new));
    }

    @Override
    public Identifier identifier() {
        return IDENTIFIER;
    }

    @Override
    public List<ShogiEffect<?>> nestedEffects() {
        return List.of(condition);
    }

    @Override
    public Either<Boolean, Object> apply(ShogiContext context) {
        final var livingEntity = context.requireLivingEntity();
        final var nestedContext = context.fork();
        for (final var hand : HANDS) {
            if (condition.test(nestedContext.withItemStack(livingEntity.getItemInHand(hand)))) {
                return Either.left(true);
            }
        }
        return Either.left(false);
    }
}
