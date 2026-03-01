package net.blay09.mods.shogi.common;

import com.google.gson.*;
import com.mojang.serialization.JsonOps;
import net.blay09.mods.shogi.common.effect.compose.AggregateEffect;
import net.blay09.mods.shogi.common.parse.ShogiRuleParser;
import net.blay09.mods.shogi.effect.ShogiEffect;
import net.blay09.mods.shogi.scope.ShogiScope;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ShogiRuleLoader {

    private static final Logger logger = LoggerFactory.getLogger(ShogiRuleLoader.class);

    public static Map<Identifier, ShogiEffect<?>> loadJson(HolderLookup.Provider registries, ShogiScope scope, Path path) {
        final Map<Identifier, ShogiEffect<?>> parsedOverrides = new HashMap<>();
        if (!Files.exists(path)) {
            return parsedOverrides;
        }

        final String content;
        try {
            content = Files.readString(path);
        } catch (IOException e) {
            logger.warn("Failed to read Shogi rules from {}", path, e);
            return parsedOverrides;
        }

        final JsonObject rootObject;
        try {
            final var root = JsonParser.parseString(content);
            if (!root.isJsonObject()) {
                logger.warn("Invalid Shogi rules JSON at {}: expected an object", path);
                return parsedOverrides;
            }
            rootObject = root.getAsJsonObject();
        } catch (JsonParseException e) {
            logger.warn("Failed to parse Shogi rules JSON at {}", path, e);
            return parsedOverrides;
        }

        final var rulesElement = rootObject.get("rules");
        if (rulesElement == null) {
            return parsedOverrides;
        }
        if (!rulesElement.isJsonObject()) {
            logger.warn("Invalid Shogi rules JSON at {}: 'rules' must be an object", path);
            return parsedOverrides;
        }

        final var registryJsonOps = RegistryOps.create(JsonOps.INSTANCE, registries);

        for (final var ruleEntry : rulesElement.getAsJsonObject().entrySet()) {
            final var valueIdentifier = Identifier.tryParse(ruleEntry.getKey());
            if (valueIdentifier == null) {
                logger.warn("Skipping invalid Shogi value identifier '{}' in {}", ruleEntry.getKey(), path);
                continue;
            }
            if (ruleEntry.getValue().isJsonObject()) {
                decodeObjectRule(scope, registryJsonOps, parsedOverrides, path, ruleEntry.getKey(), valueIdentifier, ruleEntry.getValue().getAsJsonObject());
            } else if (ruleEntry.getValue().isJsonPrimitive() && ruleEntry.getValue().getAsJsonPrimitive().isString()) {
                decodeStringRule(scope, parsedOverrides, path, ruleEntry.getKey(), valueIdentifier, ruleEntry.getValue().getAsString());
            } else if (ruleEntry.getValue().isJsonArray()) {
                decodeArrayRule(scope, registryJsonOps, parsedOverrides, path, ruleEntry.getKey(), valueIdentifier, ruleEntry.getValue().getAsJsonArray());
            } else {
                logger.warn("Skipping Shogi rule '{}' in {}: expected object, string, or string array value", ruleEntry.getKey(), path);
            }
        }
        return parsedOverrides;
    }

    private static void decodeObjectRule(ShogiScope scope, RegistryOps<JsonElement> registryJsonOps, Map<Identifier, ShogiEffect<?>> parsedOverrides, Path path, String key, Identifier valueIdentifier, JsonObject value) {
        scope.getEffectCodec().parse(registryJsonOps, value).resultOrPartial(error ->
                logger.warn("Skipping Shogi rule '{}' in {}: {}", key, path, error)
        ).ifPresent(decodedEffect -> parsedOverrides.put(valueIdentifier, decodedEffect));
    }

    private static void decodeStringRule(ShogiScope scope, Map<Identifier, ShogiEffect<?>> parsedOverrides, Path path, String key, Identifier valueIdentifier, String value) {
        ShogiRuleParser.parse(scope, value).resultOrPartial(error ->
                logger.warn("Skipping Shogi rule '{}' in {}: {}", key, path, error)
        ).ifPresent(decodedEffect -> parsedOverrides.put(valueIdentifier, decodedEffect));
    }

    private static void decodeArrayRule(ShogiScope scope, RegistryOps<JsonElement> registryJsonOps, Map<Identifier, ShogiEffect<?>> parsedOverrides, Path path, String key, Identifier valueIdentifier, JsonArray value) {
        final var rules = new ArrayList<ShogiEffect<?>>();
        for (final var element : value) {
            if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
                final var parsed = ShogiRuleParser.parse(scope, element.getAsString());
                if (parsed.error().isPresent()) {
                    logger.warn("Skipping Shogi rule '{}' in {}: {}", key, path, parsed.error().orElseThrow().message());
                    return;
                }
                rules.add(parsed.result().orElseThrow());
            } else if (element.isJsonObject()) {
                final var parsed = scope.getEffectCodec().parse(registryJsonOps, element.getAsJsonObject());
                if (parsed.error().isPresent()) {
                    logger.warn("Skipping Shogi rule '{}' in {}: {}", key, path, parsed.error().orElseThrow().message());
                    return;
                }
                rules.add(parsed.result().orElseThrow());
            } else {
                logger.warn("Skipping Shogi rule '{}' in {}: expected string or object entries in array", key, path);
                return;
            }
        }
        parsedOverrides.put(valueIdentifier, new AggregateEffect(rules));
    }

}
