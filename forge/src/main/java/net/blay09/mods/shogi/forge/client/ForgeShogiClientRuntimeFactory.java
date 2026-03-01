package net.blay09.mods.shogi.forge.client;

import net.blay09.mods.shogi.client.platform.ShogiClientRuntime;
import net.blay09.mods.shogi.client.platform.ShogiClientRuntimeFactory;

public class ForgeShogiClientRuntimeFactory implements ShogiClientRuntimeFactory {
    @Override
    public ShogiClientRuntime create() {
        return new ForgeShogiClientRuntime();
    }
}
