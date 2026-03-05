package net.blay09.mods.shogi.common.network;

import com.google.gson.JsonElement;
import net.blay09.mods.shogi.common.effect.cost.*;
import net.blay09.mods.shogi.common.effect.failure.Failure;
import net.blay09.mods.shogi.common.effect.failure.Refuse;
import net.blay09.mods.shogi.common.effect.server.cooldown.CooldownModification;
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
        ShogiStreamCodecs.register(id("failure"), Failure.class, Failure.STREAM_CODEC);
        ShogiStreamCodecs.register(id("refuse"), Refuse.class, Refuse.STREAM_CODEC);
        ShogiStreamCodecs.register(id("missing_variable_failure"), MissingVariableFailure.class, MissingVariableFailure.STREAM_CODEC);
        ShogiStreamCodecs.register(id("xp_points_cost_success"), ExperiencePointsCostSuccess.class, ExperiencePointsCostSuccess.STREAM_CODEC);
        ShogiStreamCodecs.register(id("xp_points_cost_failure"), ExperiencePointsCostFailure.class, ExperiencePointsCostFailure.STREAM_CODEC);
        ShogiStreamCodecs.register(id("xp_level_cost_success"), ExperienceLevelCostSuccess.class, ExperienceLevelCostSuccess.STREAM_CODEC);
        ShogiStreamCodecs.register(id("xp_level_cost_failure"), ExperienceLevelCostFailure.class, ExperienceLevelCostFailure.STREAM_CODEC);
        ShogiStreamCodecs.register(id("item_cost_success"), ItemCostSuccess.class, ItemCostSuccess.STREAM_CODEC);
        ShogiStreamCodecs.register(id("item_cost_failure"), ItemCostFailure.class, ItemCostFailure.STREAM_CODEC);
        ShogiStreamCodecs.register(id("cooldown_modification"), CooldownModification.class, CooldownModification.STREAM_CODEC);
        ShogiStreamCodecs.register(id("deferred"), ShogiDeferred.class, DEFERRED_CODEC);
        ShogiStreamCodecs.register(id("empty"), ShogiEmpty.class, EMPTY_CODEC);
    }
}
