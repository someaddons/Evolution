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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;

import java.util.Map;

/**
 * Trait which increase the dropped loot
 */
public class Armored implements ITraitType
{
    public static final ResourceLocation      ID         = ResourceLocation.fromNamespaceAndPath(Evolution.MOD_ID, "armored");
    final               TagKey<EntityType<?>> compatible = TagKey.create(Registries.ENTITY_TYPE, ID);

    /**
     * The attribute modifiers by level
     */
    private Int2ObjectOpenHashMap<AttributeModifier> modifiers = new Int2ObjectOpenHashMap<>();

    /**
     * The maximum level
     */
    private int                                      maxLevel  = 2;

    /**
     * Appareance chance
     */
    private int chance = 6;

    public Armored()
    {
        modifiers.put(1, new AttributeModifier(ResourceLocation.fromNamespaceAndPath(Evolution.MOD_ID,"armored"), 4, AttributeModifier.Operation.ADD_VALUE));
        modifiers.put(2, new AttributeModifier(ResourceLocation.fromNamespaceAndPath(Evolution.MOD_ID,"armored"), 8, AttributeModifier.Operation.ADD_VALUE));
    }

    @Override
    public void loadFromJson(final JsonObject data)
    {
        maxLevel = data.get("maxlevel").getAsInt();
        chance = data.get("weight").getAsInt();
        JsonObject levels = data.get("armorlevels").getAsJsonObject();
        modifiers.clear();
        for (final Map.Entry<String, JsonElement> level : levels.entrySet())
        {
            modifiers.put(Integer.valueOf(level.getKey()), new AttributeModifier(ResourceLocation.fromNamespaceAndPath(Evolution.MOD_ID,"armored"), level.getValue().getAsDouble(), AttributeModifier.Operation.ADD_VALUE));
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
    public <T extends Mob & ITraitEntity> void onAddTo(T entity, ITrait iTrait)
    {
        for (final var modifier : modifiers.values())
        {
            entity.getAttribute(Attributes.ARMOR).removeModifier(modifier);
        }
        entity.getAttribute(Attributes.ARMOR).addTransientModifier(modifiers.get(iTrait.getLevel()));
    }

    @Override
    public Component getDisplayName(ITrait trait)
    {
        return Component.translatable("evolution.trait.armored", ITraitType.levelToString(trait.getLevel()));
    }
}
