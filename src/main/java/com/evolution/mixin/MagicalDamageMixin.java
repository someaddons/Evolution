package com.evolution.mixin;

import com.evolution.trait.Traits;
import com.evolution.trait.storage.ITraitEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class MagicalDamageMixin
{
    @Inject(method = "getDamageAfterArmorAbsorb", at = @At("HEAD"), cancellable = true)
    private void checkIgnoreArmor(final DamageSource source, final float damage, final CallbackInfoReturnable<Float> cir)
    {
        if (source.getEntity() instanceof ITraitEntity traitEntity && traitEntity.hasTrait(Traits.magical))
        {
            cir.setReturnValue(damage);
        }
    }
}
