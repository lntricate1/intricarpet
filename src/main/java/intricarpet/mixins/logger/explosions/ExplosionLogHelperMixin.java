package intricarpet.mixins.logger.explosions;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import carpet.logging.LoggerRegistry;
import carpet.logging.logHelpers.ExplosionLogHelper;
import carpet.logging.logHelpers.ExplosionLogHelper.EntityChangedStatusWithCount;
import carpet.utils.Messenger;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.text.BaseText;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.explosion.Explosion;
import intricarpet.logging.logHelpers.intricarpetExplosionLogHelper;

import static carpet.utils.Messenger.c;

@Mixin(ExplosionLogHelper.class)
public class ExplosionLogHelperMixin
{
    @Shadow @Final public Vec3d pos;
    @Shadow @Final private Entity entity;
    @Shadow @Final private boolean createFire;
    @Shadow @Final private float power;
    @Shadow @Final private Explosion.DestructionType blockDestructionType;
    @Shadow private Object2IntMap<EntityChangedStatusWithCount> impactedEntities = new Object2IntOpenHashMap<>();
    @Shadow private boolean affectBlocks;
    @Shadow private static long lastGametime;
    @Shadow private static int explosionCountInCurretGT;

    private static boolean newTick;
    private static Vec3d previousPosition = null;
    private static int explosionCountInCurrentPos = 0;
    private static long startTime = 0;

    @Overwrite(remap = false)
    public void onExplosionDone(long gametime)
    {
        newTick = false;
        intricarpetExplosionLogHelper.affectBlocks = affectBlocks;
        if (!(lastGametime == gametime)){
            explosionCountInCurretGT = 0;
            explosionCountInCurrentPos = 0;
            intricarpetExplosionLogHelper.explosionCountInCurrentPos = 0;
            previousPosition = pos;
            intricarpetExplosionLogHelper.previousPosition = pos;
            lastGametime = gametime;
            newTick = true;
            startTime = System.currentTimeMillis();
            intricarpetExplosionLogHelper.startTime = startTime;
        }
        explosionCountInCurretGT++;
        intricarpetExplosionLogHelper.explosionCountInCurrentGt = explosionCountInCurretGT;
        LoggerRegistry.getLogger("explosions").log( (option) -> {
            double eyes = 1.12;
            List<BaseText> messages = new ArrayList<>();
            if(newTick) messages.add(c("wb tick : ", "d " + gametime));
            if ("brief".equals(option))
                messages.add(c("d #" + explosionCountInCurretGT,"gb :",
                    Messenger.dblt("l", pos.x, pos.y, pos.z),
                    Messenger.c("p  [Tp]", String.format("!/tp %.3f %.3f %.3f", pos.x, pos.y, pos.z)),
                    Messenger.c("p  [TpEyes]", String.format("!/tp %.3f %.3f %.3f", pos.x, pos.y - eyes, pos.z)),
                    (affectBlocks)?"m  damage":"m  no damage",
                    Messenger.c("r  " + EntityType.getId(entity.getType()))
                ));
            if ("full".equals(option))
            {
                messages.add(c("d #" + explosionCountInCurretGT,"gb :", Messenger.dblt("l", pos.x, pos.y, pos.z),
                    Messenger.c("p  [Tp]", String.format("!/tp %.3f %.3f %.3f", pos.x, pos.y, pos.z)),
                    Messenger.c("p  [TpEyes]", String.format("!/tp %.3f %.3f %.3f", pos.x, pos.y - eyes, pos.z))));
                messages.add(c("w   affects blocks: ", "m " + this.affectBlocks));
                messages.add(c("w   creates fire: ", "m " + this.createFire));
                messages.add(c("w   power: ", "c " + this.power));
                messages.add(c( "w   destruction: ", "c " + this.blockDestructionType.name()));
                if (impactedEntities.isEmpty())
                {
                    messages.add(c("w   affected entities: ", "m None"));
                }
                else
                {
                    messages.add(c("w   affected entities:"));
                    impactedEntities.forEach((k, v) ->
                    {
                        messages.add(c((k.pos.equals(pos))?"r   - TNT":"w   - ",
                                Messenger.dblt((k.pos.equals(pos))?"r":"y", k.pos.x, k.pos.y, k.pos.z), "w  dV",
                                Messenger.dblt("d", k.accel.x, k.accel.y, k.accel.z),
                                "w  "+Registry.ENTITY_TYPE.getId(k.type).getPath(), (v>1)?"l ("+v+")":""
                        ));
                    });
                }
            }
            if ("compact".equals(option))
            {
                if (previousPosition != null && !pos.equals(previousPosition))
                {
                    messages.add(Messenger.c("d #" + (explosionCountInCurretGT - explosionCountInCurrentPos) + " ","gb : ",
                        "d " + explosionCountInCurrentPos + "x ",
                        Messenger.dblt("l", previousPosition.x, previousPosition.y, previousPosition.z), (affectBlocks)?"m  damage":"m  no damage",
                        "g  (", "d " + (System.currentTimeMillis() - startTime), "g ms)",
                        Messenger.c("p  [Tp]", String.format("!/tp %.3f %.3f %.3f", previousPosition.x, previousPosition.y, previousPosition.z)),
                        Messenger.c("p  [TpEyes]", String.format("!/tp %.3f %.3f %.3f", previousPosition.x, previousPosition.y - eyes, previousPosition.z))));
                    previousPosition = pos;
                    intricarpetExplosionLogHelper.previousPosition = pos;
                    explosionCountInCurrentPos = 0;
                    intricarpetExplosionLogHelper.explosionCountInCurrentPos = 0;
                    startTime = System.currentTimeMillis();
                    intricarpetExplosionLogHelper.startTime = startTime;
                }
            }
            return messages.toArray(new BaseText[0]);
        });
        explosionCountInCurrentPos ++;
        intricarpetExplosionLogHelper.explosionCountInCurrentPos ++;
    }
}