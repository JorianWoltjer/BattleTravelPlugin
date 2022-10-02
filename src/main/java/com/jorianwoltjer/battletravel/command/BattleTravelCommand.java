package com.jorianwoltjer.battletravel.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import com.jorianwoltjer.battletravel.BattleTravel;

import static com.jorianwoltjer.battletravel.BattleTravel.plugin;

import java.util.*;
import java.util.List;

public class BattleTravelCommand implements CommandExecutor, TabCompleter, Listener {
    static BukkitTask timer;
    static BukkitTask pointer;
    public static BossBar bossBar = Bukkit.createBossBar("", BarColor.BLUE, BarStyle.SEGMENTED_12);
    static Location start;
    static boolean gameStarted = false;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(args.length > 0) {
            if (args[0].equals("start")) {
                Player player = (Player) sender;

                if (timer != null) {
                    reset();
                }
                start = player.getLocation();
                start.setYaw(180);
                gameStarted = true;
                Bukkit.getWorld("world").setTime(1000);

                for (int i = 0; i < 15; i++) {
                    Bukkit.broadcastMessage(" ");
                }
                Bukkit.broadcastMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "Go " + ChatColor.GOLD + "" + ChatColor.BOLD + "North!");
                for (Player p : Bukkit.getOnlinePlayers()) {
                    sendTitle(p, ChatColor.YELLOW + "" + ChatColor.BOLD + "Go " + ChatColor.GOLD + "" + ChatColor.BOLD + "North!", "");
                    p.teleport(start);

                    // Reset player
                    p.setHealth(20);
                    p.setSaturation(5);
                    p.setFoodLevel(20);
                    p.getInventory().clear();
                    giveCompass(p);
                }

                Bukkit.broadcastMessage(ChatColor.DARK_GREEN + "Started Game for " + ChatColor.GREEN + Bukkit.getOnlinePlayers().size() +
                        ChatColor.DARK_GREEN + " players at " + ChatColor.GREEN +
                        "[" + start.getBlockX() + ", " + start.getBlockY() + ", " + start.getBlockZ() + "]");

                timer = new BukkitRunnable() {

                    int i = 3600;

                    @Override
                    public void run() {
                        if (i == 3600) {
                            for (Player p : Bukkit.getOnlinePlayers()) {
                                bossBar.addPlayer(p);
                            }
                        } else if (i <= 0) {
                            for (int i = 0; i < 10; i++) {
                                Bukkit.broadcastMessage("");
                            }
                            Bukkit.broadcastMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "Game Over!");
                            reset();
//                            cancel();
                        } else if (i % 300 == 0) {
                            Bukkit.broadcastMessage(ChatColor.AQUA + "" + ChatColor.BOLD + secondsToString(i) + ChatColor.DARK_AQUA + " left");
                            for (Player p : Bukkit.getOnlinePlayers()) {
                                sendTitle(p, "", ChatColor.AQUA + "" + ChatColor.BOLD + secondsToString(i) + ChatColor.DARK_AQUA + " left");
                                p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 10, 1);
                            }
                            Bukkit.broadcastMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Current standings:");
                            sendStandings();
                        }

                        bossBar.setTitle(ChatColor.AQUA + "" + ChatColor.BOLD + secondsToString(i) + ChatColor.DARK_AQUA + " left");
                        bossBar.setProgress(i / 3600D);

                        i--;
                    }
                }.runTaskTimer(BattleTravel.plugin, 0L, 20L);

                pointer = new BukkitRunnable() {

                    int i = 300;

                    @Override
                    public void run() {
                        if (i <= 0) {
                            for (Player p : Bukkit.getOnlinePlayers()) {
                                p.sendActionBar(" ");
                            }
                            cancel();
                        } else {
                            for (Player p : Bukkit.getOnlinePlayers()) {
                                String arrow = "";
                                switch (p.getFacing()) {
                                    case NORTH:
                                        arrow = "^";
                                        break;
                                    case EAST:
                                        arrow = "<";
                                        break;
                                    case SOUTH:
                                        arrow = "V";
                                        break;
                                    case WEST:
                                        arrow = ">";
                                        break;
                                }
                                if (arrow.equals("^")) {
                                    p.sendActionBar(ChatColor.GREEN + "" + ChatColor.BOLD + arrow);
                                } else if ((i / 5) % 2 == 0) {
                                    p.sendActionBar(ChatColor.RED + "" + ChatColor.BOLD + arrow);
                                } else {
                                    p.sendActionBar(ChatColor.DARK_RED + "" + ChatColor.BOLD + arrow);
                                }
                            }
                        }
                        i--;
                    }
                }.runTaskTimer(BattleTravel.plugin, 0L, 1L);
                return true;
            } else if (args[0].equals("cancel")) {
                Bukkit.broadcastMessage(ChatColor.RED + "Manually Cancelled Timer.");
                reset();
//                timer.cancel();
                return true;
            }
        }
        return false;
    }

    private void sendTitle(Player player, String title, String subtitle) {
        Component titleComponent = Component.text(title);
        Component subtitleComponent = Component.text(subtitle);
        Title timeTitle = Title.title(titleComponent, subtitleComponent);
        player.showTitle(timeTitle);
    }

    private void giveCompass(Player p) {
        ItemStack compass = new ItemStack(Material.COMPASS);
        ItemMeta compassMeta = compass.getItemMeta();
        compassMeta.setDisplayName(ChatColor.RESET + "" + ChatColor.BOLD + "Player Tracker");
        compass.setItemMeta(compassMeta);
        p.getInventory().setItem(0, compass);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("start", "cancel");
        }
        return new ArrayList<>();
    }

    public void reset() {
        bossBar.removeAll();
        if (gameStarted) {
            Bukkit.broadcastMessage(ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "Final standings:");
            sendStandings();
            // Display winner
            for (Player p : Bukkit.getOnlinePlayers()) {
                sendTitle(p, ChatColor.GOLD + "" + ChatColor.BOLD + "Game Over!",
                        ChatColor.GOLD + "" + ChatColor.BOLD + getWinner() + ChatColor.YELLOW + ChatColor.BOLD + " has won the game!");
            }
            gameStarted = false;
        }
        timer.cancel();
        pointer.cancel();
    }

    private String secondsToString(int pTime) {
        return String.format("%02d:%02d", pTime / 60, pTime % 60);
    }

    public void sendStandings() {
        LinkedHashMap<String, Integer> scores = new LinkedHashMap<>();

        for (Player p : Bukkit.getOnlinePlayers()) {
            scores.put(p.getName(), -(p.getLocation().getBlockZ() - start.getBlockZ()));
        }
        scores = (LinkedHashMap<String, Integer>) sortByValue(scores);
        List<String> reverseOrderedKeys = new ArrayList<String>(scores.keySet());
        Collections.reverse(reverseOrderedKeys);
        for (String key : reverseOrderedKeys) {
            Bukkit.broadcastMessage(" - " + ChatColor.GOLD + key + ChatColor.YELLOW + " is at " +
                    ChatColor.GOLD + "" + ChatColor.BOLD + scores.get(key) + "m" + ChatColor.YELLOW + " from the start");
        }
    }

    public String getWinner() {
        String winner = "";
        double winnerScore = Float.NEGATIVE_INFINITY;

        for (Player p : Bukkit.getOnlinePlayers()) {
            double score = -(p.getLocation().getZ() - start.getBlockZ());
            if (score > winnerScore) {
                winnerScore = score;
                winner = p.getName();
            }
        }

        return winner;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (gameStarted) {
            bossBar.addPlayer(e.getPlayer());
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        if (gameStarted) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin,
                    () -> giveCompass(e.getPlayer()),
                    1L);
        }
    }

    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new ArrayList<>(map.entrySet());
        list.sort(Map.Entry.comparingByValue());

        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }
}
