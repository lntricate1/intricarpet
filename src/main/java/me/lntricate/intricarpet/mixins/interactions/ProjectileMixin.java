package me.lntricate.intricarpet.mixins.interactions;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import me.lntricate.intricarpet.interactions.Interaction;
import me.lntricate.intricarpet.interfaces.IServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;

@Mixin(Projectile.class)
public class ProjectileMixin
{
  @Inject(method = "canHitEntity", at = @At("HEAD"), cancellable = true)
  private void canHitEntity(Entity entity, CallbackInfoReturnable<Boolean> cir)
  {
    if(entity instanceof IServerPlayer player && !player.getInteraction(Interaction.ENTITIES))
      cir.setReturnValue(false);
  }
}
