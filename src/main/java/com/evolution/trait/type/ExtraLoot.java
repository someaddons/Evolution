package com.evolution.trait.type;

import com.evolution.Evolution;
import com.evolution.trait.ITrait;
import com.google.gson.JsonElement;
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

/**
 * Trait which increase the dropped loot
 */
public class ExtraLoot implements ITraitType
{
    public static final ResourceLocation      ID         = new ResourceLocation(Evolution.MOD_ID, "extraloot");
    final               TagKey<EntityType<?>> compatible = TagKey.create(Registries.ENTITY_TYPE, ID);

    /**
     * Appareance chance
     */
    private int chance = 4;

    public ExtraLoot()
    {

    }

    @Override
    public void loadFromJson(final JsonElement data)
    {
        // TODO: load data settings from json
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
        return 2;
    }

    @Override
    public Component getDisplayName(ITrait trait)
    {
        return Component.translatable("evolution.trait.extraloot", ITraitType.levelToString(trait.getLevel()));
    }
}
