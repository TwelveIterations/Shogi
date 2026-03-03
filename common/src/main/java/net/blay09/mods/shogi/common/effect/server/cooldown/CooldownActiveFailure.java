package net.blay09.mods.shogi.common.effect.server.cooldown;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

public record CooldownActiveFailure(Identifier identifier, long remainingTicks, int requestedTicks) {
    public static final StreamCodec<RegistryFriendlyByteBuf, CooldownActiveFailure> STREAM_CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC,
            CooldownActiveFailure::identifier,
            ByteBufCodecs.VAR_LONG,
            CooldownActiveFailure::remainingTicks,
            ByteBufCodecs.VAR_INT,
            CooldownActiveFailure::requestedTicks,
            CooldownActiveFailure::new
    );
}
