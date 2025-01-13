package me.zyromate.skullwarsportals.cmds;

import de.tr7zw.nbtapi.NBT;
import me.zyromate.skullwarsportals.SkullWarsPortals;
import me.zyromate.skullwarsportals.Utils.ChatUtils;
import eu.decentsoftware.holograms.api.DHAPI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class PortalCommand implements CommandExecutor {
    private final SkullWarsPortals plugin;
    private final ChatUtils chatUtils = new ChatUtils();

    public PortalCommand(SkullWarsPortals plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            List<String> usageMessages = plugin.getConfig().getStringList("skullwarsportals.usage");
            if (!usageMessages.isEmpty()) {
                for (String message : usageMessages) {
                    sender.sendMessage(chatUtils.colour(message));
                }
            } else {
                sender.sendMessage(chatUtils.colour("&cNo usage information available in the configuration!"));
            }
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                handleReload(sender);
                break;

            case "removeallportals":
                handleRemoveAllPortals(sender);
                break;

            case "giveremover":
                if (args.length < 2) {
                    List<String> usageMessages = plugin.getConfig().getStringList("skullwarsportals.usage");
                    if (!usageMessages.isEmpty()) {
                        for (String message : usageMessages) {
                            sender.sendMessage(chatUtils.colour(message));
                        }
                    } else {
                        sender.sendMessage(chatUtils.colour("&cNo usage information available in the configuration!"));
                    }
                } else {
                    handleGiveRemover(sender, args[1]);
                }
                break;

            default:
                List<String> usageMessages = plugin.getConfig().getStringList("skullwarsportals.usage");
                if (!usageMessages.isEmpty()) {
                    for (String message : usageMessages) {
                        sender.sendMessage(chatUtils.colour(message));
                    }
                } else {
                    sender.sendMessage(chatUtils.colour("&cUnknown subcommand! Use /skullwarsportals <reload/removeallportals/giveremover>."));
                }
                break;
        }
        return true;
    }


    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("skullwarsportals.reload")) {
            sender.sendMessage(chatUtils.colour(plugin.getConfig().getString("messages.no-permission")));
            return;
        }

        try {
            plugin.reloadConfig();
            sender.sendMessage(chatUtils.colour(plugin.getConfig().getString("messages.reload")));
        } catch (Exception e) {
            sender.sendMessage(chatUtils.colour("&cFailed to reload the configuration!"));
            e.printStackTrace();
        }
    }

    private void handleRemoveAllPortals(CommandSender sender) {
        if (!sender.hasPermission("skullwarsportals.deleteallportals")) {
            sender.sendMessage(chatUtils.colour(plugin.getConfig().getString("messages.no-permission")));
            return;
        }

        int numberOfPortalsRemoved = plugin.getPortalLocations().size();

        if (numberOfPortalsRemoved == 0) {
            sender.sendMessage(chatUtils.colour(plugin.getConfig().getString("messages.no-portals-to-remove")));
            return;
        }

        List<String> portalsToRemove = new ArrayList<>();

        for (Map.Entry<String, Set<Location>> entry : plugin.getPortalLocations().entrySet()) {
            Set<Location> allPortalLocations = getAllPortalBlocks(entry.getValue());

            new BukkitRunnable() {
                @Override
                public void run() {
                    allPortalLocations.forEach(loc -> loc.getBlock().setType(Material.AIR));
                    DHAPI.removeHologram(entry.getKey());
                }
            }.runTask(plugin);

            portalsToRemove.add(entry.getKey());
        }

        portalsToRemove.forEach(key -> plugin.getPortalLocations().remove(key));

        plugin.getPortalDataManager().savePortalLocations(plugin.getPortalLocations());

        String successMessage = plugin.getConfig()
                .getString("messages.portal-delete-success")
                .replace("%NumberOfPortalsRemoved%", String.valueOf(numberOfPortalsRemoved));

        sender.sendMessage(chatUtils.colour(successMessage));
    }

    private Set<Location> getAllPortalBlocks(Set<Location> portalBlocks) {
        Set<Location> allBlocksToRemove = new HashSet<>(portalBlocks);

        for (Location portalBlock : portalBlocks) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dz == 0) continue;
                    Location checkLoc = portalBlock.clone().add(dx, 0, dz);
                    if (checkLoc.getBlock().getType() == Material.ENDER_PORTAL_FRAME) {
                        allBlocksToRemove.add(checkLoc);
                    }
                }
            }
        }

        return allBlocksToRemove;
    }

    private void handleGiveRemover(CommandSender sender, String playerName) {
        if (!sender.hasPermission("skullwarsportals.giveremover")) {
            sender.sendMessage(chatUtils.colour(plugin.getConfig().getString("messages.no-permission")));
            return;
        }

        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            sender.sendMessage(chatUtils.colour(plugin.getConfig().getString("messages.player-not-found")));
            return;
        }

        givePortalRemover(target);
        target.sendMessage(chatUtils.colour(plugin.getConfig().getString("messages.remover-received")));
    }

    private void givePortalRemover(Player player) {
        String itemTypeString = plugin.getConfig().getString("items.portal-remover.type");
        int itemDamage = plugin.getConfig().getInt("items.portal-remover.damage");
        String itemName = plugin.getConfig().getString("items.portal-remover.name");
        List<String> loreList = plugin.getConfig().getStringList("items.portal-remover.lore");

        Material itemType = Material.getMaterial(itemTypeString);
        if (itemType == null) {
            player.sendMessage(chatUtils.colour(plugin.getConfig().getString("messages.invalid-item-type")));
            return;
        }

        ItemStack itemStack = new ItemStack(itemType);
        itemStack.setDurability((short) itemDamage);

        ItemMeta meta = itemStack.getItemMeta();
        meta.setDisplayName(chatUtils.colour(itemName));
        meta.setLore(chatUtils.colourList(loreList));
        itemStack.setItemMeta(meta);
        NBT.modify(itemStack, nbt -> {
            if (nbt instanceof de.tr7zw.nbtapi.NBTItem) {
                de.tr7zw.nbtapi.NBTItem nbtItem = (de.tr7zw.nbtapi.NBTItem) nbt;
                nbtItem.setBoolean("isPortalBreaker", true);
            }
        });

        player.getInventory().addItem(itemStack);
    }

}
