package intricarpet;

import com.mojang.brigadier.CommandDispatcher;

import carpet.CarpetExtension;
import carpet.CarpetServer;
import carpet.settings.SettingsManager;
import intricarpet.commands.InteractionCommand;
import intricarpet.helpers.AutoMixinAuditExecutor;
import intricarpet.helpers.Interactions;
import intricarpet.logging.intricarpetLoggerRegisty;
import net.fabricmc.api.ModInitializer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class intricarpetExtension implements CarpetExtension, ModInitializer {
    public static final Logger LOGGER = LogManager.getLogger("intricarpet");

    public static void noop() {
    }

    public static SettingsManager intricarpetSettingsManager;

    static {
        intricarpetSettingsManager = new SettingsManager("1.0", "intricarpet", "Intricate Carpet");
        CarpetServer.manageExtension(new intricarpetExtension());
    }

    @Override
    public void onInitialize() {
        AutoMixinAuditExecutor.run();
    }

    @Override
    public void registerLoggers() {
        intricarpetLoggerRegisty.registerLoggers();
    }

    @Override
    public String version() {
        return "intricarpet";
    }

    @Override
    public void onGameStarted()
    {
        CarpetServer.settingsManager.parseSettingsClass(intricarpetRules.class);
    }

    @Override
    public SettingsManager customSettingsManager()
    {
        return intricarpetSettingsManager;
    }

    @Override
    public void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher)
    {
        InteractionCommand.register(dispatcher);
    }

    @Override
    public void onPlayerLoggedIn(ServerPlayerEntity player)
    {
        Interactions.onPlayerConnect(player);
    }

    @Override
    public void onPlayerLoggedOut(ServerPlayerEntity player)
    {
        Interactions.onPlayerDisconnect(player);
    }
}
