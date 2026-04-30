package me.juusk.maceList.command;

import me.juusk.maceList.MaceList;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.*;

public class PurgeCommand implements CommandExecutor {

    private static final long THIRTY_DAYS_MS = 30L * 24 * 60 * 60 * 1000;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length == 0) {
            sender.sendMessage("§cUsage: /macepurge <days> [confirm]");
            return true;
        }

        int days;
        try {
            days = Integer.parseInt(args[0]);
            if (days <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            sender.sendMessage("§cInvalid number of days: §f" + args[0]);
            return true;
        }

        boolean confirm = args.length > 1 && args[1].equalsIgnoreCase("confirm");
        long cutoff = (long) days * 24 * 60 * 60 * 1000;

        List<UUID> candidates = new ArrayList<>();
        int totalMaces = 0;

        for (UUID uuid : MaceList.INSTANCE.getAllPlayerUUIDs()) {

            if (Bukkit.getPlayer(uuid) != null) continue;

            OfflinePlayer offline = Bukkit.getOfflinePlayer(uuid);
            long lastPlayed = offline.getLastPlayed();

            if (lastPlayed == 0) continue;
            if (System.currentTimeMillis() - lastPlayed < cutoff) continue;

            int maces = MaceList.INSTANCE.countMaces(uuid);
            if (maces <= 0) continue;

            candidates.add(uuid);
            totalMaces += maces;
        }

        if (candidates.isEmpty()) {
            sender.sendMessage("§aNo maces found on players inactive for " + days + "+ days.");
            return true;
        }

        if (!confirm) {
            sender.sendMessage("§6§lMace Purge Preview:");
            sender.sendMessage("§7Players affected: §e" + candidates.size());
            sender.sendMessage("§7Total maces to remove: §e" + totalMaces);
            sender.sendMessage("§cRun §f/macepurge " + days + " confirm §cto execute.");
            return true;
        }

        int purgedPlayers = 0;
        int purgedMaces = 0;

        for (UUID uuid : candidates) {
            int removed = MaceList.INSTANCE.purgeMaces(uuid);
            if (removed > 0) {
                purgedPlayers++;
                purgedMaces += removed;
                OfflinePlayer offline = Bukkit.getOfflinePlayer(uuid);
                String name = offline.getName() != null ? offline.getName() : uuid.toString();
                sender.sendMessage("§7Purged §e" + removed + " §7mace(s) from §e" + name);
            }
        }

        sender.sendMessage("§a§lDone! Removed §e" + purgedMaces + " §a§lmace(s) from §e" + purgedPlayers + " §a§lplayer(s).");
        return true;
    }
}

