package me.lntricate.intricarpet.mixins.logger;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.lntricate.intricarpet.logging.logHelpers.ExplosionLogHelper;
import net.minecraft.server.level.ServerLevel;

@Mixin(ServerLevel.class)
public class ServerLevelMixin
{
  @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/entity/PersistentEntitySectionManager;tick()V"))
  private void afterEntities(CallbackInfo ci)
  {
    ExplosionLogHelper.afterEntities();
  }
}
