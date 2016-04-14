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

import java.util.TreeMap;
import java.util.UUID;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author <a href="mailto:therealnuke@gmail.com">TheRealNuke</a>
 */
public class PlayerManager {

    private final Main plugin;
    private final TreeMap<UUID, PlayerStuff> players;

    private class PlayerStuff {

        private final Location location;
        private final int foodLevel;
        private final ItemStack[] items;
        private final GameMode gameMode;
        private final float experience;
        private final int totalExperience;
        private final double health;
        private final boolean isFlying;

        public PlayerStuff(Player player) {
            location = player.getLocation();
            foodLevel = player.getFoodLevel();
            items = player.getInventory().getContents();
            gameMode = player.getGameMode();
            experience = player.getExp();
            totalExperience = player.getTotalExperience();
            health = player.getHealth();
            isFlying = player.isFlying();
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
            player.setFlying(isFlying);
        }

    }

    public PlayerManager(Main plugin) {
        this.plugin = plugin;
        this.players = new TreeMap<>();
    }

    /**
     * Saves player inventory, location, hunger, health, etc.
     * @param player
     */
    public void savePlayerStuff(Player player) {
        PlayerStuff stuff = new PlayerStuff(player);
        players.put(player.getUniqueId(), stuff);
    }

    /**
     * Returns original stuff to the players.
     * @param player
     */
    public void returnPlayerStuff(Player player) {
        returnPlayerStuff(player, true);
    }

    /**
     * Returns original stuff to the players.
     * @param player
     * @param teleport If it is true player is taken to it original location.
     */
    public void returnPlayerStuff(Player player, boolean teleport) {
        PlayerStuff stuff = players.get(player.getUniqueId());
        if (stuff != null) {
            stuff.setPlayerStuff(player, teleport);
            players.remove(player.getUniqueId());
        }
    }

    /**
     * Clear player inventory, health, etc.
     * @param player
     */
    public void setPlayerSpawnStuff(Player player) {
        player.teleport(plugin.getCfg().getNextSpawnPoint());
        player.setFoodLevel(20);
        if (plugin.getCfg().isHandleInventory()) {
            player.getInventory().clear();
        }
        player.setGameMode(GameMode.ADVENTURE);
        player.setExp(0);
        player.setTotalExperience(0);
        player.setHealth(player.getMaxHealth());
        
    }

    /**
     * Gets the world where the player comes from.
     * @param player
     * @return
     */
    public World getWorldFrom(Player player) {
        World sourceWorld = null;
        PlayerStuff stuff = players.get(player.getUniqueId());
        if (stuff != null) {
            sourceWorld = stuff.location.getWorld();
        }
        return sourceWorld;
    }
    
    /**
     * Removes player from the memory.
     * @param player
     */
    public void remove(Player player) {
        players.remove(player.getUniqueId());
    }
}
