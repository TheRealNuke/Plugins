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
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.configuration.ConfigurationSection;

/**
 *
 * @author <a href="mailto:therealnuke@gmail.com">TheRealNuke</a>
 */
public class Configuration {

    public static void setBlockLocation(ConfigurationSection section, Location location) {
        section.set("x", location.getBlockX());
        section.set("y", location.getBlockY());
        section.set("z", location.getBlockZ());
        section.set("world", location.getWorld().getName());
    }

    public static void setLocation(ConfigurationSection section, Location location) {
        section.set("x", location.getX());
        section.set("y", location.getY());
        section.set("z", location.getZ());
        section.set("yaw", location.getYaw());
        section.set("pitch", location.getPitch());
        section.set("world", location.getWorld().getName());
    }

    public static void setSelection(ConfigurationSection section,
            CuboidSelection cuboidSelection) {
        section.set("min.world", "dummy");
        section.set("max.world", "dummy");
        setBlockLocation(section.getConfigurationSection("min"),
                cuboidSelection.getMinimumPoint());
        setBlockLocation(section.getConfigurationSection("max"),
                cuboidSelection.getMaximumPoint());
    }

    public static Location getLocation(ConfigurationSection section, 
            Server server) {
        String worldName;
        double x;
        double y;
        double z;
        float yaw;
        float pitch;

        worldName = section.getString("world");
        x = section.getDouble("x");
        y = section.getDouble("y");
        z = section.getDouble("z");
        yaw = (float) section.getDouble("yaw");
        pitch = (float) section.getDouble("pitch");
        return new Location(server.getWorld(worldName), x, y, z, yaw, pitch);
    }
    
    public static Location getBlockLocation(ConfigurationSection section, 
            Server server) {
        String worldName;
        int x;
        int y;
        int z;

        worldName = section.getString("world");
        x = section.getInt("x");
        y = section.getInt("y");
        z = section.getInt("z");
        return new Location(server.getWorld(worldName), x, y, z);
    }

    public static CuboidSelection getSelection(ConfigurationSection section, 
            Server server) {
            Location min = getBlockLocation(
                    section.getConfigurationSection("min"), server);
            Location max = getBlockLocation(
                    section.getConfigurationSection("max"), server);
            return new CuboidSelection(min.getWorld(), min, max);
    }
    
}
