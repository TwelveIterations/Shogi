package net.blay09.mods.shogi.common.effect.cost;

import com.google.gson.JsonPrimitive;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.blay09.mods.shogi.coercion.Coercion;
import net.blay09.mods.shogi.common.util.InventoryLookup;
import net.blay09.mods.shogi.context.ShogiContext;
import net.blay09.mods.shogi.context.executor.aggregate.AggregateKey;
import net.blay09.mods.shogi.effect.ConstantEffect;
import net.blay09.mods.shogi.effect.EffectArgumentCodecs;
import net.blay09.mods.shogi.effect.ShogiEffect;
import net.blay09.mods.shogi.scope.ShogiScope;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public record ItemCost(HolderSet<Item> item, ShogiEffect<?> count) implements ShogiEffect<ItemCostInformation> {

    public static final Identifier IDENTIFIER = Identifier.fromNamespaceAndPath("shogi", "item_cost");
    public static final AggregateKey<AggregatedItemCost> AGGREGATE_KEY = AggregateKey.of(IDENTIFIER);

    public static MapCodec<ItemCost> mapCodec(ShogiScope scope) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
                RegistryCodecs.homogeneousList(Registries.ITEM).fieldOf("item").forGetter(ItemCost::item),
                EffectArgumentCodecs.effectOrConstant(scope).fieldOf("count").orElse(new ConstantEffect(new JsonPrimitive(1))).forGetter(ItemCost::count)
        ).apply(instance, ItemCost::new));
    }

    @Override
    public Either<ItemCostInformation, Object> apply(ShogiContext context) {
        final var player = context.requirePlayer();
        final int requestedCount = count.apply(context).mapLeft(Coercion.NON_NEGATIVE_INT).orThrow();

        Predicate<ItemStack> matcher = it -> it.is(item);
        final int consumed = context.statefulAggregate(
                AGGREGATE_KEY,
                () -> new AggregatedItemCost(player),
                state -> state.simulateConsume(matcher, requestedCount)
        );
        final int available = InventoryLookup.countMatchingInPlayerInventory(player, matcher);
        if (consumed < requestedCount) {
            return Either.right(new ItemCostInformation(
                    item,
                    available,
                    requestedCount
            ));
        }

        context.consume(AGGREGATE_KEY, AggregatedItemCost::consume);
        return Either.left(new ItemCostInformation(item, available, requestedCount));
    }

    @Override
    public Identifier identifier() {
        return IDENTIFIER;
    }

    public static final class AggregatedItemCost {
        private final Player player;
        private final SimpleContainer snapshot;
        private final List<CommittableOperation> operations = new ArrayList<>();

        public AggregatedItemCost(Player player) {
            this.player = player;
            final var inventory = player.getInventory();
            snapshot = new SimpleContainer(inventory.getContainerSize());
            for (int i = 0; i < snapshot.getContainerSize(); i++) {
                snapshot.setItem(i, inventory.getItem(i).copy());
            }
        }

        public int simulateConsume(Predicate<ItemStack> matcher, int count) {
            operations.add(new CommittableOperation(matcher, count));
            return InventoryLookup.consumeFromContainer(snapshot, matcher, count);
        }

        public void consume() {
            for (final var operation : operations) {
                InventoryLookup.consumeFromPlayerInventory(player, operation.matcher(), operation.count());
            }
        }

        private record CommittableOperation(Predicate<ItemStack> matcher, int count) {
        }
    }
}
