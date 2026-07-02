package net.blay09.mods.shogi.common.scope;

import net.blay09.mods.shogi.effect.ShogiEffect;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.Optional;

public class ShogiRuleRepository {
    private Map<ResourceLocation, ShogiEffect<?>> configRules = Map.of();
    private Map<ResourceLocation, ShogiEffect<?>> datapackRules = Map.of();

    public void apply(Map<ResourceLocation, ShogiEffect<?>> valueOverrides, Map<ResourceLocation, ShogiEffect<?>> importedRules) {
        this.configRules = Map.copyOf(valueOverrides);
        this.datapackRules = Map.copyOf(importedRules);
    }

    public Optional<ShogiEffect<?>> getValueOverride(ResourceLocation identifier) {
        return Optional.ofNullable(configRules.get(identifier));
    }

    public Optional<ShogiEffect<?>> getImportedRule(ResourceLocation identifier) {
        return Optional.ofNullable(datapackRules.get(identifier));
    }
}
