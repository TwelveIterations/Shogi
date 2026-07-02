package net.blay09.mods.shogi.common.effect.server.cooldown;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.blay09.mods.shogi.coercion.Coercion;
import net.blay09.mods.shogi.context.ShogiContext;
import net.blay09.mods.shogi.effect.EffectArgumentCodecs;
import net.blay09.mods.shogi.effect.ShogiEffect;
import net.blay09.mods.shogi.effect.failure.ShogiDeferred;
import net.blay09.mods.shogi.scope.ShogiScope;
import net.blay09.mods.shogi.util.ShogiDuration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import static net.blay09.mods.shogi.common.ShogiCommon.id;

public record IsCooldownAbove(ResourceLocation cooldown, ShogiEffect<?> duration) implements ShogiEffect<Boolean> {
    public static final ResourceLocation IDENTIFIER = id("is_cooldown_above");

    public static MapCodec<IsCooldownAbove> mapCodec(ShogiScope scope) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("cooldown").forGetter(IsCooldownAbove::cooldown),
                EffectArgumentCodecs.effectOrConstant(scope).fieldOf("duration").forGetter(IsCooldownAbove::duration)
        ).apply(instance, IsCooldownAbove::new));
    }

    @Override
    public ResourceLocation identifier() {
        return IDENTIFIER;
    }

    @Override
    public Either<? extends Boolean, ?> apply(ShogiContext context) {
        if (context.requirePlayer() instanceof ServerPlayer player) {
            final var cooldowns = ((ShogiCooldownsAccess) player).shogi$getCooldowns();
            final int durationTicks = duration.apply(context)
                    .mapLeft(Coercion.DURATION)
                    .mapLeft(ShogiDuration::toTicks)
                    .orThrow();
            return Either.left(cooldowns.getRemainingTicks(cooldown) >= durationTicks);
        }
        return Either.right(ShogiDeferred.INSTANCE);
    }
}
