package com.evolution.mixin;

import com.evolution.trait.Traits;
import com.evolution.trait.storage.ITraitEntity;
import com.evolution.trait.type.Omnivore;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = Animal.class)
public class AnimalFoodMixin
{
    @Redirect(method = "mobInteract", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/Animal;isFood(Lnet/minecraft/world/item/ItemStack;)Z"))
    private boolean testFood(final Animal instance, final ItemStack stack)
    {
        return instance.isFood(stack) || (instance instanceof ITraitEntity traitEntity && traitEntity.hasTrait(Traits.omnivore) && stack.is(Omnivore.foodItems));
    }
}
