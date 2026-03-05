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
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public record AddCooldown(Identifier identifier, ShogiEffect<?> duration) implements ShogiEffect<CooldownModification> {

    public static final Identifier IDENTIFIER = Identifier.fromNamespaceAndPath("shogi", "add_cooldown");

    public static MapCodec<AddCooldown> mapCodec(ShogiScope scope) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
                Identifier.CODEC.fieldOf("identifier").forGetter(AddCooldown::identifier),
                EffectArgumentCodecs.effectOrConstant(scope).fieldOf("duration").forGetter(AddCooldown::duration)
        ).apply(instance, AddCooldown::new));
    }

    @Override
    public Either<CooldownModification, Object> apply(ShogiContext context) {
        if (!(context.requirePlayer() instanceof ServerPlayer player)) {
            return Either.right(ShogiDeferred.INSTANCE);
        }

        final var durationTicks = duration.apply(context)
                .mapLeft(Coercion.DURATION)
                .mapLeft(ShogiDuration::toTicks)
                .orThrow();
        final var cooldowns = ((ShogiCooldownsAccess) player).shogi$getCooldowns();
        final var remainingTicks = cooldowns.getRemainingTicks(identifier);
        cooldowns.addCooldown(identifier, durationTicks);
        return Either.left(new CooldownModification(identifier, remainingTicks, durationTicks));
    }

    @Override
    public Identifier identifier() {
        return IDENTIFIER;
    }
}
