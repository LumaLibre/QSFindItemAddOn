package io.myzticbean.finditemaddon.utils;

import io.myzticbean.finditemaddon.FindItemAddOn;
import io.myzticbean.finditemaddon.utils.log.Logger;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import me.kodysimpson.simpapi.colors.ColorTranslator;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@UtilityClass
public class PlayerUtil {
    public void sendMessage(HumanEntity player, String message) {
        FindItemAddOn.getScheduler().runAtEntity(player, (t) -> player.sendMessage(ColorTranslator.translateColorCodes(message)));
    }

    @SneakyThrows
    public void teleport(Player player, Location locToTeleport) {
        FindItemAddOn.getScheduler()
                .teleportAsync(player, locToTeleport, PlayerTeleportEvent.TeleportCause.PLUGIN)
                .thenAccept(isTeleported -> Logger.logDebugInfo("Player teleported to shop: " + isTeleported));
    }

    public boolean hasPermission(Player player, String permission) {
        if (Bukkit.isPrimaryThread()) {
            return player.hasPermission(permission);
        } else {
            try {
                CompletableFuture<Boolean> future = new CompletableFuture<>();
                FindItemAddOn.getScheduler().runAtEntity(player, (t) -> future.complete(player.hasPermission(permission)));
                return future.get();
            } catch (InterruptedException | ExecutionException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
    }
}
