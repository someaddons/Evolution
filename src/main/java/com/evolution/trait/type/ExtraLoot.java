package com.evolution.trait.type;

import com.evolution.Evolution;
import com.evolution.trait.ITrait;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.Level;

import java.util.Map;

/**
 * Trait which increase the dropped loot
 */
public class ExtraLoot implements ITraitType
{
    public static final ResourceLocation      ID         = new ResourceLocation(Evolution.MOD_ID, "extraloot");
    final               TagKey<EntityType<?>> compatible = TagKey.create(Registries.ENTITY_TYPE, ID);

    /**
     * The maximum level
     */
    private int                                      maxLevel  = 2;

    /**
     * Appareance chance
     */
    private int chance = 4;

    public ExtraLoot()
    {

    }

    @Override
    public void loadFromJson(final JsonObject data)
    {
        maxLevel = data.get("maxlevel").getAsInt();
        chance = data.get("weight").getAsInt();
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
        if (type == EntityType.SHEEP)
        {
            return true;
        }

        if (type.is(compatible))
        {
            return true;
        }

        final Entity entity =type.create(level);
        if (entity instanceof Enemy || entity instanceof Animal)
        {
            return true;
        }

        return false;
    }

    @Override
    public int maxLevel()
    {
        return maxLevel;
    }

    @Override
    public Component getDisplayName(ITrait trait)
    {
        return Component.translatable("evolution.trait.extraloot", ITraitType.levelToString(trait.getLevel()));
    }
}
