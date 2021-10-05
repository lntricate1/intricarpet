package intricarpet;

import carpet.settings.Rule;

public class intricarpetRules
{
    @Rule(
        desc = "EVEN MORE Optimized TNT", category = "intricate",
        extra = {"Stores velocity values, this optimization optimizes many tnt that explode in the same spot. For full effectiveness, also enable optimizedTNT."})
    public static boolean optimizedTNTExtra = false;

    @Rule(desc = "Makes TNT act like normal blocks when blown up", category = "intricate")
    public static boolean disableTNTChainReaction = false;

    @Rule(
        desc = "Makes either all or no chunks behave like Slime Chunks",
        options = {"vanilla", "all", "none"},
        category = "intricate")
    public static String slimeChunks = "vanilla";
}
