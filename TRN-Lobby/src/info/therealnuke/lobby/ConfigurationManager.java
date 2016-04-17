/*
 *            This file is part of TRN-Lobby.
 *
 *  TRN-Spawn is free software: you can redistribute it and/or 
 *  modify it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  TRN-Spawn is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with  TRN-Spawn. 
 *  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package info.therealnuke.lobby;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitTask;

/**
 *
 * @author <a href="mailto:therealnuke@gmail.com">TheRealNuke</a>
 */
public class ConfigurationManager {

    private final Main plugin;
    private FileConfiguration fc;
    private int lastSpawnGiven;
    private final YamlConfiguration mapCfg;
    private BukkitTask saveControl;
    private boolean configChanged;
    File mapConfigFile;

    // Variable to be loaded from configuration files.
    private String prefix;
    private World lobbyWorld;
    private final List<Location> spawnPoints;
    private boolean handleInventory;
    private boolean blockIgnite;
    private boolean creatureSpawn;
    private boolean pistonWorks;

    public ConfigurationManager(Main plugin) {
        this.plugin = plugin;
        spawnPoints = new ArrayList<>();
        mapCfg = new YamlConfiguration();
        configChanged = false;
        mapConfigFile = new File(plugin.getDataFolder(), "map.yml");
    }

    /**
     * Loads the all configuration files file.
     *
     * @throws java.io.IOException on 
     * @throws java.io.FileNotFoundException
     * @throws org.bukkit.configuration.InvalidConfigurationException
     */
    public void load() throws IOException,
            FileNotFoundException, InvalidConfigurationException {

        // Save the default configuration file if it does not exists.
        if (true != new File(plugin.getDataFolder(), "config.yml").exists()) {
            plugin.saveDefaultConfig();
        }

        fc = plugin.getConfig();

        // Gets the prefix value.
        prefix = ChatColor.translateAlternateColorCodes('&', fc.getString("prefix"));

        // Gets handle-inventory from config.yml file.
        handleInventory = fc.getBoolean("handle-inventory");

        if (!mapConfigFile.exists()) {
            plugin.saveResource(mapConfigFile.getName(), true);
        }

        // Gets allow/deny options from config.yml
        creatureSpawn = fc.getBoolean("creature-spawn");
        blockIgnite = fc.getBoolean("block-ignite");
        pistonWorks = fc.getBoolean("piston-works");

        mapCfg.load(mapConfigFile);

        // Gets all map spawn points.
        ConfigurationSection csSpawns
                = mapCfg.getConfigurationSection("spawnPoints");
        if (csSpawns != null) {
            csSpawns.getKeys(false).stream().forEach((position) -> {
                spawnPoints.add(getLocation(
                        csSpawns.getConfigurationSection(position)));
            });
            lastSpawnGiven = 0;
            if (!spawnPoints.isEmpty()) {
                lobbyWorld = spawnPoints.get(0).getWorld();
            }
        }

        startSaveControl();
    }

