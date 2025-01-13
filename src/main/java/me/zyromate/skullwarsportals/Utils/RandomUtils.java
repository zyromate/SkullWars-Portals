package me.zyromate.skullwarsportals.Utils;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public class RandomUtils {

    public static void sendInitializationMessage(JavaPlugin plugin, String message) {
        String version = plugin.getDescription().getVersion();
        String author = plugin.getDescription().getAuthors().isEmpty() ? "Unknown" : plugin.getDescription().getAuthors().get(0); // Get author

        // Sending each line separately to the console with color formatting
        plugin.getServer().getConsoleSender().sendMessage(ChatColor.YELLOW + "[INFO]: -------------------------");
        plugin.getServer().getConsoleSender().sendMessage(ChatColor.YELLOW + "[INFO]: ");
        plugin.getServer().getConsoleSender().sendMessage(ChatColor.YELLOW + "[INFO]: SkullWars Portals: " + ChatColor.GOLD + message);
        plugin.getServer().getConsoleSender().sendMessage(ChatColor.YELLOW + "[INFO]: Version: " + ChatColor.GOLD + version);
        plugin.getServer().getConsoleSender().sendMessage(ChatColor.YELLOW + "[INFO]: Author: " + ChatColor.GOLD + author);
        plugin.getServer().getConsoleSender().sendMessage(ChatColor.YELLOW + "[INFO]: ");
        plugin.getServer().getConsoleSender().sendMessage(ChatColor.YELLOW + "[INFO]: -------------------------");
    }
}
