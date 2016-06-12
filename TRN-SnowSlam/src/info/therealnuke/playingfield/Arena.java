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
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 *
 * @author <a href="mailto:therealnuke@gmail.com">TheRealNuke</a>
 */
public class Arena {

    private final TreeMap<String, TreeSet<CuboidSelection>> regions;
    private final TreeMap<String, TreeSet<Location>> locations;
    private final String name;

    /**
     * Manages all the points and arena.
     * @param name of the arena.
     */
    public Arena(String name) {
        regions = new TreeMap<>();
        locations = new TreeMap<>();
        this.name = name;
    }

    /**
     * Gets the name of the Arena.
     * @return a String with the name of the arena.
     */
    public String getName() {
        return name;
    }

    /**
     * Adds an area to a region.
     * @param regionName If it does not exists it is created.
     * @param minimumPoint The minimum point location of the area.
     * @param maximumPoint The maximum point location of the area.
     * @return True if added, false if it already exists.
     */
    public boolean addAreaRegion(String regionName, Location minimumPoint,
            Location maximumPoint) {
        CuboidSelection cs = new CuboidSelection(minimumPoint.getWorld(),
                minimumPoint, maximumPoint);
        TreeSet<CuboidSelection> regionAreas = regions.get(regionName);
        if (regionAreas == null) {
            regionAreas = new TreeSet<>(new Comparators.compareCuboidSelection());
            regions.put(regionName, regionAreas);
        }
        return regionAreas.add(cs);
    }

    /**
     * Removes an existent region and all added areas.
     * @param regionName The name of the region to be removed.
     * @return True if exists and deleted, false if not.
     */
    public boolean removeRegion(String regionName) {
        return regions.remove(regionName) != null;
    }

    /**
     * Removes an area from a region.
     * @param regionName The region name.
     * @param minimumPoint The minimum point location of the area.
     * @param maximumPoint The maximum point location of the area.
     * @return True if deleted, false if it does not exists.
     */
    public boolean removeAreaRegion(String regionName,
            Location minimumPoint, Location maximumPoint) {
        boolean result = false;
        CuboidSelection cs = new CuboidSelection(minimumPoint.getWorld(),
                minimumPoint, maximumPoint);
        TreeSet<CuboidSelection> regionAreas = regions.get(regionName);
        if (regionAreas != null) {
            result = regionAreas.remove(cs);
        }
        return result;
    }

    /**
     * Gets all areas for a given region.
     * @param regionName The name of the region.
     * @return A TreeSet of the areas.
     */
    public TreeSet<CuboidSelection> getAreaRegions(String regionName) {
        return regions.get(regionName);
    }

    /**
     * Adds a location point to a Location Group.
     * @param locationGroup The name of the Location Group.
     * @param location The location point to be added.
     * @return true if added, false if the location exists in this group.
     */
    public boolean addLocation(String locationGroup, Location location) {
        TreeSet<Location> regionLocations;
        regionLocations = locations.get(locationGroup);
        if (regionLocations == null) {
            regionLocations = new TreeSet<>(new  Comparators.compareLocation());
            locations.put(locationGroup, regionLocations);
        }
        return regionLocations.add(location);
    }

    /**
     * Removes a location group and all it location points.
     * @param locationGroup The name of the Location Group.
     * @return True if deleted, false if it does not exists.
     */
    public boolean removeLocationGroup(String locationGroup) {
        return locations.remove(locationGroup) != null;
    }

    /**
     * Removes a location from a location group.
     * @param locationsName The name of the Location Group.
     * @param location The location point of the group.
     * @return True if deleted, false if it does not exists.
     */
    public boolean removeLocation(String locationsName, Location location) {
        TreeSet<Location> regionLocations;
        boolean result = false;
        regionLocations = locations.get(locationsName);
        if (regionLocations != null) {
            result = regionLocations.remove(location);
        }
        return result;
    }

    /**
     * Serializes the object into a ConfigurationSection object.
     * @return The ConfigurationSection object with the all object information.
     */
    public ConfigurationSection getConfigurationSection() {
        ConfigurationSection result = new YamlConfiguration().createSection(name);
        TreeMap<String, TreeSet<CuboidSelection>> tempRegions;
        TreeMap<String, TreeSet<Location>> tempLocations;
        tempRegions = new TreeMap<>();
        tempLocations = new TreeMap<>();
        tempRegions.putAll(regions);
        tempLocations.putAll(locations);
        tempRegions.keySet().stream().forEach((regionName) -> {
            int areaId = 0;
            for (CuboidSelection area : tempRegions.get(regionName)) {
                result.set("regions." + regionName + "." + areaId + ".min", "dummy");
                Configuration.setSelection(result.getConfigurationSection(
                        "regions." + regionName + "." + areaId), area);
                areaId++;
            }
        });
        tempLocations.keySet().stream().forEach((locationName) -> {
            int locationId = 0;
            for (Location location : tempLocations.get(locationName)) {
                result.set("locations." + locationName + "." + locationId + ".world", "dummy");
                Configuration.setLocation(result.getConfigurationSection(
                        "locations." + locationName + "." + locationId), location);
                locationId++;
            }
        });
        return result;
    }

    /**
     * Loads all object information from a ConfigurationSection object.
     * @param cs the ConfigurationSection to be loaded.
     * @param server a server for accesing to world objects.
     */
    public void setValuesFromConfig(ConfigurationSection cs, Server server) {
        TreeMap<String, TreeSet<CuboidSelection>> tempRegions;
        TreeMap<String, TreeSet<Location>> tempLocations;
        tempRegions = new TreeMap<>();
        tempLocations = new TreeMap<>();
        cs.getConfigurationSection("regions").getKeys(false).stream().forEach((regionName) -> {
            TreeSet<CuboidSelection> regionAreas
                    = new TreeSet<>(new  Comparators.compareCuboidSelection());
            cs.getConfigurationSection("regions."
                    + regionName).getKeys(false).stream().forEach((regionId) -> {
                regionAreas.add(Configuration.getSelection(
                        cs.getConfigurationSection(
                                "regions." + regionName + "." + regionId), server));
            });
            tempRegions.put(regionName, regionAreas);
        });
        cs.getConfigurationSection("locations").getKeys(false).stream().forEach((regionName) -> {
            TreeSet<Location> regionLocations
                    = new TreeSet<>(new  Comparators.compareLocation());
            cs.getConfigurationSection("locations."
                    + regionName).getKeys(false).stream().forEach((regionId) -> {
                regionLocations.add(Configuration.getLocation(
                        cs.getConfigurationSection(
                                "locations." + regionName + "." + regionId), server));
            });
            tempLocations.put(regionName, regionLocations);
        });

        regions.clear();
        regions.putAll(tempRegions);
        locations.clear();
        locations.putAll(tempLocations);
    }
}
