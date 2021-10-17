package intricarpet.helpers;
//Author: masa

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.enchantment.ProtectionEnchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.explosion.Explosion;
import carpet.logging.logHelpers.ExplosionLogHelper;
import carpet.mixins.ExplosionAccessor;
import carpet.CarpetSettings;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.apache.commons.lang3.tuple.Pair;

import static carpet.script.CarpetEventServer.Event.EXPLOSION_OUTCOME;

public class OptimizedExplosion
{
    // intricate's optimizations
    public static HashMap<Box, List<Object>> velocityList;
    private static HashMap<BlockPos.Mutable, Float> blastResCache = new HashMap<>();
    private static HashMap<BlockPos.Mutable, BlockState> blockStateCache = new HashMap<>();

    // masa's optimizations
    private static List<Entity> entitylist;
    private static BlockPos.Mutable posMutable = new BlockPos.Mutable(0, 0, 0);
    private static ObjectOpenHashSet<BlockPos> affectedBlockPositionsSet = new ObjectOpenHashSet<>();
    private static boolean firstRay;
    private static boolean rayCalcDone;
    private static Vec3d vec3dmem;
    private static long tickmem;
    public static int entitiesTicked = 0;
    
    // For disabling the explosion particles and sound
    public static int explosionSound = 0;

    // Creating entity list for scarpet event
    private static List<Entity> entityList = new ArrayList<>();

    public static void doExplosionA(Explosion e, ExplosionLogHelper eLogger)
    {
        ExplosionAccessor eAccess = (ExplosionAccessor) e;
        World world = eAccess.getWorld();
        boolean eventNeeded = EXPLOSION_OUTCOME.isNeeded() && !world.isClient();
        float pow = eAccess.getPower() * 2.0F;

        Vec3d vec3d = new Vec3d(eAccess.getX(), eAccess.getY(), eAccess.getZ());

        // Check if explosion is in a different position or tick
        if (entitiesTicked > 1 || vec3dmem == null || !vec3dmem.equals(vec3d) || tickmem != world.getTime()) {
            vec3dmem = vec3d;
            tickmem = world.getTime();

            // Get Box to check for entities
            int k1 = MathHelper.floor(eAccess.getX() - (double) pow - 1.0D);
            int l1 = MathHelper.floor(eAccess.getX() + (double) pow + 1.0D);
            int i2 = MathHelper.floor(eAccess.getY() - (double) pow - 1.0D);
            int i1 = MathHelper.floor(eAccess.getY() + (double) pow + 1.0D);
            int j2 = MathHelper.floor(eAccess.getZ() - (double) pow - 1.0D);
            int j1 = MathHelper.floor(eAccess.getZ() + (double) pow + 1.0D);

            entitylist = world.getOtherEntities(null, new Box(k1, i2, j2, l1, i1, j1));
            explosionSound = 0;

            HashMap<Box, List<Object>> newVelocityList = new HashMap<>();
            for(Entity e_ : entitylist)
            {
                Box box = e_.getBoundingBox();

                // Only calculate if not already calculated
                if(newVelocityList != null && newVelocityList.containsKey(box)) continue;
                newVelocityList.put(box, getVelocity(vec3d, pow, e_, e_.getPos(), box, e_.getEyeY(), e_ instanceof TntEntity));
            }
            velocityList = newVelocityList;
            blastResCache.clear();
            blockStateCache.clear();
        }

        entitiesTicked = 0;

        // TNT below -7 or above 168 cannot break blocks
        if (!CarpetSettings.explosionNoBlockDamage && vec3d.y < 168 && vec3d.y > -7) {
            rayCalcDone = false;
            firstRay = true;
            getAffectedPositionsOnPlaneY(e,  0,  0, 15,  0, 15); // bottom
            getAffectedPositionsOnPlaneY(e, 15,  0, 15,  0, 15); // top
            getAffectedPositionsOnPlaneX(e,  0,  1, 14,  0, 15); // west
            getAffectedPositionsOnPlaneX(e, 15,  1, 14,  0, 15); // east
            getAffectedPositionsOnPlaneZ(e,  0,  1, 14,  1, 14); // north
            getAffectedPositionsOnPlaneZ(e, 15,  1, 14,  1, 14); // south

            e.getAffectedBlocks().addAll(affectedBlockPositionsSet);
            affectedBlockPositionsSet.clear();
        }

        explosionSound++;

        Entity explodingEntity = eAccess.getEntity();
        for (int k2 = 0; k2 < entitylist.size(); ++k2) {
            Entity entity = entitylist.get(k2);

            // Quickly remove self from entity list
            if (entity == explodingEntity) {
                removeFast(entitylist, k2);
                k2--;
                continue;
            }

            // If entity is another tnt and in the same position, use the precalculated value. Does not apply velocity because it assumes the compressed tnt is all on a block.
            if (entity instanceof TntEntity && explodingEntity != null &&
                    entity.getX() == explodingEntity.getX() &&
                    entity.getY() == explodingEntity.getY() &&
                    entity.getZ() == explodingEntity.getZ()) {
                if (eLogger != null) {
                    eLogger.onEntityImpacted(entity, new Vec3d(0,-0.9923437498509884d, 0));
                }
                continue;
            }

            if (!entity.isImmuneToExplosion())
            {
                Box box = entity.getBoundingBox();

                // Get cached velocity
                List<Object> velResult = velocityList.get(box);
                Vec3d vel = (Vec3d) velResult.get(0);
                Vec3d velTransformed = (Vec3d) velResult.get(1);
                float damage = (float) velResult.get(2);

                // Add to scarpet event
                if (eventNeeded) entityList.add(entity);

                // Add to logger
                if (eLogger != null) eLogger.onEntityImpacted(entity, velTransformed);

                // Damage entity
                entity.damage(e.getDamageSource(), damage);

                // Knockback entity
                entity.setVelocity(entity.getVelocity().add(velTransformed));

                // Add to affected players map
                if (entity instanceof PlayerEntity) {
                    PlayerEntity player = (PlayerEntity) entity;
                    if (!player.isSpectator()
                            && (!player.isCreative() || !player.abilities.flying)) {
                        e.getAffectedPlayers().put(player, vel);
                    }
                }
            }
        }
    }

