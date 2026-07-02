package net.blay09.mods.shogi.common.effect.context;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.blay09.mods.shogi.context.MutableShogiContext;
import net.blay09.mods.shogi.context.ShogiContext;
import net.blay09.mods.shogi.effect.ShogiEffect;
import net.blay09.mods.shogi.scope.ShogiScope;
import net.minecraft.resources.ResourceLocation;

public record WithContext<T>(ShogiEffect<?> contextEffect, ShogiEffect<T> effect) implements ShogiEffect<T> {

    public static final ResourceLocation IDENTIFIER = ResourceLocation.fromNamespaceAndPath("shogi", "with");

    public static MapCodec<WithContext<?>> mapCodec(ShogiScope scope) {
        return RecordCodecBuilder.mapCodec(builder -> builder.group(
                scope.getEffectCodec().fieldOf("context").forGetter(WithContext::contextEffect),
                scope.getEffectCodec().fieldOf("effect").forGetter(WithContext::effect)
        ).apply(builder, WithContext::new));
    }

    @Override
    public ResourceLocation identifier() {
        return IDENTIFIER;
    }

    @Override
    public Either<? extends T, ?> apply(ShogiContext context) {
        final var contextResult = contextEffect.apply(context);
        if (contextResult.right().isPresent()) {
            return Either.right(contextResult.right().orElseThrow());
        }

        final var nestedContext = MutableShogiContext.of(contextResult.left().orElseThrow(), context);
        return effect.apply(nestedContext);
    }
}
