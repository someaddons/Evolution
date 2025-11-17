package com.evolution.mixin;

import com.evolution.trait.Traits;
import com.evolution.trait.selection.EntityTraitManager;
import com.evolution.trait.storage.ITraitEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.EnchantedCountIncreaseFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(EnchantedCountIncreaseFunction.class)
public class LootMixin
{
    @ModifyVariable(method = "run", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;getEnchantmentLevel(Lnet/minecraft/core/Holder;Lnet/minecraft/world/entity/LivingEntity;)I", shift = At.Shift.AFTER),
        ordinal = 1)
    private int adjustLevel(int original, ItemStack stack, LootContext context)
    {
        Entity killer = context.getParamOrNull(LootContextParams.ATTACKING_ENTITY);
        Entity dyingEntity = context.getParamOrNull(LootContextParams.THIS_ENTITY);
        if (killer instanceof ServerPlayer && dyingEntity instanceof ITraitEntity traitEntity && traitEntity.hasTrait(Traits.extraLoot))
        {
            return original + EntityTraitManager.getLootingLevelBonus((LivingEntity) dyingEntity);
        }

        return original;
    }
}
