package net.blay09.mods.shogi.common.effect.player;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.blay09.mods.shogi.coercion.Coercion;
import net.blay09.mods.shogi.context.ShogiContext;
import net.blay09.mods.shogi.effect.EffectArgumentCodecs;
import net.blay09.mods.shogi.effect.ShogiEffect;
import net.blay09.mods.shogi.scope.ShogiScope;
import net.minecraft.resources.ResourceLocation;

public record AddHealth(ShogiEffect<?> health) implements ShogiEffect<Float> {

    public static final ResourceLocation IDENTIFIER = ResourceLocation.fromNamespaceAndPath("shogi", "add_health");

    public static MapCodec<AddHealth> mapCodec(ShogiScope scope) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
                EffectArgumentCodecs.effectOrConstant(scope).fieldOf("health").forGetter(AddHealth::health)
        ).apply(instance, AddHealth::new));
    }

    @Override
    public Either<Float, Object> apply(ShogiContext context) {
        final var livingEntity = context.requireLivingEntity();
        final float healthAmount = health.apply(context).mapLeft(Coercion.FLOAT).orThrow();
        final float previousHealth = livingEntity.getHealth();
        context.execute(IDENTIFIER, () -> {
            if (healthAmount >= 0f) {
                livingEntity.heal(healthAmount);
            } else {
                livingEntity.setHealth(livingEntity.getHealth() + healthAmount);
            }
        });
        return Either.left(previousHealth);
    }

    @Override
    public ResourceLocation identifier() {
        return IDENTIFIER;
    }
}
