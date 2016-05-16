/*
 *            This file is part of TRN-Lobby.
 *
 *  TRN-Lobby is free software: you can redistribute it and/or 
 *  modify it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  TRN-Lobby is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with  TRN-Lobby. 
 *  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package info.therealnuke.lobby;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 *
 * @author <a href="mailto:therealnuke@gmail.com">TheRealNuke</a>
 */
public class SignManager implements Listener {

    private final TreeMap<Location, SignData> signs;
    private final TreeMap<UUID, SignData> editors;
    private final Main plugin;
    private final ReentrantLock signsMutex;

    public SignManager(Main plugin) {
        Comparator<Location> locComp = new Comparator<Location>() {
            @Override
            public int compare(Location loc1, Location loc2) {
                int ret;
                ret = loc1.getWorld().getUID().compareTo(loc2.getWorld().getUID());
                if (ret == 0) {
                    ret = loc1.getBlockX() - loc2.getBlockX();
                    if (ret == 0) {
                        ret = loc1.getBlockY() - loc2.getBlockY();
                    }
                    if (ret == 0) {
                        ret = loc1.getBlockZ() - loc2.getBlockZ();
                    }
                }
                return ret;
            }

        };
        signs = new TreeMap<>(locComp);
        editors = new TreeMap<>();
        signsMutex = new ReentrantLock();
        this.plugin = plugin;
    }

    private enum SignStatus {

        EDITING, LOCATING, ACTIVE
    }

    private class SignData {

        private Sign sign;
        private Location teleportLoc;
        private SignStatus status;
        private final UUID ownerPlayerUUID;

        public SignData(Player player) {
            status = SignStatus.EDITING;
            if (player != null) {
                ownerPlayerUUID = player.getUniqueId();
            } else {
                ownerPlayerUUID = null;
            }
        }

        public void setSign(Sign sign) {
            this.sign = sign;
        }

        public void setStatus(SignStatus status) {
            this.status = status;
        }

        public void setTeleportLoc(Location teleportLoc) {
            this.teleportLoc = teleportLoc;
        }

        public UUID getOwnerPlayerUUID() {
            return ownerPlayerUUID;
        }

        public Location getTeleportLoc() {
            return teleportLoc;
        }

        public SignStatus getStatus() {
            return status;
        }

        public Sign getSign() {
            return sign;
        }
    }

    private void addEditorToList(Player player) {
        SignData sd = new SignData(player);
        if (editors.isEmpty()) {
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
        }
        editors.put(player.getUniqueId(), sd);
    }

    private void removeEditor(Player player) {
        editors.remove(player.getUniqueId());
        if (editors.isEmpty()) {
            HandlerList.unregisterAll(this);
        }
    }

    public void addEditor(Player player) {
        addEditorToList(player);
        plugin.sendMessage(player, "Place a sign in the lobby world!");
        plugin.sendMessage(player, "Type whatever you want on the sign");
        plugin.sendMessage(player, "You can use color codes like in the chat.");
    }

    public boolean isEditor(Player player) {
        return editors.containsKey(player.getUniqueId());
    }

    public boolean canSetDestination(Player player) {
        boolean ret = false;
        SignData sd = editors.get(player.getUniqueId());
        if (sd != null) {
            ret = sd.getStatus() == SignStatus.LOCATING;
        }
        return ret;
    }

    public void setDestination(Player player) {
        SignData sd = editors.get(player.getUniqueId());
        if (sd != null) {
            sd.teleportLoc = player.getLocation();
            sd.status = SignStatus.ACTIVE;
            removeEditor(player);
            signsMutex.lock();
            try {
                signs.put(sd.getSign().getLocation(), sd);
            } finally {
                signsMutex.unlock();
            }
            save();
            plugin.sendMessage(player, ChatColor.GREEN + "The sign destination has been added!");
            plugin.sendMessage(player, "Now, when players hit your sign will be teleported here.");
        }
    }

