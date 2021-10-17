package intricarpet.mixins.interaction;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import intricarpet.helpers.Interactions;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;

@Mixin(ProjectileEntity.class)
public abstract class ProjectileEntityMixin
{
    @Inject(method = "method_26958", at = @At("HEAD"), cancellable = true)
    private void canHit(Entity entity, CallbackInfoReturnable<Boolean> cir)
    {
        if(entity instanceof PlayerEntity)
        {
            String playerName = entity.getName().getString();
            if(Interactions.onlinePlayerMap.containsKey(playerName) &&
                Interactions.onlinePlayerMap.get(playerName).contains("entities"))
                cir.setReturnValue(false);
        }
    }
}
