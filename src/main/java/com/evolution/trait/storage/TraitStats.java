package com.evolution.trait.storage;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public class TraitStats
{
    /**
     * Amount of samples before averaging
     */
    private static final int   SAMPLE_SIZE = 20;
    private static final float MIN_SPAWNS  = 10;

    public final ResourceLocation traitID;

    /**
     * How many mobs with the trait were spawned and did not despawn before 60 seconds
     */
    private int spawnCount = 0;

    /**
     * Current tracking of fitness samples, on 20 it gets added to the average
     */
    private int fitnessSamples = 0;
    private double totalFitness   = 0;

    /***
     * The averaged fitness data, starts at 2.0, minimum 0.1
     */
    private double fitnessLongAverage = 2.0;

    public TraitStats(final ResourceLocation traitID)
    {
        this.traitID = traitID;
    }

    /**
     * Calculates a score on how successful a trait has been
     */
    private double calculateFitnessWeight()
    {
        double avgFitness = fitnessSamples > 0 ? (double) totalFitness / fitnessSamples : 0;
        return Math.max(0.1, (fitnessLongAverage * 100 + avgFitness * fitnessSamples) / (100 + fitnessSamples));
    }

    /**
     * Get the weight for spawning
     *
     * @param totalSpawnCount
     * @param totalTraits
     * @return
     */
    public double computeTraitWeight(
        int totalSpawnCount,
        int totalTraits
    )
    {
        // Adjusted spawn ratio (with Bayesian smoothing)
        double adjustedSpawnCount = spawnCount + MIN_SPAWNS / totalTraits;
        double adjustedTotalSpawns = totalSpawnCount + MIN_SPAWNS;
        double spawnRatio = adjustedSpawnCount / adjustedTotalSpawns;

        double avgFitness = calculateFitnessWeight();

        return (spawnRatio * 0.2) + (avgFitness * 0.8);
    }

    public TraitStats(final CompoundTag tag)
    {
        this(ResourceLocation.tryParse(tag.get("id").getAsString()));
        deserializeNbt(tag);
    }

    public CompoundTag serializeNbt()
    {
        CompoundTag tag = new CompoundTag();
        tag.putString("id", traitID.toString());
        tag.putInt("spawnCount", spawnCount);
        tag.putInt("fitnessSamples", fitnessSamples);
        tag.putDouble("totalFitness", totalFitness);
        tag.putDouble("fitnessLongAverage", fitnessLongAverage);
        return tag;
    }

    public void deserializeNbt(final CompoundTag tag)
    {
        spawnCount = tag.getInt("spawnCount");
        fitnessSamples = tag.getInt("fitnessSamples");
        totalFitness = tag.getDouble("totalFitness");
        fitnessLongAverage = tag.getDouble("fitnessLongAverage");
    }

    /**
     * Records combat fitness data, automatically also counts as spawn
     *
     * @param fitness
     */
    public void recordFitness(final double fitness)
    {
        spawnCount++;
        totalFitness += fitness;
        fitnessSamples++;

        if (fitnessSamples > SAMPLE_SIZE)
        {
            double newFitness = (double) totalFitness / fitnessSamples;
            fitnessLongAverage = Math.max(0.1, 0.8 * fitnessLongAverage + 0.2 * newFitness);
            totalFitness = 0;
            fitnessSamples = 0;
        }
    }

    public int getSpawnCount()
    {
        return spawnCount;
    }
}
