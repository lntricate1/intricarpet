package intricarpet.logging.logHelpers;

import carpet.utils.Messenger;
import carpet.logging.LoggerRegistry;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.text.BaseText;
import net.minecraft.util.math.Vec3d;

public class intricarpetExplosionLogHelper
{
    public static int explosionCountInCurrentGt = 0;
    public static int explosionCountInCurrentPos = 0;
    public static Vec3d previousPosition = null;
    public static boolean affectBlocks = false;
    public static long startTime = 0;

    public static void logLastExplosions()
    {
        List<BaseText> messages = new ArrayList<>();
        LoggerRegistry.getLogger("explosions").log((option) ->
        {
            if ("compact".equals(option))
            {
                if(previousPosition != null)
                {
                    messages.add(Messenger.c("d #" + explosionCountInCurrentGt + " ","gb -> ",
                        "d " + explosionCountInCurrentPos + "x ",
                        Messenger.dblt("l", previousPosition.x, previousPosition.y, previousPosition.z), (affectBlocks)?"m  (affects blocks)":"m  (doesn't affect blocks)",
                        "g  (", "d " + (System.currentTimeMillis() - startTime), "g ms)",
                        Messenger.c("r  [Tp]", String.format("!/tp %.3f %.3f %.3f", previousPosition.x, previousPosition.y, previousPosition.z))));
                }
            }
            return messages.toArray(new BaseText[0]);
        });
        previousPosition = null;
        startTime = 0;
    }
}