package net.blay09.mods.shogi.common.effect.server.cooldown;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.blay09.mods.shogi.effect.ShogiEffect;
import net.blay09.mods.shogi.context.ShogiContext;
import net.blay09.mods.shogi.effect.EffectArgumentCodecs;
import net.blay09.mods.shogi.effect.failure.ShogiDeferred;
import net.blay09.mods.shogi.scope.ShogiScope;
import net.blay09.mods.shogi.coercion.Coercion;
import net.blay09.mods.shogi.util.ShogiDuration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public record CooldownCost(ResourceLocation identifier, ShogiEffect<?> duration) implements ShogiEffect<CooldownInformation> {

    public static final ResourceLocation IDENTIFIER = ResourceLocation.fromNamespaceAndPath("shogi", "cooldown_cost");

    public static MapCodec<CooldownCost> mapCodec(ShogiScope scope) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("identifier").forGetter(CooldownCost::identifier),
                EffectArgumentCodecs.effectOrConstant(scope).fieldOf("duration").forGetter(CooldownCost::duration)
        ).apply(instance, CooldownCost::new));
    }

    @Override
    public Either<CooldownInformation, Object> apply(ShogiContext context) {
        if (!(context.requirePlayer() instanceof ServerPlayer player)) {
            return Either.right(ShogiDeferred.INSTANCE);
        }

        final int durationTicks = duration.apply(context)
                .mapLeft(Coercion.DURATION)
                .mapLeft(ShogiDuration::toTicks)
                .orThrow();
        final var cooldowns = ((ShogiCooldownsAccess) player).shogi$getCooldowns();
        final long remainingTicks = cooldowns.getRemainingTicks(identifier);
        final long nowUnixMs = System.currentTimeMillis();
        final var nanosecondsPerTick = player.level().tickRateManager().nanosecondsPerTick();
        if (remainingTicks > 0) {
            return Either.right(new CooldownInformation(identifier, remainingTicks, durationTicks, nowUnixMs, nanosecondsPerTick));
        }

        context.execute(IDENTIFIER, () -> cooldowns.addCooldown(identifier, durationTicks));
        return Either.left(new CooldownInformation(identifier, 0, durationTicks, nowUnixMs, nanosecondsPerTick));
    }

    @Override
    public ResourceLocation identifier() {
        return IDENTIFIER;
    }
}
