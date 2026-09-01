package net.blay09.mods.shogi.common.effect.condition.item;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.blay09.mods.shogi.effect.ShogiEffect;
import net.blay09.mods.shogi.context.ShogiContext;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.codec.HolderSetCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

public record IsItem(HolderSet<Item> item) implements ShogiEffect<Boolean> {

    public static final Identifier IDENTIFIER = Identifier.fromNamespaceAndPath("shogi", "is_item");
    public static final MapCodec<IsItem> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            HolderSetCodec.create(Registries.ITEM, BuiltInRegistries.ITEM.holderByNameCodec(), false).fieldOf("item").forGetter(IsItem::item)
    ).apply(instance, IsItem::new));

    @Override
    public Either<Boolean, Throwable> apply(ShogiContext context) {
        final var itemStack = context.itemStack();
        return Either.left(itemStack.is(item));
    }

    @Override
    public Identifier identifier() {
        return IDENTIFIER;
    }

}
