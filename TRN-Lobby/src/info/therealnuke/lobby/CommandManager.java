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

import java.io.IOException;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

/**
 *
 * @author <a href="mailto:therealnuke@gmail.com">TheRealNuke</a>
 */
public class CommandManager implements Listener, CommandExecutor {

    private final Main plugin;

    public CommandManager(Main plugin) {
        this.plugin = plugin;
    }

    /**
     * Registers commands in the plugin.
     */
    public void init() {
        plugin.getCommand("trnlobby").setExecutor(this);

        if (plugin.getCfg().overrideSpawnCmd()) {
            if (plugin.getCfg().isSpawnPointsSet()) {
                plugin.getServer().getPluginManager().registerEvents(this, plugin);
            } else {
                plugin.logMsg(ChatColor.RED
                        + "Configuration error: override-spawn-cmd "
                        + "is set but there is no configured spawn points.");
            }
        }

    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onCommand(PlayerCommandPreprocessEvent e) {
        if (isSpawnCommand(e.getMessage())) {
            e.setCancelled(true);
            final Location spawnPoint = plugin.getCfg().getNextSpawnPoint();
            // Teleports player to spawn in the next tic.
            Bukkit.getScheduler().runTask(plugin, () -> {
                e.getPlayer().teleport(spawnPoint);
            });
        }
    }

    /**
     *
     * @param cs Player or console who sends the command.
     * @param cmnd The CommandManager itself.
     * @param dummy Dummy variable.
     * @param args Arguments.
     * @return True on success / False on fail.
     */
    @Override
    public boolean onCommand(CommandSender cs, Command cmnd,
            String dummy, String[] args) {
        boolean showHelp = false;
        Player player = null;
        if (cs instanceof Player) {
            player = (Player) cs;
        }
        if (cmnd.getName().equals("trnlobby")) {

            if (args.length > 0) {
                // Process each subcommands.
                switch (args[0]) {

                    case "reload":
                        try {
                            plugin.getCfg().reload();
                            plugin.sendMessage(cs, ChatColor.GREEN
                                    + "Configuration reloaded.");
                        } catch (IOException | InvalidConfigurationException ex) {
                            plugin.alert("Error reloading plugin configuration: "
                                    + ex.getMessage());
                        }
                        break;

                    case "addspawnpoint":
                        if (player != null) {
                            World lobbyWorld = plugin.getCfg().getLobbyWorld();
                            if (lobbyWorld != null
                                    && !lobbyWorld.equals(player.getWorld())) {
                                plugin.sendMessage(cs, ChatColor.RED
                                        + "You cannot add a spawnpoint in "
                                        + "a different world than the first added.");
                            } else {
                                plugin.getCfg().addSpawnPoint(player.getLocation());
                                int x = player.getLocation().getBlockX();
                                int y = player.getLocation().getBlockY();
                                int z = player.getLocation().getBlockZ();
                                plugin.sendMessage(cs, "Spawnpoint added at "
                                        + "X=" + x + ", Y=" + y + ", Z=" + z);
                            }
                        } else {
                            sendNPGMsg(cs);
                        }
                        break;

                    default:
                        plugin.sendMessage(cs, ChatColor.RED
                                + "Invalid subcommand.");
                        showHelp = true;
                        break;

                }

            } else {
                plugin.sendMessage(cs, ChatColor.RED
                        + "Missing subcommand.");
                showHelp = true;
            }
        } else {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        return !showHelp;
    }

    private static boolean isSpawnCommand(String message) {
        String lowercaseMessage = message.toLowerCase();
        return (lowercaseMessage.equals("/spawn")
                || lowercaseMessage.startsWith("/spawn "));
    }

    private void sendNPGMsg(CommandSender cs) {
        plugin.sendMessage(cs, ChatColor.RED
                + "This command must be run by a player ingame.");
    }
}
