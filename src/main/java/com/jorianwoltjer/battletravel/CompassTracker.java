package com.jorianwoltjer.battletravel;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.HashMap;

public class CompassTracker implements Listener {
    static HashMap<Player, Player> trackerMap = new HashMap<>();

    void update(Player p) {
        Player targetPlayer = trackerMap.get(p);
        if (targetPlayer == null) {
            targetPlayer = getClosestPlayer(p);
        }
        p.setCompassTarget(targetPlayer.getLocation());

        if (p.getInventory().getItemInMainHand().getType() == Material.COMPASS) {
            int distMeters = (int) distance(p, targetPlayer);
            p.sendActionBar("Tracking " + ChatColor.YELLOW + targetPlayer.getName() + ChatColor.RESET + " (" + distMeters + "m)");
        }
    }

    public static Player getClosestPlayer(Player p) {
        Player bestPlayer = null;
        double bestDistance = Float.POSITIVE_INFINITY;

        for (Player p2 : Bukkit.getOnlinePlayers()) {
            if (p2 != p) {
                double d = distance(p, p2);
                if (d < bestDistance) {
                    bestDistance = d;
                    bestPlayer = p2;
                }
            }
        }

        return bestPlayer;
    }

    public static double distance(Player p1, Player p2) {
        double x = Math.abs(p1.getLocation().getX() - p2.getLocation().getX());
        double z = Math.abs(p1.getLocation().getZ() - p2.getLocation().getZ());
        return Math.sqrt(Math.pow(x, 2) + Math.pow(z, 2));
    }

    @EventHandler
    void onCompassUse(PlayerInteractEvent e) {
        if (e.getItem() != null && e.getItem().getType() == Material.COMPASS && e.getAction().isRightClick()) {
            openCompassMenu(e.getPlayer());
        }
    }

    void openCompassMenu(Player targetPlayer) {
        int menuSize = Bukkit.getOnlinePlayers().size() / 9 + 1;
        Inventory menu = Bukkit.createInventory(null, menuSize*9, "Player Select");
        ItemStack closestItem = new ItemStack(Material.ARROW);
        ItemMeta closestMeta = closestItem.getItemMeta();
        closestMeta.setDisplayName(ChatColor.YELLOW + "Closest Player");
        closestItem.setItemMeta(closestMeta);
        menu.addItem(closestItem);

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p != targetPlayer) {
                ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
                SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
                skullMeta.setOwningPlayer(p);
                skullMeta.setDisplayName(ChatColor.RESET + p.getName());
                skull.setItemMeta(skullMeta);
                menu.addItem(skull);
            }
        }
        targetPlayer.openInventory(menu);
    }

    @EventHandler
    void onCompassMenuClick(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        if (e.getView().getTitle().equals("Player Select")) {
            e.setCancelled(true);
            if (e.getCurrentItem() != null && e.getCurrentItem().getType() == Material.PLAYER_HEAD) {  // Selected a player
                Player targetPlayer = Bukkit.getPlayer(e.getCurrentItem().getItemMeta().getDisplayName());
                if (targetPlayer != null) {
                    p.sendMessage("Selected " + ChatColor.YELLOW + targetPlayer.getName());
                    trackerMap.put(p, targetPlayer);
                }
            } else if (e.getCurrentItem() != null && e.getCurrentItem().getType() == Material.ARROW) {  // Closest player
                p.sendMessage("Selected " + ChatColor.YELLOW + "Closest Player");
                trackerMap.remove(p);
            } else {  // Nothing selected
                return;
            }
            e.getView().close();
            update(p);
        }
    }
}