    private static List<Object> getVelocity(Vec3d pos, float power, Entity e, Vec3d epos, Box box, double eyeY, boolean tnt)
    {
        double px = pos.getX();
        double py = pos.getY();
        double pz = pos.getZ();
        double ex = epos.getX();
        double ey = epos.getY();
        double ez = epos.getZ();
        double d1 = Math.sqrt(epos.squaredDistanceTo(px, py, pz)) / (double) power;
        if(d1 > 1.0D) return null;
        double x1 = ex - px;
        double y1 = (tnt ? ey : eyeY) - py;
        double z1 = ez - pz;
        double d2 = (double) Math.sqrt(x1 * x1 + y1 * y1 + z1 * z1);
        if(d2 == 0) return null;
        x1 /= d2;
        y1 /= d2;
        z1 /= d2;
        double d3 = (1.0D - d1) * Explosion.getExposure(pos, e);
        double d4 = d3;
        if (e instanceof LivingEntity)
            d4 = ProtectionEnchantment.transformExplosionKnockback((LivingEntity) e, d3);
        float d5 = (float) ((int) ((d3 * d3 + d3) / 2.0D * 7.0D * (double) power + 1.0D));
        return List.of(new Vec3d(x1 * d3, y1 * d3, z1 * d3), new Vec3d(x1 * d4, y1 * d4, z1 * d4), d5);
    }

