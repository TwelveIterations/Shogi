package net.blay09.mods.shogi.common.effect.server.cooldown;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

public record CooldownInformation(Identifier identifier, long remainingTicks, int requestedTicks) {
    public static final StreamCodec<RegistryFriendlyByteBuf, CooldownInformation> STREAM_CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC,
            CooldownInformation::identifier,
            ByteBufCodecs.VAR_LONG,
            CooldownInformation::remainingTicks,
            ByteBufCodecs.VAR_INT,
            CooldownInformation::requestedTicks,
            CooldownInformation::new
    );
}
