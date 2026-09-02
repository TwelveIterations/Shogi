package net.blay09.mods.shogi.common.effect.context.pos;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.blay09.mods.shogi.context.ShogiContext;
import net.blay09.mods.shogi.effect.ShogiEffect;
import net.blay09.mods.shogi.scope.ShogiScope;
import net.minecraft.resources.Identifier;

import java.util.List;

public record Below<T>(ShogiEffect<T> effect) implements ShogiEffect<T> {

    public static final Identifier IDENTIFIER = Identifier.fromNamespaceAndPath("shogi", "below");

    public static MapCodec<Below<?>> mapCodec(ShogiScope scope) {
        return RecordCodecBuilder.mapCodec(builder -> builder.group(
                scope.getEffectCodec().fieldOf("effect").forGetter(Below::effect)
        ).apply(builder, Below::new));
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
        final var nestedContext = context.fork().withBlockPos(context.requireBlockPos().below());
        return effect.apply(nestedContext);
    }
}
