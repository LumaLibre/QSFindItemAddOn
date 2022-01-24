package me.ronsane.finditemaddon.finditemaddon.GUIHandler.Menus;

import io.papermc.lib.PaperLib;
import me.ronsane.finditemaddon.finditemaddon.Dependencies.EssentialsXPlugin;
import me.ronsane.finditemaddon.finditemaddon.Dependencies.PlayerWarpsPlugin;
import me.ronsane.finditemaddon.finditemaddon.Dependencies.WGPlugin;
import me.ronsane.finditemaddon.finditemaddon.FindItemAddOn;
import me.ronsane.finditemaddon.finditemaddon.GUIHandler.PaginatedMenu;
import me.ronsane.finditemaddon.finditemaddon.GUIHandler.PlayerMenuUtility;
import me.ronsane.finditemaddon.finditemaddon.Models.FoundShopItemModel;
import me.ronsane.finditemaddon.finditemaddon.Utils.CommonUtils;
import me.ronsane.finditemaddon.finditemaddon.Utils.LocationUtils;
import me.ronsane.finditemaddon.finditemaddon.Utils.LoggerUtils;
import me.ronsane.finditemaddon.finditemaddon.Utils.WarpUtils.EssentialWarpsUtil;
import me.ronsane.finditemaddon.finditemaddon.Utils.WarpUtils.PlayerWarpsUtil;
import me.ronsane.finditemaddon.finditemaddon.Utils.WarpUtils.WGRegionUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class FoundShopsMenu extends PaginatedMenu {

    public FoundShopsMenu(PlayerMenuUtility playerMenuUtility, List<FoundShopItemModel> searchResult) {
        super(playerMenuUtility, searchResult);
    }

    @Override
    public String getMenuName() {
        if(!StringUtils.isEmpty(FindItemAddOn.getConfigProvider().SHOP_SEARCH_GUI_TITLE)) {
            return CommonUtils.parseColors(FindItemAddOn.getConfigProvider().SHOP_SEARCH_GUI_TITLE);
        }
        else {
            return CommonUtils.parseColors("&l» &rShops");
        }
    }

    @Override
    public int getSlots() {
        return 54;
    }

    @Override
    public void handleMenu(InventoryClickEvent event) {
        if(event.getCurrentItem().getType().equals(super.backButton.getType())) {
            if(page == 0) {
                if(!StringUtils.isEmpty(FindItemAddOn.getConfigProvider().SHOP_NAV_FIRST_PAGE_ALERT_MSG)) {
                    event.getWhoClicked().sendMessage(
                            CommonUtils.parseColors(FindItemAddOn.getConfigProvider().PLUGIN_PREFIX
                                    + FindItemAddOn.getConfigProvider().SHOP_NAV_FIRST_PAGE_ALERT_MSG));
                }
            }
            else {
                page = page - 1;
                super.open(super.playerMenuUtility.getPlayerShopSearchResult());
            }
        }
        else if(event.getCurrentItem().getType().equals(super.nextButton.getType())) {
            if(!((index + 1) >= super.playerMenuUtility.getPlayerShopSearchResult().size())) {
                page = page + 1;
                super.open(super.playerMenuUtility.getPlayerShopSearchResult());
            }
            else {
                if(!StringUtils.isEmpty(FindItemAddOn.getConfigProvider().SHOP_NAV_LAST_PAGE_ALERT_MSG)) {
                    event.getWhoClicked().sendMessage(
                            CommonUtils.parseColors(FindItemAddOn.getConfigProvider().PLUGIN_PREFIX + FindItemAddOn.getConfigProvider().SHOP_NAV_LAST_PAGE_ALERT_MSG));
                }
            }
        }
        else if(event.getCurrentItem().getType().equals(Material.BARRIER)) {
            event.getWhoClicked().closeInventory();
        }
        else if(event.getCurrentItem().getType().equals(Material.AIR)) {
            // do nothing
        }
        else {
            if(FindItemAddOn.getConfigProvider().ALLOW_DIRECT_SHOP_TP) {
                if(playerMenuUtility.getOwner().hasPermission("finditem.shoptp")) {
                    Player player = (Player) event.getWhoClicked();

                    ItemStack item = event.getCurrentItem();
                    ItemMeta meta = item.getItemMeta();
                    NamespacedKey key = new NamespacedKey(FindItemAddOn.getInstance(), "locationData");
                    if(!meta.getPersistentDataContainer().has(key, PersistentDataType.STRING)) {
                        return;
                    }
                    else {
                        String locData = meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
                        List<String> locDataList = Arrays.asList(locData.split("\\s*,\\s*"));

                        World world = Bukkit.getWorld(locDataList.get(0));
                        int locX = Integer.parseInt(locDataList.get(1)), locY = Integer.parseInt(locDataList.get(2)), locZ = Integer.parseInt(locDataList.get(3));
                        Location shopLocation = new Location(world, locX, locY, locZ);
                        Location locToTeleport = LocationUtils.findSafeLocationAroundShop(shopLocation);
                        if(locToTeleport != null) {
                            PaperLib.teleportAsync(player, locToTeleport, PlayerTeleportEvent.TeleportCause.PLUGIN);
                        }
                        else {
                            if(!StringUtils.isEmpty(FindItemAddOn.getConfigProvider().UNSAFE_SHOP_AREA_MSG)) {
                                player.sendMessage(CommonUtils.parseColors(FindItemAddOn.getConfigProvider().PLUGIN_PREFIX + FindItemAddOn.getConfigProvider().UNSAFE_SHOP_AREA_MSG));
                            }
                        }
                        player.closeInventory();
                    }
                }
                else {
                    if(!StringUtils.isEmpty(FindItemAddOn.getConfigProvider().SHOP_TP_NO_PERMISSION_MSG)) {
                        playerMenuUtility.getOwner()
                                .sendMessage(CommonUtils.parseColors(FindItemAddOn.getConfigProvider().PLUGIN_PREFIX + FindItemAddOn.getConfigProvider().SHOP_TP_NO_PERMISSION_MSG));
                        event.getWhoClicked().closeInventory();
                    }
                }
            }
        }
    }

    @Override
    public void setMenuItems() {}

    /**
     * Sets the slots in the search result GUI
     * @param foundShops List of found shops
     */
    @Override
    public void setMenuItems(List<FoundShopItemModel> foundShops) {
        addMenuBottomBar();
        if(foundShops != null && !foundShops.isEmpty()) {
            int guiSlotCounter = 0;
            while(guiSlotCounter < super.maxItemsPerPage) {
                index = super.maxItemsPerPage * page + guiSlotCounter;

                if(index >= foundShops.size())  break;

                if(foundShops.get(index) != null) {
                    // Place Search Results here
                    FoundShopItemModel shop = foundShops.get(index);
                    NamespacedKey key = new NamespacedKey(FindItemAddOn.getInstance(), "locationData");
                    ItemStack item = new ItemStack(shop.getItem().getType());
                    ItemMeta meta = item.getItemMeta();
                    List<String> lore;
                    lore = new ArrayList<>();

                    if(shop.getItem().hasItemMeta()) {
                        meta = shop.getItem().getItemMeta();
                        if(shop.getItem().getItemMeta().hasLore()) {
                            for(String s : shop.getItem().getItemMeta().getLore()) {
                                lore.add(CommonUtils.parseColors(s));
                            }
                        }
                    }
                    List<String> shopItemLore = FindItemAddOn.getConfigProvider().SHOP_GUI_ITEM_LORE;
                    for(String shopItemLore_i : shopItemLore) {
                        if(shopItemLore_i.contains("{NEAREST_WARP}")) {
                            switch(FindItemAddOn.getConfigProvider().NEAREST_WARP_MODE) {
                                case 1:
                                    // EssentialWarp: Check nearest warp
                                    if(EssentialsXPlugin.isEnabled()) {
                                        String nearestPlayerWarp = new EssentialWarpsUtil().findNearestWarp(shop.getLocation());
                                        if(nearestPlayerWarp != null && !StringUtils.isEmpty(nearestPlayerWarp)) {
                                            lore.add(CommonUtils.parseColors(shopItemLore_i.replace("{NEAREST_WARP}", nearestPlayerWarp)));
                                        }
                                        else {
                                            lore.add(CommonUtils.parseColors(shopItemLore_i.replace("{NEAREST_WARP}", "No Warp near this shop")));
                                        }
                                    }
                                    break;
                                case 2:
                                    // PlayerWarp: Check nearest warp
                                    if(PlayerWarpsPlugin.getIsEnabled()) {
                                        com.olziedev.playerwarps.api.warp.Warp nearestPlayerWarp = new PlayerWarpsUtil().findNearestWarp(shop.getLocation());
                                        if(nearestPlayerWarp != null) {
                                            lore.add(CommonUtils.parseColors(shopItemLore_i.replace("{NEAREST_WARP}", nearestPlayerWarp.getWarpName())));
                                        }
                                        else {
                                            lore.add(CommonUtils.parseColors(shopItemLore_i.replace("{NEAREST_WARP}", "No Warp near this shop")));
                                        }
                                    }
                                    break;
                                case 3:
                                    // WG Region: Check nearest WG Region
                                    if(WGPlugin.isEnabled()) {
                                        String nearestWGRegion = new WGRegionUtils().findNearestWGRegion((shop.getLocation()));
                                        if(nearestWGRegion != null && !StringUtils.isEmpty(nearestWGRegion)) {
                                            lore.add(CommonUtils.parseColors(shopItemLore_i.replace("{NEAREST_WARP}", nearestWGRegion)));
                                        }
                                        else {
                                            lore.add(CommonUtils.parseColors(shopItemLore_i.replace("{NEAREST_WARP}", "No Region near this shop")));
                                        }
                                    }
                                    break;
                                default:
                                    LoggerUtils.logDebugInfo("Invalid value in 'nearest-warp-mode' in config.yml!");
                            }

                        }
                        else {
                            lore.add(CommonUtils.parseColors(replaceLorePlaceholders(shopItemLore_i, shop)));
                        }
                    }

                    if(FindItemAddOn.getConfigProvider().ALLOW_DIRECT_SHOP_TP) {
                        if(playerMenuUtility.getOwner().hasPermission("finditem.shoptp")) {
                            lore.add(CommonUtils.parseColors(FindItemAddOn.getConfigProvider().CLICK_TO_TELEPORT_MSG));
                        }
                    }
                    assert meta != null;
                    meta.setLore(lore);

                    // storing location data in item persistent storage
                    String locData = Objects.requireNonNull(shop.getLocation().getWorld()).getName() + ","
                            + shop.getLocation().getBlockX() + ","
                            + shop.getLocation().getBlockY() + ","
                            + shop.getLocation().getBlockZ();
                    meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, locData);

                    // handling custom model data
                    if(Objects.requireNonNull(shop.getItem().getItemMeta()).hasCustomModelData()) {
                        meta.setCustomModelData(shop.getItem().getItemMeta().getCustomModelData());
                    }
                    item.setItemMeta(meta);
                    inventory.addItem(item);
                }
                guiSlotCounter++;
            }
        }
    }

    /**
     * Replaces all the placeholders in the Shop item lore in GUI
     * @param text Line of lore
     * @param shop Shop instance
     * @return Line of lore replaced with placeholder values
     */
    private String replaceLorePlaceholders(String text, FoundShopItemModel shop) {

        if(text.contains("{ITEM_PRICE}")) {
            text = text.replace("{ITEM_PRICE}", String.valueOf(shop.getPrice()));
        }
        if(text.contains("{SHOP_STOCK}")) {
            text = text.replace("{SHOP_STOCK}", String.valueOf(shop.getRemainingStock()));
        }
        if(text.contains("{SHOP_OWNER}")) {
            text = text.replace("{SHOP_OWNER}", Objects.requireNonNull(Bukkit.getOfflinePlayer(shop.getOwner()).getName()));
        }
        if(text.contains("{SHOP_LOC}")) {
            text = text.replace("{SHOP_LOC}",
                    shop.getLocation().getBlockX() + ", "
                    + shop.getLocation().getBlockY() + ", "
                    + shop.getLocation().getBlockZ());
        }
        if(text.contains("{SHOP_WORLD}")) {
            text = text.replace("{SHOP_WORLD}", Objects.requireNonNull(shop.getLocation().getWorld()).getName());
        }
        return text;
    }
}
