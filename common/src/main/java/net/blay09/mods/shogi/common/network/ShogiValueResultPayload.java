package net.blay09.mods.shogi.common.network;

import com.mojang.datafixers.util.Either;
import net.blay09.mods.shogi.network.ShogiStreamCodecs;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ShogiValueResultPayload(Identifier scope, Identifier identifier, Either<?, ?> payload) implements CustomPacketPayload {

    public static final Type<ShogiValueResultPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath("shogi", "value_result"));
    @SuppressWarnings("unchecked")
    public static final StreamCodec<RegistryFriendlyByteBuf, ShogiValueResultPayload> STREAM_CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC,
            ShogiValueResultPayload::scope,
            Identifier.STREAM_CODEC,
            ShogiValueResultPayload::identifier,
            ByteBufCodecs.either(ShogiStreamCodecs.dynamicObjectCodec(), ShogiStreamCodecs.dynamicObjectCodec()),
            payload -> (Either<Object, Object>) payload.payload(),
            ShogiValueResultPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}
