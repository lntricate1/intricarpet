package me.lntricate.intricarpet.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import carpet.utils.Messenger;
import carpet.settings.SettingsManager;
import net.minecraft.commands.CommandSourceStack;
import me.lntricate.intricarpet.Rules;
import me.lntricate.intricarpet.interactions.Interaction;
import me.lntricate.intricarpet.interfaces.IServerPlayer;

import java.util.Map;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.BoolArgumentType.getBool;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;
import static net.minecraft.commands.SharedSuggestionProvider.suggest;

public class InteractionCommand
{
  public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
  {
    dispatcher.register(literal("interaction")
      .requires((player) -> SettingsManager.canUseCommand(player, Rules.commandInteraction))
      .then(argument("interaction", StringArgumentType.word())
        .suggests((c, b) -> suggest(Interaction.commandKeys(), b))
        .then(argument("value", BoolArgumentType.bool())
          .executes(InteractionCommand::setInteraction)
        )
        .executes(InteractionCommand::getInteraction)
      )
      .executes(InteractionCommand::getInteractions)
    );
  }

  private static String v(boolean value)
  {
    return (value ? "l " : "r ") + value;
  }

  private static String i(Interaction interaction)
  {
    return "t " + interaction.getName();
  }

  private static int setInteraction(CommandContext<CommandSourceStack> c)
  {
    try
    {
      IServerPlayer player = (IServerPlayer)c.getSource().getPlayerOrException();
      Interaction i = Interaction.byCommandKey(getString(c, "interaction"));
      boolean value = getBool(c, "value");
      player.setInteraction(i, value);
      Messenger.m(c.getSource(), "g Interaction ", i(i), "g  set to ", v(value));
      return 0;
    }
    catch(CommandSyntaxException e)
    {
      Messenger.m(c.getSource(), "r Interaction command must be executed by a player");
      return 1;
    }
  }

  private static int getInteraction(CommandContext<CommandSourceStack> c)
  {
    try
    {
      IServerPlayer player = (IServerPlayer)c.getSource().getPlayerOrException();
      Interaction i = Interaction.byCommandKey(getString(c, "interaction"));
      Messenger.m(c.getSource(), "g Interaction ", i(i), "g  is currently set to ", v(player.getInteraction(i)));
      return 0;
    }
    catch(CommandSyntaxException e)
    {
      Messenger.m(c.getSource(), "r Interaction command must be executed by a player");
      return 1;
    }
  }

  private static int getInteractions(CommandContext<CommandSourceStack> c)
  {
    try
    {
      IServerPlayer player = (IServerPlayer)c.getSource().getPlayerOrException();
      for(Map.Entry<Interaction, Boolean> entry : player.getInteractions().entrySet())
        Messenger.m(c.getSource(), i(entry.getKey()), "g : ", v(entry.getValue()));
      return 0;
    }
    catch(CommandSyntaxException e)
    {
      Messenger.m(c.getSource(), "r Interaction command must be executed by a player");
      return 1;
    }
  }
}
