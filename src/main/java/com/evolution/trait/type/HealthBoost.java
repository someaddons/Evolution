package com.evolution.trait.type;

import com.evolution.Evolution;
import com.evolution.trait.ITrait;
import com.evolution.trait.storage.ITraitEntity;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.Level;

import java.util.Map;

/**
 * Trait which increase the dropped loot
 */
public class HealthBoost implements ITraitType
{
    public static final ResourceLocation      ID         = new ResourceLocation(Evolution.MOD_ID, "healthy");
    final               TagKey<EntityType<?>> compatible = TagKey.create(Registries.ENTITY_TYPE, ID);

    /**
     * The attribute modifiers by level
     */
    private Int2ObjectOpenHashMap<AttributeModifier> modifiers = new Int2ObjectOpenHashMap<>();

    /**
     * The maximum level
     */
    private int                                      maxLevel  = 3;

    /**
     * Appareance chance
     */
    private int chance = 10;

    public HealthBoost()
    {
        modifiers.put(1, new AttributeModifier("evolution_healthboost", 0.2, AttributeModifier.Operation.MULTIPLY_TOTAL));
        modifiers.put(2, new AttributeModifier("evolution_healthboost", 0.5, AttributeModifier.Operation.MULTIPLY_TOTAL));
        modifiers.put(3, new AttributeModifier("evolution_healthboost", 1.5, AttributeModifier.Operation.MULTIPLY_TOTAL));
    }

    @Override
    public void loadFromJson(final JsonObject data)
    {
        maxLevel = data.get("maxlevel").getAsInt();
        chance = data.get("weight").getAsInt();
        JsonObject levels = data.get("healthLevels").getAsJsonObject();
        modifiers.clear();
        for (final Map.Entry<String, JsonElement> level : levels.entrySet())
        {
            modifiers.put(Integer.valueOf(level.getKey()), new AttributeModifier("evolution_healthboost", level.getValue().getAsDouble() / 100.0, AttributeModifier.Operation.MULTIPLY_TOTAL));
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
        if (entity instanceof Enemy || entity instanceof NeutralMob || entity instanceof Animal || entity instanceof Villager)
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
    public <T extends Mob & ITraitEntity> void onAddTo(T entity, ITrait iTrait)
    {
        float healthPercent = entity.getHealth() / entity.getMaxHealth();
        for (final var modifier : modifiers.values())
        {
            entity.getAttribute(Attributes.MAX_HEALTH).removeModifier(modifier);
        }
        entity.getAttribute(Attributes.MAX_HEALTH).addTransientModifier(modifiers.get(iTrait.getLevel()));

        entity.setHealth(entity.getMaxHealth() * healthPercent);
    }

    @Override
    public Component getDisplayName(ITrait trait)
    {
        return Component.translatable("evolution.trait.healthy", ITraitType.levelToString(trait.getLevel()));
    }
}
