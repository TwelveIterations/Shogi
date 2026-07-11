package net.blay09.mods.shogi.common.network;

import net.blay09.mods.shogi.common.effect.compose.AggregateEffect;
import net.blay09.mods.shogi.common.effect.compose.AndEffect;
import net.blay09.mods.shogi.common.effect.compose.AnyEffect;
import net.blay09.mods.shogi.common.effect.compose.ConditionEffect;
import net.blay09.mods.shogi.common.effect.compose.NotEffect;
import net.blay09.mods.shogi.common.effect.cost.*;
import net.blay09.mods.shogi.common.effect.failure.FailureInformation;
import net.blay09.mods.shogi.common.effect.failure.RefusalInformation;
import net.blay09.mods.shogi.common.effect.server.cooldown.CooldownInformation;
import net.blay09.mods.shogi.common.effect.variable.AssignmentEffect;
import net.blay09.mods.shogi.common.effect.variable.BinaryOpEffect;
import net.blay09.mods.shogi.common.effect.variable.ClampEffect;
import net.blay09.mods.shogi.common.effect.variable.ClampMaxEffect;
import net.blay09.mods.shogi.common.effect.variable.ClampMinEffect;
import net.blay09.mods.shogi.common.effect.variable.HasValueEffect;
import net.blay09.mods.shogi.common.effect.variable.MacroAssignmentEffect;
import net.blay09.mods.shogi.common.effect.variable.MissingVariableFailure;
import net.blay09.mods.shogi.common.effect.variable.VariableEffect;
import net.blay09.mods.shogi.effect.ShogiEffect;
import net.blay09.mods.shogi.network.ShogiStreamCodecs;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;

import static net.blay09.mods.shogi.common.ShogiCommon.id;

public final class ShogiDefaultStreamCodecs {
    private static boolean initialized;
    private static final StreamCodec<RegistryFriendlyByteBuf, ShogiEffect<?>> EFFECT_CODEC = ShogiStreamCodecs.dynamicObjectCodec()
            .map(value -> (ShogiEffect<?>) value, value -> value);
    private static final StreamCodec<RegistryFriendlyByteBuf, List<ShogiEffect<?>>> EFFECT_LIST_CODEC = EFFECT_CODEC.apply(ByteBufCodecs.list());

    private ShogiDefaultStreamCodecs() {
    }

    public static synchronized void registerDefaults() {
        if (initialized) {
            return;
        }
        initialized = true;

        ShogiStreamCodecs.register(id("failure"), FailureInformation.class, FailureInformation.STREAM_CODEC);
        ShogiStreamCodecs.register(id("refusal"), RefusalInformation.class, RefusalInformation.STREAM_CODEC);
        ShogiStreamCodecs.register(id("missing_variable_failure"), MissingVariableFailure.class, MissingVariableFailure.STREAM_CODEC);
        ShogiStreamCodecs.register(id("xp_points_cost"), ExperiencePointsCostInformation.class, ExperiencePointsCostInformation.STREAM_CODEC);
        ShogiStreamCodecs.register(id("xp_level_cost"), ExperienceLevelCostInformation.class, ExperienceLevelCostInformation.STREAM_CODEC);
        ShogiStreamCodecs.register(id("health_cost"), HealthCostInformation.class, HealthCostInformation.STREAM_CODEC);
        ShogiStreamCodecs.register(id("hunger_cost"), HungerCostInformation.class, HungerCostInformation.STREAM_CODEC);
        ShogiStreamCodecs.register(id("item_cost"), ItemCostInformation.class, ItemCostInformation.STREAM_CODEC);
        ShogiStreamCodecs.register(id("cooldown"), CooldownInformation.class, CooldownInformation.STREAM_CODEC);
        ShogiStreamCodecs.register(id("aggregate_effect"), AggregateEffect.class, StreamCodec.composite(
                EFFECT_LIST_CODEC,
                AggregateEffect::effects,
                AggregateEffect::new
        ));
        ShogiStreamCodecs.register(id("and_effect"), AndEffect.class, StreamCodec.composite(
                EFFECT_LIST_CODEC,
                AndEffect::conditions,
                AndEffect::new
        ));
        ShogiStreamCodecs.register(id("any_effect"), AnyEffect.class, StreamCodec.composite(
                EFFECT_LIST_CODEC,
                AnyEffect::conditions,
                AnyEffect::new
        ));
        ShogiStreamCodecs.register(id("not_effect"), NotEffect.class, StreamCodec.composite(
                EFFECT_CODEC,
                NotEffect::condition,
                NotEffect::new
        ));
        ShogiStreamCodecs.register(id("condition_effect"), ConditionEffect.class, StreamCodec.composite(
                EFFECT_CODEC,
                ConditionEffect::condition,
                EFFECT_CODEC,
                ConditionEffect::trueEffect,
                EFFECT_CODEC,
                ConditionEffect::falseEffect,
                ConditionEffect::new
        ));
        ShogiStreamCodecs.register(id("assignment_effect"), AssignmentEffect.class, StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8,
                AssignmentEffect::variable,
                EFFECT_CODEC,
                AssignmentEffect::value,
                AssignmentEffect::new
        ));
        ShogiStreamCodecs.register(id("macro_assignment_effect"), MacroAssignmentEffect.class, StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8,
                MacroAssignmentEffect::variable,
                EFFECT_CODEC,
                MacroAssignmentEffect::value,
                MacroAssignmentEffect::new
        ));
        ShogiStreamCodecs.register(id("variable_effect"), VariableEffect.class, StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8,
                VariableEffect::name,
                VariableEffect::new
        ));
        ShogiStreamCodecs.register(id("has_value_effect"), HasValueEffect.class, StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8,
                HasValueEffect::variable,
                HasValueEffect::new
        ));
        ShogiStreamCodecs.register(id("binary_op_effect"), BinaryOpEffect.class, StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8,
                BinaryOpEffect::op,
                EFFECT_CODEC,
                BinaryOpEffect::left,
                EFFECT_CODEC,
                BinaryOpEffect::right,
                BinaryOpEffect::new
        ));
        ShogiStreamCodecs.register(id("clamp_min_effect"), ClampMinEffect.class, StreamCodec.composite(
                EFFECT_CODEC,
                ClampMinEffect::value,
                EFFECT_CODEC,
                ClampMinEffect::min,
                ClampMinEffect::new
        ));
        ShogiStreamCodecs.register(id("clamp_max_effect"), ClampMaxEffect.class, StreamCodec.composite(
                EFFECT_CODEC,
                ClampMaxEffect::value,
                EFFECT_CODEC,
                ClampMaxEffect::max,
                ClampMaxEffect::new
        ));
        ShogiStreamCodecs.register(id("clamp_effect"), ClampEffect.class, StreamCodec.composite(
                EFFECT_CODEC,
                ClampEffect::value,
                EFFECT_CODEC,
                ClampEffect::min,
                EFFECT_CODEC,
                ClampEffect::max,
                ClampEffect::new
        ));
    }
}
