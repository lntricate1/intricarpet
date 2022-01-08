package intricarpet.mixins.logger.tnt;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import carpet.logging.LoggerRegistry;
import carpet.logging.logHelpers.TNTLogHelper;
import carpet.utils.Messenger;
import net.minecraft.text.BaseText;
import net.minecraft.util.math.Vec3d;

@Mixin(TNTLogHelper.class)
public class TNTLogHelperMixin
{
    @Shadow private double primedX, primedY, primedZ;
    @Shadow private static long lastGametime = 0;
    @Shadow private static int tntCount = 0;
    @Shadow private Vec3d primedAngle;

    @Overwrite(remap = false)
    public void onExploded(double x, double y, double z, long gametime)
    {
        if (!(lastGametime == gametime)){
            tntCount = 0;
            lastGametime = gametime;
        }
        tntCount++;
        LoggerRegistry.getLogger("tnt").log( (option) -> {
            switch (option)
            {
                case "brief":
                    return new BaseText[]{Messenger.c(
                            "l P ",Messenger.dblt("l", primedX, primedY, primedZ),
                            Messenger.c("p  [Tp]", String.format("!/tp %.3f %.3f %.3f", primedX, primedY, primedZ)),
                            "w  ",Messenger.dblt("l", primedAngle.x, primedAngle.y, primedAngle.z),
                            "r  E ",Messenger.dblt("r", x, y, z),
                            Messenger.c("p  [Tp]", String.format("!/tp %.3f %.3f %.3f", x, y, z)))};
                case "full":
                    return new BaseText[]{Messenger.c(
                            "r #" + tntCount,
                            "m @" + gametime,
                            "g : ",
                            "l P ",Messenger.dblf("l",primedX,primedY,primedZ),
                            Messenger.c("p  [Tp]", String.format("!/tp %.3f %.3f %.3f", primedX, primedY, primedZ)),
                            "w  ",Messenger.dblf("l", primedAngle.x, primedAngle.y, primedAngle.z),
                            "r  E ",Messenger.dblf("r",x, y, z),
                            Messenger.c("p  [Tp]", String.format("!/tp %.3f %.3f %.3f", x, y, z)))};
            }
            return null;
        });
    }
}
