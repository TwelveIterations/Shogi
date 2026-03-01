package net.blay09.mods.shogi.neoforge;

import net.blay09.mods.shogi.common.platform.ShogiRuntime;
import net.blay09.mods.shogi.common.platform.ShogiRuntimeFactory;

public class NeoForgeShogiRuntimeFactory implements ShogiRuntimeFactory {
    @Override
    public ShogiRuntime create() {
        return new NeoForgeShogiRuntime();
    }
}
