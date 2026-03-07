package net.blay09.mods.shogi.common.effect.condition.entity;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.blay09.mods.shogi.context.ShogiContext;
import net.blay09.mods.shogi.effect.ShogiEffect;
import net.minecraft.resources.Identifier;

import static net.blay09.mods.shogi.common.ShogiCommon.id;

public record HasEntityTag(String tag) implements ShogiEffect<Boolean> {
    public static final Identifier IDENTIFIER = id("has_entity_tag");
    public static final MapCodec<HasEntityTag> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("tag").forGetter(HasEntityTag::tag)
    ).apply(instance, HasEntityTag::new));

    @Override
    public Identifier identifier() {
        return IDENTIFIER;
    }

    @Override
    public Either<? extends Boolean, ?> apply(ShogiContext context) {
        final var entity = context.requireEntity();
        return Either.left(entity.entityTags().contains(tag));
    }
}
