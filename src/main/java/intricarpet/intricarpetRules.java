package intricarpet;

import carpet.settings.Rule;

public class intricarpetRules
{
    @Rule(
        desc = "EVEN MORE Optimized TNT", category = "intricate",
        extra = {"Stores velocity values, this optimization optimizes many tnt that explode in the same spot. For full effectiveness, also enable optimizedTNT."})
    public static boolean optimizedTNTExtra = false;
}
