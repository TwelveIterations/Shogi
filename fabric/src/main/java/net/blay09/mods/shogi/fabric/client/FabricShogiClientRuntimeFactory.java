package net.blay09.mods.shogi.fabric.client;

import net.blay09.mods.shogi.client.platform.ShogiClientRuntime;
import net.blay09.mods.shogi.client.platform.ShogiClientRuntimeFactory;

public class FabricShogiClientRuntimeFactory implements ShogiClientRuntimeFactory {
    @Override
    public ShogiClientRuntime create() {
        return new FabricShogiClientRuntime();
    }
}
