package intricarpet.mixins.rule.optimizedTNTExtra;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.At;

import carpet.helpers.OptimizedExplosion;
import intricarpet.intricarpetRules;

@Mixin(OptimizedExplosion.class)
public class OptimizedExplosionMixin
{
  @Inject(method = "doExplosionA", at = @At("HEAD"), cancellable = true)
  private static void cancelDoExplosionA(CallbackInfo ci)
  {
    if(intricarpetRules.optimizedTNTExtra) ci.cancel();
  }

  @Inject(method = "doExplosionB", at = @At("HEAD"), cancellable = true)
  private static void cancelDoExplosionB(CallbackInfo ci)
  {
    if(intricarpetRules.optimizedTNTExtra) ci.cancel();
  }
}
