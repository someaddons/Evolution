package com.evolution.trait.storage;

import com.evolution.trait.type.ITraitType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class RegionData
{
    /**
     * Nbt IDs
     */
    private static String NBT_ID = "regiontraitdata";
    private static String SAMPLE_ID = "regiontraitdata";

    /**
     * The unique position of the data
     */
    public final long position;

    /**
     * Map from trait ID to success data
     */
    private Map<ResourceLocation, TraitStats> regionTraitData = new HashMap<>();

    /**
     * Total amount of mobs sampled in the region
     */
    public int samples = 0;

    public RegionData(final long position)
    {
        this.position = position;
    }

    public RegionData(final CompoundTag tag)
    {
        this(tag.getLong("pos"));
        deserializeNbt(tag);
    }

    /**
     * Get the saved data for the given trait type
     *
     * @param traitType
     * @return
     */
    public TraitStats getTraitDataFor(final ITraitType traitType)
    {
        TraitStats stats = regionTraitData.get(traitType.getID());
        if (stats == null)
        {
            stats = new TraitStats(traitType.getID());
            regionTraitData.put(traitType.getID(), stats);
        }

        return stats;
    }

    /**
     * Serializes all trait data
     *
     * @return
     */
    public CompoundTag serializeNbt()
    {
        CompoundTag tag = new CompoundTag();
        tag.putLong("pos", position);
        tag.putInt("samples", samples);
        ListTag list = new ListTag();
        for (TraitStats traitStats : regionTraitData.values())
        {
            list.add(traitStats.serializeNbt());
        }

        tag.put(NBT_ID, list);
        return tag;
    }

    /**
     * Deserializes all trait data
     *
     * @return
     */
    public void deserializeNbt(CompoundTag tag)
    {
        if (tag.contains(NBT_ID))
        {
            if (tag.contains(SAMPLE_ID))
            {
                samples = tag.getInt(SAMPLE_ID);
            }

            ListTag list = tag.getList(NBT_ID, Tag.TAG_COMPOUND);
            for (final Tag traitDataTag : list)
            {
                TraitStats traitStats = new TraitStats((CompoundTag)traitDataTag);
                regionTraitData.put(traitStats.traitID, traitStats);
            }
        }
    }

    /**
     * Checks if empty before saving
     * @return
     */
    public boolean isEmpty()
    {
        for(final TraitStats stats: regionTraitData.values())
        {
            if (stats.getSpawnCount() != 0)
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Increases sample size
     */
    public void incSampleSize()
    {
        samples++;
    }
}
