package com.sekwang.gun;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

public class GunManager {

    private final JavaPlugin plugin;
    private final NamespacedKey gunTypeKey;
    private final NamespacedKey ammoKey;
    private final NamespacedKey maxAmmoKey;

    public GunManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.gunTypeKey = new NamespacedKey(plugin, "gun_type");
        this.ammoKey = new NamespacedKey(plugin, "ammo");
        this.maxAmmoKey = new NamespacedKey(plugin, "max_ammo");
    }

    public ItemStack createPistol() {
        ItemStack gun = new ItemStack(Material.CARROT_ON_A_STICK);
        ItemMeta meta = gun.getItemMeta();

        meta.setDisplayName("§6§lPistol");
        meta.setLore(Arrays.asList(
                "§7A reliable sidearm",
                "",
                "§eDamage: §c5.0",
                "§eFirerate: §aFast",
                "§eMagazine: §b12",
                "",
                "§7Right-click to shoot",
                "§7Shift + Right-click to reload"));

        // CustomModelData for 3D model
        meta.setCustomModelData(1001);

        // Store gun data
        meta.getPersistentDataContainer().set(gunTypeKey, PersistentDataType.STRING, "pistol");
        meta.getPersistentDataContainer().set(ammoKey, PersistentDataType.INTEGER, 12);
        meta.getPersistentDataContainer().set(maxAmmoKey, PersistentDataType.INTEGER, 12);

        gun.setItemMeta(meta);
        return gun;
    }

    public ItemStack createRifle() {
        ItemStack gun = new ItemStack(Material.CARROT_ON_A_STICK);
        ItemMeta meta = gun.getItemMeta();

        meta.setDisplayName("§c§lAssault Rifle");
        meta.setLore(Arrays.asList(
                "§7High-powered automatic rifle",
                "",
                "§eDamage: §c8.0",
                "§eFirerate: §aVery Fast",
                "§eMagazine: §b30",
                "",
                "§7Right-click to shoot",
                "§7Shift + Right-click to reload"));

        meta.setCustomModelData(1002);

        meta.getPersistentDataContainer().set(gunTypeKey, PersistentDataType.STRING, "rifle");
        meta.getPersistentDataContainer().set(ammoKey, PersistentDataType.INTEGER, 30);
        meta.getPersistentDataContainer().set(maxAmmoKey, PersistentDataType.INTEGER, 30);

        gun.setItemMeta(meta);
        return gun;
    }

    public ItemStack createAmmo(int amount) {
        ItemStack ammo = new ItemStack(Material.ARROW, amount);
        ItemMeta meta = ammo.getItemMeta();
        meta.setDisplayName("§e§lBullets");
        meta.setLore(Arrays.asList(
                "§7Ammunition for guns",
                "§7Used when reloading"));
        ammo.setItemMeta(meta);
        return ammo;
    }

    public boolean isGun(ItemStack item) {
        if (item == null || item.getType() != Material.CARROT_ON_A_STICK)
            return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null)
            return false;
        return meta.getPersistentDataContainer().has(gunTypeKey, PersistentDataType.STRING);
    }

    public String getGunType(ItemStack item) {
        if (!isGun(item))
            return null;
        return item.getItemMeta().getPersistentDataContainer().get(gunTypeKey, PersistentDataType.STRING);
    }

    public int getAmmo(ItemStack item) {
        if (!isGun(item))
            return 0;
        Integer ammo = item.getItemMeta().getPersistentDataContainer().get(ammoKey, PersistentDataType.INTEGER);
        return ammo != null ? ammo : 0;
    }

    public int getMaxAmmo(ItemStack item) {
        if (!isGun(item))
            return 0;
        Integer maxAmmo = item.getItemMeta().getPersistentDataContainer().get(maxAmmoKey, PersistentDataType.INTEGER);
        return maxAmmo != null ? maxAmmo : 0;
    }

    public void setAmmo(ItemStack item, int ammo) {
        if (!isGun(item))
            return;
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(ammoKey, PersistentDataType.INTEGER, ammo);
        item.setItemMeta(meta);
    }

    public double getDamage(String gunType) {
        switch (gunType) {
            case "pistol":
                return 5.0;
            case "rifle":
                return 8.0;
            default:
                return 3.0;
        }
    }

    public long getCooldown(String gunType) {
        switch (gunType) {
            case "pistol":
                return 200; // 200ms
            case "rifle":
                return 100; // 100ms (faster)
            default:
                return 300;
        }
    }

    public NamespacedKey getGunTypeKey() {
        return gunTypeKey;
    }

    public NamespacedKey getAmmoKey() {
        return ammoKey;
    }

    public NamespacedKey getMaxAmmoKey() {
        return maxAmmoKey;
    }
}
