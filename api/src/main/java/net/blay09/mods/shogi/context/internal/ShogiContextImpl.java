package net.blay09.mods.shogi.context.internal;

import net.blay09.mods.shogi.context.ShogiContext;
import net.blay09.mods.shogi.context.MutableShogiContext;
import net.blay09.mods.shogi.context.executor.EffectExecutor;
import net.blay09.mods.shogi.context.executor.internal.ImmediateEffectExecutor;
import net.blay09.mods.shogi.context.executor.internal.SimulatedEffectExecutor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ShogiContextImpl implements MutableShogiContext {

    private final EffectExecutor executor;
    private final @Nullable ShogiContext parent;
    private @Nullable Level level;
    private @Nullable Entity entity;
    private @Nullable BlockPos blockPos;
    private @Nullable BlockState blockState;
    private @Nullable ItemStack itemStack;
    private @Nullable Map<String, Object> variables;

    public ShogiContextImpl() {
        this(null, null);
    }

    public ShogiContextImpl(EffectExecutor executor) {
        this(null, executor);
    }

    public ShogiContextImpl(@Nullable ShogiContext parent) {
        this(parent, parent != null ? parent.executor() : null);
    }

    public ShogiContextImpl(@Nullable ShogiContext parent, @Nullable EffectExecutor executor) {
        this.parent = parent;
        this.executor = executor != null ? executor : new ImmediateEffectExecutor();
    }

    @Override
    public EffectExecutor executor() {
        return executor;
    }

    @Override
    public @Nullable Level level() {
        if (level == null && parent != null) {
            return parent.level();
        }
        return level;
    }

    @Override
    public MutableShogiContext withLevel(@Nullable Level level) {
        this.level = level;
        return this;
    }

    @Override
    public @Nullable Entity entity() {
        if (entity == null && parent != null) {
            return parent.entity();
        }
        return entity;
    }

    @Override
    public MutableShogiContext withEntity(@Nullable Entity entity) {
        this.entity = entity;
        if (entity != null && level == null) {
            level = entity.level();
        }
        return this;
    }

    @Override
    public @Nullable BlockPos blockPos() {
        if (blockPos == null && entity instanceof Entity currentEntity) {
            return currentEntity.blockPosition();
        }
        if (blockPos == null && parent != null) {
            return parent.blockPos();
        }
        return blockPos;
    }

    @Override
    public MutableShogiContext withBlockPos(@Nullable BlockPos blockPos) {
        this.blockPos = blockPos;
        return this;
    }

    @Override
    public @Nullable BlockState blockState() {
        if (blockState == null && level() != null && blockPos() != null) {
            return level().getBlockState(blockPos());
        }
        if (blockState == null && parent != null) {
            return parent.blockState();
        }
        return blockState;
    }

    @Override
    public MutableShogiContext withBlockState(@Nullable BlockState blockState) {
        this.blockState = blockState;
        return this;
    }

    @Override
    public ItemStack itemStack() {
        if (itemStack != null) {
            return itemStack;
        }
        if (entity() instanceof LivingEntity livingEntity) {
            return livingEntity.getMainHandItem();
        }
        if (parent != null) {
            return parent.itemStack();
        }
        return ItemStack.EMPTY;
    }

    @Override
    public MutableShogiContext withItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
        return this;
    }

    @Override
    public Optional<Object> getVariable(String path) {
        final var value = variables != null ? variables.get(path) : null;
        if (value != null) {
            return Optional.of(value);
        }
        if (parent != null) {
            return parent.getVariable(path);
        }
        return Optional.empty();
    }

    @Override
    public MutableShogiContext withVariable(String path, Object value) {
        if (variables == null) {
            variables = new HashMap<>();
        }
        variables.put(path, value);
        return this;
    }

    @Override
    public MutableShogiContext fork() {
        return new ShogiContextImpl(this);
    }
}
