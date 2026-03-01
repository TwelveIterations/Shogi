package net.blay09.mods.shogi.common.effect.server.condition.pos;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.blay09.mods.shogi.effect.ShogiEffect;
import net.blay09.mods.shogi.context.ShogiContext;
import net.blay09.mods.shogi.effect.failure.ShogiDeferred;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.HolderSetCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiType;

public record IsNearPoi(HolderSet<PoiType> poiTypes, int distance) implements ShogiEffect<Boolean> {
    public static final Identifier IDENTIFIER = Identifier.fromNamespaceAndPath("shogi", "is_near_poi");
    public static final MapCodec<IsNearPoi> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            HolderSetCodec.create(Registries.POINT_OF_INTEREST_TYPE, BuiltInRegistries.POINT_OF_INTEREST_TYPE.holderByNameCodec(), false).fieldOf("poi").forGetter(IsNearPoi::poiTypes),
            Codec.INT.fieldOf("distance").forGetter(IsNearPoi::distance)
    ).apply(instance, IsNearPoi::new));

    @Override
    public Identifier identifier() {
        return IDENTIFIER;
    }

    @Override
    public Either<? extends Boolean, ?> apply(ShogiContext context) {
        final var level = context.requireLevel();
        final var pos = context.requireBlockPos();
        if (level instanceof ServerLevel serverLevel) {
            return Either.left(serverLevel.getPoiManager().findClosestWithType(it -> poiTypes().contains(it), pos, distance, PoiManager.Occupancy.ANY).isPresent());
        }
        return Either.right(ShogiDeferred.INSTANCE);
    }
}
