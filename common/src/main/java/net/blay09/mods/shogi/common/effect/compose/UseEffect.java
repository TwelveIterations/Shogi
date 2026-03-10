package net.blay09.mods.shogi.common.effect.compose;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.blay09.mods.shogi.common.scope.ShogiRuleRepositories;
import net.blay09.mods.shogi.context.ShogiContext;
import net.blay09.mods.shogi.context.executor.aggregate.AggregateKey;
import net.blay09.mods.shogi.effect.EmptyEffect;
import net.blay09.mods.shogi.effect.ShogiEffect;
import net.blay09.mods.shogi.scope.ShogiScope;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.Deque;

public class UseEffect implements ShogiEffect<Object> {
    public static final Identifier IDENTIFIER = Identifier.fromNamespaceAndPath("shogi", "use");
    private static final AggregateKey<Deque<Identifier>> IMPORT_STACK = AggregateKey.of(Identifier.fromNamespaceAndPath("shogi", "import_stack"));
    private static final Logger logger = LoggerFactory.getLogger(UseEffect.class);

    private final ShogiScope scope;
    private final Identifier identifier;

    public UseEffect(ShogiScope scope, Identifier identifier) {
        this.scope = scope;
        this.identifier = identifier;
    }

    public static MapCodec<UseEffect> mapCodec(ShogiScope scope) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
                Identifier.CODEC.fieldOf("identifier").forGetter(UseEffect::importedIdentifier)
        ).apply(instance, identifier -> new UseEffect(scope, identifier)));
    }

    public Identifier importedIdentifier() {
        return identifier;
    }

    @Override
    public Identifier identifier() {
        return IDENTIFIER;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Either<Object, ?> apply(ShogiContext context) {
        final var importedEffect = scope.getOverride(identifier)
                .or(() -> ShogiRuleRepositories.get(scope).flatMap(repository -> repository.getImportedRule(identifier)))
                .orElse(null);
        if (importedEffect == null) {
            logger.warn("Failed to resolve Shogi import '{}': no matching value override or datapack rule found", identifier);
            return (Either<Object, ?>) EmptyEffect.INSTANCE.apply(context);
        }

        return context.statefulAggregate(IMPORT_STACK, ArrayDeque::new, stack -> {
            if (stack.contains(identifier)) {
                final var cycle = new ArrayDeque<>(stack);
                cycle.addLast(identifier);
                logger.warn("Failed to resolve Shogi import '{}': cyclic import detected {}", identifier, cycle);
                return (Either<Object, ?>) EmptyEffect.INSTANCE.apply(context);
            }

            stack.addLast(identifier);
            try {
                return (Either<Object, ?>) importedEffect.apply(context);
            } finally {
                stack.removeLastOccurrence(identifier);
            }
        });
    }
}
