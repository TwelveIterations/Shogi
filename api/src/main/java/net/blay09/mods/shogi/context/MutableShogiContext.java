package net.blay09.mods.shogi.context;

import net.blay09.mods.shogi.context.executor.EffectExecutor;
import net.blay09.mods.shogi.context.executor.internal.ImmediateEffectExecutor;
import net.blay09.mods.shogi.context.internal.ShogiContextImpl;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

/**
 * Mutable builder API for constructing and updating {@link ShogiContext} instances.
 */
public interface MutableShogiContext extends ShogiContext {
    /**
     * Sets the level in this context.
     *
     * @param level the level to store, or {@code null}
     * @return this builder
     */
    MutableShogiContext withLevel(@Nullable Level level);

    /**
     * Sets the entity in this context.
     *
     * @param entity the entity to store, or {@code null}
     * @return this builder
     */
    MutableShogiContext withEntity(@Nullable Entity entity);

    /**
     * Sets the block position in this context.
     *
     * @param blockPos the block position to store, or {@code null}
     * @return this builder
     */
    MutableShogiContext withBlockPos(@Nullable BlockPos blockPos);

    /**
     * Sets the block state in this context.
     *
     * @param blockState the block state to store, or {@code null}
     * @return this builder
     */
    MutableShogiContext withBlockState(@Nullable BlockState blockState);

    /**
     * Sets the item stack in this context.
     *
     * @param itemStack the item stack to store
     * @return this builder
     */
    MutableShogiContext withItemStack(ItemStack itemStack);

    /**
     * Stores a custom variable in this context.
     *
     * @param path  variable path
     * @param value variable value
     * @return this builder
     */
    MutableShogiContext withVariable(String path, Object value);

    /**
     * Creates an empty context builder with an immediate executor.
     *
     * @return a new mutable context builder
     */
    static MutableShogiContext create() {
        return new ShogiContextImpl();
    }

    /**
     * Creates an empty context builder.
     *
     * @param executor the executor to run effects on
     * @return a new mutable context builder
     * @see EffectExecutor#simulated()
     * @see EffectExecutor#immediate()
     * @see EffectExecutor#deferred()
     */
    static MutableShogiContext create(EffectExecutor executor) {
        return new ShogiContextImpl(executor);
    }

    /**
     * Creates a context builder from a known input object type.
     *
     * @param input object used to seed context fields
     * @return a mutable context containing values inferred from {@code input}
     */
    static MutableShogiContext of(Object input) {
        if (input instanceof MutableShogiContext mutableContext) {
            return mutableContext;
        }

        if (input instanceof ShogiContext context) {
            return extend(context);
        }

        final var context = create(new ImmediateEffectExecutor());
        if (input instanceof Entity entity) {
            context.withEntity(entity);
            context.withBlockPos(entity.blockPosition());
        } else if (input instanceof Level level) {
            context.withLevel(level);
        }

        if (input instanceof BlockPos blockPos) {
            context.withBlockPos(blockPos);
        } else if (input instanceof Vec3i vec3i) {
            context.withBlockPos(new BlockPos(vec3i));
        } else if (input instanceof Position position) {
            context.withBlockPos(BlockPos.containing(position));
        }

        if (input instanceof ItemStack itemStack) {
            context.withItemStack(itemStack);
        } else if (input instanceof LivingEntity livingEntity) {
            context.withItemStack(livingEntity.getMainHandItem());
        }

        if (input instanceof BlockState blockState) {
            context.withBlockState(blockState);
        }

        return context;
    }

    /**
     * Creates a context builder that extends the provided parent.
     *
     * @param parent parent context to extend
     * @return an extending mutable context
     */
    static MutableShogiContext extend(ShogiContext parent) {
        return new ShogiContextImpl(parent);
    }
}
