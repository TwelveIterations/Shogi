package net.blay09.mods.shogi.common.effect.condition.pos;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.blay09.mods.shogi.effect.ShogiEffect;
import net.blay09.mods.shogi.context.ShogiContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;

public record IsBlockStateProperty(String property, String value) implements ShogiEffect<Boolean> {

    public static final ResourceLocation IDENTIFIER = ResourceLocation.fromNamespaceAndPath("shogi", "is_block_state_property");
    public static final MapCodec<IsBlockStateProperty> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("property").forGetter(it -> it.property),
            Codec.STRING.fieldOf("value").forGetter(it -> it.value)
    ).apply(instance, IsBlockStateProperty::new));

    @Override
    public Either<Boolean, Throwable> apply(ShogiContext context) {
        final var state = context.requireBlockState();
        return Either.left(state.getProperties().stream().anyMatch(it -> {
            final var value = state.getValue(it);
            final var stringValue = value instanceof StringRepresentable rep ? rep.getSerializedName() : value.toString();
            return stringValue.equals(this.value);
        }));
    }

    @Override
    public ResourceLocation identifier() {
        return IDENTIFIER;
    }

}
