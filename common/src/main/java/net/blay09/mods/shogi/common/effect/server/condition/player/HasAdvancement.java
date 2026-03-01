package net.blay09.mods.shogi.common.effect.server.condition.player;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.blay09.mods.shogi.effect.ShogiEffect;
import net.blay09.mods.shogi.context.ShogiContext;
import net.blay09.mods.shogi.effect.failure.ShogiDeferred;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;

public record HasAdvancement(ResourceKey<Advancement> advancement) implements ShogiEffect<Boolean> {

    public static final Identifier IDENTIFIER = Identifier.fromNamespaceAndPath("shogi", "has_advancement");
    public static final MapCodec<HasAdvancement> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ResourceKey.codec(Registries.ADVANCEMENT).fieldOf("advancement").forGetter(it -> it.advancement)
    ).apply(instance, HasAdvancement::new));

    @Override
    public Either<Boolean, ?> apply(ShogiContext context) {
        if (context.requirePlayer() instanceof ServerPlayer player) {
            final var resolvedAdvancement = player.level().getServer().getAdvancements().get(advancement.identifier());
            if (resolvedAdvancement != null) {
                return Either.left(player.getAdvancements().getOrStartProgress(resolvedAdvancement).isDone());
            }
            return Either.left(false);
        }
        return Either.right(ShogiDeferred.INSTANCE);
    }

    @Override
    public Identifier identifier() {
        return IDENTIFIER;
    }

}
