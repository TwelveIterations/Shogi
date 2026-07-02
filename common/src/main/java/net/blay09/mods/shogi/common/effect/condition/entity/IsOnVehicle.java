package net.blay09.mods.shogi.common.effect.condition.entity;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.blay09.mods.shogi.context.ShogiContext;
import net.blay09.mods.shogi.effect.ShogiEffect;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.HolderSetCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

import static net.blay09.mods.shogi.common.ShogiCommon.id;

public record IsOnVehicle(HolderSet<EntityType<?>> vehicle) implements ShogiEffect<Boolean> {
    public static final ResourceLocation IDENTIFIER = id("is_on_vehicle");
    public static final MapCodec<IsOnVehicle> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            HolderSetCodec.create(Registries.ENTITY_TYPE, BuiltInRegistries.ENTITY_TYPE.holderByNameCodec(), false).fieldOf("vehicle").forGetter(IsOnVehicle::vehicle)
    ).apply(instance, IsOnVehicle::new));

    @Override
    public ResourceLocation identifier() {
        return IDENTIFIER;
    }

    @Override
    public Either<? extends Boolean, ?> apply(ShogiContext context) {
        final var entity = context.requireEntity();
        final var mountedVehicle = entity.getVehicle();
        if (mountedVehicle == null) {
            return Either.left(false);
        }
        return Either.left(vehicle.contains(mountedVehicle.getType().builtInRegistryHolder()));
    }
}
