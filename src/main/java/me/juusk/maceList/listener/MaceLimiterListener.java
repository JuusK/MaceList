package me.juusk.maceList.listener;

import me.juusk.maceList.MaceList;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class MaceLimiterListener implements Listener {

    public MaceLimiterListener() {
        Bukkit.getPluginManager().registerEvents(this, MaceList.INSTANCE);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        ItemStack offhand = event.getPlayer().getInventory().getItemInOffHand();
        if (offhand != null && isMace(offhand)) {
            event.setCancelled(true);
        }

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block block = event.getClickedBlock();
            if (block != null && (isShelf(block.getType()) || isPot(block.getType()))) {
                if(isMace(event.getItem())) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!MaceList.INSTANCE.getConfig().getBoolean("macelimiter.disable-mace-storage")) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;

        ItemStack cursor = event.getCursor();
        ItemStack current = event.getCurrentItem();
        if(event.getClick() == ClickType.SWAP_OFFHAND) {
            ItemStack offhand = player.getInventory().getItemInOffHand();
            if(isMace(current) || isMace(offhand)) {
                event.setCancelled(true);
            }

        }
        if (event.getSlot() == 40) {

            if ((cursor != null && isMace(cursor)) ||
                    (current != null && isMace(current))) {
                event.setCancelled(true);
            }
        }
        if (current != null && isBundle(current) || isMace(current)) {
            if (cursor != null && isMace(cursor) || isBundle(cursor)) {
                event.setCancelled(true);
            }
        }

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
    public void onSwap(PlayerSwapHandItemsEvent event) {
        if(isMace(event.getOffHandItem())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!MaceList.INSTANCE.getConfig().getBoolean("macelimiter.disable-mace-storage")) return;

        ItemStack cursor = event.getOldCursor();
        if (!isMace(cursor)) return;
        if (event.getInventorySlots().contains(40)) {
            event.setCancelled(true);
            return;
        }
        int topSize = event.getView().getTopInventory().getSize();

        for (int slot : event.getRawSlots()) {
            if (slot < topSize) {
                event.setCancelled(true);
                return;
            }
        }
    }
    @EventHandler
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        if (!MaceList.INSTANCE.getConfig().getBoolean("macelimiter.disable-mace-storage")) return;

        if (!(isStorage(event.getRightClicked()))) return;

        ItemStack held = event.getPlayer().getInventory().getItem(event.getHand());

        if (isMace(held)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityPickup(EntityPickupItemEvent event) {
        if (!MaceList.INSTANCE.getConfig().getBoolean("macelimiter.disable-mace-storage")) return;

        ItemStack item = event.getItem().getItemStack();

        if (isMace(item) && isStorage(event.getEntity())) {
            event.setCancelled(true);
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

    private boolean isStorage(Entity type) {
        return (type instanceof ItemFrame
                || type instanceof Allay
                || type instanceof ArmorStand
                || type instanceof Fox
        );
    }

    private boolean isBundle(ItemStack item) {
        if (item == null) return false;

        Material type = item.getType();
        return type == Material.BUNDLE
                || type == Material.WHITE_BUNDLE
                || type == Material.ORANGE_BUNDLE
                || type == Material.MAGENTA_BUNDLE
                || type == Material.LIGHT_BLUE_BUNDLE
                || type == Material.YELLOW_BUNDLE
                || type == Material.LIME_BUNDLE
                || type == Material.PINK_BUNDLE
                || type == Material.GRAY_BUNDLE
                || type == Material.LIGHT_GRAY_BUNDLE
                || type == Material.CYAN_BUNDLE
                || type == Material.PURPLE_BUNDLE
                || type == Material.BLUE_BUNDLE
                || type == Material.BROWN_BUNDLE
                || type == Material.GREEN_BUNDLE
                || type == Material.RED_BUNDLE
                || type == Material.BLACK_BUNDLE;
    }
    private boolean isShelf(Material type) {
        return type == Material.ACACIA_SHELF
                || type == Material.BAMBOO_SHELF
                || type == Material.BIRCH_SHELF
                || type == Material.SPRUCE_SHELF
                || type == Material.CHERRY_SHELF
                || type == Material.CRIMSON_SHELF
                || type == Material.JUNGLE_SHELF
                || type == Material.OAK_SHELF
                || type == Material.MANGROVE_SHELF
                || type == Material.WARPED_SHELF
                || type == Material.DARK_OAK_SHELF
                || type == Material.PALE_OAK_SHELF
                || type == Material.CHISELED_BOOKSHELF;
    }

    private boolean isPot(Material type) {
        return type == Material.DECORATED_POT;
    }

}
