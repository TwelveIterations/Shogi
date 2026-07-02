package net.blay09.mods.shogi.common.effect.condition.pos;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.blay09.mods.shogi.effect.ShogiEffect;
import net.blay09.mods.shogi.context.ShogiContext;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;

public record IsBiome(ResourceKey<Biome> biome) implements ShogiEffect<Boolean> {

    public static final ResourceLocation IDENTIFIER = ResourceLocation.fromNamespaceAndPath("shogi", "is_biome");
    public static final MapCodec<IsBiome> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ResourceKey.codec(Registries.BIOME).fieldOf("biome").forGetter(it -> it.biome)
    ).apply(instance, IsBiome::new));

    @Override
    public Either<Boolean, Throwable> apply(ShogiContext context) {
        final var level = context.requireLevel();
        final var pos = context.requireBlockPos();
        return Either.left(level.getBiome(pos).is(biome));
    }

    @Override
    public ResourceLocation identifier() {
        return IDENTIFIER;
    }

}
