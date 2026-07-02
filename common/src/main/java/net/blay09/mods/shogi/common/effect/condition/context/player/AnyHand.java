package net.blay09.mods.shogi.common.effect.condition.context.player;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.blay09.mods.shogi.effect.ShogiEffect;
import net.blay09.mods.shogi.context.ShogiContext;
import net.blay09.mods.shogi.scope.ShogiScope;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;

public record AnyHand(ShogiEffect<?> condition) implements ShogiEffect<Boolean> {

    private static final InteractionHand[] HANDS = InteractionHand.values();
    public static final ResourceLocation IDENTIFIER = ResourceLocation.fromNamespaceAndPath("shogi", "any_hand");

    public static MapCodec<AnyHand> mapCodec(ShogiScope scope) {
        return RecordCodecBuilder.mapCodec(builder -> builder.group(
                scope.getEffectCodec().fieldOf("condition").forGetter(AnyHand::condition)
        ).apply(builder, AnyHand::new));
    }

    @Override
    public ResourceLocation identifier() {
        return IDENTIFIER;
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
