package com.evolution.trait.type;

import com.evolution.Evolution;
import com.evolution.trait.ITrait;
import com.evolution.trait.storage.ITraitEntity;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;

import java.util.Map;

/**
 * Trait which increase limits damage taken to a maximum per hit
 */
public class Enduring implements ITraitType
{
    public static final ResourceLocation      ID         = new ResourceLocation(Evolution.MOD_ID, "enduring");
    final               TagKey<EntityType<?>> compatible = TagKey.create(Registries.ENTITY_TYPE, ID);

    /**
     * The maximum level
     */
    private int                                      maxLevel  = 3;

    /**
     * Levels and their respective max damage taken
     */
    private Int2IntOpenHashMap levelPercentMap = new Int2IntOpenHashMap();

    /**
     * Appareance chance
     */
    private int chance = 5;

    public Enduring()
    {
        levelPercentMap.put(1, 35);
        levelPercentMap.put(2, 25);
        levelPercentMap.put(3, 15);
    }

    @Override
    public void loadFromJson(final JsonObject data)
    {
        maxLevel = data.get("maxlevel").getAsInt();
        chance = data.get("weight").getAsInt();
        JsonObject levels = data.get("maxhppercentlostlevels").getAsJsonObject();
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

        final Entity entity = type.create(level);
        if (entity instanceof Mob mob && mob.getAttribute(Attributes.ARMOR) != null && !(entity instanceof Animal))
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
        return Component.translatable("evolution.trait.enduring", ITraitType.levelToString(trait.getLevel()));
    }

    /**
     *
     * @param level
     * @return
     */
    public float getMaxHpLostOnHit(final int level)
    {
        return levelPercentMap.get(level) / 100.0f;
    }
}
