package com.evolution.trait.type;

import com.evolution.Evolution;
import com.evolution.trait.ITrait;
import com.google.gson.JsonElement;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * Applies blindness on getting hit
 */
public class Darkness implements ITraitType
{
    public static final ResourceLocation      ID         = new ResourceLocation(Evolution.MOD_ID, "darkness");
    final               TagKey<EntityType<?>> compatible = TagKey.create(Registries.ENTITY_TYPE, ID);

    /**
     * Appareance chance
     */
    private int chance = 0;

    public Darkness()
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
        return true;
    }

    @Override
    public int maxLevel()
    {
        return 1;
    }

    @Override
    public Component getDisplayName(ITrait trait)
    {
        return Component.translatable("evolution.trait.darkness", ITraitType.levelToString(trait.getLevel()));
    }
}
