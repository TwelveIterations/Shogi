package net.blay09.mods.shogi.common.network;

import com.google.gson.JsonElement;
import net.blay09.mods.shogi.common.effect.cost.*;
import net.blay09.mods.shogi.common.effect.failure.FailureInformation;
import net.blay09.mods.shogi.common.effect.failure.RefusalInformation;
import net.blay09.mods.shogi.common.effect.server.cooldown.CooldownInformation;
import net.blay09.mods.shogi.common.effect.variable.MissingVariableFailure;
import net.blay09.mods.shogi.effect.EmptyEffect;
import net.blay09.mods.shogi.effect.ShogiEmpty;
import net.blay09.mods.shogi.effect.failure.ShogiDeferred;
import net.blay09.mods.shogi.network.ShogiStreamCodecs;
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
    private static final StreamCodec<RegistryFriendlyByteBuf, ShogiDeferred> DEFERRED_CODEC = StreamCodec.unit(ShogiDeferred.INSTANCE);
    private static final StreamCodec<RegistryFriendlyByteBuf, ShogiEmpty> EMPTY_CODEC = StreamCodec.unit(EmptyEffect.INSTANCE);

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
        ShogiStreamCodecs.register(id("json"), JsonElement.class, ByteBufCodecs.lenientJson(Short.MAX_VALUE).cast());
        ShogiStreamCodecs.register(id("component"), Component.class, ComponentSerialization.STREAM_CODEC);
        ShogiStreamCodecs.register(id("throwable"), Throwable.class, THROWABLE_CODEC);
        ShogiStreamCodecs.register(id("list"), List.class, ShogiStreamCodecs.LIST_STREAM_CODEC);
        ShogiStreamCodecs.register(id("failure"), FailureInformation.class, FailureInformation.STREAM_CODEC);
        ShogiStreamCodecs.register(id("refusal"), RefusalInformation.class, RefusalInformation.STREAM_CODEC);
        ShogiStreamCodecs.register(id("missing_variable_failure"), MissingVariableFailure.class, MissingVariableFailure.STREAM_CODEC);
        ShogiStreamCodecs.register(id("xp_points_cost"), ExperiencePointsCostInformation.class, ExperiencePointsCostInformation.STREAM_CODEC);
        ShogiStreamCodecs.register(id("xp_level_cost"), ExperienceLevelCostInformation.class, ExperienceLevelCostInformation.STREAM_CODEC);
        ShogiStreamCodecs.register(id("health_cost"), HealthCostInformation.class, HealthCostInformation.STREAM_CODEC);
        ShogiStreamCodecs.register(id("hunger_cost"), HungerCostInformation.class, HungerCostInformation.STREAM_CODEC);
        ShogiStreamCodecs.register(id("item_cost"), ItemCostInformation.class, ItemCostInformation.STREAM_CODEC);
        ShogiStreamCodecs.register(id("cooldown"), CooldownInformation.class, CooldownInformation.STREAM_CODEC);
        ShogiStreamCodecs.register(id("deferred"), ShogiDeferred.class, DEFERRED_CODEC);
        ShogiStreamCodecs.register(id("empty"), ShogiEmpty.class, EMPTY_CODEC);
    }
}
