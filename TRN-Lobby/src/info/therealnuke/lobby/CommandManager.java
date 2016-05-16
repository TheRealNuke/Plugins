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
        plugin.getCommand("register").setExecutor(this);
        plugin.getCommand("login").setExecutor(this);
        plugin.getCommand("logout").setExecutor(this);
        plugin.getCommand("changepassword").setExecutor(this);

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
    public void onCommand(final PlayerCommandPreprocessEvent e) {
        if (isSpawnCommand(e.getMessage())) {
            e.setCancelled(true);
            final Location spawnPoint = plugin.getCfg().getNextSpawnPoint();
            // Teleports player to spawn in the next tic.
            Bukkit.getScheduler().runTask(plugin, new Runnable() {

                public void run() {
                    e.getPlayer().teleport(spawnPoint);
                }
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
        switch (cmnd.getName()) {
            case "register":
                if (player == null) {
                    sendNPGMsg(cs);
                } else {
                    if (args.length > 0) {
                        if (!plugin.getPm().isRegistered(player)) {
                            if (validatePassword(args[0], player)) {
                                plugin.getPm().register(player, args[0]);
                                plugin.getText().sendRegSuccessMsg(player);
                            }
                        } else {
                            plugin.getText().sendAlreadyRegMsg(player);
                        }
                    } else {
                        plugin.getText().sendRegMissingPasswordMsg(player);
                    }
                }
                break;
            case "login":
                if (player == null) {
                    sendNPGMsg(cs);
                } else {
                    if (args.length > 0) {
                        if (!plugin.getPm().isRegistered(player)) {
                            plugin.getText().sendLogNotRegMsg(player);
                        } else if (plugin.getPm().isLogged(player)) {
                            plugin.getText().sendLogAlreadyMsg(player);
                        } else {
                            plugin.getPm().login(player, args[0]);
                        }
                    } else {
                        plugin.getText().sendLogNoPassMsg(player);
                    }
                }
                break;
            case "changepassword":
                if (player == null) {
                    sendNPGMsg(cs);
                } else {
                    if (args.length > 0) {
                        if (!plugin.getPm().isRegistered(player)) {
                            plugin.getText().sendCPNotRegMsg(player);
                        } else {
                            if (!plugin.getPm().isLogged(player)) {
                                plugin.getText().sendCPNotLogMsg(player);
                            } else {
                                if (validatePassword(args[0], player)) {
                                    plugin.getPm().register(player, args[0]);
                                    plugin.getText().sendCPSuccessMsg(player);
                                }
                            }
                        }
                    } else {
                        plugin.getText().sendCPMissingPasswordMsg(player);
                    }
                }
                break;
            case "logout":
                if (player == null) {
                    sendNPGMsg(cs);
                } else {
                    plugin.getPm().removePlayer(player);
                    player.kickPlayer(plugin.getText().getLogOutMsg());
                }
                break;
            case "trnlobby":
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
                                    boolean kickAll = !plugin.getCfg().isSpawnPointsSet();
                                    plugin.getCfg().addSpawnPoint(player.getLocation());
                                    int x = player.getLocation().getBlockX();
                                    int y = player.getLocation().getBlockY();
                                    int z = player.getLocation().getBlockZ();
                                    plugin.sendMessage(cs, "Spawnpoint added at "
                                            + "X=" + x + ", Y=" + y + ", Z=" + z);
                                    if (kickAll && plugin.getCfg().isEnhanceSecurityEnabled()) {
                                        plugin.getPm().kickAllPlayers();
                                    }
                                }
                            } else {
                                sendNPGMsg(cs);
                            }
                            break;
                        case "sign":
                            if (player != null) {
                                World lobbyWorld = plugin.getCfg().getLobbyWorld();
                                if (lobbyWorld != null
                                        && !lobbyWorld.equals(player.getWorld())) {
                                    plugin.sendMessage(cs, ChatColor.RED
                                            + "You cannot manage a sign in "
                                            + "a different world than the lobby's world.");
                                } else {
                                    if (args.length != 2 || (!args[1].equalsIgnoreCase("add")
                                            && !args[1].equalsIgnoreCase("set-dest"))) {
                                        plugin.sendMessage(cs, ChatColor.RED
                                                + "You must to specify the action: "
                                                + "\"add\" or \"set-dest\" for \"sign\" command.");
                                    } else {
                                        switch (args[1].toLowerCase()) {
                                            case "add":
                                                plugin.getSignManager()
                                                        .addEditor(player);
                                                break;
                                            case "set-dest":
                                                if (plugin.getSignManager().isEditor(player)) {
                                                    if (plugin.getSignManager().canSetDestination(player)) {
                                                        plugin.getSignManager()
                                                                .setDestination(player);
                                                    } else {
                                                        plugin.sendMessage(cs, ChatColor.RED
                                                                + "You must to place a sign first.");
                                                    }
                                                } else {
                                                    plugin.sendMessage(cs, ChatColor.RED
                                                            + "You must to type : " + ChatColor.ITALIC
                                                            + "/trnlobby sign add " + ChatColor.RED
                                                            + "and place a sign first.");
                                                }
                                                break;
                                        }
                                    }
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
                break;

            default:
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

    private boolean validatePassword(String pass, Player player) {
        boolean ret = false;
        if (pass.length() < 6) {
            plugin.getText().sendRegFewCharsMsg(player);
        } else if (plugin.getCfg().getDisallowedPassList()
                .contains(pass.toLowerCase())) {
            plugin.getText().sendRegdisallowPwMsg(player);
        } else {
            ret = true;
        }
        return ret;
    }
}
