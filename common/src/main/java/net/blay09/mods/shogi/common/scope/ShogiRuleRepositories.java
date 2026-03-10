package net.blay09.mods.shogi.common.scope;

import net.blay09.mods.shogi.scope.ShogiScope;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;

public final class ShogiRuleRepositories {
    private static final Map<ShogiScope, ShogiRuleRepository> REPOSITORIES = Collections.synchronizedMap(new WeakHashMap<>());

    private ShogiRuleRepositories() {
    }

    public static void register(ShogiScope scope, ShogiRuleRepository repository) {
        REPOSITORIES.put(scope, repository);
    }

    public static Optional<ShogiRuleRepository> get(ShogiScope scope) {
        return Optional.ofNullable(REPOSITORIES.get(scope));
    }
}
