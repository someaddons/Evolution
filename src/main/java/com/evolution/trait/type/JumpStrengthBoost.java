package com.evolution.trait.type;

import com.evolution.Evolution;
import com.evolution.trait.ITrait;
import com.evolution.trait.storage.ITraitEntity;
import com.google.gson.JsonElement;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PlayerRideable;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.Level;

/**
 * Trait which increases rideable jump strength
 */
public class JumpStrengthBoost implements ITraitType
{
    public static final ResourceLocation      ID         = new ResourceLocation(Evolution.MOD_ID, "leaper");
    final               TagKey<EntityType<?>> compatible = TagKey.create(Registries.ENTITY_TYPE, ID);

    private static AttributeModifier SPEED_ONE = new AttributeModifier("evolution_leaper", 0.2, AttributeModifier.Operation.MULTIPLY_TOTAL);
    private static AttributeModifier SPEED_TWO = new AttributeModifier("evolution_leaper", 0.35, AttributeModifier.Operation.MULTIPLY_TOTAL);

    /**
     * Appareance chance
     */
    private int chance = 7;

    public JumpStrengthBoost()
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
        if (entity instanceof PlayerRideable && entity instanceof Mob mob && mob.getAttribute(Attributes.JUMP_STRENGTH) != null)
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
    public <T extends Mob & ITraitEntity> void onAddTo(T entity, ITrait iTrait)
    {
        entity.getAttribute(Attributes.JUMP_STRENGTH).removeModifier(SPEED_ONE);
        entity.getAttribute(Attributes.JUMP_STRENGTH).removeModifier(SPEED_TWO);

        if (iTrait.getLevel() == 1)
        {
            entity.getAttribute(Attributes.JUMP_STRENGTH).addTransientModifier(SPEED_ONE);
        }
        if (iTrait.getLevel() == 2)
        {
            entity.getAttribute(Attributes.JUMP_STRENGTH).addTransientModifier(SPEED_TWO);
        }
    }

    @Override
    public Component getDisplayName(ITrait trait)
    {
        return Component.translatable("evolution.trait.leaper", ITraitType.levelToString(trait.getLevel()));
    }
}
