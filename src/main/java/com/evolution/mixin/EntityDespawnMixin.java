package com.evolution.mixin;

import com.evolution.trait.selection.EntityTraitManager;
import com.evolution.trait.storage.ITraitEntity;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mob.class)
public class EntityDespawnMixin
{
    @Inject(method = "checkDespawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;discard()V"))
    private void onDiscard(final CallbackInfo ci)
    {
        if (this instanceof ITraitEntity traitEntity)
        {
            EntityTraitManager.onEntityDespawn((Mob & ITraitEntity)traitEntity);
        }
    }
}
