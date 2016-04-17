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
 *  along with TRN-Lobby. 
 *  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package info.therealnuke.lobby;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.scheduler.BukkitTask;

/**
 *
 * @author <a href="mailto:therealnuke@gmail.com">TheRealNuke</a>
 */
public class EventListener implements Listener {

    private final Main plugin;
    private final PlayerManager pm;
    private BukkitTask fallControl;

    public EventListener(Main plugin) {
        this.plugin = plugin;
        pm = new PlayerManager(plugin);
    }

    /**
     * Initializes the Events.
     */
    public void init() {
        // Register this object in the plugin manager.
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        // Rescues players who fall into the void.
        if (fallControl == null) {
            fallControl = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                if (plugin.getCfg().getLobbyWorld() != null) {
                    for (Player player : plugin.getCfg()
                            .getLobbyWorld().getPlayers()) {
                        if (player.getLocation().getBlockY() < 0) {
                            player.teleport(plugin.getCfg().getNextSpawnPoint());
                        }
                    }
                }
            }, 20, 20);
        }
    }

    /**
     * Stops the Bukkit Scheduler.
     */
    public void finish() {
        fallControl.cancel();
        HandlerList.unregisterAll(this);
    }

    /**
     * *********** Map related Events ************
     */
    /**
     * Cancel raining in Lobby World
     *
     * @param e The event
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onWeatherChange(WeatherChangeEvent e) {
        if (e.getWorld().equals(plugin.getCfg().getLobbyWorld())
                && e.toWeatherState()) {
            e.setCancelled(true);
        }
    }

    /**
     * Cancel Explosions in Lobby World
     *
     * @param e The event
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onExplosion(ExplosionPrimeEvent e) {
        if (e.getEntity().getWorld().equals(plugin.getCfg().getLobbyWorld())) {
            e.setCancelled(true);
        }
    }

    /**
     * Cancel Block Physics in Lobby World
     *
     * @param e The event
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPhysicsEvent(BlockPhysicsEvent e) {
        if (e.getBlock().getWorld().equals(plugin.getCfg().getLobbyWorld())) {
            e.setCancelled(true);
        }
    }

    /**
     * Cancel Creature Spawn if they are not allowed in Lobby World
     *
     * @param e The event
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onCreatureSpawn(CreatureSpawnEvent e) {
        if (plugin.getCfg().creatureSpawnAllowed()
                && e.getEntity().getWorld().equals(plugin.getCfg().getLobbyWorld())) {
            e.setCancelled(true);
        }
    }

    /**
     * Cancel Block Ignite if they are not allowed in Lobby World
     *
     * @param e The event
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockIgnite(BlockIgniteEvent e) {
        if (plugin.getCfg().blockIgniteAllowed()
                && e.getBlock().getWorld().equals(plugin.getCfg().getLobbyWorld())) {
            e.setCancelled(true);
        }
    }

    /**
     * Cancel Piston Extend if they are not allowed in Lobby World
     *
     * @param e The event
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPiston(BlockPistonExtendEvent e) {
        if (plugin.getCfg().isPistonWorks() && plugin.getCfg().getLobbyWorld()
                .equals(plugin.getCfg().getLobbyWorld())) {
            e.setCancelled(true);
        }
    }

    /**
     * Cancel Piston Retract if they are not allowed in Lobby World
     *
     * @param e The event
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPiston(BlockPistonRetractEvent e) {
        if (plugin.getCfg().isPistonWorks() && plugin.getCfg().getLobbyWorld()
                .equals(plugin.getCfg().getLobbyWorld())) {
            e.setCancelled(true);
        }
    }

    /**
     * Cancel Leaves Decay in Lobby World
     *
     * @param e The event
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onLeavesDecay(LeavesDecayEvent e) {
        if (e.getBlock().getWorld().equals(plugin.getCfg().getLobbyWorld())) {
            e.setCancelled(true);
        }
    }

    /**
     * Cancel Item Spawn in Lobby World
     *
     * @param e The event
     */
    @EventHandler(ignoreCancelled = true)
    public void onItemSpawnEvent(ItemSpawnEvent e) {
        if (e.getEntity().getWorld().equals(plugin.getCfg().getLobbyWorld())) {
            e.setCancelled(true);
        }
    }

    /**
     * Cancel Block Move in Lobby World
     *
     * @param e The event
     */
    @EventHandler(ignoreCancelled = true)
    public void onBlockFromToEvent(BlockFromToEvent e) {
        if (e.getBlock().getWorld().equals(plugin.getCfg().getLobbyWorld())) {
            e.setCancelled(true);
        }
    }

    /**
     * *********** Player related Events ************
     */
    /**
     * Cancel Player Combust if they are not allowed in Lobby World
     *
     * @param e The event
     */
    @EventHandler(ignoreCancelled = false, priority = EventPriority.LOWEST)
    public void onEntityCombust(EntityCombustEvent e) {
        if (e.getEntity().getWorld().equals(plugin.getCfg().getLobbyWorld())) {
            e.setCancelled(true);
        }
    }

    /**
     * Cancel Player Block Event in Lobby World
     *
     * @param e The event
     */
    @EventHandler(ignoreCancelled = false, priority = EventPriority.LOWEST)
    public void onEntityBlockForm(EntityBlockFormEvent e) {
        if (e.getEntity().getWorld().equals(plugin.getCfg().getLobbyWorld())) {
            e.setCancelled(true);
        }
    }

    /**
     * Cancel Entity Damage in Lobby World
     *
     * @param e The event
     */
    @EventHandler(ignoreCancelled = false, priority = EventPriority.LOWEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        if (e.getEntity().getWorld().equals(plugin.getCfg().getLobbyWorld())) {
            e.setCancelled(true);
        }
    }

    /**
     * Cancel Entity Damage in Lobby World
     *
     * @param e The event
     */
    @EventHandler(ignoreCancelled = false, priority = EventPriority.LOWEST)
    public void onEntityDamage(EntityDamageEvent e) {
        if (e.getEntity().getWorld().equals(plugin.getCfg().getLobbyWorld())) {
            e.setCancelled(true);
        }
    }

    /**
     * Cancel Player Iteract if they are not allowed in Lobby World
     *
     * @param e The event
     */
    @EventHandler(ignoreCancelled = false, priority = EventPriority.LOWEST)
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (e.getPlayer().getWorld().equals(plugin.getCfg().getLobbyWorld())) {
            if (!e.getPlayer().hasPermission("trnlobby.edit")) {
                e.setCancelled(true);
            }
        }
    }

    /**
     * Cancel Player Items Drop if they are not allowed in Lobby World
     *
     * @param e The event
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPlayerDrop(PlayerDropItemEvent e) {
        if (e.getPlayer().getWorld().equals(plugin.getCfg().getLobbyWorld())) {
            if (!e.getPlayer().hasPermission("trnlobby.edit")) {
                e.setCancelled(true);
            }
        }
    }

    /**
     * Cancel Player Inteact events if they are not allowed in Lobby World
     *
     * @param e The event
     */
    @EventHandler(ignoreCancelled = false, priority = EventPriority.LOWEST)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent e) {
        if (e.getPlayer().getWorld().equals(plugin.getCfg().getLobbyWorld())) {
            if (!e.getPlayer().hasPermission("trnlobby.edit")) {
                e.setCancelled(true);
            }
        }
    }

    /**
     * Cancel Block Place events if they are not allowed in Lobby World
     *
     * @param e The event
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onBlockPlaceEvent(BlockPlaceEvent e) {
        if (e.getPlayer().getWorld().equals(plugin.getCfg().getLobbyWorld())) {
            if (!e.getPlayer().hasPermission("trnlobby.edit")) {
                e.setCancelled(true);
            }
        }
    }

    /**
     * Cancel Block Break events if they are not allowed in Lobby World
     *
     * @param e The event
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onBlockBreakEvent(BlockBreakEvent e) {
        if (e.getPlayer().getWorld().equals(plugin.getCfg().getLobbyWorld())) {
            if (!e.getPlayer().hasPermission("trnlobby.edit")) {
                e.setCancelled(true);
            }
        }
    }

    /**
     * Cancel Block Damage events if they are not allowed in Lobby World
     *
     * @param e The event
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onBlockDamage(BlockDamageEvent e) {
        if (e.getPlayer().getWorld().equals(plugin.getCfg().getLobbyWorld())) {
            if (!e.getPlayer().hasPermission("trnlobby.edit")) {
                e.setCancelled(true);
            }
        }
    }

    /**
     * Cancel Player items pickup if they are not allowed in Lobby World
     *
     * @param e The event
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPlayerPickupItem(PlayerPickupItemEvent e) {
        if (e.getPlayer().getWorld().equals(plugin.getCfg().getLobbyWorld())) {
            if (!e.getPlayer().hasPermission("trnlobby.edit")) {
                e.setCancelled(true);
            }
        }
    }

    /**
     * Cancel Etities to target in Lobby World
     *
     * @param e The event
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onEntityTarget(EntityTargetEvent e) {
        if (e.getEntity().getWorld().equals(plugin.getCfg().getLobbyWorld())) {
            e.setCancelled(true);
        }
    }

    /**
     * Cancel Hunger in Lobby World
     *
     * @param e The event
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onFoodLevelChange(FoodLevelChangeEvent e) {
        if (e.getEntity().getWorld().equals(plugin.getCfg().getLobbyWorld())) {
            e.setCancelled(true);
        }
    }

    /**
     * Cancel Player Inventory events if they are not allowed in Lobby World
     *
     * @param e The event
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onInventoryOpen(InventoryOpenEvent e) {
        if (e.getPlayer().getWorld().equals(plugin.getCfg().getLobbyWorld())) {
            if (!e.getPlayer().hasPermission("trnlobby.edit")) {
                e.setCancelled(true);
            }
        }
    }

    /**
     * Cancel Player Inventory events if they are not allowed in Lobby World
     *
     * @param e The event
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.getWhoClicked().getWorld().equals(plugin.getCfg().getLobbyWorld())) {
            if (!e.getWhoClicked().hasPermission("trnlobby.edit")) {
                e.setCancelled(true);
            }
        }
    }

    /**
     * Teleport to lobby or advertice to admins if it is not configured
     *
     * @param e The event
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onLoginEvent(PlayerLoginEvent e) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (plugin.getCfg().isSpawnPointsSet()) {
                pm.savePlayerStuff(e.getPlayer());
                pm.setPlayerSpawnStuff(e.getPlayer());
            } else {
                if (e.getPlayer().hasPermission("trnlobby.admin")) {
                    plugin.sendMessage(e.getPlayer(),
                            "No spawnpoint set yet, go to the "
                            + "lobby world and type: " + ChatColor.ITALIC
                            + "/trnlobby addspawnpoint");
                }
            }
        }, 5);
    }

    /**
     * Restore all player settings
     *
     * @param e The event
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPlayerQuit(PlayerQuitEvent e) {

        if (e.getPlayer().getWorld().equals(plugin.getCfg().getLobbyWorld())) {
            pm.returnPlayerStuff(e.getPlayer());
        }

    }

    /**
     * If player goes back to the original world teleport to it location.
     *
     * @param e The event
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPlayerTeleport(PlayerTeleportEvent e) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (e.getFrom().getWorld().equals(plugin.getCfg().getLobbyWorld())) {
                if (e.getTo().getWorld().equals(pm.getWorldFrom(e.getPlayer()))) {
                    pm.returnPlayerStuff(e.getPlayer());
                } else if (!e.getTo().getWorld()
                        .equals(plugin.getCfg().getLobbyWorld())) {
                    pm.returnPlayerStuff(e.getPlayer(), false);
                }
            }
        }, 5);
    }

}
