package me.juusk.maceList.listener;

import me.juusk.maceList.MaceList;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class MaceLimiterListener implements Listener {

    public MaceLimiterListener() {
        Bukkit.getPluginManager().registerEvents(this, MaceList.INSTANCE);
    }


    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!MaceList.INSTANCE.getConfig().getBoolean("macelimiter.disable-mace-storage")) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;

        ItemStack cursor = event.getCursor();
        ItemStack current = event.getCurrentItem();


        if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
            ItemStack item = event.getCurrentItem();
            if (item == null || !isMace(item)) return;
            boolean fromPlayer = event.getClickedInventory().getType() == InventoryType.PLAYER;
            if (fromPlayer && isStorage(event.getView().getTopInventory().getType())) {
                event.setCancelled(true);
            }
        }
        if (event.getClickedInventory() == null) return;
        if (!isStorage(event.getClickedInventory().getType())) return;

        if (isMace(cursor)) {
            event.setCancelled(true);
            return;
        }

        if (cursor != null && isMace(cursor)
                && event.getClickedInventory() != null
                && isStorage(event.getClickedInventory().getType())) {
            event.setCancelled(true);
            return;
        }





        if (event.getAction() == InventoryAction.HOTBAR_SWAP
                || event.getAction() == InventoryAction.HOTBAR_MOVE_AND_READD) {

            int hotbarSlot = event.getHotbarButton();
            if (hotbarSlot >= 0) {
                ItemStack hotbarItem = player.getInventory().getItem(hotbarSlot);
                if (isMace(hotbarItem)) {
                    event.setCancelled(true);
                }
            }
        }
    }
    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!MaceList.INSTANCE.getConfig().getBoolean("macelimiter.disable-mace-storage")) return;

        ItemStack cursor = event.getOldCursor();
        if (!isMace(cursor)) return;

        int topSize = event.getView().getTopInventory().getSize();

        for (int slot : event.getRawSlots()) {
            if (slot < topSize) {
                event.setCancelled(true);
                return;
            }
        }
    }
    @EventHandler
    public void onInventoryMoveItem(InventoryMoveItemEvent event) {
        if (!MaceList.INSTANCE.getConfig().getBoolean("macelimiter.disable-mace-storage")) return;

        if (isMace(event.getItem()) && isStorage(event.getDestination().getType())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onHopperPickup(InventoryPickupItemEvent event) {
        if (!MaceList.INSTANCE.getConfig().getBoolean("macelimiter.disable-mace-storage")) return;

        Item entity = event.getItem();
        ItemStack stack = entity.getItemStack();

        if (isMace(stack) && isStorage(event.getInventory().getType())) {
            event.setCancelled(true);
        }
    }

    private boolean isMace(ItemStack item) {
        return item != null && item.getType() == Material.MACE;
    }

    private boolean isStorage(InventoryType type) {
        return type != InventoryType.PLAYER
                && type != InventoryType.CRAFTING
                && type != InventoryType.WORKBENCH
                && type != InventoryType.ANVIL
                && type != InventoryType.ENCHANTING
                && type != InventoryType.GRINDSTONE
                && type != InventoryType.SMITHING
                && type != InventoryType.STONECUTTER
                && type != InventoryType.LOOM
                && type != InventoryType.CARTOGRAPHY;
    }

}
