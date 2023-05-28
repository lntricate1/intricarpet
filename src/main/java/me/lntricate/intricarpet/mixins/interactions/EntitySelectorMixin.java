package me.lntricate.intricarpet.mixins.interactions;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import me.lntricate.intricarpet.interactions.Interaction;
import me.lntricate.intricarpet.interfaces.IServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;

@Mixin(EntitySelector.class)
public class EntitySelectorMixin
{
  @Inject(method = "pushableBy", at = @At("HEAD"), cancellable = true)
  private static void pushableBy(Entity entity, CallbackInfoReturnable<Predicate<Entity>> cir)
  {
    if(entity instanceof IServerPlayer player && !player.getInteraction(Interaction.ENTITIES))
      cir.setReturnValue(Predicates.alwaysFalse());
  }
}