    /**
     * Capture the sign change event for processing the text and changing sign
     * status.
     *
     * @param e The event
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onSignChange(SignChangeEvent e) {
        final SignData sd = editors.get(e.getPlayer().getUniqueId());
        final Player player = e.getPlayer();
        if (sd != null && sd.status == SignStatus.EDITING) {
            final Block block = e.getBlock();
            Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                @Override
                public void run() {
                    sd.setSign((Sign) block.getState());
                    for (int line = 0; line < 4; line++) {
                        String colouredLine = ChatColor
                                .translateAlternateColorCodes('&', sd.getSign().getLine(line));
                        sd.getSign().setLine(line, colouredLine);
                    }
                    sd.getSign().update();
                    sd.setStatus(SignStatus.LOCATING);
                    plugin.sendMessage(player, ChatColor.GREEN + "Your sign has been added!");
                    plugin.sendMessage(player, "Now go to the place where your "
                            + "players will be teleported and type " + ChatColor.ITALIC
                            + "/trnlobby sign set-dest" + ChatColor.YELLOW
                            + ".");
                    plugin.sendMessage(player, "Your players will be teleported "
                            + "there looking at the same place you are when you type this command.");
                }
            }, 5);
        }
    }

    public boolean isSign(Location loc) {
        return signs.containsKey(loc);
    }

    public void removeSign(BlockBreakEvent e) {
        SignData sd = signs.get(e.getBlock().getLocation());
        if (sd != null) {
            signsMutex.lock();
            try {
                signs.remove(e.getBlock().getLocation());
            } finally {
                signsMutex.unlock();
            }
            plugin.sendMessage(e.getPlayer(), ChatColor.GREEN
                    + "This sign has been Removed!");
        }
    }

    public void signTeleport(PlayerInteractEvent e) {
        SignData sd = signs.get(e.getClickedBlock().getLocation());
        if (sd != null) {
            e.getPlayer().teleport(sd.getTeleportLoc());
        }
    }

    /**
     * Capture the sign remove event. status.
     *
     * @param e The event
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onSignRemove(BlockBreakEvent e) {
        SignData sd = editors.get(e.getPlayer().getUniqueId());
        if (sd != null) {
            removeEditor(e.getPlayer());
            plugin.sendMessage(e.getPlayer(), ChatColor.GREEN
                    + "Edition canceled!");
        }
    }

    public void sincronousSave() {
        YamlConfiguration signsConfig = new YamlConfiguration();
        signsMutex.lock();
        try {
            int id = 0;
            for (SignData sd : signs.values()) {
                signsConfig.set("signs." + id + ".sign.world", "dummy");
                ConfigurationManager.setLocation(signsConfig
                        .getConfigurationSection("signs." + id + ".sign"),
                        sd.getSign().getLocation());
                signsConfig.set("signs." + id + ".teleport.world", "dummy");
                ConfigurationManager.setLocation(signsConfig
                        .getConfigurationSection("signs." + id + ".teleport"),
                        sd.getTeleportLoc());
                id++;
            }
        } finally {
            signsMutex.unlock();
        }
        try {
            signsConfig.save(new File(plugin.getDataFolder(), "signs.yml"));
        } catch (IOException ex) {
            Logger.getLogger(SignManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void save() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                sincronousSave();
            }
        });
    }

    public void load() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                YamlConfiguration signsConfig = new YamlConfiguration();
                try {
                    signsConfig.load(new File(plugin.getDataFolder(), "signs.yml"));
                } catch (IOException | InvalidConfigurationException ex) {
                    Logger.getLogger(SignManager.class.getName()).log(Level.SEVERE, null, ex);
                }
                signsMutex.lock();
                try {
                    signs.clear();
                    if (signsConfig.contains("signs")) {

                        for (String id : signsConfig.getConfigurationSection("signs").getKeys(false)) {
                            Location signLoc = plugin.getCfg().getLocation(signsConfig.getConfigurationSection("signs." + id + ".sign"));
                            Material material = signLoc.getBlock().getType();
                            if (material != Material.SIGN_POST && material != Material.WALL_SIGN) {
                                plugin.alert("Sign removed from: " + signLoc.toString());
                                continue;
                            }
                            Location teleLoc = plugin.getCfg().getLocation(signsConfig.getConfigurationSection("signs." + id + ".teleport"));
                            Sign sign = (Sign) signLoc.getBlock().getState();
                            SignData sd = new SignData(null);
                            sd.setSign(sign);
                            sd.setStatus(SignStatus.ACTIVE);
                            sd.setTeleportLoc(teleLoc);
                            signs.put(signLoc, sd);
                        }
                    }
                } finally {
                    signsMutex.unlock();
                }

            }
        });

    }
}
