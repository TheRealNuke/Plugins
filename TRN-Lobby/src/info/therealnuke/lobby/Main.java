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

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author <a href="mailto:therealnuke@gmail.com">TheRealNuke</a>
 */
public class Main extends JavaPlugin {

    private final ConfigurationManager cfg;
    private final CommandManager cmd;
    private final EventListener el;
    private final TextManager text;
    private final PlayerManager pm;
    private final SignManager sm;

    public Main() {
        cfg = new ConfigurationManager(this);
        cmd = new CommandManager(this);
        pm = new PlayerManager(this);
        el = new EventListener(this);
        text = new TextManager(this);
        sm = new SignManager(this);
    }

    /**
     * Loads plugin config and initializes the listeners and schedulers.
     */
    @Override
    public void onEnable() {
        try {
            cfg.load();
            text.init();
            cmd.init();
            el.init();
            sm.load();
        } catch (IOException | InvalidConfigurationException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Saves plugin configuration and stops the schedulers.
     */
    @Override
    public void onDisable() {
        try {
            if (getCfg().isEnhanceSecurityEnabled()) {
                pm.kickAllPlayersFromLobby();
            }
            cfg.finish();
            el.finish();
            sm.sincronousSave();
        } catch (IOException ex) {
            alert("Unable to save configuration: " + ex.getMessage());
        }
    }

    /**
     * Gets the configuration manager.
     *
     * @return
     */
    public ConfigurationManager getCfg() {
        return cfg;
    }

    /**
     * Logs a message in the console.
     *
     * @param message to be logged.
     */
    public void logMsg(String message) {
        sendMessage(getServer().getConsoleSender(), message);
    }

    /**
     * Send an alert message to all admins and logs it in the console.
     *
     * @param message of alert.
     */
    public void alert(String message) {
        for (Player player : getServer().getOnlinePlayers()) {
            if (player.hasPermission("trnlobby.admin")) {
                sendMessage(player, ChatColor.RED + message);
            }
        }
        logMsg(ChatColor.RED + message);
    }

    /**
     * Sends a message to a command sender.
     *
     * @param cs
     * @param message
     */
    public void sendMessage(CommandSender cs, String message) {
        cs.sendMessage(cfg.getPrefix()
                + ChatColor.translateAlternateColorCodes('&', message));
    }

    public void sendMessages(CommandSender cs, List<String> messages) {
        for (String message : messages) {
            sendMessage(cs, message);
        }
    }

    public TextManager getText() {
        return text;
    }

    public PlayerManager getPm() {
        return pm;
    }

    public SignManager getSignManager() {
        return sm;
    }

    
}
