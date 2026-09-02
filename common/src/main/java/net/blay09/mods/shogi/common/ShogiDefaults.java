package net.blay09.mods.shogi.common;

import net.blay09.mods.shogi.common.effect.compose.AggregateEffect;
import net.blay09.mods.shogi.common.effect.compose.AndEffect;
import net.blay09.mods.shogi.common.effect.compose.AnyEffect;
import net.blay09.mods.shogi.common.effect.compose.ConditionEffect;
import net.blay09.mods.shogi.common.effect.compose.UseEffect;
import net.blay09.mods.shogi.common.effect.compose.NotEffect;
import net.blay09.mods.shogi.common.effect.condition.context.player.AnyHand;
import net.blay09.mods.shogi.common.effect.condition.entity.*;
import net.blay09.mods.shogi.common.effect.condition.item.HasEnchantment;
import net.blay09.mods.shogi.common.effect.condition.item.IsItem;
import net.blay09.mods.shogi.common.effect.condition.player.HasItem;
import net.blay09.mods.shogi.common.effect.condition.player.IsInTeam;
import net.blay09.mods.shogi.common.effect.condition.player.LookupTeam;
import net.blay09.mods.shogi.common.effect.condition.player.SimplePlayerEffects;
import net.blay09.mods.shogi.common.effect.condition.pos.*;
import net.blay09.mods.shogi.common.effect.context.WithContext;
import net.blay09.mods.shogi.common.effect.context.player.OffHand;
import net.blay09.mods.shogi.common.effect.context.pos.Above;
import net.blay09.mods.shogi.common.effect.context.pos.Below;
import net.blay09.mods.shogi.common.effect.cost.DamageItem;
import net.blay09.mods.shogi.common.effect.cost.ExperienceLevelCost;
import net.blay09.mods.shogi.common.effect.cost.ExperiencePointsCost;
import net.blay09.mods.shogi.common.effect.cost.HealthCost;
import net.blay09.mods.shogi.common.effect.cost.HungerCost;
import net.blay09.mods.shogi.common.effect.cost.ItemCost;
import net.blay09.mods.shogi.common.effect.failure.Failure;
import net.blay09.mods.shogi.common.effect.failure.Refuse;
import net.blay09.mods.shogi.common.effect.player.AddHealth;
import net.blay09.mods.shogi.common.effect.player.AddHunger;
import net.blay09.mods.shogi.common.effect.player.Dismount;
import net.blay09.mods.shogi.common.effect.player.SetFoodLevel;
import net.blay09.mods.shogi.common.effect.player.SetHealth;
import net.blay09.mods.shogi.common.effect.server.condition.player.HasAdvancement;
import net.blay09.mods.shogi.common.effect.server.condition.pos.IsNearPoi;
import net.blay09.mods.shogi.common.effect.server.cooldown.AddCooldown;
import net.blay09.mods.shogi.common.effect.server.cooldown.CooldownCost;
import net.blay09.mods.shogi.common.effect.server.cooldown.HasCooldown;
import net.blay09.mods.shogi.common.effect.server.cooldown.IsCooldownAbove;
import net.blay09.mods.shogi.common.effect.variable.*;
import net.blay09.mods.shogi.common.network.ShogiNetworkCacheImpl;
import net.blay09.mods.shogi.common.scope.ShogiOverrideProviderImpl;
import net.blay09.mods.shogi.common.scope.ShogiRuleRepositories;
import net.blay09.mods.shogi.common.scope.ShogiRuleRepository;
import net.blay09.mods.shogi.effect.ConstantEffect;
import net.blay09.mods.shogi.effect.EmptyEffect;
import net.blay09.mods.shogi.scope.ShogiScope;

import java.util.List;

import static net.blay09.mods.shogi.common.ShogiCommon.id;

public class ShogiDefaults {

    public static ShogiScope registerDefaults(ShogiScope scope) {
        registerEffects(scope);

        final var repository = new ShogiRuleRepository();
        final var overrideProvider = new ShogiOverrideProviderImpl(repository);
        scope.registerOverrideProvider(overrideProvider);
        ShogiRuleRepositories.register(scope, repository);

        scope.setNetworkCache(new ShogiNetworkCacheImpl(scope.identifier()));

        return scope;
    }

