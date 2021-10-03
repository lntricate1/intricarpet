package intricarpet.helpers;
//Author: masa

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.enchantment.ProtectionEnchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.explosion.Explosion;
import carpet.logging.logHelpers.ExplosionLogHelper;
import carpet.mixins.ExplosionAccessor;
import carpet.CarpetSettings;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import static carpet.script.CarpetEventServer.Event.EXPLOSION_OUTCOME;

public class OptimizedExplosion
{
    private static List<Entity> entitylist;
    public static Map<Box, List<Object>> velocityList;
    private static Vec3d vec3dmem;
    private static long tickmem;
    // For disabling the explosion particles and sound
    public static int explosionSound = 0;

    // masa's optimizations
    private static Object2ObjectOpenHashMap<BlockPos, BlockState> stateCache = new Object2ObjectOpenHashMap<>();
    private static Object2ObjectOpenHashMap<BlockPos, FluidState> fluidCache = new Object2ObjectOpenHashMap<>();
    private static BlockPos.Mutable posMutable = new BlockPos.Mutable(0, 0, 0);
    private static ObjectOpenHashSet<BlockPos> affectedBlockPositionsSet = new ObjectOpenHashSet<>();
    private static boolean firstRay;
    private static boolean rayCalcDone;
    private static ArrayList<Float> chances = new ArrayList<>();
    private static BlockPos blastChanceLocation;

    // Creating entity list for scarpet event
    private static List<Entity> entityList = new ArrayList<>();

