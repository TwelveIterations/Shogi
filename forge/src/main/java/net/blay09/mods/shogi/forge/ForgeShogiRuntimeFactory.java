package net.blay09.mods.shogi.forge;

import net.blay09.mods.shogi.common.platform.ShogiRuntime;
import net.blay09.mods.shogi.common.platform.ShogiRuntimeFactory;

public class ForgeShogiRuntimeFactory implements ShogiRuntimeFactory {
    @Override
    public ShogiRuntime create() {
        return new ForgeShogiRuntime();
    }
}
