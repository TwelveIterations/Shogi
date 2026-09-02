package net.blay09.mods.shogi.common.effect.condition.pos;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.blay09.mods.shogi.context.ShogiContext;
import net.blay09.mods.shogi.effect.ShogiEffect;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.codec.HolderSetCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.material.Fluid;

public record IsFluid(HolderSet<Fluid> fluid) implements ShogiEffect<Boolean> {

    public static final Identifier IDENTIFIER = Identifier.fromNamespaceAndPath("shogi", "is_fluid");
    public static final MapCodec<IsFluid> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            HolderSetCodec.create(Registries.FLUID, BuiltInRegistries.FLUID.holderByNameCodec(), false).fieldOf("fluid").forGetter(IsFluid::fluid)
    ).apply(instance, IsFluid::new));

    @Override
    public Either<Boolean, Throwable> apply(ShogiContext context) {
        return Either.left(context.requireFluidState().is(fluid));
    }

    @Override
    public Identifier identifier() {
        return IDENTIFIER;
    }
}
