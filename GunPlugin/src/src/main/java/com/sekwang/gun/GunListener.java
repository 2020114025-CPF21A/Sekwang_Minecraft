package com.sekwang.gun;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import java.util.*;

public class GunListener implements Listener {
    private final GunManager gm;
    private final JavaPlugin plugin;
    private final Map<UUID, Long> cd = new HashMap<>();
    
    public GunListener(GunManager gm, JavaPlugin plugin) {
        this.gm = gm;
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        ItemStack item = p.getInventory().getItemInMainHand();
        if (!gm.isGun(item) || !e.getAction().name().contains("RIGHT")) return;
        e.setCancelled(true);
        String type = gm.getGunType(item);
        if (p.isSneaking()) { reload(p, item); return; }
        long now = System.currentTimeMillis();
        if (cd.containsKey(p.getUniqueId()) && now - cd.get(p.getUniqueId()) < gm.getCooldown(type)) return;
        int ammo = gm.getAmmo(item);
        if (ammo <= 0) { p.playSound(p.getLocation(), Sound.BLOCK_DISPENSER_FAIL, 1f, 1.5f); return; }
        shoot(p, type);
        gm.setAmmo(item, ammo - 1);
        cd.put(p.getUniqueId(), now);
        p.sendActionBar(net.kyori.adventure.text.Component.text("탄약: " + (ammo-1) + "/" + gm.getMaxAmmo(item)));
    }
    
    private void shoot(Player p, String type) {
        Location eye = p.getEyeLocation();
        Vector dir = eye.getDirection();
        Snowball b = p.launchProjectile(Snowball.class);
        b.setVelocity(dir.multiply(3.0));
        b.setMetadata("gun_dmg", new FixedMetadataValue(plugin, gm.getDamage(type)));
        p.getWorld().spawnParticle(Particle.FLASH, eye.add(dir.multiply(0.5)), 1);
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1.5f, type.equals("rifle") ? 1.5f : 1.8f);
        new BukkitRunnable() { public void run() { if(b.isValid()) b.remove(); }}.runTaskLater(plugin, 60L);
    }
    
    private void reload(Player p, ItemStack gun) {
        int cur = gm.getAmmo(gun), max = gm.getMaxAmmo(gun), needed = max - cur, arrows = 0;
        if (cur >= max) { p.sendActionBar(net.kyori.adventure.text.Component.text("탄창이 가득 찼습니다!")); return; }
        for (ItemStack i : p.getInventory()) if (i != null && i.getType() == Material.ARROW) arrows += i.getAmount();
        if (arrows == 0) { p.playSound(p.getLocation(), Sound.BLOCK_DISPENSER_FAIL, 1f, 0.5f); return; }
        int toReload = Math.min(needed, arrows), rem = toReload;
        for (ItemStack i : p.getInventory()) if (i != null && i.getType() == Material.ARROW && rem > 0) { int t = Math.min(i.getAmount(), rem); i.setAmount(i.getAmount()-t); rem -= t; }
        gm.setAmmo(gun, cur + toReload);
        p.playSound(p.getLocation(), Sound.BLOCK_IRON_DOOR_CLOSE, 1f, 1.5f);
        p.sendActionBar(net.kyori.adventure.text.Component.text("재장전! " + (cur+toReload) + "/" + max));
    }
    
    @EventHandler
    public void onHit(ProjectileHitEvent e) {
        if (!(e.getEntity() instanceof Snowball) || !e.getEntity().hasMetadata("gun_dmg")) return;
        e.getEntity().getWorld().spawnParticle(Particle.CRIT, e.getEntity().getLocation(), 10, 0.2, 0.2, 0.2, 0.1);
    }
    
    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Snowball)) return;
        Snowball b = (Snowball) e.getDamager();
        if (!b.hasMetadata("gun_dmg")) return;
        e.setDamage(b.getMetadata("gun_dmg").get(0).asDouble());
        e.getEntity().getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, e.getEntity().getLocation().add(0,1,0), 5, 0.3, 0.3, 0.3, 0.1);
    }
}
