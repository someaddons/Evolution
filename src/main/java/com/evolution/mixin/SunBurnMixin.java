package com.evolution.mixin;

import com.evolution.trait.Traits;
import com.evolution.trait.storage.ITraitEntity;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Mob.class)
public class SunBurnMixin
{
    @Inject(method = "isSunBurnTick", at = @At("HEAD"), cancellable = true)
    private void checkSunBurn(final CallbackInfoReturnable<Boolean> cir)
    {
        if (this instanceof ITraitEntity traitEntity && traitEntity.hasTrait(Traits.environmentalAdaption))
        {
            cir.setReturnValue(false);
        }
    }
}
