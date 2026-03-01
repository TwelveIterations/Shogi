package net.blay09.mods.shogi.client.platform;

import java.util.ServiceLoader;

public class ShogiClientRuntimeSpi {
    private static final ShogiClientRuntime runtime = create();

    public static ShogiClientRuntime get() {
        return runtime;
    }

    private static ShogiClientRuntime create() {
        var loader = ServiceLoader.load(ShogiClientRuntimeFactory.class);
        return loader.findFirst().map(ShogiClientRuntimeFactory::create).orElseThrow(() -> new RuntimeException("No ShogiClientRuntimeFactory found"));
    }
}
