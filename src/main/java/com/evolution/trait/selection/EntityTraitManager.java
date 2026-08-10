package com.evolution.trait.selection;

import com.evolution.Evolution;
import com.evolution.config.CommonConfiguration;
import com.evolution.trait.ITrait;
import com.evolution.trait.Traits;
import com.evolution.trait.storage.ITraitEntity;
import com.evolution.trait.storage.RegionData;
import com.evolution.trait.storage.TraitRegionLevelData;
import com.evolution.trait.type.ITraitType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.entity.animal.horse.Donkey;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.entity.animal.horse.Mule;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

import java.util.List;
import java.util.Map;

/**
 * Manages entity spawns, rolling traits and entity events
 */
public class EntityTraitManager
{
    /**
     * Applies a bonus to looting level calculations(like the enchantment)
     *
     * @param entity
     * @return
     */
    public static int getLootingLevelBonus(final LivingEntity entity)
    {
        if (entity instanceof ITraitEntity traitEntity && traitEntity.hasTrait(Traits.extraLoot))
        {
            return traitEntity.getTraits().get(Traits.extraLoot).getLevel();
        }

        return 0;
    }

    /**
     * Handles entity spawning
     *
     * @param entity
     */
    public static void onEntityAdd(final Mob entity)
    {
        if (entity instanceof ITraitEntity traitEntity && !traitEntity.hasRolledTraits())
        {
            assignTraitsFromArea((Mob & ITraitEntity) entity, 1 + Evolution.rand.nextInt(CommonConfiguration.config.getCommonConfig().maxTraits), entity.blockPosition());
        }
    }

    /**
     * Assigns traits from an area
     * // TODO: Look into temporarily caching the calculation
     *
     * @param entity
     * @param spawnPos
     */
    private static <T extends Mob & ITraitEntity> void assignTraitsFromArea(final T entity, int maxTraits, final BlockPos spawnPos)
    {
        entity.rolledTraits();
        final TraitRegionLevelData levelData = TraitRegionLevelData.getTraitRegionLevelData((ServerLevel) entity.level());

        RegionData center = levelData.getForPos(spawnPos);
        RegionData left = levelData.getForPos(spawnPos.offset(-64, 0, 0));
        RegionData right = levelData.getForPos(spawnPos.offset(64, 0, 0));
        RegionData top = levelData.getForPos(spawnPos.offset(0, 0, 64));
        RegionData bottom = levelData.getForPos(spawnPos.offset(0, 0, -64));

        final List<ITraitType> entityTraitTypes = Traits.getTraitTypesFor(entity.getType(), entity.level());
        if (entityTraitTypes.isEmpty())
        {
            return;
        }

        double[] traitWeights = new double[entityTraitTypes.size() + 1];

        fillTraitWeightsFromRegion(traitWeights, entityTraitTypes, center, 1.0);
        fillTraitWeightsFromRegion(traitWeights, entityTraitTypes, left, Math.min(0.3, 0.05 * (left.samples / 100.0)));
        fillTraitWeightsFromRegion(traitWeights, entityTraitTypes, right, Math.min(0.3, 0.05 * (right.samples / 100.0)));
        fillTraitWeightsFromRegion(traitWeights, entityTraitTypes, top, Math.min(0.3, 0.05 * (top.samples / 100.0)));
        fillTraitWeightsFromRegion(traitWeights, entityTraitTypes, bottom, Math.min(0.3, 0.05 * (bottom.samples / 100.0)));

        for (int i = 0; i < entityTraitTypes.size(); i++)
        {
            final ITraitType type = entityTraitTypes.get(i);
            traitWeights[i] *= type.getChance();
        }

        // Neutral weight for "no trait", 1.0 base weight times 10 since most traits get boost by chance of min ~5
        traitWeights[traitWeights.length - 1] = CommonConfiguration.config.getCommonConfig().noTraitWeight * entityTraitTypes.size();

        double totalWeight = 0.0;
        for (final double w : traitWeights)
        {
            totalWeight += w;
        }

        if (totalWeight <= 0)
        {
            return;
        }

        // Normalize for nicer numbers
        for (int i = 0; i < traitWeights.length; i++)
        {
            traitWeights[i] /= totalWeight;
        }

        boolean mutant = entity.hasTrait(Traits.mutant);
        int traitsUsed = 0;
        for (int i = 0; i < maxTraits * 2; i++)
        {
            if (traitsUsed == maxTraits)
            {
                break;
            }

            int selected;
            if (mutant)
            {
                selected = Evolution.rand.nextInt(traitWeights.length);

                for (int j = 0; j < 50 && selected != traitWeights.length - 1 && Traits.mutant.isBlackListed(entityTraitTypes.get(selected)); j++)
                {
                    selected = Evolution.rand.nextInt(traitWeights.length);
                }
            }
            else
            {
                selected = weightedPick(traitWeights);
            }

            if (selected < 0)
            {
                break;
            }

            if (selected == traitWeights.length - 1)
            {
                traitsUsed++;
                continue;
            }

            final ITraitType type = entityTraitTypes.get(selected);
            if (entity.addTraitType(type))
            {
                if (type == Traits.mutant)
                {
                    mutant = true;
                    maxTraits++;
                }

                int level = entity.getTraits().get(type).getLevel() - 1;
                double consumeChance = Math.max(0, 1.0 - level * 0.25);
                if (Evolution.rand.nextDouble() < consumeChance)
                {
                    traitsUsed++;
                }
            }
        }
    }

