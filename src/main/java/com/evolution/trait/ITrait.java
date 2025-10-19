package com.evolution.trait;

import com.evolution.trait.type.ITraitType;
import net.minecraft.nbt.CompoundTag;

public interface ITrait
{
     public int getLevel();

    /**
     * Get the type of this trait instance
     * @return
     */
    public ITraitType getType();

    /**
     * Write the trait to nbt
     * @return
     */
    CompoundTag serializeNbt();

    /**
     * Reads the trait from data
     *
     * @return
     */
    ITrait read(final CompoundTag compoundTag);

    /**
     * Sets the level to the given
     * @param i
     */
    void setLevel(int newLevel);
}
