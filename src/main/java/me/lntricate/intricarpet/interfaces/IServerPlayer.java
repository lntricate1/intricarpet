package me.lntricate.intricarpet.interfaces;

import java.util.Map;

import me.lntricate.intricarpet.interactions.Interaction;

public interface IServerPlayer
{
  public boolean getInteraction(Interaction key);
  public Map<Interaction, Boolean> getInteractions();
  public void setInteraction(Interaction key, boolean value);
}
