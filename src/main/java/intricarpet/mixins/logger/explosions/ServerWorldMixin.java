package intricarpet.mixins.logger.explosions;

import net.minecraft.server.world.ServerWorld;
import intricarpet.logging.logHelpers.intricarpetExplosionLogHelper;
import carpet.logging.LoggerRegistry;
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
        if (LoggerRegistry.__explosions) intricarpetExplosionLogHelper.logLastExplosions();
    }
}
