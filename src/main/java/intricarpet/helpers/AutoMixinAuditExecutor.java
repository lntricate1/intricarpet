package intricarpet.helpers;

import carpet.utils.Messenger;
import intricarpet.intricarpetExtension;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.BaseText;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.MixinEnvironment;
import net.minecraft.text.Text;

public class AutoMixinAuditExecutor {
    private static final String KEYWORD_PROPERTY = "intricarpet.mixin_audit";

    public static void run() {
        if (FabricLoader.getInstance().isDevelopmentEnvironment() && "true".equals(System.getProperty(KEYWORD_PROPERTY))) {
            intricarpetExtension.LOGGER.info("Triggered auto mixin audit");
            boolean ok = audit(null);
            intricarpetExtension.LOGGER.info("Mixin audit result: " + (ok ? "successful" : "failed"));
            System.exit(ok ? 0 : 1);
        }
    }

    public static boolean audit(@Nullable ServerCommandSource source) {
        boolean ok;
        //#if MC>=11900
        //$$ Text response;
        //#else
        BaseText response;
        //#endif
        try {
            MixinEnvironment.getCurrentEnvironment().audit();
            response = Messenger.s("Mixin environment audited successfully");
            ok = true;
        } catch (Exception e) {
            intricarpetExtension.LOGGER.error("Error when auditing mixin", e);
            response = Messenger.s(String.format("Mixin environment auditing failed, check console for more information (%s)", e));
            ok = false;
        }
        if (source != null) {
            source.sendFeedback(response, false);
        }
        return ok;
    }
}