package intricarpet.mixins.rule.fallingBlockThreadSleep;

import java.util.Random;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import intricarpet.intricarpetRules;
import net.minecraft.block.BlockState;
import net.minecraft.block.FallingBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

@Mixin(FallingBlock.class)
public class FallingBlockMixin
{
    @Inject(method = "scheduledTick", at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/util/math/BlockPos;getX()I"
    ))
    private void threadSleep(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci)
    {
        if(intricarpetRules.fallingBlockThreadSleep > 0)
        {
            try{
                Thread.sleep(intricarpetRules.fallingBlockThreadSleep);
            } catch(Exception e){}
        }
    }
}
