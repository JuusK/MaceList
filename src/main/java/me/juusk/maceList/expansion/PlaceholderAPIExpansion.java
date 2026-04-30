package me.juusk.maceList.expansion;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.juusk.maceList.MaceList;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.ArrayList;
import java.util.List;

public class PlaceholderAPIExpansion extends PlaceholderExpansion {

    public PlaceholderAPIExpansion() {
    }

    @Override
    public String getIdentifier() {
        return "macelist";
    }

    @Override
    public String getAuthor() {
        return "JuusK";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {

        if (params.equalsIgnoreCase("has")) {
            return MaceList.INSTANCE.hasMace(player.getUniqueId()) ? "yes" : "no";
        }

        if (params.equalsIgnoreCase("list")) {
            List<String> list = new ArrayList<>();

            for (OfflinePlayer p : Bukkit.getOfflinePlayers()) {
                if (MaceList.INSTANCE.hasMace(p.getUniqueId())) {
                    list.add(p.getName());
                }
            }

            return String.join(", ", list);
        }


        if (params.startsWith("top_")) {

            try {
                int index = Integer.parseInt(params.replace("top_", "")) - 1;

                List<String> sorted = MaceList.INSTANCE.getSortedMacePlayers();

                if (index < 0 || index >= 10) return "None";
                if (index >= sorted.size()) return "None";

                return sorted.get(index);

            } catch (NumberFormatException e) {
                return "None";
            }
        }

        return null;
    }
}
