/*
 *            This file is part of TRN-Snow Slam.
 *
 *  TRN-Snow Slam is free software: you can redistribute it and/or 
 *  modify it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  TRN-Snow Slam is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with TRN-Snow Slam. 
 *  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package info.therealnuke.playingfield;

import com.sk89q.worldedit.bukkit.selections.CuboidSelection;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author <a href="mailto:therealnuke@gmail.com">TheRealNuke</a>
 */
public class AreaGuard implements Listener {

    private final TreeMap<String, CuboidSelection> protectedNameAreas;
    private final TreeMap<UUID, TreeSet<CuboidSelection>> forbiddenPlayerAccess;
    private final TreeMap<UUID, TreeSet<CuboidSelection>> forbiddenPlayerExit;
    private final ReentrantLock _areasLock_;
    private final Plugin plugin;
    private String overRidePermissionName;

    /**
     * Creates an AreaGuard object.
     *
     * @param plugin
     */
    public AreaGuard(Plugin plugin) {
        protectedNameAreas = new TreeMap<>();
        forbiddenPlayerAccess = new TreeMap<>();
        forbiddenPlayerExit = new TreeMap<>();
        _areasLock_ = new ReentrantLock();
        this.plugin = plugin;
        overRidePermissionName = "plugin.areaguard";
    }

    /**
     * Initializes the Listener.
     */
    public void init() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Unregisters the Listener.
     */
    public void finish() {
        HandlerList.unregisterAll(this);
    }

    /**
     * Gets the name of the Override Permission.
     *
     * @return The Override Permission Name
     */
    public String getOverridePermissionName() {
        return overRidePermissionName;
    }

    /**
     * Sets the name of the Override Permission.
     *
     * @param overRidePermissionName String with the permission name.
     */
    public void setOverRidePermissionName(String overRidePermissionName) {
        this.overRidePermissionName = overRidePermissionName;
    }

    /**
     * Adds an area to be protected.
     *
     * @param id Name of the Area.
     * @param area The area to be protected.
     */
    public void addProtectedArea(String id, CuboidSelection area) {
        _areasLock_.lock();
        try {
            protectedNameAreas.put(id, area);
        } finally {
            _areasLock_.unlock();
        }
    }

    /**
     * Removes a protected Area.
     *
     * @param id the name of the protected area.
     */
    public void removeProtectedArea(String id) {
        _areasLock_.lock();
        try {
            protectedNameAreas.remove(id);
        } finally {
            _areasLock_.unlock();
        }
    }

    /**
     * Adds an area where a Player cannot enter.
     *
     * @param player who cannot enter.
     * @param area where cannot enter.
     */
    public void addForbiddenAccess(Player player, CuboidSelection area) {
        _areasLock_.lock();
        try {
            TreeSet<CuboidSelection> cs
                    = forbiddenPlayerAccess.get(player.getUniqueId());
            if (cs == null) {
                cs = new TreeSet<>(new Comparators.compareCuboidSelection());
                forbiddenPlayerAccess.put(player.getUniqueId(), cs);
            }
            cs.add(area);
        } finally {
            _areasLock_.unlock();
        }
    }

    /**
     * Removes an area where a Player cannot enter.
     *
     * @param player who cannot enter.
     * @param area where cannot enter.
     */
    public void removeForbiddenAccess(Player player, CuboidSelection area) {
        _areasLock_.lock();
        try {
            forbiddenPlayerAccess.remove(player.getUniqueId(), area);
        } finally {
            _areasLock_.unlock();
        }
    }

    /**
     * Removes all the areas where a player cannot enter.
     *
     * @param player who will be allowed.
     */
    public void removeForbiddenAccess(Player player) {
        _areasLock_.lock();
        try {
            forbiddenPlayerAccess.remove(player.getUniqueId());
        } finally {
            _areasLock_.unlock();
        }
    }

    /**
     * Adds an area where a Player cannot exit.
     *
     * @param player who cannot exit.
     * @param area where cannot exit.
     */
    public void addForbiddenExit(Player player, CuboidSelection area) {
        _areasLock_.lock();
        try {
            TreeSet<CuboidSelection> cs
                    = forbiddenPlayerExit.get(player.getUniqueId());
            if (cs == null) {
                cs = new TreeSet<>(new Comparators.compareCuboidSelection());
                forbiddenPlayerExit.put(player.getUniqueId(), cs);
            }
            cs.add(area);
        } finally {
            _areasLock_.unlock();
        }
    }

