package net.blay09.mods.shogi.fabric;

import net.blay09.mods.shogi.common.platform.ShogiRuntime;
import net.blay09.mods.shogi.common.platform.ShogiRuntimeFactory;

public class FabricShogiRuntimeFactory implements ShogiRuntimeFactory {
    @Override
    public ShogiRuntime create() {
        return new FabricShogiRuntime();
    }
}

