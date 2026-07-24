package net.blay09.mods.shogi.common.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "net.minecraft.client.gui.components.MultilineTextField$StringView")
public interface MultilineTextFieldStringViewAccessor {
    @Accessor
    int getBeginIndex();

    @Accessor
    int getEndIndex();
}
