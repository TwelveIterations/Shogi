package net.blay09.mods.shogi.common.effect.server.cooldown;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public record CooldownInformation(ResourceLocation identifier, long remainingTicks, int requestedTicks, long nowUnixMs, long nanosecondsPerTick) {
    public static final StreamCodec<RegistryFriendlyByteBuf, CooldownInformation> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC,
            CooldownInformation::identifier,
            ByteBufCodecs.VAR_LONG,
            CooldownInformation::remainingTicks,
            ByteBufCodecs.VAR_INT,
            CooldownInformation::requestedTicks,
            ByteBufCodecs.VAR_LONG,
            CooldownInformation::nowUnixMs,
            ByteBufCodecs.VAR_LONG,
            CooldownInformation::nanosecondsPerTick,
            CooldownInformation::new
    );
}
