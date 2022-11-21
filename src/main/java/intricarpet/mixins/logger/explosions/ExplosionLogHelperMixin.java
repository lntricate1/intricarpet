package intricarpet.mixins.logger.explosions;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import carpet.logging.logHelpers.ExplosionLogHelper;
import net.minecraft.text.Text;
import net.minecraft.text.BaseText;
import net.minecraft.util.math.Vec3d;

@Mixin(ExplosionLogHelper.class)
public class ExplosionLogHelperMixin
{
  @Shadow @Final private Vec3d pos;
  @Shadow private static int explosionCountInCurretGT;
  @Shadow private static long lastGametime;
  @Shadow private boolean affectBlocks;

  @Inject(method = "onExplosionDone", at = @At("HEAD"), remap = false)
  private void onExplosion(long gametime, CallbackInfo ci)
  {
    intricarpet.logging.logHelpers.ExplosionLogHelper.onExplosion(lastGametime, gametime, pos, affectBlocks, explosionCountInCurretGT);
  }

  @Inject(method = "lambda$onExplosionDone$1", at = @At("RETURN"), remap = false, cancellable = true)
  private void addCompact(long gametime, String option,
    //#if MC >= 11900
    //$$ CallbackInfoReturnable<Text[]> cir
    //#else
    CallbackInfoReturnable<BaseText[]> cir
    //#endif
  )
  {
    cir.setReturnValue(intricarpet.logging.logHelpers.ExplosionLogHelper.onLog(cir.getReturnValue(), gametime, option, pos));
  }
}
