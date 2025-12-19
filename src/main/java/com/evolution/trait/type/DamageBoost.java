package com.evolution.trait.type;

import com.evolution.Evolution;
import com.evolution.trait.ITrait;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.Level;

import java.util.Map;

/**
 * Trait which increase the dropped loot
 */
public class DamageBoost implements ITraitType
{
    public static final ResourceLocation      ID         = new ResourceLocation(Evolution.MOD_ID, "damageboost");
    final               TagKey<EntityType<?>> compatible = TagKey.create(Registries.ENTITY_TYPE, ID);

    /**
     * The maximum level
     */
    private int                                      maxLevel  = 2;

    /**
     * Appareance chance
     */
    private int chance = 6;

    /**
     * Damage increase per level
     */
    private float damageIncreasePerLevel = 0.2f;

    public DamageBoost()
    {

    }

    @Override
    public void loadFromJson(final JsonObject data)
    {
        maxLevel = data.get("maxlevel").getAsInt();
        chance = data.get("weight").getAsInt();
        damageIncreasePerLevel = data.get("damageIncreasePerLevel").getAsFloat();
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

        if (type.create(level) instanceof Enemy)
        {
            return true;
        }

        return false;
    }

    public float damageIncreasePerLevel()
    {
        return damageIncreasePerLevel;
    }

    @Override
    public int maxLevel()
    {
        return maxLevel;
    }

    @Override
    public Component getDisplayName(ITrait trait)
    {
        return Component.translatable("evolution.trait.damageboost", ITraitType.levelToString(trait.getLevel()));
    }
}
