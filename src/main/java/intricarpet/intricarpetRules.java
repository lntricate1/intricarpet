package intricarpet;

import carpet.settings.Rule;

@SuppressWarnings("removal")
public class intricarpetRules
{
  //@Rule(desc = "EVEN MORE Optimized TNT",
  //  extra = {"Stores velocity values, this optimization optimizes many tnt that explode in the same spot. For full effectiveness, also enable optimizedTNT."},
  //  category = "intricate")
  //public static boolean optimizedTNTExtra = false;

  @Rule(desc = "Makes TNT act like normal blocks when blown up",
    category = "intricate")
  public static boolean disableTNTChainReaction = false;

  @Rule(desc = "Makes either all or no chunks behave like Slime Chunks",
    options = {"vanilla", "all", "none"},
    category = "intricate")
  public static String slimeChunks = "vanilla";

  @Rule(desc = "Make sand do the thing",
    extra = {"(The argument is in milliseconds)", "((if you set this higher than 60 seconds you are a FOOL))"},
    category = "intricate")
  public static int fallingBlockThreadSleep = 0;

  @Rule(desc = "Makes tile ticks execute instantly",
    options = {"false", "fluids", "blocks", "all"},
    category = "intricate")
  public static String instantScheduling = "false";
}
