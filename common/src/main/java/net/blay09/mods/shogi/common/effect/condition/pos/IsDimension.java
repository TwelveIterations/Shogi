package net.blay09.mods.shogi.common.effect.condition.pos;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.blay09.mods.shogi.effect.ShogiEffect;
import net.blay09.mods.shogi.context.ShogiContext;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public record IsDimension(ResourceKey<Level> dimension) implements ShogiEffect<Boolean> {

    public static final Identifier IDENTIFIER = Identifier.fromNamespaceAndPath("shogi", "is_dimension");
    public static final MapCodec<IsDimension> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ResourceKey.codec(Registries.DIMENSION).fieldOf("dimension").forGetter(it -> it.dimension)
    ).apply(instance, IsDimension::new));

    @Override
    public Either<Boolean, Throwable> apply(ShogiContext context) {
        final var level = context.requireLevel();
        return Either.left(level.dimension().equals(dimension));
    }

    @Override
    public Identifier identifier() {
        return IDENTIFIER;
    }

}
