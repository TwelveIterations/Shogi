package net.blay09.mods.shogi.common.platform;

import java.util.ServiceLoader;

public class ShogiRuntimeSpi {
    private static final ShogiRuntime runtime = create();

    public static ShogiRuntime get() {
        return runtime;
    }

    private static ShogiRuntime create() {
        var loader = ServiceLoader.load(ShogiRuntimeFactory.class);
        var factory = loader.findFirst().orElseThrow(() -> new RuntimeException("No ShogiRuntimeFactory found"));
        return factory.create();
    }
}
