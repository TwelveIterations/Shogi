package net.blay09.mods.shogi.neoforge.client;

import net.blay09.mods.shogi.client.platform.ShogiClientRuntime;
import net.blay09.mods.shogi.client.platform.ShogiClientRuntimeFactory;

public class NeoForgeShogiClientRuntimeFactory implements ShogiClientRuntimeFactory {
    @Override
    public ShogiClientRuntime create() {
        return new NeoForgeShogiClientRuntime();
    }
}
