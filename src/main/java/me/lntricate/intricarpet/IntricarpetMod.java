package me.lntricate.intricarpet;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.commands.CommandSourceStack;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mojang.brigadier.CommandDispatcher;

import carpet.CarpetExtension;
import carpet.CarpetServer;
import me.lntricate.intricarpet.commands.InteractionCommand;
import me.lntricate.intricarpet.logging.LoggerRegistry;

public class IntricarpetMod implements ModInitializer, CarpetExtension
{
  public static final Logger LOGGER = LogManager.getLogger();

  public static final String MOD_ID = "intricarpet";
  public static String MOD_VERSION = "unknown";
  public static String MOD_NAME = "unknown";

  @Override
  public void onInitialize()
  {
    CarpetServer.manageExtension(new IntricarpetMod());
  }

  @Override
  public void onGameStarted()
  {
    ModMetadata metadata = FabricLoader.getInstance().getModContainer(MOD_ID).orElseThrow(RuntimeException::new).getMetadata();
    MOD_NAME = metadata.getName();
    MOD_VERSION = metadata.getVersion().getFriendlyString();

    CarpetServer.settingsManager.parseSettingsClass(Rules.class);
  }

  @Override
  public void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher)
  {
    InteractionCommand.register(dispatcher);
  }

  @Override
  public void registerLoggers()
  {
    LoggerRegistry.registerLoggers();
  }
}
