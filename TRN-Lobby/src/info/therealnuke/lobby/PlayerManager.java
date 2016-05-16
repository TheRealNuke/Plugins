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

import info.therealnuke.tools.PasswordManager;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.scheduler.BukkitTask;

/**
 *
 * @author <a href="mailto:therealnuke@gmail.com">TheRealNuke</a>
 */
public class PlayerManager {

    private final Main plugin;
    private final TreeMap<UUID, PlayerStuff> players;
    private final TreeSet<UUID> playerToProcess;
    private final ReentrantLock playerProcessLock;
    private BukkitTask playerProcessTask;

    private enum Status {

        REGISTERED, LOGGED_IN, UNREGISTERED
    }

    private class PlayerStuff {

        private Location location;
        private int foodLevel;
        private ItemStack[] items;
        private GameMode gameMode;
        private float experience;
        private int totalExperience;
        private double health;
        private boolean allowToFly;
        private boolean isFlying;
        private Status status;
        private final UUID playerUid;
        private String hashedPassword;
        private String typedPassword;
        private Player player;
        private InetAddress lastIP;
        private boolean isOp;
        private boolean canEditLobby;

        private PlayerStuff(Player player) {
            location = player.getLocation();
            foodLevel = player.getFoodLevel();
            items = player.getInventory().getContents();
            gameMode = player.getGameMode();
            experience = player.getExp();
            totalExperience = player.getTotalExperience();
            health = player.getHealth();
            isFlying = player.isFlying();
            allowToFly = player.getAllowFlight();
            setStatus(Status.UNREGISTERED);
            playerUid = player.getUniqueId();
            this.player = player;
            lastIP = player.getAddress().getAddress();
            isOp = player.isOp();
        }

        private void update() {
            location = player.getLocation();
            foodLevel = player.getFoodLevel();
            items = player.getInventory().getContents();
            gameMode = player.getGameMode();
            experience = player.getExp();
            totalExperience = player.getTotalExperience();
            health = player.getHealth();
            allowToFly = player.getAllowFlight();
            isFlying = player.isFlying();
            lastIP = player.getAddress().getAddress();
            isOp = player.isOp();
        }

        public void setPlayerStuff(Player player, boolean teleport) {
            if (teleport) {
                player.teleport(location);
            }
            player.setFoodLevel(foodLevel);
            if (plugin.getCfg().isHandleInventory()) {
                player.getInventory().setContents(items);
            }
            player.setGameMode(gameMode);
            player.setExp(experience);
            player.setTotalExperience(totalExperience);
            player.setHealth(health);
            player.setAllowFlight(allowToFly);
            player.setFlying(isFlying);
            player.setOp(isOp);
        }

        private void setStatus(Status status) {
            this.status = status;
            if (player != null) {
                PermissionAttachment at = player.addAttachment(plugin);
                switch (status) {
                    case LOGGED_IN:
                        at.setPermission("trnlobby.register", false);
                        at.setPermission("trnlobby.login", false);
                        at.setPermission("trnlobby.logout", true);
                        break;
                    case REGISTERED:
                        at.setPermission("trnlobby.register", false);
                        at.setPermission("trnlobby.login", true);
                        at.setPermission("trnlobby.logout", false);
                        break;
                    case UNREGISTERED:
                        at.setPermission("trnlobby.register", true);
                        at.setPermission("trnlobby.login", false);
                        at.setPermission("trnlobby.logout", false);
                        break;
                }
            }
        }

        private Status getStatus() {
            return status;
        }

        private String getHashedPassword() {
            return hashedPassword;
        }

        private void storeHashedPassword(String password) {
            this.hashedPassword = PasswordManager.
                    getHashedPassword(playerUid.toString(), password);
        }

        private void setTypedPassword(String typedPassword) {
            this.typedPassword = typedPassword;
        }

