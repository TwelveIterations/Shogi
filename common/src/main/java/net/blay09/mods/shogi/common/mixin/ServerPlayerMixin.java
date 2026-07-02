package net.blay09.mods.shogi.common.mixin;

import net.blay09.mods.shogi.common.effect.server.cooldown.ShogiCooldownsAccess;
import net.blay09.mods.shogi.common.effect.server.cooldown.ShogiCooldowns;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin implements ShogiCooldownsAccess {

    @Unique
    private final ShogiCooldowns shogi$cooldowns = new ShogiCooldowns();

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void shogi$readAdditionalSaveData(CompoundTag input, CallbackInfo ci) {
        shogi$cooldowns.load(input);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void shogi$addAdditionalSaveData(CompoundTag output, CallbackInfo ci) {
        shogi$cooldowns.save(output);
    }

    @Inject(method = "restoreFrom", at = @At("TAIL"))
    private void shogi$restoreFrom(ServerPlayer oldPlayer, boolean restoreAll, CallbackInfo ci) {
        if (oldPlayer instanceof ShogiCooldownsAccess access) {
            shogi$cooldowns.copyFrom(access.shogi$getCooldowns());
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void shogi$tick(CallbackInfo ci) {
        shogi$cooldowns.tick();
    }

    @Override
    public ShogiCooldowns shogi$getCooldowns() {
        return shogi$cooldowns;
    }
}