    public static void doExplosionB(Explosion e, boolean spawnParticles)
    {
        ExplosionAccessor eAccess = (ExplosionAccessor) e; 
        World world = eAccess.getWorld();
        double posX = eAccess.getX();
        double posY = eAccess.getY();
        double posZ = eAccess.getZ();

        // If it is needed, calls scarpet event
        if (EXPLOSION_OUTCOME.isNeeded() && !world.isClient()) {
            EXPLOSION_OUTCOME.onExplosion((ServerWorld) world, eAccess.getEntity(), e::getCausingEntity,  eAccess.getX(), eAccess.getY(), eAccess.getZ(), eAccess.getPower(), eAccess.isCreateFire(), e.getAffectedBlocks(), entityList, eAccess.getDestructionType());
        }

        boolean damagesTerrain = eAccess.getDestructionType() != Explosion.DestructionType.NONE;

        // explosionSound incremented till disabling the explosion particles and sound
        if (explosionSound < 100 || explosionSound % 100 == 0)
        {
            world.playSound(null, posX, posY, posZ, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 4.0F,
                    (1.0F + (world.random.nextFloat() - world.random.nextFloat()) * 0.2F) * 0.7F);

            if (spawnParticles)
            {
                if (eAccess.getPower() >= 2.0F && damagesTerrain)
                {
                    world.addParticle(ParticleTypes.EXPLOSION_EMITTER, posX, posY, posZ, 1.0D, 0.0D, 0.0D);
                }
                else
                {
                    world.addParticle(ParticleTypes.EXPLOSION, posX, posY, posZ, 1.0D, 0.0D, 0.0D);
                }
            }
        }

        if (damagesTerrain)
        {
            ObjectArrayList<Pair<ItemStack, BlockPos>> objectArrayList = new ObjectArrayList<>();
            Collections.shuffle(e.getAffectedBlocks(), world.random);

            for (BlockPos blockpos : e.getAffectedBlocks())
            {
                BlockState state = world.getBlockState(blockpos);
                Block block = state.getBlock();

                if (state.getMaterial() != Material.AIR)
                {
                    if (block.shouldDropItemsOnExplosion(e) && world instanceof ServerWorld)
                    {
                        BlockEntity blockEntity = block.hasBlockEntity() ? world.getBlockEntity(blockpos) : null;  //hasBlockEntity()

                        LootContext.Builder lootBuilder = (new LootContext.Builder((ServerWorld) world))
                                .random(world.random)
                                .parameter(LootContextParameters.ORIGIN, Vec3d.ofCenter(blockpos))
                                .parameter(LootContextParameters.TOOL, ItemStack.EMPTY)
                                .optionalParameter(LootContextParameters.BLOCK_ENTITY, blockEntity)
                                .optionalParameter(LootContextParameters.THIS_ENTITY, eAccess.getEntity());

                        if (eAccess.getDestructionType() == Explosion.DestructionType.DESTROY)
                            lootBuilder.parameter(LootContextParameters.EXPLOSION_RADIUS, eAccess.getPower());

                        state.getDroppedStacks(lootBuilder).forEach((itemStackx) -> {
                            method_24023(objectArrayList, itemStackx, blockpos.toImmutable());
                        });
                    }

                    world.setBlockState(blockpos, Blocks.AIR.getDefaultState(), 3);
                    block.onDestroyedByExplosion(world, blockpos, e);
                }
            }
            objectArrayList.forEach(p -> Block.dropStack(world, p.getRight(), p.getLeft()));

        }

        if (eAccess.isCreateFire())
        {
            for (BlockPos blockpos1 : e.getAffectedBlocks())
            {
                // Use the same Chunk reference because the positions are in the same xz-column
                Chunk chunk = world.getChunk(blockpos1.getX() >> 4, blockpos1.getZ() >> 4);

                BlockPos down = blockpos1.down(1);
                if (eAccess.getRandom().nextInt(3) == 0 &&
                        chunk.getBlockState(blockpos1).getMaterial() == Material.AIR &&
                        chunk.getBlockState(down).isOpaqueFullCube(world, down)
                        )
                {
                    world.setBlockState(blockpos1, Blocks.FIRE.getDefaultState());
                }
            }
        }
    }

    // copied from Explosion, need to move the code to the explosion code anyways and use shadows for
    // simplicity, its not jarmodding anyways
    private static void method_24023(ObjectArrayList<Pair<ItemStack, BlockPos>> objectArrayList, ItemStack itemStack, BlockPos blockPos) {
        int i = objectArrayList.size();

        for(int j = 0; j < i; ++j) {
            Pair<ItemStack, BlockPos> pair = (Pair<ItemStack, BlockPos>) objectArrayList.get(j);
            ItemStack itemStack2 = pair.getLeft();
            if (ItemEntity.canMerge(itemStack2, itemStack)) {
                ItemStack itemStack3 = ItemEntity.merge(itemStack2, itemStack, 16);
                objectArrayList.set(j, Pair.of(itemStack3, pair.getRight()));
                if (itemStack.isEmpty()) {
                    return;
                }
            }
        }

        objectArrayList.add(Pair.of(itemStack, blockPos));
    }

    private static void removeFast(List<Entity> lst, int index) {
        if (index < lst.size() - 1)
            lst.set(index, lst.get(lst.size() - 1));
        lst.remove(lst.size() - 1);
    }

