package net.blay09.mods.shogi.common.scope;

import net.blay09.mods.shogi.effect.ShogiEffect;
import net.blay09.mods.shogi.scope.ShogiOverrideProvider;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

public class ShogiOverrideProviderImpl implements ShogiOverrideProvider {
    private final ShogiRuleRepository repository;

    public ShogiOverrideProviderImpl(ShogiRuleRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<ShogiEffect<?>> getOverride(ResourceLocation identifier) {
        return repository.getValueOverride(identifier);
    }
}
