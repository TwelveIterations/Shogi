package net.blay09.mods.shogi.common;

import net.blay09.mods.shogi.scope.ShogiScope;
import net.minecraft.core.HolderLookup;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;
import java.util.function.Supplier;

public class ShogiClientRuleReloadListener extends AbstractShogiRuleReloadListener {
    static boolean loadsScopeOnClient(ShogiScope scope) {
        return scope.isLoadedOnClient();
    }

    public ShogiClientRuleReloadListener(Path configDirectory, Supplier<HolderLookup.@Nullable Provider> registriesSupplier) {
        super(registriesSupplier, configDirectory, ShogiClientRuleReloadListener::loadsScopeOnClient);
    }
}
