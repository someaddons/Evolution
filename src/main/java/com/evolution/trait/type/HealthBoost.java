package com.evolution.trait.type;

import com.evolution.Evolution;
import com.evolution.trait.ITrait;
import com.evolution.trait.storage.ITraitEntity;
import com.google.gson.JsonElement;
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

/**
 * Trait which increase the dropped loot
 */
public class HealthBoost implements ITraitType
{
    public static final ResourceLocation      ID         = ResourceLocation.fromNamespaceAndPath(Evolution.MOD_ID, "healthy");
    final               TagKey<EntityType<?>> compatible = TagKey.create(Registries.ENTITY_TYPE, ID);

    private static AttributeModifier MAX_HP_MOD_ONE   = new AttributeModifier(ResourceLocation.fromNamespaceAndPath(Evolution.MOD_ID,"healthboost"), 0.2, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    private static AttributeModifier MAX_HP_MOD_TWO   = new AttributeModifier(ResourceLocation.fromNamespaceAndPath(Evolution.MOD_ID,"healthboost"), 0.5, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    private static AttributeModifier MAX_HP_MOD_THREE = new AttributeModifier(ResourceLocation.fromNamespaceAndPath(Evolution.MOD_ID,"healthboost"), 1.5, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

    /**
     * Appareance chance
     */
    private int chance = 10;

    public HealthBoost()
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
        return 3;
    }

    @Override
    public <T extends Mob & ITraitEntity> void onAddTo(T entity, ITrait iTrait)
    {
        float healthPercent = entity.getHealth() / entity.getMaxHealth();
        entity.getAttribute(Attributes.MAX_HEALTH).removeModifier(MAX_HP_MOD_ONE);
        entity.getAttribute(Attributes.MAX_HEALTH).removeModifier(MAX_HP_MOD_TWO);
        entity.getAttribute(Attributes.MAX_HEALTH).removeModifier(MAX_HP_MOD_THREE);

        if (iTrait.getLevel() == 1)
        {
            entity.getAttribute(Attributes.MAX_HEALTH).addTransientModifier(MAX_HP_MOD_ONE);
        }
        if (iTrait.getLevel() == 2)
        {
            entity.getAttribute(Attributes.MAX_HEALTH).addTransientModifier(MAX_HP_MOD_TWO);
        }
        if (iTrait.getLevel() == 3)
        {
            entity.getAttribute(Attributes.MAX_HEALTH).addTransientModifier(MAX_HP_MOD_THREE);
        }

        entity.setHealth(entity.getMaxHealth() * healthPercent);
    }

    @Override
    public Component getDisplayName(ITrait trait)
    {
        return Component.translatable("evolution.trait.healthy", ITraitType.levelToString(trait.getLevel()));
    }
}