    private static int weightedPick(double[] weights)
    {
        double roll = Evolution.rand.nextDouble();
        double cumulative = 0.0;
        for (int i = 0; i < weights.length; i++)
        {
            cumulative += weights[i];
            if (roll < cumulative)
            {
                return i;
            }
        }
        return -1;
    }

    /**
     * Fill the weight map from the different regions
     *
     * @param traitWeights
     * @param traitTypes
     * @param region
     * @param factor
     */
    private static void fillTraitWeightsFromRegion(final double[] traitWeights, final List<ITraitType> traitTypes, final RegionData region, final double factor)
    {
        int totalSpawns = 0;
        for (final ITraitType type : traitTypes)
        {
            totalSpawns += region.getTraitDataFor(type).getSpawnCount();
        }

        for (int i = 0; i < traitTypes.size(); i++)
        {
            final ITraitType type = traitTypes.get(i);
            traitWeights[i] = traitWeights[i] + (factor * region.getTraitDataFor(type).computeTraitWeight(totalSpawns, traitTypes.size()));
        }
    }

    /**
     * Animal breeding
     *
     * @param parentA
     * @param parentB
     * @param child
     * @param <T>
     * @return
     */
    public static <T extends Mob & ITraitEntity> AgeableMob onAnimalBreed(T parentA, T parentB, T child)
    {
        Map<ITraitType, ITrait> traitsA = parentA.getTraits();
        Map<ITraitType, ITrait> traitsB = parentB.getTraits();

        List<ITraitType> possibleTraits = Traits.getTraitTypesFor(child.getType(), child.level());

        if (possibleTraits.isEmpty())
        {
            return (AgeableMob) child;
        }

        if (parentA.hasTrait(Traits.mutant) || parentB.hasTrait(Traits.mutant))
        {
            // Mutation
            // Sheep children get random colors
            if (parentA instanceof Sheep && child instanceof Sheep childSheep)
            {
                childSheep.setColor(DyeColor.values()[Evolution.rand.nextInt(DyeColor.values().length)]);
            }

            // Axolotl children get random colors
            if (parentA instanceof Axolotl && child instanceof Axolotl childAxolotl)
            {
                childAxolotl.setVariant(Axolotl.Variant.values()[Evolution.rand.nextInt(Axolotl.Variant.values().length)]);
            }

            // Cow children can get replaced with shroom cows
            if (parentA instanceof Cow && child instanceof Cow && Evolution.rand.nextInt(100) < 30)
            {
                final AgeableMob shroomCow = EntityType.MOOSHROOM.create(parentA.level());
                shroomCow.setBaby(true);
                for (final var entry : child.getTraits().entrySet())
                {
                    if (entry.getKey().isCompatibleEntityType(shroomCow.getType(), parentA.level()))
                    {
                        ((ITraitEntity) shroomCow).getTraits().put(entry.getKey(), entry.getValue());
                    }
                }

                child = (T) shroomCow;
            }

            // Horse children can get replaced with skeleton horse
            if ((parentA instanceof Horse || parentA instanceof Donkey || parentA instanceof Mule) && Evolution.rand.nextInt(100) < 30)
            {
                final AgeableMob mutantHorse = (Evolution.rand.nextBoolean() ? EntityType.ZOMBIE_HORSE : EntityType.SKELETON_HORSE).create(parentA.level());
                mutantHorse.setBaby(true);
                for (final var entry : child.getTraits().entrySet())
                {
                    if (entry.getKey().isCompatibleEntityType(mutantHorse.getType(), parentA.level()))
                    {
                        ((ITraitEntity) mutantHorse).getTraits().put(entry.getKey(), entry.getValue());
                    }
                }

                child = (T) mutantHorse;
            }

            // Frog children get random color
            if (parentA instanceof Frog parentFrog && child instanceof Frog childFrog && Evolution.rand.nextInt(100) < 30)
            {
                if (parentFrog.getVariant() == FrogVariant.COLD)
                {
                    childFrog.setVariant(FrogVariant.TEMPERATE);
                }
                if (parentFrog.getVariant() == FrogVariant.TEMPERATE)
                {
                    childFrog.setVariant(FrogVariant.WARM);
                }
                if (parentFrog.getVariant() == FrogVariant.WARM)
                {
                    childFrog.setVariant(FrogVariant.COLD);
                }
            }

            // Rabbit children are angry
            if (parentA instanceof Rabbit && child instanceof Rabbit rabbitChild)
            {
                rabbitChild.setVariant(Rabbit.Variant.EVIL);
            }

            // Bee children are angry
            if (parentA instanceof Bee && child instanceof Bee beeChild)
            {
                beeChild.setRemainingPersistentAngerTime(Integer.MAX_VALUE);
            }

            // Cat children random color
            if (parentA instanceof Cat && child instanceof Cat catChild)
            {
                var catVariantRegistry = parentA.level().registryAccess().registry(Registries.CAT_VARIANT).get();
                Object[] variantIDs = catVariantRegistry.keySet().toArray();
                catChild.setVariant(catVariantRegistry.get((ResourceLocation) variantIDs[Evolution.rand.nextInt(variantIDs.length)]));
            }

            // Fox children random color
            if (parentA instanceof Fox && child instanceof Fox foxChild)
            {
                foxChild.setVariant(Fox.Type.values()[Evolution.rand.nextInt(Fox.Type.values().length)]);
            }

            // Llama children random color
            if (parentA instanceof Llama && child instanceof Llama llamaChild)
            {
                llamaChild.setVariant(Llama.Variant.values()[Evolution.rand.nextInt(Llama.Variant.values().length)]);
            }

            // Villagers can breed witches/husks/zombievillagers
            if (parentA instanceof Villager && child instanceof Villager && Evolution.rand.nextInt(100) < 30)
            {
                final EntityType replacement = Evolution.rand.nextBoolean() ? EntityType.WITCH : (Evolution.rand.nextBoolean() ? EntityType.HUSK : EntityType.ZOMBIE_VILLAGER);
                final Mob witch = (Mob) replacement.create(parentA.level());
                for (final var entry : child.getTraits().entrySet())
                {
                    if (entry.getKey().isCompatibleEntityType(witch.getType(), parentA.level()))
                    {
                        ((ITraitEntity) witch).getTraits().put(entry.getKey(), entry.getValue());
                    }
                }

                witch.moveTo(parentA.getX(), parentA.getY(), parentA.getZ());
                witch.finalizeSpawn((ServerLevelAccessor) parentA.level(), parentA.level().getCurrentDifficultyAt(parentA.blockPosition()), MobSpawnType.BREEDING, null, null);
                parentA.level().addFreshEntity(witch);
                child = (T) witch;
            }
        }

        for (ITraitType type : possibleTraits)
        {
            ITrait traitA = traitsA.get(type);
            ITrait traitB = traitsB.get(type);

            // Both parents have it -> guaranteed inheritance (or averaged level)
            if (traitA != null && traitB != null)
            {
                int newLevel = Math.max(1, (traitA.getLevel() + traitB.getLevel()) / 2);
                if (Evolution.rand.nextDouble() < 0.99)
                { // 90% inherit chance
                    if (child.addTraitType(type))
                    {
                        child.getTraits().get(type).setLevel(newLevel);
                    }
                }
            }
            // One parent has it -> partial inheritance chance
            else if (traitA != null || traitB != null)
            {
                if (Evolution.rand.nextDouble() < 0.6)
                {
                    child.addTraitType(type);
                    if (child.addTraitType(type))
                    {
                        child.getTraits().get(type).setLevel((traitA != null ? traitA.getLevel() : traitB.getLevel()));
                    }
                }
            }
        }

        // Mutation: occasionally gain a random new trait
        if (child.getTraits().size() < 1 + Evolution.rand.nextInt(CommonConfiguration.config.getCommonConfig().maxTraits))
        {
            if (Evolution.rand.nextDouble() < 0.2 || child.hasTrait(Traits.mutant))
            {
                assignTraitsFromArea(child, 1, parentA.blockPosition());
            }
        }

        child.rolledTraits();
        if (child instanceof AgeableMob)
        {
            // Randomly add eternal youth 5%
            if (Evolution.rand.nextInt(100) <= Traits.eternalYouth.getActualChance())
            {
                if (!CommonConfiguration.config.getCommonConfig().traitBlackList.contains(Traits.eternalYouth.getID().toString()))
                {
                    child.addTraitType(Traits.eternalYouth);
                    ((AgeableMob) child).setAge(Integer.MIN_VALUE);
                }
            }
            return (AgeableMob) child;
        }

        return null;
    }

