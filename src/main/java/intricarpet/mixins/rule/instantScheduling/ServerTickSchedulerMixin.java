package intricarpet.mixins.rule.instantScheduling;

import java.util.function.Predicate;


import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import intricarpet.intricarpetRules;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.TickPriority;
import net.minecraft.server.world.ServerTickScheduler;
import net.minecraft.world.ScheduledTick;

@Mixin(ServerTickScheduler.class)
public abstract class ServerTickSchedulerMixin<T>
{
  @Shadow @Final private ServerWorld world;
  @Shadow abstract void addScheduledTick(ScheduledTick<T> tick);
  @Shadow @Final Predicate<T> invalidObjPredicate;

  @SuppressWarnings("all")
  @Inject(method = "schedule", at = @At("HEAD"), cancellable = true)
  private void instantSchedule(BlockPos pos, T object, int delay, TickPriority priority, CallbackInfo ci)
  {
    if(!intricarpetRules.instantScheduling.equals("false"))
    {
      if(object instanceof Block && !intricarpetRules.instantScheduling.equals("fluids"))
      {
        BlockState blockState = world.getBlockState(pos);
        if (blockState.isOf((Block)object))
          blockState.scheduledTick(world, pos, world.random);
        ci.cancel();
      }
      else if(object instanceof Fluid && !intricarpetRules.instantScheduling.equals("blocks"))
      {
        FluidState fluidState = world.getFluidState(pos);
        if (fluidState.getFluid() == (Fluid)object)
          fluidState.onScheduledTick(world, pos);
        ci.cancel();
      }
    }
  }
}
