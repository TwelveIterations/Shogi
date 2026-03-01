package net.blay09.mods.shogi.common.effect.failure;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.blay09.mods.shogi.effect.ShogiEffect;
import net.blay09.mods.shogi.context.ShogiContext;
import net.blay09.mods.shogi.effect.failure.ShogiFatalFailure;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.Identifier;

public record Refuse(Component message) implements ShogiEffect<Boolean>, ShogiFatalFailure {

    public static final Identifier IDENTIFIER = Identifier.fromNamespaceAndPath("shogi", "refuse");
    public static final MapCodec<Refuse> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            ComponentSerialization.CODEC.fieldOf("message").orElse(Component.empty()).forGetter(Refuse::message)
    ).apply(builder, Refuse::new));

    @Override
    public Either<Boolean, ?> apply(ShogiContext context) {
        return Either.right(this);
    }

    @Override
    public Identifier identifier() {
        return IDENTIFIER;
    }

}
