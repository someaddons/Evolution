package com.evolution.trait.storage;

import com.evolution.trait.type.ITraitType;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * Saved data alongside levels for trait history and statistics
 */
public class TraitRegionLevelData extends SavedData
{
    public static final String ID = "evolutionmoddata";

    /**
     * Chunk section position to structure data matching map, multiple positions can point to the same structure
     */
    private final Long2ObjectOpenHashMap<RegionData> traitRegionData = new Long2ObjectOpenHashMap<>();

    /**
     * Time elapsed on the world in seconds
     */
    private long elapsedTime = 5;

    /**
     * Gets the structure data for a given pos, does trigger updates to that data
     */
    public static TraitStats getTraitData(final ServerLevel level, final BlockPos pos, final ITraitType traitType)
    {
        final TraitRegionLevelData respawnData = level.getDataStorage().computeIfAbsent(TraitRegionLevelData::load, TraitRegionLevelData::new, TraitRegionLevelData.ID);
        return respawnData.getForPos(pos).getTraitDataFor(traitType);
    }

    /**
     * Gets the structure data for a given pos, does trigger updates to that data
     */
    public static TraitRegionLevelData getTraitRegionLevelData(final ServerLevel level)
    {
        final TraitRegionLevelData respawnData = level.getDataStorage().computeIfAbsent(TraitRegionLevelData::load, TraitRegionLevelData::new, TraitRegionLevelData.ID);
        return respawnData;
    }

    private static TraitRegionLevelData load(CompoundTag compoundTag)
    {
        TraitRegionLevelData data = new TraitRegionLevelData();
        data.read(compoundTag);
        return data;
    }

    public TraitRegionLevelData()
    {

    }

    /**
     * Converts blockpos to region long, similar to chunkpos. 64x64 regions unlike chunks 16x16
     *
     * @param pos
     * @return
     */
    private static long getRegionFor(final BlockPos pos)
    {
        return (long) (pos.getX() >> 6) & 4294967295L | ((long) (pos.getY() >> 6) & 4294967295L) << 32;
    }

    /**
     * Get the structure data for a given pos when a trigger update happened
     *
     * @param pos
     * @return
     */
    public RegionData getForPos(final BlockPos pos)
    {
        RegionData regionData = traitRegionData.get(getRegionFor(pos));
        if (regionData == null)
        {
            regionData = new RegionData(getRegionFor(pos));
            traitRegionData.put(getRegionFor(pos), regionData);
        }

        return regionData;
    }

    /**
     * Increases internal level time point
     *
     * @param seconds
     */
    public void increaseTime(final int seconds)
    {
        elapsedTime += seconds;
        setDirty(true);
    }

    /**
     * Get the level time point
     *
     * @return
     */
    public long getLevelTime()
    {
        return elapsedTime;
    }

    @Override
    public CompoundTag save(CompoundTag tag)
    {
        tag.putLong("elapsedTime", elapsedTime);

        ListTag list = new ListTag();
        for (final RegionData data : traitRegionData.values())
        {
            if (data != null && !data.isEmpty())
            {
                list.add(data.serializeNbt());
            }
        }

        tag.put("Regions", list);
        return tag;
    }

    /**
     * Read from nbt
     *
     * @param nbt
     */
    public void read(CompoundTag nbt)
    {
        elapsedTime = nbt.getLong("elapsedTime");
        ListTag list = nbt.getList("Regions", Tag.TAG_COMPOUND);

        for (final Tag tag : list)
        {
            if (tag instanceof CompoundTag)
            {
                final RegionData data = new RegionData((CompoundTag) tag);
                traitRegionData.put(data.position, data);
            }
        }
    }
}
