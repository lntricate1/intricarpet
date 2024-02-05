// package me.lntricate.intricarpet.helpers;
//
// import java.util.ArrayList;
// import java.util.Collections;
// import java.util.HashMap;
// import java.util.HashSet;
// import java.util.Iterator;
// import java.util.List;
// import java.util.Map;
// import java.util.Optional;
//
// import org.apache.commons.lang3.tuple.Pair;
//
// import com.google.common.collect.Sets;
//
// import carpet.mixins.ExplosionAccessor;
// import it.unimi.dsi.fastutil.objects.ObjectArrayList;
// import me.lntricate.intricarpet.logging.logHelpers.ExplosionLogHelper;
// import me.lntricate.intricarpet.mixins.ExplosionDamageAccessor;
// import net.minecraft.core.BlockPos;
// import net.minecraft.server.level.ServerLevel;
// import net.minecraft.util.Mth;
// import net.minecraft.world.entity.Entity;
// import net.minecraft.world.entity.LivingEntity;
// import net.minecraft.world.entity.item.ItemEntity;
// import net.minecraft.world.entity.item.PrimedTnt;
// import net.minecraft.world.entity.player.Player;
// import net.minecraft.world.item.ItemStack;
// import net.minecraft.world.item.enchantment.ProtectionEnchantment;
// import net.minecraft.world.level.ClipContext;
// import net.minecraft.world.level.Explosion;
// import net.minecraft.world.level.ExplosionDamageCalculator;
// import net.minecraft.world.level.Level;
// import net.minecraft.world.level.block.Block;
// import net.minecraft.world.level.block.Blocks;
// import net.minecraft.world.level.block.entity.BlockEntity;
// import net.minecraft.world.level.block.state.BlockState;
// import net.minecraft.world.level.material.FluidState;
// import net.minecraft.world.level.storage.loot.LootContext;
// import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
// import net.minecraft.world.phys.AABB;
// import net.minecraft.world.phys.BlockHitResult;
// import net.minecraft.world.phys.HitResult;
// import net.minecraft.world.phys.Vec3;
//
// /*
//  * WIP optimizedTNT rewrite
//  */
// public class OptimizedExplosion
// {
//   private static Explosion explosion;
//   private static ExplosionAccessor eAccess;
//   private static ExplosionDamageAccessor eAccess2;
//
//   // Explosion data
//   private static double ex, ey, ez;
//   private static Vec3 pos;
//   private static Level level;
//   private static float power;
//   private static List<BlockPos> toBlow = new ArrayList<>();
//
//   // For checking
//   private static long memTick = 0;
//   private static Vec3 memPos;
//   private static float memPower;
//   private static ExplosionDamageCalculator memDamageCalculator;
//
//   // For using
//   private static List<Entity> memEntities;
//   private static Map<EntityKey, Vec3> memVelocity = new HashMap<>();
//   private static Map<BlockPos, Pair<Float, BlockState>> memBlocks = new HashMap<>();
//
//   private static final Vec3[] RAYS = new Vec3[1352];
//
//   static
//   {
//     int i = 0;
//     for(int x = 0; x < 16; ++x)
//       for(int y = 0; y < 16; ++y)
//         for(int z = 0; z < 16; ++z)
//           if(x == 0 || x == 15 || y == 0 || y == 15 || z == 0 || z == 15)
//           {
//             double dx = x / 15f * 2f - 1f;
//             double dy = y / 15f * 2f - 1f;
//             double dz = z / 15f * 2f - 1f;
//             double mag = Math.sqrt(dx*dx + dy*dy + dz*dz);
//             RAYS[i++] = new Vec3(dx / mag * 0.3f, dy / mag * 0.3f, dz / mag * 0.3f);
//           }
//   }
//
//   private static final class EntityKey
//   {
//     private final ExposureKey exposureKey;
//     private final float eyeHeight;
//     private final Integer hash;
//
//     EntityKey(Entity entity)
//     {
//       exposureKey = new ExposureKey(entity);
//       eyeHeight = entity.getEyeHeight();
//       hash = entity instanceof LivingEntity ? entity.hashCode() : null;
//     }
//   }
//
//   private static final class ExposureKey
//   {
//     public final double x, y, z;
//     public final AABB aabb;
//
//     ExposureKey(Entity entity)
//     {
//       x = entity.getX();
//       y = entity.getY();
//       z = entity.getZ();
//       aabb = entity.getBoundingBox();
//     }
//   }
//
//   public static void doExplosionA(Explosion e, ExplosionLogHelper logger)
//   {
//     setExplosion(e);
//     checkMem();
//     collectBlocks();
//     pushEntities();
//   }
//
//   public static void setExplosion(Explosion e)
//   {
//     explosion = e;
//     eAccess = (ExplosionAccessor)explosion;
//     eAccess2 = (ExplosionDamageAccessor)explosion;
//
//     ex = eAccess.getX(); ey = eAccess.getY(); eAccess.getZ();
//     pos = new Vec3(ex, ey, ez);
//     level = eAccess.getWorld();
//     power = eAccess.getPower();
//     toBlow.clear();
//   }
//
//   public static void checkMem()
//   {
//     if(memPos.equals(pos) && memTick == level.getGameTime() && memPower == power)
//       return;
//
//     if(memTick != level.getGameTime() || eAccess2.getDamageCalculator() != memDamageCalculator)
//     {
//       memBlocks.clear();
//       memDamageCalculator = eAccess2.getDamageCalculator();
//     }
//
//     memPos = pos;
//     memTick = level.getGameTime();
//     memPower = power;
//
//     memVelocity.clear();
//
//     double x = eAccess.getX(); double y = eAccess.getY(); double z = eAccess.getZ();
//     int minX = Mth.floor(x - power - 1d);
//     int maxX = Mth.floor(x - power - 1d);
//     int minY = Mth.floor(y - power - 1d);
//     int maxY = Mth.floor(y - power - 1d);
//     int minZ = Mth.floor(z - power - 1d);
//     int maxZ = Mth.floor(z - power - 1d);
//
//     Entity tnt = eAccess2.getSource();
//     memEntities = level.getEntities(tnt, new AABB(minX, minY, minZ, maxX, maxY, maxZ));
//
//     Iterator<Entity> iter = memEntities.iterator();
//     while(iter.hasNext())
//       if(iter.next().ignoreExplosion())
//         iter.remove();
//
//     if(tnt != null)
//     {
//       memEntities.remove(tnt);
//       if(tnt instanceof PrimedTnt && tnt.isOnGround())
//       {
//         iter = memEntities.iterator();
//         while(iter.hasNext())
//         {
//           Entity entity = iter.next();
//           if(entity instanceof PrimedTnt &&
//             entity.getX() == tnt.getX() &&
//             entity.getY() == tnt.getY() &&
//             entity.getZ() == tnt.getZ())
//           {
//             iter.remove();
//           }
//         }
//       }
//     }
//   }
//
//   private static void doRay(HashSet<BlockPos> blocks, Vec3 rayPos, Vec3 delta, ExplosionDamageCalculator damageCalculator, float rand)
//   {
//     for(float rayStrength = power * (0.7F + rand * 0.6F); rayStrength > 0F; rayStrength -= 0.22500001F, rayPos = rayPos.add(delta))
//     {
//       BlockPos pos = new BlockPos(rayPos);
//       if(memBlocks.containsKey(pos))
//       {
//         Pair<Float, BlockState> entry = memBlocks.get(pos);
//         rayStrength -= entry.getLeft();
//         if(rayStrength > 0F && damageCalculator.shouldBlockExplode(explosion, level, pos, entry.getRight(), rayStrength))
//           blocks.add(pos);
//       }
//       else
//       {
//         BlockState blockState = level.getBlockState(pos);
//         FluidState fluidState = level.getFluidState(pos);
//         if(!level.isInWorldBounds(pos))
//           break;
//
//         Optional<Float> blastResistance = damageCalculator.getBlockExplosionResistance(explosion, level, pos, blockState, fluidState);
//
//         if(blastResistance.isPresent())
//         {
//           float decrement = (blastResistance.get() + 0.3F) * 0.3F;
//           rayStrength -= decrement;
//           memBlocks.put(pos, Pair.of(decrement, blockState));
//         }
//         else
//           memBlocks.put(pos, Pair.of(0f, blockState));
//
//         if(rayStrength > 0F && damageCalculator.shouldBlockExplode(explosion, level, pos, blockState, rayStrength))
//           blocks.add(pos);
//       }
//     }
//   }
//
//   public static void collectBlocks()
//   {
//     toBlow.clear();
//     HashSet<BlockPos> blocks = Sets.newHashSet();
//     ExplosionDamageCalculator damageCalculator = eAccess2.getDamageCalculator();
//
//     for(Vec3 ray : RAYS)
//       doRay(blocks, pos, ray, damageCalculator, level.random.nextFloat());
//
//     toBlow.addAll(blocks);
//   }
//
//   public static void pushEntities()
//   {
//     for(Entity entity : memEntities)
//     {
//       EntityKey entityKey = new EntityKey(entity);
//       if(memVelocity.containsKey(entityKey))
//       {
//         entity.setDeltaMovement(entity.getDeltaMovement().add(memVelocity.get(entityKey)));
//         continue;
//       }
//
//       double x = entity.getX(), y = entity.getY(), z = entity.getZ();
//       double dx = x - ex, dy = y - ey, dz = z - ez;
//       double magSq = dx*dx + dy*dy + dz*dz;
//
//       double distance = Math.sqrt(magSq);
//       if(distance > power)
//         continue;
//
//       distance /= power;
//       double mag = distance;
//
//       if(!(entity instanceof PrimedTnt))
//       {
//         dy = entity.getEyeY() - y;
//         magSq = dx*dx + dy*dy + dz*dz;
//
//         if(magSq == 0d)
//           continue;
//
//         mag = Math.sqrt(magSq);
//       }
//       else if(magSq == 0d)
//         continue;
//
//       dx /= mag;
//       dy /= mag;
//       dz /= mag;
//
//       double knockback = (1d - distance) * getExposure(entity);
//       entity.hurt(explosion.getDamageSource(), (int)((knockback * knockback + knockback) / 2d * 7d * (double)power + 1d));
//
//       if(entity instanceof Player player && !player.isSpectator() && (!player.isCreative() || !player.getAbilities().flying))
//         explosion.getHitPlayers().put(player, new Vec3(dx*knockback, dy*knockback, dz*knockback));
//
//       if(entity instanceof LivingEntity livingEntity)
//         knockback = ProtectionEnchantment.getExplosionKnockbackAfterDampener(livingEntity, knockback);
//
//       Vec3 velocity = new Vec3(dx*knockback, dy*knockback, dz*knockback);
//       entity.setDeltaMovement(entity.getDeltaMovement().add(velocity));
//       memVelocity.put(entityKey, velocity);
//     }
//   }
//
//   private static float getExposure(Entity entity)
//   {
//     AABB aabb = entity.getBoundingBox();
//     double xw = 1d / ((aabb.maxX - aabb.minX) * 2d + 1d);
//     double yw = 1d / ((aabb.maxY - aabb.minY) * 2d + 1d);
//     double zw = 1d / ((aabb.maxZ - aabb.minZ) * 2d + 1d);
//     double xh = (1d - Math.floor(1d/xw) * xw) / 2d;
//     double zh = (1d - Math.floor(1d/zw) * zw) / 2d;
//
//     int hit = 0, total = 0;
//
//     if(xw < 0d || yw < 0d || zw < 0d)
//       return 0f;
//
//     for(float dx = 0f; dx <= 1f; dx += xw)
//       for(float dy = 0f; dy <= 1f; dy += yw)
//         for(float dz = 0f; dz <= 1f; dz += zw)
//     {
//       double x = Mth.lerp((double)dx, aabb.minX, aabb.maxX);
//       double y = Mth.lerp((double)dy, aabb.minY, aabb.maxY);
//       double z = Mth.lerp((double)dz, aabb.minZ, aabb.maxZ);
//       Vec3 vec3 = new Vec3(x + xh, y, z + zh);
//       BlockHitResult result = level.clip(new ClipContext(vec3, pos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity));
//       if(result.getType() == HitResult.Type.MISS)
//         ++hit;
//
//       ++total;
//     }
//
//     return (float)hit / (float)total;
//   }
//
//   public static void doExplosionB(boolean spawnParticles)
//   {
//     ObjectArrayList<com.mojang.datafixers.util.Pair<ItemStack, BlockPos>> list = new ObjectArrayList<>();
//     Collections.shuffle(toBlow, level.random);
//     for(BlockPos pos : toBlow)
//     {
//       BlockState state = memBlocks.get(pos).getRight();
//       Block block = state.getBlock();
//       if(!state.isAir())
//       {
//         BlockPos pos1 = pos.immutable();
//         level.getProfiler().push("explosion_blocks");
//         if(block.dropFromExplosion(explosion) && level instanceof ServerLevel serverLevel)
//         {
//           BlockEntity blockEntity = state.hasBlockEntity() ? level.getBlockEntity(pos) : null;
//           LootContext.Builder builder = new LootContext.Builder(serverLevel)
//             .withRandom(level.random)
//             .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
//             .withParameter(LootContextParams.TOOL, ItemStack.EMPTY)
//             .withOptionalParameter(LootContextParams.BLOCK_ENTITY, blockEntity)
//             .withOptionalParameter(LootContextParams.THIS_ENTITY, eAccess2.getSource());
//           if(eAccess.getDestructionType() == Explosion.BlockInteraction.DESTROY)
//             builder.withParameter(LootContextParams.EXPLOSION_RADIUS, power);
//           state.getDrops(builder).forEach((ItemStack stack) -> addBlockDrops(list, stack, pos1));
//         }
//         level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
//         block.wasExploded(level, pos, explosion);
//         level.getProfiler().pop();
//       }
//     }
//   }
//
//   private static void addBlockDrops(ObjectArrayList<com.mojang.datafixers.util.Pair<ItemStack, BlockPos>> list, ItemStack stack, BlockPos pos)
//   {
//     int s = list.size();
//     for(int i = 0; i < s; ++i)
//     {
//       com.mojang.datafixers.util.Pair<ItemStack, BlockPos> pair = list.get(i);
//       ItemStack stack2 = pair.getFirst();
//       if(ItemEntity.areMergable(stack2, stack))
//       {
//         ItemStack stack3 = ItemEntity.merge(stack2, stack, 16);
//         list.set(i, com.mojang.datafixers.util.Pair.of(stack3, pair.getSecond()));
//         if(stack.isEmpty())
//           return;
//       }
//     }
//     list.add(com.mojang.datafixers.util.Pair.of(stack, pos));
//   }
// }
