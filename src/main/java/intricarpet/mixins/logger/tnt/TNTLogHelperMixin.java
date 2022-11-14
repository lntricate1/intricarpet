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
    if (!(lastGametime == gametime))
    {
      tntCount = 0;
      lastGametime = gametime;
    }
    tntCount++;
    LoggerRegistry.getLogger("tnt").log( (option) -> {
      double eyes = 1.12;
      switch (option)
      {
        case "brief":
          return new BaseText[]{(BaseText)Messenger.c(
            "l ",Messenger.dblt("l", primedX, primedY, primedZ),
            Messenger.c("p  [Tp]", String.format("!/tp %.3f %.3f %.3f", primedX, primedY, primedZ)),
            Messenger.c("p  [TpEyes]", String.format("!/tp %.3f %.3f %.3f", primedX, primedY - eyes, primedZ)),
            "w  ",Messenger.dblt("m", primedAngle.x, primedAngle.y, primedAngle.z),
            "w  ",Messenger.dblt("r", x, y, z),
            Messenger.c("p  [Tp]", String.format("!/tp %.3f %.3f %.3f", x, y, z)),
            Messenger.c("p  [TpEyes]", String.format("!/tp %.3f %.3f %.3f", x, y - eyes, z))
          )};
        case "full":
          return new BaseText[]{(BaseText)Messenger.c(
            "r #" + tntCount,
            "m @" + gametime,
            "g : ",
            "l ",Messenger.dblf("l",primedX,primedY,primedZ),
            Messenger.c("p  [Tp]", String.format("!/tp %.3f %.3f %.3f", primedX, primedY, primedZ)),
            Messenger.c("p  [TpEyes]", String.format("!/tp %.3f %.3f %.3f", primedX, primedY - eyes, primedZ)),
            "w  ",Messenger.dblf("m", primedAngle.x, primedAngle.y, primedAngle.z),
            "r  ",Messenger.dblf("r",x, y, z),
            Messenger.c("p  [Tp]", String.format("!/tp %.3f %.3f %.3f", x, y, z)),
            Messenger.c("p  [TpEyes]", String.format("!/tp %.3f %.3f %.3f", x, y - eyes, z))
          )};
      }
      return null;
    });
  }
}