    /**
     * Removes an area where a Player cannot exit.
     *
     * @param player who cannot exit.
     * @param area where cannot exit.
     */
    public void removeForbiddenExit(Player player, CuboidSelection area) {
        _areasLock_.lock();
        try {
            forbiddenPlayerExit.remove(player.getUniqueId(), area);
        } finally {
            _areasLock_.unlock();
        }
    }

    /**
     * Removes all the areas where a player cannot exit.
     *
     * @param player who will be allowed.
     */
    public void removeForbiddenExit(Player player) {
        _areasLock_.lock();
        try {
            forbiddenPlayerAccess.remove(player.getUniqueId());
        } finally {
            _areasLock_.unlock();
        }
    }

    /**
     * Cancel Block Break events if they are not allowed.
     *
     * @param e The event
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    protected void onBlockBreakEvent(BlockBreakEvent e) {
        _areasLock_.lock();
        try {
            for (CuboidSelection cs : protectedNameAreas.values()) {
                if (cs.contains(e.getBlock().getLocation())
                        && !e.getPlayer().hasPermission(overRidePermissionName)) {
                    e.setCancelled(true);
                    break;
                }
            }
        } finally {
            _areasLock_.unlock();
        }
    }

    /**
     * Cancel Block Place events if they are not allowed.
     *
     * @param e The event
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    protected void onBlockPlaceEvent(BlockPlaceEvent e) {
        _areasLock_.lock();
        try {
            for (CuboidSelection cs : protectedNameAreas.values()) {
                if (cs.contains(e.getBlock().getLocation())
                        && !e.getPlayer().hasPermission(overRidePermissionName)) {
                    e.setCancelled(true);
                    break;
                }
            }
        } finally {
            _areasLock_.unlock();
        }
    }

    /**
     * Cancel Player Interact events if they are not allowed.
     *
     * @param e The event
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    protected void onPlayerInteractEvent(PlayerInteractEvent e) {
        _areasLock_.lock();
        try {
            for (CuboidSelection cs : protectedNameAreas.values()) {
                if (cs.contains(e.getClickedBlock().getLocation())
                        && !e.getPlayer().hasPermission(overRidePermissionName)) {
                    e.setCancelled(true);
                    break;
                }
            }
        } finally {
            _areasLock_.unlock();
        }
    }

    /**
     * Cancel Entity Damage events if they are not allowed.
     *
     * @param e The event
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    protected void onEntityDamage(EntityDamageByEntityEvent e) {
        _areasLock_.lock();
        try {
            if (e.getDamager() instanceof Player) {
                for (CuboidSelection cs : protectedNameAreas.values()) {
                    if (cs.contains(e.getDamager().getLocation())
                            && !e.getDamager().hasPermission(overRidePermissionName)) {
                        e.setCancelled(true);
                        break;
                    }
                }
            }
        } finally {
            _areasLock_.unlock();
        }
    }

    /**
     * Cancel Move Event if they are not allowed.
     *
     * @param e The event
     */
    @EventHandler(ignoreCancelled = false, priority = EventPriority.LOWEST)
    public void onMoveEvent(PlayerMoveEvent e) {
        _areasLock_.lock();
        try {
            if (forbiddenPlayerAccess.get(e.getPlayer().getUniqueId()) != null) {
                for (CuboidSelection cs
                        : forbiddenPlayerAccess.get(e.getPlayer().getUniqueId())) {
                    if (cs.contains(e.getTo())) {
                        e.setCancelled(true);
                        e.getPlayer().teleport(e.getFrom());
                        break;
                    }
                }
            }
            if (forbiddenPlayerExit.get(e.getPlayer().getUniqueId()) != null) {
                for (CuboidSelection cs
                        : forbiddenPlayerExit.get(e.getPlayer().getUniqueId())) {
                    if (cs.contains(e.getFrom()) && !cs.contains(e.getTo())) {
                        e.setCancelled(true);
                        e.getPlayer().teleport(e.getFrom());
                        break;
                    }
                }
            }
        } finally {
            _areasLock_.unlock();
        }
    }
}
