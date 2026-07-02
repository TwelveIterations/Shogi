package net.blay09.mods.shogi.internal;

import net.blay09.mods.shogi.scope.ShogiScope;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

public final class ShogiScopeRegistry {

    private static final ConcurrentMap<ResourceLocation, ShogiScope> scopes = new ConcurrentHashMap<>();

    private ShogiScopeRegistry() {
    }

    public static ShogiScope getOrCreate(ResourceLocation identifier, Function<ResourceLocation, ShogiScope> factory) {
        return scopes.computeIfAbsent(identifier, factory);
    }

    public static Optional<ShogiScope> get(ResourceLocation identifier) {
        return Optional.ofNullable(scopes.get(identifier));
    }

    public static Collection<ShogiScope> getAll() {
        return scopes.values();
    }
}
