package intricarpet.logging.logHelpers;

import carpet.utils.Messenger;
import carpet.logging.logHelpers.ExplosionLogHelper;
import carpet.logging.LoggerRegistry;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.text.BaseText;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.explosion.Explosion;

public class intricarpetExplosionLogHelper extends ExplosionLogHelper
{
    private static int explosionCountInCurretGT = 0;
    private static int explosionCountInCurretPos = 0;
    private static long lastGametime = 0;
    private static Vec3d previousPosition = null;
    private static boolean affectBlocks = false;
    private static long startTime = 0;

    public intricarpetExplosionLogHelper(Entity entity, double x, double y, double z, float power, boolean createFire, Explosion.DestructionType blockDestructionType)
    {
        super(entity, x, y, z, power, createFire, blockDestructionType);
    }

    public void onExplosionDone(long gametime)
    {
        if (lastGametime != gametime)
        {
            explosionCountInCurretPos = 0;
            explosionCountInCurretGT = 1;
            previousPosition = pos;
            lastGametime = gametime;
            startTime = System.currentTimeMillis();
        }
        LoggerRegistry.getLogger("explosions").log((option) ->
        {
            List<BaseText> messages = new ArrayList<>();
            if ("compact".equals(option))
            {
                if (previousPosition != null && !pos.equals(previousPosition))
                {
                    messages.add(Messenger.c("d #" + explosionCountInCurretGT + " ","gb -> ",
                        "d " + explosionCountInCurretPos + "x ",
                        Messenger.dblt("l", previousPosition.x, previousPosition.y, previousPosition.z), (affectBlocks)?"m  (affects blocks)":"m  (doesn't affect blocks)",
                        "g  (", "d " + (System.currentTimeMillis() - startTime), "g ms)"));
                    previousPosition = pos;
                    explosionCountInCurretGT += explosionCountInCurretPos;
                    explosionCountInCurretPos = 0;
                    startTime = System.currentTimeMillis();
                }
            }
            return messages.toArray(new BaseText[0]);
        });
        explosionCountInCurretPos++;
    }

    public static void logLastExplosions()
    {
        List<BaseText> messages = new ArrayList<>();
        LoggerRegistry.getLogger("explosions").log((option) ->
        {
            if ("compact".equals(option))
            {
                if(previousPosition != null)
                {
                    messages.add(Messenger.c("d #" + explosionCountInCurretGT + " ","gb -> ",
                        "d " + explosionCountInCurretPos + "x ",
                        Messenger.dblt("l", previousPosition.x, previousPosition.y, previousPosition.z), (affectBlocks)?"m  (affects blocks)":"m  (doesn't affect blocks)",
                        "g  (", "d " + (System.currentTimeMillis() - startTime), "g ms)"));
                }
            }
            return messages.toArray(new BaseText[0]);
        });
        previousPosition = null;
        startTime = 0;
    }
}