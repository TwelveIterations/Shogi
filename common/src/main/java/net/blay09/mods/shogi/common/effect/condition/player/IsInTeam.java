package net.blay09.mods.shogi.common.effect.condition.player;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.blay09.mods.shogi.coercion.Coercion;
import net.blay09.mods.shogi.context.ShogiContext;
import net.blay09.mods.shogi.effect.EffectArgumentCodecs;
import net.blay09.mods.shogi.effect.ShogiEffect;
import net.blay09.mods.shogi.scope.ShogiScope;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.scores.PlayerTeam;
import org.jspecify.annotations.Nullable;

public record IsInTeam(ShogiEffect<?> team) implements ShogiEffect<Boolean> {

    public static final ResourceLocation IDENTIFIER = ResourceLocation.fromNamespaceAndPath("shogi", "is_in_team");

    public static MapCodec<IsInTeam> mapCodec(ShogiScope scope) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
                EffectArgumentCodecs.effectOrConstant(scope).fieldOf("team").forGetter(IsInTeam::team)
        ).apply(instance, IsInTeam::new));
    }

    @Override
    public Either<Boolean, ?> apply(ShogiContext context) {
        final var teamResult = team.apply(context);
        final var failure = teamResult.right().orElse(null);
        if (failure != null) {
            return Either.right(failure);
        }

        final var expectedTeam = teamResult.mapLeft(Coercion.STRING).orThrow();
        return Either.left(expectedTeam.isEmpty() || isInTeam(context.requirePlayer().getTeam(), expectedTeam));
    }

    static boolean isInTeam(@Nullable PlayerTeam playerTeam, String expectedTeam) {
        return playerTeam != null && playerTeam.getName().equals(expectedTeam);
    }

    @Override
    public ResourceLocation identifier() {
        return IDENTIFIER;
    }
}
