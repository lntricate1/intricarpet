package intricarpet.mixins.carpet;

import java.util.ArrayList;
import java.util.List;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import carpet.logging.LoggerRegistry;
import carpet.logging.logHelpers.ExplosionLogHelper;
import carpet.logging.logHelpers.ExplosionLogHelper.EntityChangedStatusWithCount;
import carpet.utils.Messenger;

import net.minecraft.text.BaseText;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.explosion.Explosion;

@Mixin(ExplosionLogHelper.class)
public class ExplosionLogHelperMixin
{
    @Shadow @Final public boolean createFire;
    @Shadow @Final public Explosion.DestructionType blockDestructionType;
    @Shadow @Final public Vec3d pos;
    @Shadow @Final float power;
    @Shadow private boolean affectBlocks;
    @Shadow Object2IntMap<EntityChangedStatusWithCount> impactedEntities = new Object2IntOpenHashMap<>();

    @Shadow private static long lastGametime;
    @Shadow private static int explosionCountInCurretGT;

    private boolean newTick;

    @Overwrite
    public void onExplosionDone(long gametime)
    {
        newTick = false;
        if (!(lastGametime == gametime)){
            explosionCountInCurretGT = 0;
            lastGametime = gametime;
            newTick = true;
        }
        explosionCountInCurretGT++;
        LoggerRegistry.getLogger("explosions").log( (option) -> {
            List<BaseText> messages = new ArrayList<>();
            if(newTick) messages.add(Messenger.c("wb tick : ", "d " + gametime));
            if ("brief".equals(option))
            {
                messages.add(Messenger.c("d #" + explosionCountInCurretGT,"gb ->",
                        Messenger.dblt("l", pos.x, pos.y, pos.z), (affectBlocks)?"m  (affects blocks)":"m  (doesn't affect blocks)" ));
            }
            if ("full".equals(option))
            {
                messages.add(Messenger.c("d #" + explosionCountInCurretGT,"gb ->", Messenger.dblt("l", pos.x, pos.y, pos.z) ));
                messages.add(Messenger.c("w   affects blocks: ", "m " + this.affectBlocks));
                messages.add(Messenger.c("w   creates fire: ", "m " + this.createFire));
                messages.add(Messenger.c("w   power: ", "c " + this.power));
                messages.add(Messenger.c( "w   destruction: ",   "c " + this.blockDestructionType.name()));
                if (impactedEntities.isEmpty())
                {
                    messages.add(Messenger.c("w   affected entities: ", "m None"));
                }
                else
                {
                    messages.add(Messenger.c("w   affected entities:"));
                    impactedEntities.forEach((k, v) ->
                    {
                        messages.add(Messenger.c((k.pos.equals(pos))?"r   - TNT":"w   - ",
                                Messenger.dblt((k.pos.equals(pos))?"r":"y", k.pos.x, k.pos.y, k.pos.z), "w  dV",
                                Messenger.dblt("d", k.accel.x, k.accel.y, k.accel.z),
                                "w  "+Registry.ENTITY_TYPE.getId(k.type).getPath(), (v>1)?"l ("+v+")":""
                        ));
                    });
                }
            }
            return messages.toArray(new BaseText[0]);
        });
    }
}
