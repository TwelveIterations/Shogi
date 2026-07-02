package net.blay09.mods.shogi.common.effect.condition.player;

import com.google.gson.JsonPrimitive;
import com.mojang.serialization.JsonOps;
import net.blay09.mods.shogi.common.parse.ShogiRuleParser;
import net.blay09.mods.shogi.effect.ConstantEffect;
import net.blay09.mods.shogi.scope.internal.ShogiScopeImpl;
import net.minecraft.SharedConstants;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.scores.Scoreboard;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TeamEffectsTest {

    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void matchesPlayerTeamByName() {
        final var scoreboard = new Scoreboard();
        final var team = scoreboard.addPlayerTeam("red");

        assertTrue(IsInTeam.isInTeam(team, "red"));
        assertFalse(IsInTeam.isInTeam(team, "blue"));
        assertFalse(IsInTeam.isInTeam(null, "red"));
    }

    @Test
    void looksUpTeamFromScoreboardUsername() {
        final var scoreboard = new Scoreboard();
        final var team = scoreboard.addPlayerTeam("red");
        scoreboard.addPlayerToTeam("Blay", team);

        assertEquals("red", LookupTeam.lookupTeam(scoreboard, "Blay"));
        assertEquals("", LookupTeam.lookupTeam(scoreboard, "Other"));
    }

    @Test
    void parsesTeamEffects() {
        final var scope = new ShogiScopeImpl(ResourceLocation.fromNamespaceAndPath("shogi", "test"));
        scope.registerEffect(ConstantEffect.IDENTIFIER, ConstantEffect.MAP_CODEC, List.of("value"));
        scope.registerEffect(IsInTeam.IDENTIFIER, IsInTeam.mapCodec(scope), List.of("team"));
        scope.registerEffect(LookupTeam.IDENTIFIER, LookupTeam.mapCodec(scope), List.of("username"));

        final var isInTeam = ShogiRuleParser.parse(scope, JsonOps.INSTANCE, "is_in_team('red')").result().orElseThrow();
        final var isInTeamEffect = assertInstanceOf(IsInTeam.class, isInTeam);
        assertEquals(new JsonPrimitive("red"), assertInstanceOf(ConstantEffect.class, isInTeamEffect.team()).value());

        final var lookupTeam = ShogiRuleParser.parse(scope, JsonOps.INSTANCE, "lookup_team('Blay')").result().orElseThrow();
        final var lookupTeamEffect = assertInstanceOf(LookupTeam.class, lookupTeam);
        assertEquals(new JsonPrimitive("Blay"), assertInstanceOf(ConstantEffect.class, lookupTeamEffect.username()).value());
    }
}