    private static void registerEffects(ShogiScope scope) {
        scope.registerEffect(ConstantEffect.IDENTIFIER, ConstantEffect.MAP_CODEC, List.of("value"));
        scope.registerEffect(EmptyEffect.IDENTIFIER, EmptyEffect.MAP_CODEC);

        scope.registerEffect(AggregateEffect.IDENTIFIER, AggregateEffect.mapCodec(scope), List.of("effects"));
        scope.registerEffect(AndEffect.IDENTIFIER, AndEffect.mapCodec(scope), List.of("conditions"));
        scope.registerEffect(AnyEffect.IDENTIFIER, AnyEffect.mapCodec(scope), List.of("conditions"));
        scope.registerEffect(UseEffect.IDENTIFIER, UseEffect.mapCodec(scope), List.of("identifier"));
        scope.registerEffect(NotEffect.IDENTIFIER, NotEffect.mapCodec(scope), List.of("condition"));
        scope.registerEffect(ConditionEffect.IDENTIFIER, ConditionEffect.mapCodec(scope), List.of("condition", "then", "else"));
        scope.registerEffect(Failure.IDENTIFIER, Failure.MAP_CODEC, List.of("message"));
        scope.registerEffect(Refuse.IDENTIFIER, Refuse.MAP_CODEC, List.of("message"));

        scope.registerEffect(AnyHand.IDENTIFIER, AnyHand.mapCodec(scope), List.of("condition"));
        scope.registerEffect(WithContext.IDENTIFIER, WithContext.mapCodec(scope), List.of("context", "effect"));
        scope.registerEffect(OffHand.IDENTIFIER, OffHand.mapCodec(scope), List.of("effect"));
        scope.registerEffect(Above.IDENTIFIER, Above.mapCodec(scope), List.of("effect"));
        scope.registerEffect(Below.IDENTIFIER, Below.mapCodec(scope), List.of("effect"));

        scope.registerEffect(HasEntityTag.IDENTIFIER, HasEntityTag.MAP_CODEC, List.of("tag"));
        scope.registerEffect(HasMobEffect.IDENTIFIER, HasMobEffect.MAP_CODEC, List.of("effect"));
        scope.registerEffect(IsOnAnyVehicle.IDENTIFIER, IsOnAnyVehicle.MAP_CODEC);
        scope.registerEffect(IsOnVehicle.IDENTIFIER, IsOnVehicle.MAP_CODEC, List.of("vehicle"));
        scope.registerEffect(IsPlayer.IDENTIFIER, IsPlayer.MAP_CODEC);

        scope.registerEffect(HasEnchantment.IDENTIFIER, HasEnchantment.MAP_CODEC, List.of("enchantment", "level"));
        scope.registerEffect(IsItem.IDENTIFIER, IsItem.MAP_CODEC, List.of("item"));

        scope.registerEffect(HasItem.IDENTIFIER, HasItem.mapCodec(scope), List.of("item", "count"));
        scope.registerEffect(IsInTeam.IDENTIFIER, IsInTeam.mapCodec(scope), List.of("team"));
        scope.registerEffect(LookupTeam.IDENTIFIER, LookupTeam.mapCodec(scope), List.of("username"));
        scope.registerSimpleEffect(id("has_empty_inventory"), SimplePlayerEffects::hasEmptyInventory);
        scope.registerSimpleEffect(id("is_wearing_any_armor"), SimplePlayerEffects::isWearingAnyArmor);

        scope.registerEffect(CanSeeSky.IDENTIFIER, CanSeeSky.MAP_CODEC);
        scope.registerEffect(GetLightLevel.IDENTIFIER, GetLightLevel.MAP_CODEC);
        scope.registerEffect(IsAboveY.IDENTIFIER, IsAboveY.MAP_CODEC, List.of("y"));
        scope.registerEffect(IsAnimalNearby.IDENTIFIER, IsAnimalNearby.MAP_CODEC, List.of("distance", "min"));
        scope.registerEffect(IsAnyStructure.IDENTIFIER, IsAnyStructure.MAP_CODEC);
        scope.registerEffect(IsAt.IDENTIFIER, IsAt.MAP_CODEC, List.of("pos"));
        scope.registerEffect(IsBelowY.IDENTIFIER, IsBelowY.MAP_CODEC, List.of("y"));
        scope.registerEffect(IsBiome.IDENTIFIER, IsBiome.MAP_CODEC, List.of("biome"));
        scope.registerEffect(IsBrighterThan.IDENTIFIER, IsBrighterThan.mapCodec(scope), List.of("light_level"));
        scope.registerEffect(IsBlock.IDENTIFIER, IsBlock.MAP_CODEC, List.of("block"));
        scope.registerEffect(IsFluid.IDENTIFIER, IsFluid.MAP_CODEC, List.of("fluid"));
        scope.registerEffect(IsBlockEntity.IDENTIFIER, IsBlockEntity.MAP_CODEC, List.of("block_entity_type"));
        scope.registerEffect(IsBlockStateProperty.IDENTIFIER, IsBlockStateProperty.MAP_CODEC, List.of("property", "value"));
        scope.registerEffect(IsDarkerThan.IDENTIFIER, IsDarkerThan.mapCodec(scope), List.of("light_level"));
        scope.registerEffect(IsDimension.IDENTIFIER, IsDimension.MAP_CODEC, List.of("dimension"));
        scope.registerEffect(IsEntityNearby.IDENTIFIER, IsEntityNearby.MAP_CODEC, List.of("entity", "distance", "min"));
        scope.registerEffect(IsLightLevel.IDENTIFIER, IsLightLevel.mapCodec(scope), List.of("light_level"));
        scope.registerEffect(IsMobNearby.IDENTIFIER, IsMobNearby.MAP_CODEC, List.of("distance", "min"));
        scope.registerEffect(IsNear.IDENTIFIER, IsNear.MAP_CODEC, List.of("pos", "distance"));
        scope.registerEffect(FindBlockEntity.IDENTIFIER, FindBlockEntity.MAP_CODEC, List.of("block_entity_type", "distance"));
        scope.registerEffect(IsStructure.IDENTIFIER, IsStructure.MAP_CODEC, List.of("structure"));
        scope.registerEffectAlias(id("is_near_block_entity"), FindBlockEntity.IDENTIFIER);
        scope.registerEffect(IsPlayerNearby.IDENTIFIER, IsPlayerNearby.MAP_CODEC, List.of("distance", "min"));
        scope.registerEffect(IsWithin.IDENTIFIER, IsWithin.MAP_CODEC, List.of("bounds"));

        scope.registerEffect(HasAdvancement.IDENTIFIER, HasAdvancement.MAP_CODEC, List.of("advancement"));
        scope.registerEffect(IsNearPoi.IDENTIFIER, IsNearPoi.MAP_CODEC, List.of("poi", "distance"));
        scope.registerEffect(Dismount.IDENTIFIER, Dismount.MAP_CODEC);
        scope.registerEffect(AddHealth.IDENTIFIER, AddHealth.mapCodec(scope), List.of("health"));
        scope.registerEffect(AddHunger.IDENTIFIER, AddHunger.mapCodec(scope), List.of("hunger"));
        scope.registerEffect(SetHealth.IDENTIFIER, SetHealth.mapCodec(scope), List.of("health"));
        scope.registerEffect(SetFoodLevel.IDENTIFIER, SetFoodLevel.mapCodec(scope), List.of("food_level"));

        scope.registerEffect(ItemCost.IDENTIFIER, ItemCost.mapCodec(scope), List.of("item", "count"));
        scope.registerEffect(DamageItem.IDENTIFIER, DamageItem.mapCodec(scope), List.of("amount"));
        scope.registerEffect(ExperiencePointsCost.IDENTIFIER, ExperiencePointsCost.mapCodec(scope), List.of("xp"));
        scope.registerEffect(ExperienceLevelCost.IDENTIFIER, ExperienceLevelCost.mapCodec(scope), List.of("level"));
        scope.registerEffect(HealthCost.IDENTIFIER, HealthCost.mapCodec(scope), List.of("health"));
        scope.registerEffect(HungerCost.IDENTIFIER, HungerCost.mapCodec(scope), List.of("hunger"));

        scope.registerEffect(HasCooldown.IDENTIFIER, HasCooldown.MAP_CODEC, List.of("identifier"));
        scope.registerEffect(IsCooldownAbove.IDENTIFIER, IsCooldownAbove.mapCodec(scope), List.of("identifier", "duration"));
        scope.registerEffect(AddCooldown.IDENTIFIER, AddCooldown.mapCodec(scope), List.of("identifier", "duration"));
        scope.registerEffect(CooldownCost.IDENTIFIER, CooldownCost.mapCodec(scope), List.of("identifier", "duration"));

        scope.registerEffect(AssignmentEffect.IDENTIFIER, AssignmentEffect.mapCodec(scope), List.of("variable", "value"));
        scope.registerEffect(MacroAssignmentEffect.IDENTIFIER, MacroAssignmentEffect.mapCodec(scope), List.of("variable", "value"));
        scope.registerEffect(BinaryOpEffect.IDENTIFIER, BinaryOpEffect.mapCodec(scope), List.of("op", "left", "right"));
        scope.registerEffect(ClampMinEffect.IDENTIFIER, ClampMinEffect.mapCodec(scope), List.of("value", "min"));
        scope.registerEffect(ClampMaxEffect.IDENTIFIER, ClampMaxEffect.mapCodec(scope), List.of("value", "max"));
        scope.registerEffect(ClampEffect.IDENTIFIER, ClampEffect.mapCodec(scope), List.of("value", "min", "max"));
        scope.registerEffect(VariableEffect.IDENTIFIER, VariableEffect.MAP_CODEC, List.of("name"));
        scope.registerEffect(HasValueEffect.IDENTIFIER, HasValueEffect.MAP_CODEC, List.of("variable"));
    }
}
