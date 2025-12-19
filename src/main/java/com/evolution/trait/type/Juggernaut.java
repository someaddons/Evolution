package com.evolution.trait.type;

import com.evolution.Evolution;
import com.evolution.trait.ITrait;
import com.evolution.trait.storage.ITraitEntity;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.Level;

/**
 * Trait which increase the dropped loot
 */
public class Juggernaut implements ITraitType
{
    public static final ResourceLocation      ID         = new ResourceLocation(Evolution.MOD_ID, "juggernaut");
    final               TagKey<EntityType<?>> compatible = TagKey.create(Registries.ENTITY_TYPE, ID);

    private static AttributeModifier KNOCKBACK_IMMUNE = new AttributeModifier("evolution_juggernaut", 1, AttributeModifier.Operation.ADDITION);
    private static AttributeModifier SLOWER = new AttributeModifier("evolution_juggernaut_speed", -0.2, AttributeModifier.Operation.MULTIPLY_TOTAL);

    /**
     * The maximum level
     */
    private int                                      maxLevel  = 2;

    /**
     * Appareance chance
     */
    private int chance = 4;

    public Juggernaut()
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
    public <T extends Mob & ITraitEntity> void onAddTo(T entity, ITrait iTrait)
    {
        entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, -1, iTrait.getLevel() - 1, true, true));
        entity.getAttribute(Attributes.KNOCKBACK_RESISTANCE).removeModifier(KNOCKBACK_IMMUNE);
        entity.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(SLOWER);
        entity.getAttribute(Attributes.KNOCKBACK_RESISTANCE).addTransientModifier(KNOCKBACK_IMMUNE);
        entity.getAttribute(Attributes.MOVEMENT_SPEED).addTransientModifier(SLOWER);
    }

    @Override
    public Component getDisplayName(ITrait trait)
    {
        return Component.translatable("evolution.trait.juggernaut", ITraitType.levelToString(trait.getLevel()));
    }
}
