package io.github.thebusybiscuit.slimefun4.implementation.listeners;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;

import io.github.thebusybiscuit.slimefun4.implementation.items.weapons.SlimefunBow;
import me.mrCookieSlime.Slimefun.SlimefunPlugin;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.SlimefunItem;
import me.mrCookieSlime.Slimefun.Objects.handlers.BowShootHandler;
import me.mrCookieSlime.Slimefun.api.Slimefun;

/**
 * This {@link Listener} is responsible for tracking {@link Arrow Arrows} fired from a
 * {@link SlimefunBow}.
 * 
 * @author TheBusyBiscuit
 * 
 * @see SlimefunBow
 *
 */
public class SlimefunBowListener implements Listener {

    private final Map<UUID, SlimefunBow> projectiles = new HashMap<>();

    public void register(SlimefunPlugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * This returns a {@link HashMap} holding the {@link UUID} of a {@link Arrow} and the
     * associated {@link SlimefunBow} with which this {@link Arrow} was fired from.
     * 
     * @return A {@link HashMap} with all actively tracked {@link Arrow Arrows}
     */
    public Map<UUID, SlimefunBow> getProjectileData() {
        return projectiles;
    }

    @EventHandler
    public void onBowUse(EntityShootBowEvent e) {
        if (e.getEntity() instanceof Player && e.getProjectile() instanceof Arrow) {
            SlimefunItem bow = SlimefunItem.getByItem(e.getBow());

            if (bow instanceof SlimefunBow) {
                projectiles.put(e.getProjectile().getUniqueId(), (SlimefunBow) bow);
            }
        }
    }

    @EventHandler
    public void onArrowHit(ProjectileHitEvent e) {
        Slimefun.runSync(() -> {
            if (e.getEntity().isValid() && e.getEntity() instanceof Arrow) {
                projectiles.remove(e.getEntity().getUniqueId());
            }
        }, 4L);
    }

    @EventHandler
    public void onArrowSuccessfulHit(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Arrow && e.getEntity() instanceof LivingEntity && e.getCause() != EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) {
            SlimefunBow bow = projectiles.get(e.getDamager().getUniqueId());

            if (bow != null) {
                bow.callItemHandler(BowShootHandler.class, handler -> handler.onHit(e, (LivingEntity) e.getEntity()));
            }

            projectiles.remove(e.getDamager().getUniqueId());
        }
    }

}
