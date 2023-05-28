package me.lntricate.intricarpet.interactions;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public enum Interaction
{
  BLOCKS      ("blocks",       "Blocks"),
  CHUNKLOADING("chunkloading", "Chunkloading"),
  ENTITIES    ("entities",     "Entities"),
  MOBSPAWNING ("mobSpawning",  "Mob Spawning"),
  RANDOMTICKS ("randomTicks",  "Random Ticks"),
  UPDATES     ("updates",      "Updates");

  private final String name;
  private final String commandKey;

  private static final Map<Interaction, Boolean> defaultInteractions = new HashMap<>();
  private static final Map<String, Interaction> byCommandKey = new HashMap<>();
  private static final Map<UUID, Map<Interaction, Boolean>> playerInteractionMap = new HashMap<>();

  static
  {
    for(Interaction i : values())
    {
      defaultInteractions.put(i, true);
      byCommandKey.put(i.commandKey, i);
    }
  }

  Interaction(String commandKey, String name)
  {
    this.commandKey = commandKey;
    this.name = name;
  }

  public static String[] commandKeys()
  {
    return byCommandKey.keySet().toArray(new String[0]);
  }

  public static Interaction byCommandKey(String key)
  {
    return byCommandKey.get(key);
  }

  public String getName()
  {
    return name;
  }

  public static Map<Interaction, Boolean> get(UUID id)
  {
    Map<Interaction, Boolean> interactions = playerInteractionMap.get(id);
    if(interactions != null)
      return interactions;

    interactions = new HashMap<>(defaultInteractions);
    playerInteractionMap.put(id, interactions);
    return interactions;
  }

  public static void set(UUID id, Interaction i, boolean value)
  {
    playerInteractionMap.get(id).put(i, value);
  }
}