    private void startSaveControl() {
        if (saveControl == null) {
            saveControl = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
                if (configChanged) {
                    try {
                        saveConfiguration();
                    } catch (IOException ex) {
                        plugin.alert("Error saving configuration: " + ex.getMessage());
                    }
                    configChanged = false;
                }
            }, 1200, 1200);
        }
    }

    /**
     * Reloads the plugin configuration.
     *
     * @throws java.io.IOException
     * @throws java.io.FileNotFoundException
     * @throws org.bukkit.configuration.InvalidConfigurationException
     */
    public void reload() throws IOException,
            FileNotFoundException, InvalidConfigurationException {
        plugin.reloadConfig();
        load();
    }

    /**
     * Gets a String from the config.yml file.
     *
     * @param keyName The key name of the string.
     * @return The String value.
     */
    public String getString(String keyName) {
        return fc.getString(keyName);
    }

    /**
     * Gets a Boolean from the config.yml file.
     *
     * @param keyName The key name of the boolean.
     * @return true if the value is true, false if it is false or was not found.
     */
    public boolean getBoolean(String keyName) {
        return fc.getBoolean(keyName);
    }

    /**
     * Gets the plugin prefix from the config.yml file.
     *
     * @return A coloured String with the prefix.
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Gets the override-spawn-cmd value from the config.yml file.
     *
     * @return true or false.
     */
    public boolean overrideSpawnCmd() {
        return fc.getBoolean("override-spawn-cmd");
    }

    /**
     * Get the next spawn point.
     *
     * @return A Location or Null if there is no configured locations.
     */
    public Location getNextSpawnPoint() {
        Location nextSpawn = null;
        if (isSpawnPointsSet()) {
            if (lastSpawnGiven < spawnPoints.size()) {
                nextSpawn = spawnPoints.get(lastSpawnGiven++);
            } else {
                lastSpawnGiven = 0;
                nextSpawn = spawnPoints.get(lastSpawnGiven);
            }
        }
        return nextSpawn;
    }

    public void addSpawnPoint(Location loc) {
        spawnPoints.add(loc);
        lobbyWorld = loc.getWorld();
        configChanged = true;
    }

    /**
     * Checks if at least one spawnpoint were set.
     *
     * @return true if there is at least one configured spawn point.
     */
    public boolean isSpawnPointsSet() {
        return (spawnPoints != null && !spawnPoints.isEmpty());
    }

    /**
     * Get the configured world for the Lobby.
     *
     * @return The world of the lobby or null if not loaded.
     */
    public World getLobbyWorld() {
        return lobbyWorld;
    }

    private Location getLocation(ConfigurationSection section) {
        Location result = null;
        int x;
        int y;
        int z;
        float yaw;
        float pitch;

        x = section.getInt("x");
        y = section.getInt("y");
        z = section.getInt("z");
        yaw = (float) section.getDouble("yaw");
        pitch = (float) section.getDouble("pitch");
        String worldName = section.getString("world");
        if (lobbyWorld == null) {
            lobbyWorld = plugin.getServer().getWorld(worldName);
        }
        if (lobbyWorld != null) {
            result = new Location(lobbyWorld, x, y, z, yaw, pitch);
        } else {
            plugin.alert("Invalid configured World for location: " + worldName);
        }
        return result;
    }

    private void setLocation(ConfigurationSection section, Location location) {
        section.set("world", location.getWorld().getName());
        section.set("x", location.getBlockX());
        section.set("y", location.getBlockY());
        section.set("z", location.getBlockZ());
        section.set("yaw", location.getYaw());
        section.set("pitch", location.getPitch());
    }

    private void saveSpawns() throws IOException {
        YamlConfiguration spawns = new YamlConfiguration();
        int spawnId = 0;
        for (Location spawn : spawnPoints) {
            spawns.set("spawnPoints." + spawnId + ".world", "dummy");
            setLocation(spawns.getConfigurationSection("spawnPoints." + spawnId), spawn);
            spawnId++;
        }
        spawns.save(mapConfigFile);
    }

    private void saveConfiguration() throws IOException {
        saveSpawns();
        plugin.alert("Configuration autosaved.");
    }

    /**
     * Stops the configuration save monitor and save current configuration.
     *
     * @throws java.io.IOException
     */
    public void finish() throws IOException {
        saveControl.cancel();
        saveConfiguration();
    }

    /**
     * Checks if Block Ignite Event is allowed on Spawn World.
     *
     * @return True if allowed, false if denied
     */
    public boolean blockIgniteAllowed() {
        return blockIgnite;
    }

    /**
     * Checks if Creature Spawn Event is allowed on Spawn World.
     *
     * @return True if allowed, false if denied
     */
    public boolean creatureSpawnAllowed() {
        return creatureSpawn;
    }

    /**
     * Checks if Piston Extend/Retract Events are allowed on Spawn World.
     *
     * @return True if allowed, false if denied
     */
    public boolean isPistonWorks() {
        return pistonWorks;
    }

    public boolean isHandleInventory() {
        return handleInventory;
    }

}
