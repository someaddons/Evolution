package com.evolution.trait.type;

import com.evolution.mixin.EntityTraitStorageMixin;
import com.evolution.trait.DefaultTraitData;
import com.evolution.trait.ITrait;
import com.evolution.trait.storage.ITraitEntity;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;

/**
 * Trait types handle deserialization and meta data for a type of trait.
 */
public interface ITraitType
{
    /**
     * Creates a new instance for a trait from the given compound data
     *
     * @param tag
     * @return new trait instance
     */
    default public ITrait deserialize(final CompoundTag tag)
    {
        return createTraitData().read(tag);
    }

    /**
     * Serializes the given trait
     *
     * @param trait
     * @return
     */
    public default CompoundTag serialize(final ITrait trait)
    {
        if (trait.getType() != this)
        {
            throw new RuntimeException("Serialize called with wrong trait");
        }

        CompoundTag tag = new CompoundTag();
        tag.put(getID().toString(), trait.serializeNbt());
        return tag;
    }

    /**
     * Loads type data from json
     *
     * @param data
     */
    public void loadFromJson(final JsonObject data);

    /**
     * ID of the trait type
     *
     * @return
     */
    public ResourceLocation getID();

    /**
     * The chance for this trait to appear, in percent
     *
     * @return chance
     */
    public int getChance();

    /**
     * Check if this trait is compatible with this entity type
     *
     * @return
     */
    boolean isCompatibleEntityType(EntityType type, final Level level);

    /**
     * Maximum allowed level of this trait
     *
     * @return
     */
    int maxLevel();

    /**
     * Creates a data tracking instance which contains extra data for the trait, e.g. levels
     *
     * @return
     */
    public default ITrait createTraitData()
    {
        return new DefaultTraitData(this);
    }

    /**
     * Returns the display name
     * @return
     */
    public Component getDisplayName(final ITrait trait);

    /**
     * Helper to convert a string level to roman numbers
     * @param traitLevel
     * @return
     */
    public static String levelToString(final int traitLevel)
    {
        switch (traitLevel)
        {
            case 1: return "";
            case 2: return "II";
            case 3: return "III";
            case 4: return "IV";
            case 5: return "V";
            case 6: return "VI";
            case 7: return "VII";
            case 8: return "VIII";
            case 9: return "IX";
            case 10: return "X";
        }

        return "level:"+traitLevel+" not implemented";
    }

    /**
     * Callback on addding a trait to an entity
     * @param iTrait
     * @param <T>
     */
    default  <T extends Mob & ITraitEntity> void onAddTo(T entity, ITrait iTrait){}
}
