package com.evolution.trait;

import com.evolution.trait.type.ITraitType;
import net.minecraft.nbt.CompoundTag;

public class DefaultTraitData implements ITrait
{
    /**
     * Strength level of the trait
     */
    private int level = 1;

    private ITraitType type;

    public DefaultTraitData(final ITraitType type)
    {
        this.type = type;
    }

    @Override
    public int getLevel()
    {
        return Math.min(level, type.maxLevel());
    }

    @Override
    public ITraitType getType()
    {
        return type;
    }

    @Override
    public CompoundTag serializeNbt()
    {
        CompoundTag tag = new CompoundTag();
        tag.putInt("level", level);
        return tag;
    }

    @Override
    public ITrait read(final CompoundTag compoundTag)
    {
        level = compoundTag.getInt("level");
        return this;
    }

    @Override
    public void setLevel(final int newLevel)
    {
        level = newLevel;
    }
}
