package net.blay09.mods.shogi.common;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.blay09.mods.shogi.common.effect.compose.AggregateEffect;
import net.blay09.mods.shogi.common.parse.ShogiRuleParser;
import net.blay09.mods.shogi.effect.ShogiEffect;
import net.blay09.mods.shogi.scope.ShogiScope;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.resources.RegistryOps;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class ShogiRuleLoader {

    private static final Logger logger = LoggerFactory.getLogger(ShogiRuleLoader.class);

    private ShogiRuleLoader() {
    }

    public static Map<ResourceLocation, ShogiEffect<?>> loadConfigRules(HolderLookup.Provider registries, ShogiScope scope, Path configDirectory) {
        final Map<ResourceLocation, ShogiEffect<?>> parsedOverrides = new HashMap<>();
        final var path = configPath(configDirectory, scope);
        if (!Files.exists(path)) {
            return parsedOverrides;
        }

        final JsonObject rootObject;
        try {
            rootObject = readJsonObject(path);
        } catch (IOException e) {
            logger.warn("Failed to read Shogi rules from {}", path, e);
            return parsedOverrides;
        } catch (JsonParseException e) {
            logger.warn("Failed to parse Shogi rules JSON at {}", path, e);
            return parsedOverrides;
        }

        final var registryJsonOps = RegistryOps.create(JsonOps.INSTANCE, registries);
        for (final var ruleEntry : rootObject.entrySet()) {
            final var valueIdentifier = ResourceLocation.tryParse(ruleEntry.getKey());
            if (valueIdentifier == null) {
                logger.warn("Skipping invalid Shogi value identifier '{}' in {}", ruleEntry.getKey(), path);
                continue;
            }

            decodeRulePayload(scope, registryJsonOps, ruleEntry.getValue(), path + "#" + ruleEntry.getKey())
                    .ifPresent(decodedEffect -> parsedOverrides.put(valueIdentifier, decodedEffect));
        }
        return parsedOverrides;
    }

    public static Map<ResourceLocation, ShogiEffect<?>> loadDatapackRules(HolderLookup.Provider registries, ShogiScope scope, ResourceManager resourceManager) {
        final Map<ResourceLocation, ShogiEffect<?>> parsedRules = new HashMap<>();
        final var registryJsonOps = RegistryOps.create(JsonOps.INSTANCE, registries);
        final var resourcePrefix = datapackScopePrefix(scope);
        final var resources = resourceManager.listResources(resourcePrefix, path -> path.getPath().endsWith(".json"));
        for (final var entry : resources.entrySet()) {
            final var ruleIdentifier = toRuleIdentifier(scope, entry.getKey());
            if (ruleIdentifier == null) {
                logger.warn("Skipping invalid Shogi datapack rule resource '{}'", entry.getKey());
                continue;
            }

            readJson(entry.getValue(), entry.getKey().toString())
                    .flatMap(element -> decodeRulePayload(scope, registryJsonOps, element, entry.getKey().toString()))
                    .ifPresent(decodedEffect -> parsedRules.put(ruleIdentifier, decodedEffect));
        }
        return parsedRules;
    }

    private static String datapackScopePrefix(ShogiScope scope) {
        final var scopeIdentifier = scope.identifier();
        return scopeIdentifier.getNamespace() + "/" + scopeIdentifier.getPath();
    }

    static Path configPath(Path configDirectory, ShogiScope scope) {
        final var scopeIdentifier = scope.identifier();
        final var normalizedScopePath = scopeIdentifier.getPath().replace('/', '.');
        return configDirectory.resolve(scopeIdentifier.getNamespace() + "." + normalizedScopePath + ".json");
    }

    public static Optional<ShogiEffect<?>> decodeRulePayload(ShogiScope scope, RegistryOps<JsonElement> registryJsonOps, JsonElement payload, String source) {
        if (payload.isJsonObject()) {
            return scope.getEffectCodec().parse(registryJsonOps, payload.getAsJsonObject()).resultOrPartial(error ->
                    logger.warn("Skipping Shogi rule '{}': {}", source, error)
            );
        }
        if (payload.isJsonPrimitive() && payload.getAsJsonPrimitive().isString()) {
            return ShogiRuleParser.parse(scope, registryJsonOps, payload.getAsString()).resultOrPartial(error ->
                    logger.warn("Skipping Shogi rule '{}': {}", source, error)
            );
        }
        if (payload.isJsonArray()) {
            final var rules = new ArrayList<ShogiEffect<?>>();
            for (final var element : payload.getAsJsonArray()) {
                final var parsed = decodeRulePayload(scope, registryJsonOps, element, source);
                if (parsed.isEmpty()) {
                    return Optional.empty();
                }
                rules.add(parsed.orElseThrow());
            }
            return Optional.of(AggregateEffect.withAutoApplied(scope, registryJsonOps, rules));
        }

        logger.warn("Skipping Shogi rule '{}': expected string, object, or array payload", source);
        return Optional.empty();
    }

    private static JsonObject readJsonObject(Path path) throws IOException {
        final String content = Files.readString(path);
        final var root = JsonParser.parseString(content);
        if (!root.isJsonObject()) {
            throw new JsonParseException("expected an object");
        }
        return root.getAsJsonObject();
    }

    private static Optional<JsonElement> readJson(Resource resource, String source) {
        try (Reader reader = resource.openAsReader()) {
            return Optional.of(JsonParser.parseReader(reader));
        } catch (IOException e) {
            logger.warn("Failed to read Shogi rule from {}", source, e);
        } catch (JsonParseException e) {
            logger.warn("Failed to parse Shogi rule JSON at {}", source, e);
        }
        return Optional.empty();
    }

    @Nullable
    private static ResourceLocation toRuleIdentifier(ShogiScope scope, ResourceLocation resourceLocation) {
        final var resourcePath = resourceLocation.getPath();
        final var scopePrefix = datapackScopePrefix(scope) + "/";
        if (!resourcePath.startsWith(scopePrefix) || !resourcePath.endsWith(".json")) {
            return null;
        }

        final var rulePath = resourcePath.substring(scopePrefix.length(), resourcePath.length() - ".json".length());
        if (rulePath.isEmpty()) {
            return null;
        }
        if (!ResourceLocation.isValidPath(rulePath)) {
            return null;
        }

        return ResourceLocation.fromNamespaceAndPath(resourceLocation.getNamespace(), rulePath);
    }
}