    public static void doExplosionA(Explosion e, ExplosionLogHelper eLogger) {
        ExplosionAccessor eAccess = (ExplosionAccessor) e;
        
        entityList.clear();
        boolean eventNeeded = EXPLOSION_OUTCOME.isNeeded() && !eAccess.getWorld().isClient();
        blastCalc(e);

        if (!CarpetSettings.explosionNoBlockDamage) {
            rayCalcDone = false;
            firstRay = true;
            getAffectedPositionsOnPlaneY(e,  0,  0, 15,  0, 15); // bottom
            getAffectedPositionsOnPlaneY(e, 15,  0, 15,  0, 15); // top
            getAffectedPositionsOnPlaneX(e,  0,  1, 14,  0, 15); // west
            getAffectedPositionsOnPlaneX(e, 15,  1, 14,  0, 15); // east
            getAffectedPositionsOnPlaneZ(e,  0,  1, 14,  1, 14); // north
            getAffectedPositionsOnPlaneZ(e, 15,  1, 14,  1, 14); // south
            stateCache.clear();
            fluidCache.clear();

            e.getAffectedBlocks().addAll(affectedBlockPositionsSet);
            affectedBlockPositionsSet.clear();
        }

        float f3 = eAccess.getPower() * 2.0F;
        int k1 = MathHelper.floor(eAccess.getX() - (double) f3 - 1.0D);
        int l1 = MathHelper.floor(eAccess.getX() + (double) f3 + 1.0D);
        int i2 = MathHelper.floor(eAccess.getY() - (double) f3 - 1.0D);
        int i1 = MathHelper.floor(eAccess.getY() + (double) f3 + 1.0D);
        int j2 = MathHelper.floor(eAccess.getZ() - (double) f3 - 1.0D);
        int j1 = MathHelper.floor(eAccess.getZ() + (double) f3 + 1.0D);
        Vec3d vec3d = new Vec3d(eAccess.getX(), eAccess.getY(), eAccess.getZ());

        if (vec3dmem == null || !vec3dmem.equals(vec3d) || tickmem != eAccess.getWorld().getTime()) {
            vec3dmem = vec3d;
            tickmem = eAccess.getWorld().getTime();
            entitylist = eAccess.getWorld().getOtherEntities(null, new Box(k1, i2, j2, l1, i1, j1));
            explosionSound = 0;

            Map<Box, List<Object>> newVelocityList = new HashMap<>();
            for(Entity e_ : entitylist)
            {
                Box box = e_.getBoundingBox();
                if(velocityList != null && velocityList.containsKey(box))
                {
                    newVelocityList.put(box, velocityList.get(box));
                    continue;
                }
                List<Object> result = getVelocity(vec3d, f3, e_, e_.getPos(), box, e_.getEyeY(), e_ instanceof TntEntity);
                newVelocityList.put(box, result);
            }
            velocityList = newVelocityList;
        }

        explosionSound++;

        Entity explodingEntity = eAccess.getEntity();
        for (int k2 = 0; k2 < entitylist.size(); ++k2) {
            Entity entity = entitylist.get(k2);

            if (entity == explodingEntity) {
                removeFast(entitylist, k2);
                k2--;
                continue;
            }

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
                List<Object> velResult = velocityList.get(box);
                if(velResult == null) continue;

                Vec3d vel = (Vec3d) velResult.get(0);
                Vec3d velTransformed = (Vec3d) velResult.get(1);
                float damage = (float) velResult.get(2);

                if (eventNeeded) entityList.add(entity);

                entity.damage(e.getDamageSource(), damage);

                if (eLogger != null) eLogger.onEntityImpacted(entity, velTransformed);

                entity.setVelocity(entity.getVelocity().add(velTransformed));

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
        double d1 = MathHelper.sqrt(epos.squaredDistanceTo(px, py, pz)) / (double) power;
        if(d1 > 1.0D) return null;
        double x1 = ex - px;
        double y1 = (tnt ? ey : eyeY) - py;
        double z1 = ez - pz;
        double d2 = (double) MathHelper.sqrt(x1 * x1 + y1 * y1 + z1 * z1);
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
        float sizeRand = (CarpetSettings.tntRandomRange >= 0 ? (float) CarpetSettings.tntRandomRange : eAccess.getWorld().random.nextFloat());
        float size = eAccess.getPower() * (0.7F + sizeRand * 0.6F);
        double posX = eAccess.getX();
        double posY = eAccess.getY();
        double posZ = eAccess.getZ();

        for (; size > 0.0F; size -= 0.22500001F)
        {
            posMutable.set(posX, posY, posZ);

            // Don't query already cached positions again from the world
            BlockState state = stateCache.get(posMutable);
            FluidState fluid = fluidCache.get(posMutable);
            BlockPos posImmutable = null;

            if (state == null)
            {
                posImmutable = posMutable.toImmutable();
                state = eAccess.getWorld().getBlockState(posImmutable);
                stateCache.put(posImmutable, state);
                fluid = eAccess.getWorld().getFluidState(posImmutable);
                fluidCache.put(posImmutable, fluid);
            }

            if (state.getMaterial() != Material.AIR)
            {
                float resistance = Math.max(state.getBlock().getBlastResistance(), fluid.getBlastResistance());

                if (eAccess.getEntity() != null)
                {
                    resistance = eAccess.getEntity().getEffectiveExplosionResistance(e, eAccess.getWorld(), posMutable, state, fluid, resistance);
                }

                size -= (resistance + 0.3F) * 0.3F;
            }

            if (size > 0.0F)
            {
                if ((eAccess.getEntity() == null || eAccess.getEntity().canExplosionDestroyBlock(e, eAccess.getWorld(), posMutable, state, size)))
                    affectedBlockPositionsSet.add(posImmutable != null ? posImmutable : posMutable.toImmutable());
            }
            else if (firstRay)
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

    public static void setBlastChanceLocation(BlockPos p){
        blastChanceLocation = p;
    }

    private static void blastCalc(Explosion e){
        ExplosionAccessor eAccess = (ExplosionAccessor) e;
        if(blastChanceLocation == null || blastChanceLocation.getSquaredDistance(eAccess.getX(), eAccess.getY(), eAccess.getZ(), false) > 200) return;
        chances.clear();
        for (int j = 0; j < 16; ++j) {
            for (int k = 0; k < 16; ++k) {
                for (int l = 0; l < 16; ++l) {
                    if (j == 0 || j == 15 || k == 0 || k == 15 || l == 0 || l == 15) {
                        double d0 = (double) ((float) j / 15.0F * 2.0F - 1.0F);
                        double d1 = (double) ((float) k / 15.0F * 2.0F - 1.0F);
                        double d2 = (double) ((float) l / 15.0F * 2.0F - 1.0F);
                        double d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
                        d0 = d0 / d3;
                        d1 = d1 / d3;
                        d2 = d2 / d3;
                        float f = eAccess.getPower() * (0.7F + 0.6F);
                        double d4 = eAccess.getX();
                        double d6 = eAccess.getY();
                        double d8 = eAccess.getZ();
                        boolean found = false;

                        for (; f > 0.0F; f -= 0.22500001F) {
                            BlockPos blockpos = new BlockPos(d4, d6, d8);
                            BlockState state = eAccess.getWorld().getBlockState(blockpos);
                            FluidState fluidState = eAccess.getWorld().getFluidState(blockpos);

                            if (state.getMaterial() != Material.AIR) {
                                float f2 = Math.max(state.getBlock().getBlastResistance(), fluidState.getBlastResistance());
                                if (eAccess.getEntity() != null)
                                    f2 = eAccess.getEntity().getEffectiveExplosionResistance(e, eAccess.getWorld(), blockpos, state, fluidState, f2);
                                f -= (f2 + 0.3F) * 0.3F;
                            }

                            if (f > 0.0F && (eAccess.getEntity() == null ||
                                    eAccess.getEntity().canExplosionDestroyBlock(e, eAccess.getWorld(), blockpos, state, f))) {
                                if(!found && blockpos.equals(blastChanceLocation)){
                                    chances.add(f);
                                    found = true;
                                }
                            }

                            d4 += d0 * 0.30000001192092896D;
                            d6 += d1 * 0.30000001192092896D;
                            d8 += d2 * 0.30000001192092896D;
                        }
                    }
                }
            }
        }

        //showTNTblastChance(e);
    }
}
