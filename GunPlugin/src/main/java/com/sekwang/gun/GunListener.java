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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GunListener implements Listener {

    private final GunManager gunManager;
    private final JavaPlugin plugin;
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    public GunListener(GunManager gunManager) {
        this.gunManager = gunManager;
        this.plugin = (JavaPlugin) gunManager.getGunTypeKey().getPlugin();
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (!gunManager.isGun(item))
            return;

        // Only handle right-click
        if (!event.getAction().name().contains("RIGHT"))
            return;

        event.setCancelled(true);

        String gunType = gunManager.getGunType(item);

        // Shift + Right-click = Reload
        if (player.isSneaking()) {
            reload(player, item);
            return;
        }

        // Check cooldown
        long now = System.currentTimeMillis();
        long cooldownTime = gunManager.getCooldown(gunType);
        if (cooldowns.containsKey(player.getUniqueId())) {
            long lastShot = cooldowns.get(player.getUniqueId());
            if (now - lastShot < cooldownTime) {
                return; // Still on cooldown
            }
        }

        // Check ammo
        int ammo = gunManager.getAmmo(item);
        if (ammo <= 0) {
            player.playSound(player.getLocation(), Sound.BLOCK_DISPENSER_FAIL, 1.0f, 1.5f);
            player.sendActionBar("§c탄약이 없습니다! Shift+우클릭으로 재장전");
            return;
        }

        // Shoot!
        shoot(player, item, gunType);

        // Update ammo
        gunManager.setAmmo(item, ammo - 1);
        cooldowns.put(player.getUniqueId(), now);

        // Update action bar
        player.sendActionBar("§e탄약: §f" + (ammo - 1) + " / " + gunManager.getMaxAmmo(item));
    }

    private void shoot(Player player, ItemStack gun, String gunType) {
        Location eyeLocation = player.getEyeLocation();
        Vector direction = eyeLocation.getDirection();

        // Create bullet (snowball)
        Snowball bullet = player.launchProjectile(Snowball.class);
        bullet.setVelocity(direction.multiply(3.0)); // Fast bullet

        // Store damage info on the bullet
        double damage = gunManager.getDamage(gunType);
        bullet.setMetadata("gun_damage", new FixedMetadataValue(plugin, damage));
        bullet.setMetadata("gun_shooter", new FixedMetadataValue(plugin, player.getUniqueId().toString()));

        // Muzzle flash effect
        Location muzzle = eyeLocation.add(direction.multiply(0.5));
        player.getWorld().spawnParticle(Particle.FLASH, muzzle, 1);
        player.getWorld().spawnParticle(Particle.SMOKE, muzzle, 3, 0.05, 0.05, 0.05, 0.01);

        // Gun sound based on type
        switch (gunType) {
            case "pistol":
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1.5f, 1.8f);
                break;
            case "rifle":
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST, 2.0f, 1.5f);
                break;
        }

        // Remove bullet after 3 seconds (if it doesn't hit anything)
        new BukkitRunnable() {
            @Override
            public void run() {
                if (bullet.isValid()) {
                    bullet.remove();
                }
            }
        }.runTaskLater(plugin, 60L);
    }

    private void reload(Player player, ItemStack gun) {
        int currentAmmo = gunManager.getAmmo(gun);
        int maxAmmo = gunManager.getMaxAmmo(gun);

        if (currentAmmo >= maxAmmo) {
            player.sendActionBar("§e탄창이 이미 가득 찼습니다!");
            return;
        }

        int needed = maxAmmo - currentAmmo;
        int arrowsInInventory = 0;

        // Count arrows in inventory
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == Material.ARROW) {
                arrowsInInventory += item.getAmount();
            }
        }

        if (arrowsInInventory == 0) {
            player.sendActionBar("§c탄약(화살)이 없습니다!");
            player.playSound(player.getLocation(), Sound.BLOCK_DISPENSER_FAIL, 1.0f, 0.5f);
            return;
        }

        int toReload = Math.min(needed, arrowsInInventory);

        // Remove arrows from inventory
        int remaining = toReload;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == Material.ARROW && remaining > 0) {
                int take = Math.min(item.getAmount(), remaining);
                item.setAmount(item.getAmount() - take);
                remaining -= take;
            }
        }

        // Update gun ammo
        gunManager.setAmmo(gun, currentAmmo + toReload);

        // Reload sound and message
        player.playSound(player.getLocation(), Sound.BLOCK_IRON_DOOR_CLOSE, 1.0f, 1.5f);
        player.playSound(player.getLocation(), Sound.BLOCK_IRON_DOOR_OPEN, 1.0f, 1.2f);
        player.sendActionBar("§a재장전 완료! §f" + (currentAmmo + toReload) + " / " + maxAmmo);
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Snowball))
            return;

        Snowball bullet = (Snowball) event.getEntity();
        if (!bullet.hasMetadata("gun_damage"))
            return;

        // Impact effect
        Location hitLocation = bullet.getLocation();
        bullet.getWorld().spawnParticle(Particle.CRIT, hitLocation, 10, 0.2, 0.2, 0.2, 0.1);

        // If hit entity, damage is handled in EntityDamageByEntityEvent
        if (event.getHitEntity() != null) {
            return;
        }

        // Block hit effect
        if (event.getHitBlock() != null) {
            bullet.getWorld().playSound(hitLocation, Sound.BLOCK_STONE_HIT, 1.0f, 1.0f);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Snowball))
            return;

        Snowball bullet = (Snowball) event.getDamager();
        if (!bullet.hasMetadata("gun_damage"))
            return;

        double damage = bullet.getMetadata("gun_damage").get(0).asDouble();
        event.setDamage(damage);

        // Headshot detection (if hit location is above entity's eye level - 0.5)
        Entity victim = event.getEntity();
        if (victim instanceof LivingEntity) {
            LivingEntity living = (LivingEntity) victim;
            double hitY = bullet.getLocation().getY();
            double headY = living.getEyeLocation().getY() - 0.3;

            if (hitY >= headY) {
                // Headshot! Double damage
                event.setDamage(damage * 2);
                victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 0.5f);

                // Notify shooter
                if (bullet.hasMetadata("gun_shooter")) {
                    String shooterUUID = bullet.getMetadata("gun_shooter").get(0).asString();
                    Player shooter = Bukkit.getPlayer(UUID.fromString(shooterUUID));
                    if (shooter != null) {
                        shooter.sendActionBar("§c§l헤드샷!");
                    }
                }
            }
        }

        // Blood effect
        victim.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, victim.getLocation().add(0, 1, 0), 5, 0.3, 0.3, 0.3,
                0.1);
    }
}