    /**
     * Record stats on entity despawn
     *
     * @param traitEntity
     * @param <T>
     */
    public static <T extends Mob & ITraitEntity> void onEntityDespawn(final T traitEntity)
    {
        if (!(traitEntity instanceof Enemy))
        {
            return;
        }

        if (traitEntity.getFirstPlayerContact() == 0)
        {
            return;
        }

        if (traitEntity.getTraits().isEmpty())
        {
            return;
        }

        final double fitness = traitEntity.getCombatSuccess() + Math.max(0, getSurvivalTimeScore(traitEntity));

        RegionData regionData = TraitRegionLevelData.getTraitRegionLevelData((ServerLevel) traitEntity.level()).getForPos(traitEntity.blockPosition());
        for (final ITraitType traitType : traitEntity.getTraits().keySet())
        {
            regionData.getTraitDataFor(traitType).recordFitness(fitness);
        }
        regionData.incSampleSize();
        TraitRegionLevelData.getTraitRegionLevelData((ServerLevel) traitEntity.level()).setDirty();
    }

    /**
     * Record stats on entity death
     *
     * @param traitEntity
     * @param source
     * @param <T>
     */
    public static <T extends Mob & ITraitEntity> void onEntityDeath(final T traitEntity, final DamageSource source)
    {
        if (traitEntity.getTraits().isEmpty())
        {
            return;
        }

        if (traitEntity.hasTrait(Traits.explosive))
        {
            traitEntity.level().explode(traitEntity, traitEntity.getX(), traitEntity.getY(), traitEntity.getZ(), 4, Level.ExplosionInteraction.MOB);
        }

        if (traitEntity.isPersistenceRequired() && !(traitEntity instanceof Enemy))
        {
            // Do not record statistics of animals/villagers as they reproduce via breeding
            return;
        }

        final double fitness = traitEntity.getCombatSuccess() + getSurvivalTimeScore(traitEntity) + getEnvDeathScore(traitEntity, source);

        RegionData regionData = TraitRegionLevelData.getTraitRegionLevelData((ServerLevel) traitEntity.level()).getForPos(traitEntity.blockPosition());
        for (final ITraitType traitType : traitEntity.getTraits().keySet())
        {
            regionData.getTraitDataFor(traitType).recordFitness(fitness);
        }
        regionData.incSampleSize();
        TraitRegionLevelData.getTraitRegionLevelData((ServerLevel) traitEntity.level()).setDirty();

    }

    /**
     * Get the deathscore for dying in an environment
     *
     * @param <T>
     * @param traitEntity
     * @param source
     * @return
     */
    private static <T extends Mob & ITraitEntity> double getEnvDeathScore(final T traitEntity, final DamageSource source)
    {
        if (source.getEntity() == null)
        {
            return -0.5;
        }
        else if (source.getEntity() instanceof ServerPlayer)
        {
            return -1;
        }

        return 0;
    }

    private static <T extends Mob & ITraitEntity> double getSurvivalTimeScore(final T traitEntity)
    {
        final long firstPlayerContact = traitEntity.getFirstPlayerContact();
        if (firstPlayerContact == 0)
        {
            if (traitEntity.tickCount > 60 * 20)
            {
                return 0.5;
            }
            return 0;
        }

        final long ticks = traitEntity.tickCount - firstPlayerContact;
        if (ticks < 10 * 20)
        {
            return -1;
        }
        else if (ticks < 30 * 20)
        {
            return 0;
        }
        else if (ticks < 60 * 20)
        {
            return 1;
        }
        else
        {
            return 2;
        }
    }
}
