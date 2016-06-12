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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.TreeMap;
import java.util.concurrent.locks.ReentrantLock;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author <a href="mailto:therealnuke@gmail.com">TheRealNuke</a>
 */
public class Arenas {

    private final TreeMap<String, Arena> arenas;
    private final ReentrantLock _arenas_lock;
    private final YamlConfiguration arenasYaml;
    private final Plugin plugin;

    public Arenas(Plugin plugin) {
        this.arenas = new TreeMap<>();
        this._arenas_lock = new ReentrantLock();
        arenasYaml = new YamlConfiguration();
        this.plugin = plugin;
    }

    public void addArena(Arena arena) {
        _arenas_lock.lock();
        try {
            arenas.put(arena.getName(), arena);
        } finally {
            _arenas_lock.unlock();
        }
    }

    public boolean removeArena(Arena arena) {
        boolean result = false;
        _arenas_lock.lock();
        try {
            result = arenas.remove(arena.getName()) != null;
        } finally {
            _arenas_lock.unlock();
        }
        return result;
    }
    
    public int size() {
        return arenas.size();
    }
    
    public Arena getArena(String arenaName) {
        return arenas.get(arenaName);
    }
    
    public Collection<Arena> getArenas() {
        return arenas.values();
    }

    public void synchronousLoad() throws IOException, FileNotFoundException,
            InvalidConfigurationException {
        arenasYaml.load(new File(plugin.getDataFolder(), "arenas.yml"));
        _arenas_lock.lock();
        try {
            arenas.clear();
            for (String arenaName : arenasYaml.getKeys(false)) {
                Arena arena = new Arena(arenaName);
                arena.setValuesFromConfig(arenasYaml
                        .getConfigurationSection(arenaName), plugin.getServer());
                arenas.put(arenaName, arena);
            }
        } finally {
            _arenas_lock.unlock();
        }
    }

    public void synchronousSave() throws IOException, FileNotFoundException,
            InvalidConfigurationException {
        arenasYaml.loadFromString("");
        for (Arena arena : arenas.values()) {
            arenasYaml.set(arena.getName(), arena.getConfigurationSection());
        }
        arenasYaml.save(new File(plugin.getDataFolder(), "arenas.yml"));
    }
    
}
