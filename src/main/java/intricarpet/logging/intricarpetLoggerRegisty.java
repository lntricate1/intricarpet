package intricarpet.logging;

import carpet.logging.Logger;
import carpet.logging.LoggerRegistry;

public class intricarpetLoggerRegisty
{
    public static boolean __explosions;
    
    public static void registerLoggers()
    {
        LoggerRegistry.registerLogger("explosions", defaultStandardLogger("explosions", "brief", new String[]{"compact", "brief", "full"}));
    }

    static Logger standardLogger(String logName, String def, String [] options)
    {
        try
        {
            return new Logger(intricarpetLoggerRegisty.class.getField("__"+logName), logName, def, options);
        }
        catch (NoSuchFieldException e)
        {
            throw new RuntimeException("Failed to create logger "+logName);
        }
    }

    static Logger defaultStandardLogger(String logName, String def, String [] options)
    {
        try
        {
            return new Logger(LoggerRegistry.class.getField("__"+logName), logName, def, options);
        }
        catch (NoSuchFieldException e)
        {
            throw new RuntimeException("Failed to create logger "+logName);
        }
    }
}