        private boolean validateTypedPassword() {
            boolean ret = false;
            if (typedPassword != null && hashedPassword != null) {
                ret = PasswordManager.getHashedPassword(
                        playerUid.toString(), typedPassword)
                        .equals(hashedPassword);

            }
            return ret;
        }

        public InetAddress getLastIP() {
            return lastIP;
        }

    }

    public PlayerManager(Main plugin) {
        this.plugin = plugin;
        this.players = new TreeMap<>();
        this.playerProcessLock = new ReentrantLock();
        this.playerToProcess = new TreeSet<>();
    }

    /**
     * Saves player inventory, location, hunger, health, etc.
     *
     * @param player
     */
    public void savePlayerStuff(Player player) {
        PlayerStuff stuff = new PlayerStuff(player);
        players.put(player.getUniqueId(), stuff);
    }

    /**
     * Returns original stuff to the players.
     *
     * @param player
     */
    public void returnPlayerStuff(Player player) {
        returnPlayerStuff(player, true);
    }

    /**
     * Returns original stuff to the players.
     *
     * @param player
     * @param teleport If it is true player is taken to it original location.
     */
    public void returnPlayerStuff(Player player, boolean teleport) {
        PlayerStuff stuff = players.get(player.getUniqueId());
        if (stuff != null) {
            stuff.setPlayerStuff(player, teleport);

        }
    }

    /**
     * Clear player inventory, health, etc.
     *
     * @param player
     */
    public void setPlayerSpawnStuff(final Player player) {
        Bukkit.getScheduler().runTask(plugin, new Runnable() {
            @Override
            public void run() {
                player.setFoodLevel(20);
                if (plugin.getCfg().isHandleInventory()) {
                    player.getInventory().clear();
                }
                player.setGameMode(GameMode.ADVENTURE);
                player.setExp(0);
                player.setTotalExperience(0);
                player.setHealth(player.getMaxHealth());
                player.setFlying(false);
                player.setAllowFlight(false);
                if (plugin.getCfg().isEnhanceSecurityEnabled()) {
                    player.setOp(false);
                }
                PermissionAttachment at = player.addAttachment(plugin);
            }
        });
    }

    public void teleportToLobby(final Player player) {
        Bukkit.getScheduler().runTask(plugin, new Runnable() {
            @Override
            public void run() {
                player.teleport(plugin.getCfg().getNextSpawnPoint());
            }
        });
    }

    /**
     * Gets the world where the player comes from.
     *
     * @param player
     * @return
     */
    public World getSourceWorld(Player player) {
        World sourceWorld = null;
        PlayerStuff stuff = players.get(player.getUniqueId());
        if (stuff != null) {
            sourceWorld = stuff.location.getWorld();
        }
        return sourceWorld;
    }

