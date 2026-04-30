package me.juusk.maceList.command;

import me.juusk.maceList.MaceList;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.*;

public class MaceListCommand implements CommandExecutor {


    public MaceListCommand() {

    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        Map<String, Integer> playersWithMace = new HashMap<String, Integer>();

        for (UUID uuid : MaceList.INSTANCE.getAllPlayerUUIDs()) {

            int maces = MaceList.INSTANCE.countMaces(uuid);
            if (maces <= 0) continue;

            OfflinePlayer offline = Bukkit.getOfflinePlayer(uuid);
            String name = offline.getName();

            if (name == null) name = uuid.toString();

            playersWithMace.put(name, maces);
        }

        StringBuilder message = new StringBuilder("§6§lPlayers with a mace:\n");

        playersWithMace.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .forEach(entry -> message.append("§e")
                        .append(entry.getKey())
                        .append(" §7(x")
                        .append(entry.getValue())
                        .append(")\n"));

        sender.sendMessage(message.toString());
        return true;
    }
}