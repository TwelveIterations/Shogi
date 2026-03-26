package net.blay09.mods.shogi.common.effect.failure;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.blay09.mods.shogi.effect.ShogiEffect;
import net.blay09.mods.shogi.context.ShogiContext;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.Identifier;

public record Failure(Component message) implements ShogiEffect<Boolean> {

    public static final Identifier IDENTIFIER = Identifier.fromNamespaceAndPath("shogi", "failure");
    public static final MapCodec<Failure> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            ComponentSerialization.CODEC.fieldOf("message").orElse(Component.empty()).forGetter(Failure::message)
    ).apply(builder, Failure::new));

    @Override
    public Either<Boolean, ?> apply(ShogiContext context) {
        return Either.right(new FailureInformation(message));
    }

    @Override
    public Identifier identifier() {
        return IDENTIFIER;
    }

}
