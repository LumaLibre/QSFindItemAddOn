package io.mysticbeans.finditemaddon.Utils;

import io.mysticbeans.finditemaddon.FindItemAddOn;
import me.kodysimpson.simpapi.colors.ColorTranslator;
import org.bukkit.Bukkit;

public class LoggerUtils {
    public static void logDebugInfo(String text) {
        if(FindItemAddOn.getConfigProvider().DEBUG_MODE) {
            Bukkit.getLogger().warning(ColorTranslator.translateColorCodes("[QSFindItemAddOn-DebugLog] &e" + text));
        }
    }
    public static void logInfo(String text) {
        Bukkit.getLogger().info(ColorTranslator.translateColorCodes("[QSFindItemAddOn] " + text));
    }
    public static void logError(String text) {
        Bukkit.getLogger().severe(ColorTranslator.translateColorCodes("[QSFindItemAddOn] &c" + text));
    }
    public static void logWarning(String text) {
        Bukkit.getConsoleSender().sendMessage(ColorTranslator.translateColorCodes("[QSFindItemAddOn] &6" + text));
    }
}
