package com.evolution.trait.type;

import com.evolution.Evolution;
import com.evolution.trait.ITrait;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.Level;

/**
 * Applies blindness on getting hit
 */
public class Darkness implements ITraitType
{
    public static final ResourceLocation      ID         = ResourceLocation.fromNamespaceAndPath(Evolution.MOD_ID, "darkness");
    final               TagKey<EntityType<?>> compatible = TagKey.create(Registries.ENTITY_TYPE, ID);

    /**
     * The blindness duration in ticks
     */
    private int                                      duration  = 40;

    /**
     * Appareance chance
     */
    private int chance = 2;

    public Darkness()
    {

    }

    @Override
    public void loadFromJson(final JsonObject data)
    {
        chance = data.get("weight").getAsInt();
        duration = data.get("duration").getAsInt();
    }

    @Override
    public ResourceLocation getID()
    {
        return ID;
    }

    @Override
    public int getChance()
    {
        return chance;
    }

    @Override
    public boolean isCompatibleEntityType(final EntityType type, final Level level)
    {
        if (type.is(compatible))
        {
            return true;
        }

        final Entity entity = type.create(level);
        if (entity instanceof Enemy)
        {
            return true;
        }

        return false;
    }

    @Override
    public int maxLevel()
    {
        return 1;
    }

    public int getDuration()
    {
        return duration;
    }

    @Override
    public Component getDisplayName(ITrait trait)
    {
        return Component.translatable("evolution.trait.darkness", ITraitType.levelToString(trait.getLevel()));
    }
}
