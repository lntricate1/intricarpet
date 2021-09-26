package intricarpet;

import carpet.CarpetExtension;
import intricarpet.logging.intricarpetLoggerRegisty;

public class intricarpetExtension implements CarpetExtension
{
    @Override
    public void registerLoggers()
    {
        intricarpetLoggerRegisty.registerLoggers();
    }
}
