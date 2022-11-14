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
import net.minecraft.server.world.ServerTickScheduler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.TickPriority;
//#if MC >= 11800
//$$ import net.minecraft.world.tick.QueryableTickScheduler;
//$$ import net.minecraft.world.ServerWorldAccess;
//$$ import net.minecraft.world.tick.OrderedTick;
//#else
import net.minecraft.world.ScheduledTick;
//#endif

@Mixin(ServerTickScheduler.class)
public abstract class ServerTickSchedulerMixin<T>
{
  //#if MC >= 11800
  //$$ @Shadow abstract QueryableTickScheduler<Block> getBlockTickScheduler();
  //$$ @Shadow abstract QueryableTickScheduler<Fluid> getFluidTickScheduler();
  //$$ @Shadow abstract <T> OrderedTick<T> createOrderedTick(BlockPos pos, T type, int delay, TickPriority priority);
  //$$ @Shadow abstract <T> OrderedTick<T> createOrderedTick(BlockPos pos, T type, int delay);
  //$$ private ServerWorld world = ((ServerWorldAccess)this).toServerWorld();
  //#else
  @Shadow @Final private ServerWorld world;
  @Shadow abstract void addScheduledTick(ScheduledTick<T> tick);
  @Shadow @Final Predicate<T> invalidObjPredicate;
  //#endif

  //#if MC >= 11800
  //$$ private void tickFluid(OrderedTick tick) {
  //$$ FluidState fluidState = world.getFluidState(tick.pos());
  //$$   if (fluidState.isOf((Fluid)tick.type()))
  //$$     fluidState.onScheduledTick(world, tick.pos());
  //$$ }

  //$$ private void tickBlock(OrderedTick tick) {
  //$$ BlockState blockState = world.getBlockState(tick.pos());
  //$$   if (blockState.isOf((Block)tick.type()))
  //$$     blockState.scheduledTick(world, tick.pos(), world.random);
  //$$ }
  //#else
  private void tickFluid(ScheduledTick<Fluid> tick) {
    FluidState fluidState = world.getFluidState(tick.pos);
    if (fluidState.getFluid() == tick.getObject())
      fluidState.onScheduledTick(world, tick.pos);
  }

  private void tickBlock(ScheduledTick<Block> tick) {
    BlockState blockState = world.getBlockState(tick.pos);
    if (blockState.isOf((Block)tick.getObject()))
      blockState.scheduledTick(world, tick.pos, world.random);
  }
  //#endif

  //#if MC >= 11800
  //$$ @Inject(method = "createAndScheduleBlockTick(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;ILnet/minecraft/world/TickPriority;)V", at = @At("HEAD"), cancellable = true)
  //$$ private void instantSchedule(BlockPos pos, Block block, int delay, TickPriority priority, CallbackInfo ci)
  //$$ {
  //$$   OrderedTick tick = this.createOrderedTick(pos, block, delay, priority);
  //$$   QueryableTickScheduler<Block> tickScheduler = this.getBlockTickScheduler();
  //$$   if(intricarpetRules.instantScheduling.equals("false") || intricarpetRules.instantScheduling.equals("fluids")) tickScheduler.scheduleTick(tick);
  //$$     else tickBlock(tick);
  //$$   ci.cancel();
  //$$ }

  //$$ @Inject(method = "createAndScheduleFluidTick(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;ILnet/minecraft/world/TickPriority;)V", at = @At("HEAD"), cancellable = true)
  //$$ private void instantSchedule(BlockPos pos, Fluid fluid, int delay, TickPriority priority, CallbackInfo ci)
  //$$ {
  //$$   OrderedTick tick = this.createOrderedTick(pos, fluid, delay, priority);
  //$$   QueryableTickScheduler<Fluid> tickScheduler = this.getFluidTickScheduler();
  //$$   if(intricarpetRules.instantScheduling.equals("false") || intricarpetRules.instantScheduling.equals("blocks")) tickScheduler.scheduleTick(tick);
  //$$     else tickFluid(tick);
  //$$   ci.cancel();
  //$$ }

  //$$ @Inject(method = "createAndScheduleBlockTick(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;)V", at = @At("HEAD"), cancellable = true)
  //$$ private void instantSchedule(BlockPos pos, Block block, int delay, CallbackInfo ci)
  //$$ {
  //$$   OrderedTick tick = this.createOrderedTick(pos, block, delay);
  //$$   QueryableTickScheduler<Block> tickScheduler = this.getBlockTickScheduler();
  //$$   if(intricarpetRules.instantScheduling.equals("false") || intricarpetRules.instantScheduling.equals("fluids")) tickScheduler.scheduleTick(tick);
  //$$     else tickBlock(tick);
  //$$   ci.cancel();
  //$$ }

  //$$ @Inject(method = "createAndScheduleFluidTick(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;)V", at = @At("HEAD"), cancellable = true)
  //$$ private void instantSchedule(BlockPos pos, Fluid fluid, int delay, CallbackInfo ci)
  //$$ {
  //$$   OrderedTick tick = this.createOrderedTick(pos, fluid, delay);
  //$$   QueryableTickScheduler<Fluid> tickScheduler = this.getFluidTickScheduler();
  //$$   if(intricarpetRules.instantScheduling.equals("false") || intricarpetRules.instantScheduling.equals("blocks")) tickScheduler.scheduleTick(tick);
  //$$     else tickFluid(tick);
  //$$   ci.cancel();
  //$$ }
  //#else
  @SuppressWarnings("all")
  @Inject(method = "schedule", at = @At("HEAD"), cancellable = true)
  private void instantSchedule(BlockPos pos, T object, int delay, TickPriority priority, CallbackInfo ci)
  {
    if (!invalidObjPredicate.test(object))
    {
      ScheduledTick tick = new ScheduledTick(pos, object, (long)delay + world.getTime(), priority);
      if(intricarpetRules.instantScheduling.equals("false")) addScheduledTick(tick);
      else
      {
        if(object instanceof Block)
          if(intricarpetRules.instantScheduling.equals("fluids")) addScheduledTick(tick); else tickBlock(tick);
        if(object instanceof Fluid)
          if(intricarpetRules.instantScheduling.equals("blocks")) addScheduledTick(tick); else tickFluid(tick);
      }
    }
    ci.cancel();
  }
  //#endif
}
