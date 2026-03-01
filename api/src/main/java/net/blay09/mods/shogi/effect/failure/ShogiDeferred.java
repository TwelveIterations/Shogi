package net.blay09.mods.shogi.effect.failure;

public interface ShogiDeferred extends ShogiFatalFailure {
    ShogiDeferred INSTANCE = new ShogiDeferred() {
    };
}
