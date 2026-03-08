package net.blay09.mods.shogi.common.effect.server.cooldown;

import com.google.common.collect.Maps;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public final class ShogiCooldowns {

    private static final String COOLDOWNS_TAG = "shogi_cooldowns";

    private final Map<Identifier, ShogiCooldownInstance> cooldowns = Maps.newHashMap();

    public long getRemainingTicks(Identifier identifier) {
        final var cooldownInstance = cooldowns.get(identifier);
        if (cooldownInstance == null) {
            return 0;
        }

        final int remainingTicks = cooldownInstance.remainingTicks();
        return Math.max(0, remainingTicks);
    }

    public boolean hasCooldown(Identifier identifier) {
        return getRemainingTicks(identifier) > 0;
    }

    public long addCooldown(Identifier identifier, int ticks) {
        final long currentRemainingTicks = getRemainingTicks(identifier);
        final int totalDuration = (int) Math.min(Integer.MAX_VALUE, currentRemainingTicks + ticks);
        cooldowns.put(identifier, new ShogiCooldownInstance(identifier, totalDuration));
        return totalDuration;
    }

    public void resetCooldown(Identifier identifier) {
        cooldowns.remove(identifier);
    }

    public void resetAllCooldowns() {
        cooldowns.clear();
    }

    public void tick() {
        cooldowns.values().removeIf(ShogiCooldownInstance::tickAndIsComplete);
    }

    public void copyFrom(ShogiCooldowns other) {
        clear();
        for (final var cooldownInstance : other.cooldowns.values()) {
            cooldowns.put(cooldownInstance.identifier(), cooldownInstance.copy());
        }
    }

    public void save(ValueOutput output) {
        final var entries = cooldowns.values();
        if (entries.isEmpty()) {
            output.discard(COOLDOWNS_TAG);
            return;
        }

        final var list = output.list(COOLDOWNS_TAG, ShogiCooldownInstance.CODEC);
        entries.forEach(list::add);
    }

    public void load(ValueInput input) {
        clear();
        for (final var cooldownInstance : input.listOrEmpty(COOLDOWNS_TAG, ShogiCooldownInstance.CODEC)) {
            if (cooldownInstance.remainingTicks() > 0) {
                cooldowns.put(cooldownInstance.identifier(), cooldownInstance);
            }
        }
    }

    private void clear() {
        cooldowns.clear();
    }

    public Set<Identifier> getCooldownIds() {
        return cooldowns.keySet();
    }

    public Collection<ShogiCooldownInstance> getCooldowns() {
        return cooldowns.values();
    }

    public static ShogiCooldowns get(ServerPlayer player) {
        return ((ShogiCooldownsAccess) player).shogi$getCooldowns();
    }
}
