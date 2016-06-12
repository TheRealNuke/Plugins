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
import java.util.Comparator;
import org.bukkit.Location;

/**
 *
 * @author <a href="mailto:therealnuke@gmail.com">TheRealNuke</a>
 */
public class Comparators {

    public static int compareLoc(Location loc1, Location loc2) {
        int result;
        result = loc1.getWorld().getUID().compareTo(loc2.getWorld().getUID());
        if (result == 0) {
            result = loc1.getBlockX() - loc2.getBlockX();
            if (result == 0) {
                result = loc1.getBlockY() - loc2.getBlockY();
            }
            if (result == 0) {
                result = loc1.getBlockZ() - loc2.getBlockZ();
            }
        }
        return result;
    }

    public static class compareLocation implements Comparator<Location> {

        @Override
        public int compare(Location loc1, Location loc2) {
            return compareLoc(loc1, loc2);
        }
    }

    public static class compareCuboidSelection implements Comparator<CuboidSelection> {

        @Override
        public int compare(CuboidSelection o1, CuboidSelection o2) {
            int result;
            result = compareLoc(o1.getMinimumPoint(), o2.getMinimumPoint());
            if (result == 0) {
                result = compareLoc(o1.getMaximumPoint(), o2.getMaximumPoint());
            }
            return result;
        }
    }
}
