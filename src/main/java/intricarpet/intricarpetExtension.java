package intricarpet;

import carpet.CarpetExtension;
import carpet.CarpetServer;
import intricarpet.logging.intricarpetLoggerRegisty;

public class intricarpetExtension implements CarpetExtension
{
    public static void noop() { }
    static
    {
        CarpetServer.manageExtension(new intricarpetExtension());
    }

    @Override
    public void registerLoggers()
    {
        intricarpetLoggerRegisty.registerLoggers();
    }
}
