package net.blay09.mods.shogi.common.effect.server.cooldown;

import net.minecraft.resources.Identifier;

public record CooldownActiveFailure(Identifier identifier, long remainingTicks, int requestedTicks) {
}
