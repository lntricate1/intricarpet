package me.lntricate.intricarpet.mixins.logger;

import java.util.List;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.ModifyReceiver;

import carpet.logging.logHelpers.ExplosionLogHelper;
import net.minecraft.network.chat.BaseComponent;
import net.minecraft.world.phys.Vec3;

@Mixin(ExplosionLogHelper.class)
public class ExplosionLogHelperMixin
{
  @Shadow @Final private Vec3 pos;
  @Shadow private boolean affectBlocks;

  @Inject(method = "onExplosionDone", at = @At("HEAD"), remap = false)
  private void onExplosionDone(long gametime, CallbackInfo ci)
  {
    me.lntricate.intricarpet.logging.logHelpers.ExplosionLogHelper.onExplosion(pos, gametime, affectBlocks);
  }

  private String option = "";

  @Inject(method = "lambda$onExplosionDone$1", at = @At("HEAD"), remap = false)
  private void getOption(long gametime, String option_, CallbackInfoReturnable<BaseComponent> cir)
  {
    option = option_;
  }

  @ModifyReceiver(method = "lambda$onExplosionDone$1", at = @At(value = "INVOKE", target = "Ljava/util/List;toArray([Ljava/lang/Object;)[Ljava/lang/Object;", remap = false))
  private List<BaseComponent> addLoggers(List<BaseComponent> messages, Object[] dummy)
  {
    return me.lntricate.intricarpet.logging.logHelpers.ExplosionLogHelper.onLog(messages, option);
  }
}
