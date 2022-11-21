package intricarpet.mixins.logger.explosions;

import carpet.logging.LoggerRegistry;
import intricarpet.logging.logHelpers.ExplosionLogHelper;

import java.util.List;
import java.util.ArrayList;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.BaseText;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerWorld.class)
public class ServerWorldMixin
{
  @Inject(method = "tick", at = @At("TAIL"))
  private void onTickEnd(CallbackInfo ci)
  {
    if(LoggerRegistry.__explosions)
    {
      LoggerRegistry.getLogger("explosions").log((option) ->
      {
        List<BaseText> messages = new ArrayList<>();
        if("compact".equals(option)) ExplosionLogHelper.finishCompact(messages);
        return messages.toArray(new BaseText[0]);
      });
      ExplosionLogHelper.resetAtEndOfTick();
    }
  }
}
