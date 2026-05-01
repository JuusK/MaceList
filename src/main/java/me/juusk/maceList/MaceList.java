package me.juusk.maceList;

import de.tr7zw.nbtapi.NBTFile;
import de.tr7zw.nbtapi.iface.ReadWriteNBT;
import me.juusk.maceList.command.MaceListCommand;
import me.juusk.maceList.command.PurgeCommand;
import me.juusk.maceList.expansion.PlaceholderAPIExpansion;
import me.juusk.maceList.listener.MaceLimiterListener;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class MaceList extends JavaPlugin {
    public static MaceList INSTANCE;
    public static MaceLimiterListener listener;

    @Override
    public void onEnable() {
        INSTANCE = this;
        saveDefaultConfig();
        getCommand("macelist").setExecutor(new MaceListCommand());
        getCommand("purgemaces").setExecutor(new PurgeCommand());
        if(getConfig().getBoolean("macelimiter.enabled")) {
            listener = new MaceLimiterListener();
        }

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PlaceholderAPIExpansion().register();
        }

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public boolean hasMace(UUID uuid) {

        Player player = Bukkit.getPlayer(uuid);

        if (player != null) {
            for (ItemStack item : player.getInventory().getContents()) {
                if (item != null && item.getType() == Material.MACE) {
                    return true;
                }
            }
            for (ItemStack item : player.getEnderChest().getContents()) {
                if (item != null && item.getType() == Material.MACE) {
                    return true;
                }
            }
            return false;
        }

        return hasMaceOffline(uuid);
    }


    public boolean hasMaceOffline(UUID uuid) {

        try {
            File folder = new File(Bukkit.getWorld("world").getWorldFolder(), "playerdata");
            File file = new File(folder, uuid.toString() + ".dat");

            if (!file.exists()) return false;

            NBTFile nbtFile = new NBTFile(file);

            for (ReadWriteNBT item : nbtFile.getCompoundList("Inventory")) {

                String id = item.getString("id");

                if ("minecraft:mace".equals(id)) {
                    return true;
                }

            }
            for (ReadWriteNBT item : nbtFile.getCompoundList("EnderItems")) {

                String id = item.getString("id");

                if ("minecraft:mace".equals(id)) {
                    return true;
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
    public List<UUID> getAllPlayerUUIDs() {

        List<UUID> uuids = new ArrayList<>();

        File folder = new File(Bukkit.getWorld("world").getWorldFolder(), "playerdata");
        File[] files = folder.listFiles();

        if (files == null) return uuids;

        for (File file : files) {

            if (!file.getName().endsWith(".dat")) continue;

            try {
                String uuidString = file.getName().replace(".dat", "");
                uuids.add(UUID.fromString(uuidString));
            } catch (Exception ignored) {}
        }

        return uuids;
    }

    public List<String> getSortedMacePlayers() {

        List<String> expanded = new ArrayList<>();

        for (UUID uuid : getAllPlayerUUIDs()) {

            int count = countMaces(uuid);

            if (count <= 0) continue;

            OfflinePlayer offline = Bukkit.getOfflinePlayer(uuid);
            String name = offline.getName();

            if (name == null) name = uuid.toString(); // fallback

            for (int i = 0; i < count; i++) {
                expanded.add(name);
            }
        }

        expanded.sort(String.CASE_INSENSITIVE_ORDER);

        return expanded;
    }

    public int countMaces(UUID uuid) {

        Player player = Bukkit.getPlayer(uuid);

        if (player != null) {
            return countMaces(player);
        }

        return countMacesOffline(uuid);
    }

    public int countMaces(Player player) {

        int count = 0;

        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == Material.MACE) {
                count += item.getAmount();
            }
        }
        for (ItemStack item : player.getEnderChest().getContents()) {
            if (item != null && item.getType() == Material.MACE) {
                count += item.getAmount();
            }
        }

        return count;
    }

    public int countMacesOffline(UUID uuid) {
        try {
            File folder = new File(Bukkit.getWorld("world").getWorldFolder(), "playerdata");
            File file = new File(folder, uuid.toString() + ".dat");

            if (!file.exists()) return 0;

            NBTFile nbtFile = new NBTFile(file);

            int count = 0;

            for (ReadWriteNBT item : nbtFile.getCompoundList("Inventory")) {
                if ("minecraft:mace".equals(item.getString("id"))) {
                    count += 1;
                }
            }
            for (ReadWriteNBT item : nbtFile.getCompoundList("EnderItems")) {
                if ("minecraft:mace".equals(item.getString("id"))) {
                    count += 1;
                }
            }

            return count;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    public int purgeMaces(UUID uuid) {
        try {
            File folder = new File(Bukkit.getWorld("world").getWorldFolder(), "playerdata");
            File file = new File(folder, uuid.toString() + ".dat");

            if (!file.exists()) return 0;

            NBTFile nbtFile = new NBTFile(file);
            int removed = 0;

            removed += purgeFromNBTList(nbtFile.getCompoundList("Inventory"));
            removed += purgeFromNBTList(nbtFile.getCompoundList("EnderItems"));

            if (removed > 0) nbtFile.save();

            return removed;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    private int purgeFromNBTList(de.tr7zw.nbtapi.iface.ReadWriteNBTCompoundList list) {
        List<Integer> toRemove = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {
            if ("minecraft:mace".equals(list.get(i).getString("id"))) {
                toRemove.add(i);
            }
        }

        for (int i = toRemove.size() - 1; i >= 0; i--) {
            list.remove(toRemove.get(i));
        }

        return toRemove.size();
    }
}
