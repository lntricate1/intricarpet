package me.lntricate.intricarpet.logging;

import carpet.logging.Logger;

public class LoggerRegistry
{
  public static boolean __explosions;

  public static void registerLoggers()
  {
    carpet.logging.LoggerRegistry.registerLogger("explosions", standardLogger("explosions", "brief", new String[]{"compact", "brief", "full"}, true));
  }

  static Logger standardLogger(String logName, String def, String [] options, boolean strictOptions)
  {
    try
    {
      return new Logger(carpet.logging.LoggerRegistry.class.getField("__"+logName), logName, def, options, strictOptions);
    }
    catch(NoSuchFieldException e)
    {
      throw new RuntimeException("Failed to create logger "+logName);
    }
  }
}
