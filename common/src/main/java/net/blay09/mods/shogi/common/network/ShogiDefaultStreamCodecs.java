package net.blay09.mods.shogi.common.network;

import net.blay09.mods.shogi.sync.ShogiStreamCodecs;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;

import static net.blay09.mods.shogi.common.ShogiCommon.id;

public final class ShogiDefaultStreamCodecs {
    private static boolean initialized;
    private static final StreamCodec<RegistryFriendlyByteBuf, Throwable> THROWABLE_CODEC = StreamCodec.unit(new Exception());

    private ShogiDefaultStreamCodecs() {
    }

    public static synchronized void registerDefaults() {
        if (initialized) {
            return;
        }
        initialized = true;

        ShogiStreamCodecs.register(id("int"), Integer.class, ByteBufCodecs.VAR_INT.cast());
        ShogiStreamCodecs.register(id("float"), Float.class, ByteBufCodecs.FLOAT.cast());
        ShogiStreamCodecs.register(id("bool"), Boolean.class, ByteBufCodecs.BOOL.cast());
        ShogiStreamCodecs.register(id("string"), String.class, ByteBufCodecs.STRING_UTF8.cast());
        ShogiStreamCodecs.register(id("component"), Component.class, ComponentSerialization.STREAM_CODEC);
        ShogiStreamCodecs.register(id("throwable"), Throwable.class, THROWABLE_CODEC);
        ShogiStreamCodecs.register(id("list"), List.class, ShogiStreamCodecs.LIST_STREAM_CODEC);
    }
}
