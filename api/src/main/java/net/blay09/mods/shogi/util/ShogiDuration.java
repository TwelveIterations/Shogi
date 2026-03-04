package net.blay09.mods.shogi.util;

import net.minecraft.util.Mth;

import java.time.Duration;

public class ShogiDuration {
    public static Duration parse(String input) {
        return Duration.parse(input);
    }

    public static int toTicks(Duration duration) {
        return toTicks(duration, 20);
    }

    public static int toTicks(Duration duration, int tps) {
        return Mth.floor(duration.toMillis() / 1000f * tps);
    }
}
