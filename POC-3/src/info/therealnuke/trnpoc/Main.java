/*
 *            This file is part of TRN-POC-3.
 *
 *  TRN-POC-3 is free software: you can redistribute it and/or 
 *  modify it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  TRN-POC-3 is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with  TRN-POC-3. 
 *  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package info.therealnuke.trnpoc;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author TheRealNuke <therealnuke@gmail.com>
 */
public class Main extends JavaPlugin
        implements Listener {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }

    private ItemStack getNewSnowball() {
        ItemStack newSnowBall = new ItemStack(Material.SNOW_BALL);
        String ballName = "ball_" + new Random().nextInt(10000);
        ItemMeta im = newSnowBall.getItemMeta();
        im.setDisplayName(ballName);
        List<String> list = new ArrayList<>();
        list.add(ballName);
        im.setLore(list);
        newSnowBall.setItemMeta(im);
        return newSnowBall;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onProjectileThrownEvent(ProjectileLaunchEvent e) {
        if (e.getEntity() instanceof Snowball) {
            e.getEntity().setBounce(true);
            e.getEntity().setGlowing(true);
            e.getEntity().setVelocity(e.getEntity().getVelocity().multiply(0.75));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    void onProjectileHit(ProjectileHitEvent e) {
        if (e.getEntity() instanceof Snowball) {
            Snowball s = (Snowball) e.getEntity();
            s.getWorld().dropItem(s.getLocation(), getNewSnowball())
                    .setVelocity(s.getVelocity().multiply(1.5));
            s.setGlowing(true);
        }
        Player p = (Player) e.getEntity().getShooter();
        p.playSound(e.getEntity().getLocation(), Sound.ENTITY_SNOWMAN_HURT, 1, 1);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    void despawn(ItemDespawnEvent e) {
        if (e.getEntity() instanceof Snowball) {
            e.setCancelled(true);
        }
    }

}
