package net.blay09.mods.shogi.common.effect.server.cooldown;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

public class ShogiCooldownInstance {

    public static final Codec<ShogiCooldownInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("identifier").forGetter(ShogiCooldownInstance::identifier),
            Codec.INT.fieldOf("total_duration").forGetter(ShogiCooldownInstance::totalDuration),
            Codec.INT.fieldOf("remaining_ticks").forGetter(ShogiCooldownInstance::remainingTicks)
    ).apply(instance, ShogiCooldownInstance::new));

    private final ResourceLocation identifier;
    private final int totalDuration;
    private int remainingTicks;

    public ShogiCooldownInstance(ResourceLocation identifier, int totalDuration) {
        this(identifier, totalDuration, totalDuration);
    }

    public ShogiCooldownInstance(ResourceLocation identifier, int totalDuration, int remainingTicks) {
        this.identifier = identifier;
        this.totalDuration = totalDuration;
        this.remainingTicks = remainingTicks;
    }

    public ResourceLocation identifier() {
        return identifier;
    }

    public int totalDuration() {
        return totalDuration;
    }

    public int remainingTicks() {
        return remainingTicks;
    }

    public boolean tickAndIsComplete() {
        remainingTicks--;
        return remainingTicks <= 0;
    }

    public ShogiCooldownInstance copy() {
        return new ShogiCooldownInstance(identifier, totalDuration, remainingTicks);
    }
}
