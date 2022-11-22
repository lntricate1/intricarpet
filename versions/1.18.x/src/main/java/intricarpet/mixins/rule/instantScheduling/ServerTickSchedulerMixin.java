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
import net.minecraft.world.World;
import net.minecraft.world.TickPriority;
//#if MC >= 11900
//$$import net.minecraft.world.dimension.DimensionOptions;
//#endif
import net.minecraft.world.tick.WorldTickScheduler;
import net.minecraft.world.tick.OrderedTick;
import net.minecraft.world.WorldAccess;

import net.minecraft.server.MinecraftServer;
import java.util.concurrent.Executor;
import net.minecraft.world.level.storage.LevelStorage.Session;
import net.minecraft.world.level.ServerWorldProperties;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import java.util.List;
import net.minecraft.world.spawner.Spawner;

@Mixin(ServerWorld.class)
public abstract class ServerTickSchedulerMixin extends World// implements WorldAccess
{
//#if MC > 11900
//$$  public ServerTickSchedulerMixin(MinecraftServer server, Executor workerExecutor, Session session, ServerWorldProperties properties, RegistryKey<World> worldKey, DimensionOptions dimensionOptions, RegistryEntry<DimensionType> registryEntry, WorldGenerationProgressListener worldGenerationProgressListener, ChunkGenerator chunkGenerator, boolean debugWorld, long seed, List<Spawner> spawners, boolean shouldTickTime)
//$$  {
//$$    super(properties, worldKey, dimensionOptions.getDimensionTypeEntry(), server::getProfiler, false, debugWorld, seed, server.getMaxChainedNeighborUpdates());

//#else
  public ServerTickSchedulerMixin(MinecraftServer server, Executor workerExecutor, Session session, ServerWorldProperties properties, RegistryKey<World> worldKey, RegistryEntry<DimensionType> registryEntry, WorldGenerationProgressListener worldGenerationProgressListener, ChunkGenerator chunkGenerator, boolean debugWorld, long seed, List<Spawner> spawners, boolean shouldTickTime)
  {
    super(properties, worldKey, registryEntry, server::getProfiler, false, debugWorld, seed);
//#endif
  }

  @Shadow abstract void tickBlock(BlockPos pos, Block block);
  @Shadow abstract void tickFluid(BlockPos pos, Fluid fluid);
  @Shadow public abstract WorldTickScheduler<Block> getBlockTickScheduler();
  @Shadow public abstract WorldTickScheduler<Fluid> getFluidTickScheduler();

  private <T> OrderedTick<T> createOrderedTick(BlockPos pos, T type, int delay, TickPriority priority)
  {
    return new OrderedTick<T>(type, pos, this.getLevelProperties().getTime() + (long)delay, priority, this.getTickOrder());
  }

  private <T> OrderedTick<T> createOrderedTick(BlockPos pos, T type, int delay)
  {
    return new OrderedTick<T>(type, pos, this.getLevelProperties().getTime() + (long)delay, this.getTickOrder());
  }

  @Override
  public void createAndScheduleBlockTick(BlockPos pos, Block block, int delay, TickPriority priority)
  {
    if(!intricarpetRules.instantScheduling.equals("false") && !intricarpetRules.instantScheduling.equals("fluids"))
      this.tickBlock(pos, block);
    else this.getBlockTickScheduler().scheduleTick(this.createOrderedTick(pos, block, delay, priority));
  }

  @Override
  public void createAndScheduleBlockTick(BlockPos pos, Block block, int delay)
  {
    if(!intricarpetRules.instantScheduling.equals("false") && !intricarpetRules.instantScheduling.equals("fluids"))
      this.tickBlock(pos, block);
    else this.getBlockTickScheduler().scheduleTick(this.createOrderedTick(pos, block, delay));
  }

  @Override
  public void createAndScheduleFluidTick(BlockPos pos, Fluid fluid, int delay, TickPriority priority)
  {
    if(!intricarpetRules.instantScheduling.equals("false") && !intricarpetRules.instantScheduling.equals("blocks"))
      this.tickFluid(pos, fluid);
    else this.getFluidTickScheduler().scheduleTick(this.createOrderedTick(pos, fluid, delay, priority));
  }

  @Override
  public void createAndScheduleFluidTick(BlockPos pos, Fluid fluid, int delay)
  {
    if(!intricarpetRules.instantScheduling.equals("false") && !intricarpetRules.instantScheduling.equals("blocks"))
      this.tickFluid(pos, fluid);
    else this.getFluidTickScheduler().scheduleTick(this.createOrderedTick(pos, fluid, delay));
  }
}
