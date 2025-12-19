package com.sekwang.gun;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
import java.util.Arrays;
import java.util.List;

public class GunManager {
    private final JavaPlugin plugin;
    private final NamespacedKey gunTypeKey;
    private final NamespacedKey gunAmmoKey;
    private final NamespacedKey gunMaxAmmoKey;
    
    public GunManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.gunTypeKey = new NamespacedKey(plugin, "gun_type");
        this.gunAmmoKey = new NamespacedKey(plugin, "gun_ammo");
        this.gunMaxAmmoKey = new NamespacedKey(plugin, "gun_max_ammo");
    }
    
    public ItemStack createPistol() {
        ItemStack gun = new ItemStack(Material.CARROT_ON_A_STICK);
        ItemMeta meta = gun.getItemMeta();
        
        meta.displayName(net.kyori.adventure.text.Component.text("Pistol").color(net.kyori.adventure.text.format.NamedTextColor.GOLD));
        meta.lore(Arrays.asList(
            net.kyori.adventure.text.Component.text("Damage: 5.0").color(net.kyori.adventure.text.format.NamedTextColor.GRAY),
            net.kyori.adventure.text.Component.text("Magazine: 12").color(net.kyori.adventure.text.format.NamedTextColor.GRAY),
            net.kyori.adventure.text.Component.text("Right-click to shoot").color(net.kyori.adventure.text.format.NamedTextColor.YELLOW),
            net.kyori.adventure.text.Component.text("Shift+Right-click to reload").color(net.kyori.adventure.text.format.NamedTextColor.YELLOW)
        ));
        
        // 1.21.4 CustomModelData - strings list
        CustomModelDataComponent cmd = meta.getCustomModelDataComponent();
        cmd.setStrings(List.of("1001"));
        meta.setCustomModelDataComponent(cmd);
        
        PersistentDataContainer data = meta.getPersistentDataContainer();
        data.set(gunTypeKey, PersistentDataType.STRING, "pistol");
        data.set(gunAmmoKey, PersistentDataType.INTEGER, 12);
        data.set(gunMaxAmmoKey, PersistentDataType.INTEGER, 12);
        
        gun.setItemMeta(meta);
        return gun;
    }
    
    public ItemStack createRifle() {
        ItemStack gun = new ItemStack(Material.CARROT_ON_A_STICK);
        ItemMeta meta = gun.getItemMeta();
        
        meta.displayName(net.kyori.adventure.text.Component.text("Rifle").color(net.kyori.adventure.text.format.NamedTextColor.RED));
        meta.lore(Arrays.asList(
            net.kyori.adventure.text.Component.text("Damage: 8.0").color(net.kyori.adventure.text.format.NamedTextColor.GRAY),
            net.kyori.adventure.text.Component.text("Magazine: 30").color(net.kyori.adventure.text.format.NamedTextColor.GRAY),
            net.kyori.adventure.text.Component.text("Right-click to shoot").color(net.kyori.adventure.text.format.NamedTextColor.YELLOW),
            net.kyori.adventure.text.Component.text("Shift+Right-click to reload").color(net.kyori.adventure.text.format.NamedTextColor.YELLOW)
        ));
        
        CustomModelDataComponent cmd = meta.getCustomModelDataComponent();
        cmd.setStrings(List.of("1002"));
        meta.setCustomModelDataComponent(cmd);
        
        PersistentDataContainer data = meta.getPersistentDataContainer();
        data.set(gunTypeKey, PersistentDataType.STRING, "rifle");
        data.set(gunAmmoKey, PersistentDataType.INTEGER, 30);
        data.set(gunMaxAmmoKey, PersistentDataType.INTEGER, 30);
        
        gun.setItemMeta(meta);
        return gun;
    }
    
    public ItemStack createAmmo(int amount) {
        ItemStack ammo = new ItemStack(Material.ARROW, amount);
        ItemMeta meta = ammo.getItemMeta();
        meta.displayName(net.kyori.adventure.text.Component.text("Ammo").color(net.kyori.adventure.text.format.NamedTextColor.YELLOW));
        ammo.setItemMeta(meta);
        return ammo;
    }
    
    public boolean isGun(ItemStack item) {
        if (item == null || item.getType() != Material.CARROT_ON_A_STICK) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        return meta.getPersistentDataContainer().has(gunTypeKey, PersistentDataType.STRING);
    }
    
    public String getGunType(ItemStack item) {
        if (!isGun(item)) return null;
        return item.getItemMeta().getPersistentDataContainer().get(gunTypeKey, PersistentDataType.STRING);
    }
    
    public int getAmmo(ItemStack item) {
        if (!isGun(item)) return 0;
        Integer ammo = item.getItemMeta().getPersistentDataContainer().get(gunAmmoKey, PersistentDataType.INTEGER);
        return ammo != null ? ammo : 0;
    }
    
    public int getMaxAmmo(ItemStack item) {
        if (!isGun(item)) return 0;
        Integer max = item.getItemMeta().getPersistentDataContainer().get(gunMaxAmmoKey, PersistentDataType.INTEGER);
        return max != null ? max : 0;
    }
    
    public void setAmmo(ItemStack item, int ammo) {
        if (!isGun(item)) return;
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(gunAmmoKey, PersistentDataType.INTEGER, ammo);
        item.setItemMeta(meta);
    }
    
    public double getDamage(String type) { return "rifle".equals(type) ? 8.0 : 5.0; }
    public long getCooldown(String type) { return "rifle".equals(type) ? 100 : 200; }
    public NamespacedKey getGunTypeKey() { return gunTypeKey; }
}