    private void persistPlayer(final PlayerStuff ps) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                File userFileName = new File(plugin.getCfg().getPasswordDirFile(),
                        ps.playerUid.toString() + ".yml");
                YamlConfiguration passYml = new YamlConfiguration();
                passYml.set("password", ps.getHashedPassword());
                try {
                    passYml.save(userFileName);
                } catch (IOException ex) {
                    Logger.getLogger(PlayerManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }

    private void getPlayerStatus(final PlayerStuff ps) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                File userFileName = new File(plugin.getCfg().getPasswordDirFile(),
                        ps.playerUid.toString() + ".yml");
                if (userFileName.exists()) {
                    YamlConfiguration passYml = new YamlConfiguration();
                    try {
                        passYml.load(userFileName);
                    } catch (IOException | InvalidConfigurationException ex) {
                        Logger.getLogger(PlayerManager.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    ps.hashedPassword = passYml.getString("password");
                    ps.setStatus(Status.REGISTERED);
                } else {
                    ps.setStatus(Status.UNREGISTERED);
                }
            }
        });
    }

    private void login(PlayerStuff ps) {
        if (ps.validateTypedPassword()) {
            ps.setStatus(Status.LOGGED_IN);
            plugin.getText().sendLoginSuccessMessage(ps.player);
            ps.player.setOp(ps.isOp);
        } else {
            plugin.getText().sendLoginUnsuccessMessage(ps.player);
        }
    }

    private void register(final PlayerStuff ps) {
        ps.hashedPassword = PasswordManager.getHashedPassword(
                ps.playerUid.toString(), ps.typedPassword);
        persistPlayer(ps);
        ps.setStatus(Status.LOGGED_IN);
    }

    public void playerConnect(final Player player) {
        if (plugin.getCfg().isEnhanceSecurityEnabled()) {
            PlayerStuff ps;
            ps = players.get(player.getUniqueId());
            if (ps == null) {
                ps = new PlayerStuff(player);
                players.put(player.getUniqueId(), ps);
                getPlayerStatus(ps);
            } else {
                ps.player = player;
            }

            final PlayerStuff psFinal = ps;
            Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, new Runnable() {
                @Override
                public void run() {

                    if (plugin.getCfg().isAutologinEnabled()
                            && psFinal.status == Status.LOGGED_IN) {
                        if (psFinal.lastIP.equals(player.getAddress().getAddress())) {
                            plugin.getText().sendAutologinMessage(player);
                        } else {
                            psFinal.setStatus(Status.REGISTERED);
                            manageRegisteredPlayers(psFinal);
                        }
                    } else {
                        if (psFinal.status == Status.UNREGISTERED) {
                            manageUnregistered(psFinal);
                        } else {
                            manageRegisteredPlayers(psFinal);
                        }
                    }
                }
            }, 5);

        } else {
            savePlayerStuff(player);
            teleportToLobby(player);
            setPlayerSpawnStuff(player);
        }
    }

    private void manageRegisteredPlayers(PlayerStuff ps) {
        ps.update();
        plugin.getText().sendRegPlayerWlcMessage(ps.player);
        teleportToLobby(ps.player);
        setPlayerSpawnStuff(ps.player);
    }

    private void manageUnregistered(PlayerStuff ps) {
        ps.update();
        plugin.getText().sendUnregPlayerWlcMessage(ps.player);
        teleportToLobby(ps.player);
        setPlayerSpawnStuff(ps.player);
    }

    private Status getStatus(Player player) {
        Status stat = Status.UNREGISTERED;
        PlayerStuff ps = players.get(player.getUniqueId());
        if (ps != null) {
            stat = ps.getStatus();
        }
        return stat;
    }

    public boolean isAllowedAction(Player player) {
        boolean ret = true;
        if (plugin.getCfg().isEnhanceSecurityEnabled()) {
            Status stat = getStatus(player);
            if (stat != Status.LOGGED_IN) {
                switch (stat) {
                    case UNREGISTERED:
                        plugin.getText().sendNotAllowedToUnregPl(player);
                        break;
                    case REGISTERED:
                        plugin.getText().sendNotAllowedToRegPl(player);
                        break;
                }
                ret = false;
            }
        }
        return ret;
    }

    public boolean isRegistered(Player player) {
        Status stat = getStatus(player);
        return stat == Status.REGISTERED || stat == Status.LOGGED_IN;
    }

    public boolean isLogged(Player player) {
        return getStatus(player) == Status.LOGGED_IN;
    }

    public void register(Player player, String password) {
        PlayerStuff ps = players.get(player.getUniqueId());
        ps.typedPassword = password;
        register(ps);
    }

    public void login(Player player, String password) {
        PlayerStuff ps = players.get(player.getUniqueId());
        ps.typedPassword = password;
        login(ps);
    }

    public void kickAllPlayersFromLobby() {
        if (plugin.getCfg().getLobbyWorld() != null) {
            for (Player player : plugin.getCfg().getLobbyWorld().getPlayers()) {
                returnPlayerStuff(player);
            }
        }
        kickAllPlayers();
    }
    
    public void kickAllPlayers() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            player.kickPlayer(plugin.getText().getLogOutMsg());
        }        
    }

    public void removePlayer(Player player) {
        players.remove(player.getUniqueId());
    }
}
