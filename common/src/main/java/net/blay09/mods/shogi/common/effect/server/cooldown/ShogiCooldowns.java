package net.blay09.mods.shogi.common.effect.server.cooldown;

import com.google.common.collect.Maps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public final class ShogiCooldowns {

    private static final String COOLDOWNS_TAG = "shogi_cooldowns";

    private final Map<ResourceLocation, ShogiCooldownInstance> cooldowns = Maps.newHashMap();

    public long getRemainingTicks(ResourceLocation identifier) {
        final var cooldownInstance = cooldowns.get(identifier);
        if (cooldownInstance == null) {
            return 0;
        }

        final int remainingTicks = cooldownInstance.remainingTicks();
        return Math.max(0, remainingTicks);
    }

    public boolean hasCooldown(ResourceLocation identifier) {
        return getRemainingTicks(identifier) > 0;
    }

    public long addCooldown(ResourceLocation identifier, int ticks) {
        final long currentRemainingTicks = getRemainingTicks(identifier);
        final int totalDuration = (int) Math.min(Integer.MAX_VALUE, currentRemainingTicks + ticks);
        cooldowns.put(identifier, new ShogiCooldownInstance(identifier, totalDuration));
        return totalDuration;
    }

    public void resetCooldown(ResourceLocation identifier) {
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

    public void save(CompoundTag output) {
        final var entries = cooldowns.values();
        if (entries.isEmpty()) {
            output.remove(COOLDOWNS_TAG);
            return;
        }

        final var list = new ListTag();
        for (final var cooldownInstance : entries) {
            ShogiCooldownInstance.CODEC.encodeStart(NbtOps.INSTANCE, cooldownInstance)
                    .result()
                    .ifPresent(list::add);
        }
        output.put(COOLDOWNS_TAG, list);
    }

    public void load(CompoundTag input) {
        clear();
        for (final Tag tag : input.getList(COOLDOWNS_TAG, Tag.TAG_COMPOUND)) {
            ShogiCooldownInstance.CODEC.parse(NbtOps.INSTANCE, tag)
                    .result()
                    .filter(cooldownInstance -> cooldownInstance.remainingTicks() > 0)
                    .ifPresent(cooldownInstance -> cooldowns.put(cooldownInstance.identifier(), cooldownInstance));
        }
    }

    private void clear() {
        cooldowns.clear();
    }

    public Set<ResourceLocation> getCooldownIds() {
        return cooldowns.keySet();
    }

    public Collection<ShogiCooldownInstance> getCooldowns() {
        return cooldowns.values();
    }

    public static ShogiCooldowns get(ServerPlayer player) {
        return ((ShogiCooldownsAccess) player).shogi$getCooldowns();
    }
}
