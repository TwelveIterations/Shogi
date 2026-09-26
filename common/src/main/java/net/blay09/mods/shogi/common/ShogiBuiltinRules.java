package net.blay09.mods.shogi.common;

import com.mojang.datafixers.util.Either;
import net.blay09.mods.shogi.Shogi;
import net.blay09.mods.shogi.ShogiValue;
import net.blay09.mods.shogi.effect.EmptyEffect;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;

public class ShogiBuiltinRules {

    public static final ShogiValue<LivingEntity, ?> ON_DEATH = Shogi.defaultScope().maybe(
            Identifier.fromNamespaceAndPath("shogi", "on_death"),
            _ -> Either.right(EmptyEffect.INSTANCE)
    );

}
