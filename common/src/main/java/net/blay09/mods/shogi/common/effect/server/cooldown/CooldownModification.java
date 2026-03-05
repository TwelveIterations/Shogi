package net.blay09.mods.shogi.common.effect.server.cooldown;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

public record CooldownModification(Identifier identifier, long remainingTicks, int requestedTicks) {
    public static final StreamCodec<RegistryFriendlyByteBuf, CooldownModification> STREAM_CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC,
            CooldownModification::identifier,
            ByteBufCodecs.VAR_LONG,
            CooldownModification::remainingTicks,
            ByteBufCodecs.VAR_INT,
            CooldownModification::requestedTicks,
            CooldownModification::new
    );
}
