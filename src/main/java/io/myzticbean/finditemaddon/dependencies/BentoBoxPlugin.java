/**
 * QSFindItemAddOn: An Minecraft add-on plugin for the QuickShop Hikari
 * and Reremake Shop plugins for Spigot server platform.
 * Copyright (C) 2021  myzticbean
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package io.myzticbean.finditemaddon.dependencies;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.lists.Flags;

public class BentoBoxPlugin {
    
    private boolean isBentoBoxEnabled = false;

    public BentoBoxPlugin() {
        checkBentoBoxPlugin();
    }

    private void checkBentoBoxPlugin() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("BentoBox");
        isBentoBoxEnabled = plugin != null && plugin.isEnabled();
    }

    public boolean isIslandLocked(Location loc, Player searchingPlayer) {
        if (!isBentoBoxEnabled) {
            return false;
        }
        User bentoboxUser = User.getInstance(searchingPlayer);
        return BentoBox.getInstance()
                .getIslands()
                .getIslandAt(loc)
                .filter(island -> !island.isAllowed(bentoboxUser, Flags.LOCK))
                .isPresent();
    }
}
