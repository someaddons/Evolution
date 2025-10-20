package com.evolution.trait;

import com.evolution.Evolution;
import com.evolution.config.CommonConfiguration;
import com.evolution.trait.type.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

/**
 * Manages and registers trait types and serialization
 */
public class Traits
{
    /**
     * Registered Trait types
     */
    private static Map<ResourceLocation, ITraitType> registeredTraits = new HashMap<>();

    /**
     * Traits for all types of damages sources as migitation/immunity
     * Magical(attacks ignore armor)
     *
     *
     *
     *
     * TODO: Blacklist config for entities or trait types
     */
    public static ExtraLoot            extraLoot = registerTrait(new ExtraLoot());
    public static ProjectileProtection projectileProtection = registerTrait(new ProjectileProtection());
    public static MeleeProtection meleeProtection = registerTrait(new MeleeProtection());
    public static DamageBoost           damageBoost = registerTrait(new DamageBoost());
    public static EnvironmentalAdaption environmentalAdaption = registerTrait(new EnvironmentalAdaption());
    public static Thorns thorns = registerTrait(new Thorns());
    public static HealthBoost healthBoost = registerTrait(new HealthBoost());
    public static Mutant mutant = registerTrait(new Mutant());
    public static Explosive explosive = registerTrait(new Explosive());
    public static Darkness darkness = registerTrait(new Darkness());
    public static SpeedBoost speedBoost = registerTrait(new SpeedBoost());
    public static Juggernaut juggernaut = registerTrait(new Juggernaut());
    public static Armored armored = registerTrait(new Armored());
    public static Omnivore omnivore = registerTrait(new Omnivore());
    public static Fertile fertile = registerTrait(new Fertile());
    public static JumpStrengthBoost jumpStrengthBoost = registerTrait(new JumpStrengthBoost());
    public static EternalYouth eternalYouth = registerTrait(new EternalYouth());
    public static Generous generous = registerTrait(new Generous());
    public static Magical magical = registerTrait(new Magical());
    public static Enduring enduring = registerTrait(new Enduring());

    /**
     * Registers a new trait type, requires unique ID
     *
     * @param traitType
     * @param <T>
     * @return
     */
    public static <T extends ITraitType> T registerTrait(final T traitType)
    {
        if (traitType.getID() == null)
        {
            throw new RuntimeException("Trait type: "+traitType+" has a null ID!");
        }

        var prev = registeredTraits.put(traitType.getID(), traitType);
        if (prev != null)
        {
            Evolution.LOGGER.warn("Trying to register duplicate trait type with ID:" + traitType.getID());
        }

        return traitType;
    }

    /**
     * Reads all traits from a compound
     *
     * @param tag
     * @return
     */
    public static Set<ITrait> readTraits(final CompoundTag tag)
    {
        Set<ITrait> traits = null;

        for (final ITraitType entry : registeredTraits.values())
        {
            if (tag.contains(entry.getID().toString()))
            {
                final ITrait trait = entry.deserialize((CompoundTag) tag.get(entry.getID().toString()));
                if (traits == null)
                {
                    traits = new HashSet<>();
                }
                traits.add(trait);
            }
        }

        return traits;
    }

    /**
     * Get the trait type by ID
     *
     * @param id
     * @param <T>
     * @return
     */
    public static <T extends ITraitType> T getTraitType(final ResourceLocation id)
    {
        return (T) registeredTraits.get(id);
    }

    /**
     * Entity trait type cache
     */
    private static Map<EntityType, List<ITraitType>> entityTypeITraitTypeCache = new HashMap<>();

    /**
     * Get a list of all applicable traits for a given entity type, cached
     *
     * @param type
     * @param level
     * @return
     */
    public static List<ITraitType> getTraitTypesFor(final EntityType<?> type, final Level level)
    {
        List<ITraitType> types = entityTypeITraitTypeCache.get(type);
        if (types == null)
        {
            types = new ArrayList<>();

            if (!CommonConfiguration.config.getCommonConfig().entityBlackList.contains(ForgeRegistries.ENTITY_TYPES.getKey(type).toString()))
            {
                for (final ITraitType traitType : registeredTraits.values())
                {
                    if (!CommonConfiguration.config.getCommonConfig().traitBlackList.contains(traitType.getID().toString()) && traitType.isCompatibleEntityType(type, level))
                    {
                        types.add(traitType);
                    }
                }
            }

            types = Collections.unmodifiableList(types);
            entityTypeITraitTypeCache.put(type, types);
        }

        return types;
    }
}
