package net.blay09.mods.shogi.common.effect.context.player;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.blay09.mods.shogi.effect.ShogiEffect;
import net.blay09.mods.shogi.context.ShogiContext;
import net.blay09.mods.shogi.scope.ShogiScope;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;

import java.util.List;

public record OffHand<T>(ShogiEffect<T> effect) implements ShogiEffect<T> {

    public static final Identifier IDENTIFIER = Identifier.fromNamespaceAndPath("shogi", "offhand");

    public static MapCodec<OffHand<?>> mapCodec(ShogiScope scope) {
        return RecordCodecBuilder.mapCodec(builder -> builder.group(
                scope.getEffectCodec().fieldOf("effect").forGetter(OffHand::effect)
        ).apply(builder, OffHand::new));
    }

    @Override
    public Identifier identifier() {
        return IDENTIFIER;
    }

    @Override
    public List<ShogiEffect<?>> nestedEffects() {
        return List.of(effect);
    }

    @Override
    public Either<? extends T, ?> apply(ShogiContext context) {
        final var livingEntity = context.requireLivingEntity();
        final var nestedContext = context.fork().withItemStack(livingEntity.getItemInHand(InteractionHand.OFF_HAND));
        return effect.apply(nestedContext);
    }
}
