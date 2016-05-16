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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

/**
 *
 * @author TheRealNuke <therealnuke@gmail.com>
 */
public class TextManager {

    private final Main plugin;
    private final YamlConfiguration text;

    public TextManager(Main plugin) {
        this.plugin = plugin;
        text = new YamlConfiguration();
    }

    public void init() throws IOException, FileNotFoundException,
            InvalidConfigurationException {
        File textsFile = new File(plugin.getDataFolder(), "text.yml");
        if (!textsFile.exists()) {
            plugin.saveResource("text.yml", true);
        }
        text.load(textsFile);
    }
    
    public void sendTextMessage(final Player player, final String textMessage) {
        Bukkit.getScheduler().runTask(plugin, new Runnable() {
            @Override
            public void run() {
                for (String line : text.getStringList(textMessage)) {
                    plugin.sendMessage(player,
                            line.replace("%PLAYER%", player.getName()));
                }
            }
        });
    }

    public void sendUnregPlayerWlcMessage(Player player) {
        sendTextMessage(player, "unregistered-players.welcome");
    }

    public void sendRegPlayerWlcMessage(Player player) {
        sendTextMessage(player, "registered-players.welcome");
    }

    public void sendLoginSuccessMessage(Player player) {
        sendTextMessage(player, "registered-players.login.successful");
    }

    public void sendLoginUnsuccessMessage(Player player) {
        sendTextMessage(player, "registered-players.login.unsuccessful");
    }

    public void sendAutologinMessage(Player player) {
        sendTextMessage(player, "registered-players.autologin");
    }

    public void sendNotAllowedToRegPl(Player player) {
        sendTextMessage(player, "registered-players.notallowed");
    }

    public void sendNotAllowedToUnregPl(Player player) {
        sendTextMessage(player, "unregistered-players.notallowed");
    }

    public void sendRegMissingPasswordMsg(Player player) {
        sendTextMessage(player, "commands.register.missing-password");
    }

    public void sendRegFewCharsMsg(Player player) {
        sendTextMessage(player, "commands.register.few-password-chars");
    }
    public void sendRegdisallowPwMsg(Player player) {
        sendTextMessage(player, "commands.register.disallowed-password");
    }
    public void sendAlreadyRegMsg(Player player) {
        sendTextMessage(player, "commands.register.already-registered");
    }
    public void sendRegSuccessMsg(Player player) {
        sendTextMessage(player, "commands.register.success");
    }
    
    public void sendLogSuccessMsg(Player player) {
        sendTextMessage(player, "commands.login.success");
    }
    public void sendLogNotRegMsg(Player player) {
        sendTextMessage(player, "commands.login.not-registered");
    }
    public void sendLogAlreadyMsg(Player player) {
        sendTextMessage(player, "commands.login.already-logged");
    }
    
    public void sendLogNoPassMsg(Player player) {
        sendTextMessage(player, "commands.login.missing-password");
    }
    
    public void sendCPNotRegMsg(Player player) {
        sendTextMessage(player, "commands.changepassword.not-registered");
    }
    
    public void sendCPNotLogMsg(Player player) {
        sendTextMessage(player, "commands.changepassword.not-logged");
    }
    
    public void sendCPSuccessMsg(Player player) {
        sendTextMessage(player, "commands.changepassword.success");
    }
    public void sendCPMissingPasswordMsg(Player player) {
        sendTextMessage(player, "commands.changepassword.missing-password");
    }
    public String getLogOutMsg() {
        return ChatColor.translateAlternateColorCodes('&', 
                text.getString("commands.logout.kick-message"));
    }
    
}
