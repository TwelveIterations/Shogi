package net.blay09.mods.shogi.context;

import net.blay09.mods.shogi.context.executor.aggregate.AggregateKey;
import net.blay09.mods.shogi.context.executor.EffectExecutor;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Evaluation context passed to Shogi effects and value resolution.
 */
public interface ShogiContext {
    /**
     * Returns the aggregate executor backing this context.
     *
     * @return the effect executor
     */
    EffectExecutor executor();

    /**
     * Returns the level in this context.
     *
     * @return the current level, or {@code null}
     */
    @Nullable
    Level level();

    /**
     * Returns the entity in this context.
     *
     * @return the current entity, or {@code null}
     */
    @Nullable
    Entity entity();

    /**
     * Returns the block position in this context.
     *
     * @return the current block position, or {@code null}
     */
    @Nullable
    BlockPos blockPos();

    /**
     * Returns the block state in this context.
     *
     * @return the current block state, or {@code null}
     */
    @Nullable
    BlockState blockState();

    /**
     * Returns the block entity in this context.
     *
     * @return the current block entity, or {@code null}
     */
    @Nullable
    BlockEntity blockEntity();

    /**
     * Returns the item stack in this context.
     *
     * @return the current item stack
     */
    ItemStack itemStack();

    /**
     * Reads a custom variable from this context.
     *
     * @param path variable path
     * @return the variable value if present
     */
    Optional<Object> getVariable(String path);

    /**
     * Returns the entity in this context or throws if absent.
     *
     * @return the current entity
     * @throws MissingContextException if no entity is available
     */
    default Entity requireEntity() {
        final var entity = entity();
        if (entity != null) {
            return entity;
        }
        throw new MissingContextException(this);
    }

    /**
     * Returns the player in this context or throws if absent/not a player.
     *
     * @return the current player
     * @throws MissingContextException if no player is available
     */
    default LivingEntity requireLivingEntity() {
        final var entity = entity();
        if (entity instanceof LivingEntity livingEntity) {
            return livingEntity;
        }
        throw new MissingContextException(this);
    }

    /**
     * Returns the player in this context or throws if absent/not a player.
     *
     * @return the current player
     * @throws MissingContextException if no player is available
     */
    default Player requirePlayer() {
        final var entity = entity();
        if (entity instanceof Player player) {
            return player;
        }
        throw new MissingContextException(this);
    }

    /**
     * Returns the level in this context or throws if absent.
     *
     * @return the current level
     * @throws MissingContextException if no level is available
     */
    default Level requireLevel() {
        final var level = level();
        if (level != null) {
            return level;
        }
        throw new MissingContextException(this);
    }

    /**
     * Returns the block position in this context or throws if absent.
     *
     * @return the current block position
     * @throws MissingContextException if no block position is available
     */
    default BlockPos requireBlockPos() {
        final var blockPos = blockPos();
        if (blockPos != null) {
            return blockPos;
        }
        throw new MissingContextException(this);
    }

    /**
     * Returns the block state in this context or throws if absent.
     *
     * @return the current block state
     * @throws MissingContextException if no block state is available
     */
    default BlockState requireBlockState() {
        final var blockState = blockState();
        if (blockState != null) {
            return blockState;
        }
        throw new MissingContextException(this);
    }

    /**
     * Returns the block entity in this context or throws if absent.
     *
     * @return the current block entity
     * @throws MissingContextException if no block entity is available
     */
    default BlockEntity requireBlockEntity() {
        final var blockEntity = blockEntity();
        if (blockEntity != null) {
            return blockEntity;
        }
        throw new MissingContextException(this);
    }

    /**
     * Creates a child context that starts with this context's values.
     *
     * @return a forked context
     */
    default MutableShogiContext fork() {
        return MutableShogiContext.extend(this);
    }

    /**
     * Aggregates a value in this context's aggregate executor.
     *
     * @param key aggregate key
     * @param initializer initializer used when no value exists yet
     * @param aggregator function that merges or updates the current aggregate
     * @param <T> aggregate value type
     * @return the aggregated value after applying the update
     */
    default <T> T aggregate(AggregateKey<T> key, Supplier<T> initializer, Function<T, T> aggregator) {
        return executor().aggregate(key, initializer, aggregator);
    }

    /**
     * Updates an aggregate value while returning a custom operation result.
     *
     * @param key aggregate key
     * @param initializer initializer used when the key has no value yet
     * @param aggregator function that modifies the aggregate state and returns a step-scoped result
     * @param <T> aggregate value type
     * @param <R> step return value type
     * @return the step-scoped result value returned by {@code aggregator}
     */
    default <T, R> R statefulAggregate(AggregateKey<T> key, Supplier<T> initializer, Function<T, R> aggregator) {
        return executor().statefulAggregate(key, initializer, aggregator);
    }

    /**
     * Consumes an aggregate value if present.
     *
     * @param key aggregate key
     * @param consumer consumer invoked with the current aggregate value
     * @param <T> aggregate value type
     */
    default <T> void consume(AggregateKey<T> key, Consumer<T> consumer) {
        executor().consume(key, consumer);
    }

    /**
     * Schedules a side effect to run on success.
     *
     * @param identifier side effect identifier
     * @param runnable runnable invoked if the evaluation succeeded
     */
    default void execute(Identifier identifier, Runnable runnable) {
        executor().execute(identifier, runnable);
    }
}
