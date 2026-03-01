package net.blay09.mods.shogi.common.effect.server.cooldown;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.blay09.mods.shogi.effect.ShogiEffect;
import net.blay09.mods.shogi.context.ShogiContext;
import net.blay09.mods.shogi.effect.failure.ShogiDeferred;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public record HasCooldown(Identifier cooldown) implements ShogiEffect<Boolean> {

    public static final Identifier IDENTIFIER = Identifier.fromNamespaceAndPath("shogi", "has_cooldown");
    public static final MapCodec<HasCooldown> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Identifier.CODEC.fieldOf("identifier").forGetter(HasCooldown::cooldown)
    ).apply(instance, HasCooldown::new));

    @Override
    public Either<Boolean, ?> apply(ShogiContext context) {
        if (context.requirePlayer() instanceof ServerPlayer player) {
            final var cooldowns = ((ShogiCooldownsAccess) player).shogi$getCooldowns();
            return Either.left(cooldowns.hasCooldown(cooldown));
        }
        return Either.right(ShogiDeferred.INSTANCE);
    }

    @Override
    public Identifier identifier() {
        return IDENTIFIER;
    }
}
