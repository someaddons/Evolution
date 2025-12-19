package com.evolution.trait.type;

import com.evolution.Evolution;
import com.evolution.trait.ITrait;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.Level;

import java.util.Map;

/**
 * Trait which increase the dropped loot
 */
public class EnvironmentalAdaption implements ITraitType
{
    public static final ResourceLocation      ID         = new ResourceLocation(Evolution.MOD_ID, "envadapt");
    final               TagKey<EntityType<?>> compatible = TagKey.create(Registries.ENTITY_TYPE, ID);

    /**
     * The maximum level
     */
    private int                                      maxLevel  = 2;

    /**
     * Levels and their respective max damage taken
     */
    private Int2IntOpenHashMap levelPercentMap = new Int2IntOpenHashMap();

    /**
     * Appareance chance
     */
    private int chance = 7;

    public EnvironmentalAdaption()
    {

    }

    @Override
    public void loadFromJson(final JsonObject data)
    {
        maxLevel = data.get("maxlevel").getAsInt();
        chance = data.get("weight").getAsInt();
        JsonObject levels = data.get("envhppercentlostlevels").getAsJsonObject();
        levelPercentMap.clear();
        for (final Map.Entry<String, JsonElement> level : levels.entrySet())
        {
            levelPercentMap.put(Integer.valueOf(level.getKey()).intValue(), level.getValue().getAsInt());
        }
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

    @Override
    public int maxLevel()
    {
        return maxLevel;
    }

    @Override
    public Component getDisplayName(ITrait trait)
    {
        return Component.translatable("evolution.trait.envadapt", ITraitType.levelToString(trait.getLevel()));
    }


    /**
     *
     * @param level
     * @return
     */
    public float getMaxEnvHpLostOnHit(final int level)
    {
        return levelPercentMap.get(level) / 100.0f;
    }
}
