package net.blay09.mods.shogi.common;

import net.blay09.mods.shogi.scope.ShogiScope;
import net.minecraft.core.HolderLookup;
import java.nio.file.Path;

public class ShogiRuleReloadListener extends AbstractShogiRuleReloadListener {
    static boolean loadsScopeOnServer(ShogiScope scope) {
        return scope.isLoadedOnServer();
    }

    public ShogiRuleReloadListener(HolderLookup.Provider registries, Path configDirectory) {
        super(() -> registries, configDirectory, ShogiRuleReloadListener::loadsScopeOnServer);
    }
}
