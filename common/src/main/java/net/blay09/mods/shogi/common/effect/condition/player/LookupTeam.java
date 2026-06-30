package net.blay09.mods.shogi.common.effect.condition.player;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.blay09.mods.shogi.coercion.Coercion;
import net.blay09.mods.shogi.context.ShogiContext;
import net.blay09.mods.shogi.effect.EffectArgumentCodecs;
import net.blay09.mods.shogi.effect.ShogiEffect;
import net.blay09.mods.shogi.scope.ShogiScope;
import net.minecraft.resources.Identifier;
import net.minecraft.world.scores.Scoreboard;

public record LookupTeam(ShogiEffect<?> username) implements ShogiEffect<String> {

    public static final Identifier IDENTIFIER = Identifier.fromNamespaceAndPath("shogi", "lookup_team");

    public static MapCodec<LookupTeam> mapCodec(ShogiScope scope) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
                EffectArgumentCodecs.effectOrConstant(scope).fieldOf("username").forGetter(LookupTeam::username)
        ).apply(instance, LookupTeam::new));
    }

    @Override
    public Either<String, ?> apply(ShogiContext context) {
        final var usernameResult = username.apply(context);
        final var failure = usernameResult.right().orElse(null);
        if (failure != null) {
            return Either.right(failure);
        }

        final var resolvedUsername = usernameResult.mapLeft(Coercion.STRING).orThrow();
        return Either.left(lookupTeam(context.requireLevel().getScoreboard(), resolvedUsername));
    }

    static String lookupTeam(Scoreboard scoreboard, String username) {
        final var team = scoreboard.getPlayersTeam(username);
        return team != null ? team.getName() : "";
    }

    @Override
    public Identifier identifier() {
        return IDENTIFIER;
    }
}