    private static void getAffectedPositionsOnPlaneX(Explosion e, int x, int yStart, int yEnd, int zStart, int zEnd)
    {
        if (!rayCalcDone)
        {
            final double xRel = (double) x / 15.0D * 2.0D - 1.0D;

            for (int z = zStart; z <= zEnd; ++z)
            {
                double zRel = (double) z / 15.0D * 2.0D - 1.0D;

                for (int y = yStart; y <= yEnd; ++y)
                {
                    double yRel = (double) y / 15.0D * 2.0D - 1.0D;

                    if (checkAffectedPosition(e, xRel, yRel, zRel))
                    {
                        return;
                    }
                }
            }
        }
    }

    private static void getAffectedPositionsOnPlaneY(Explosion e, int y, int xStart, int xEnd, int zStart, int zEnd)
    {
        if (!rayCalcDone)
        {
            final double yRel = (double) y / 15.0D * 2.0D - 1.0D;

            for (int z = zStart; z <= zEnd; ++z)
            {
                double zRel = (double) z / 15.0D * 2.0D - 1.0D;

                for (int x = xStart; x <= xEnd; ++x)
                {
                    double xRel = (double) x / 15.0D * 2.0D - 1.0D;

                    if (checkAffectedPosition(e, xRel, yRel, zRel))
                    {
                        return;
                    }
                }
            }
        }
    }

    private static void getAffectedPositionsOnPlaneZ(Explosion e, int z, int xStart, int xEnd, int yStart, int yEnd)
    {
        if (!rayCalcDone)
        {
            final double zRel = (double) z / 15.0D * 2.0D - 1.0D;

            for (int x = xStart; x <= xEnd; ++x)
            {
                double xRel = (double) x / 15.0D * 2.0D - 1.0D;

                for (int y = yStart; y <= yEnd; ++y)
                {
                    double yRel = (double) y / 15.0D * 2.0D - 1.0D;

                    if (checkAffectedPosition(e, xRel, yRel, zRel))
                    {
                        return;
                    }
                }
            }
        }
    }

    private static boolean checkAffectedPosition(Explosion e, double xRel, double yRel, double zRel)
    {
        ExplosionAccessor eAccess = (ExplosionAccessor) e;
        double len = Math.sqrt(xRel * xRel + yRel * yRel + zRel * zRel);
        double xInc = (xRel / len) * 0.3;
        double yInc = (yRel / len) * 0.3;
        double zInc = (zRel / len) * 0.3;
        World world = eAccess.getWorld();
        float rand = world.random.nextFloat();
        float sizeRand = (CarpetSettings.tntRandomRange >= 0 ? (float) CarpetSettings.tntRandomRange : rand);
        float size = eAccess.getPower() * (0.7F + sizeRand * 0.6F);
        double posX = eAccess.getX();
        double posY = eAccess.getY();
        double posZ = eAccess.getZ();

        for (; size > 0.0F; size -= 0.22500001F)
        {
            posMutable.set(posX, posY, posZ);

            BlockPos posImmutable = null;

            // Don't query already cached positions again from the world
            if (!blastResCache.containsKey(posMutable))
            {
                posImmutable = posMutable.toImmutable();
                BlockState state = world.getBlockState(posImmutable);
                FluidState fluid = world.getFluidState(posImmutable);
                Float resistance = null;
                if (state.getMaterial() != Material.AIR)
                {
                    resistance = Math.max(state.getBlock().getBlastResistance(), fluid.getBlastResistance());
                    if (eAccess.getEntity() != null)
                    {
                        resistance = eAccess.getEntity().getEffectiveExplosionResistance(e, world, posMutable, state, fluid, resistance);
                    }
                }
                blockStateCache.put(posMutable, state);
                blastResCache.put(posMutable, resistance);
            }

            Float blastRes = blastResCache.get(posMutable);
            BlockState state = blockStateCache.get(posMutable);

            if(blastRes != null)
            {
                size -= (blastRes + 0.3F) * 0.3F;
                if (size > 0.0F)
                {
                    if (eAccess.getEntity() == null || eAccess.getEntity().canExplosionDestroyBlock(e, world, posMutable, state, size))
                    {
                        affectedBlockPositionsSet.add(posImmutable != null ? posImmutable : posMutable.toImmutable());
                        blastResCache.put(posMutable, null);
                        blockStateCache.remove(posMutable);
                    }
                }
            }
            if (size <= 0.0F && firstRay)
            {
                rayCalcDone = true;
                return true;
            }

            firstRay = false;

            posX += xInc;
            posY += yInc;
            posZ += zInc;
        }

        return false;
    }
}
