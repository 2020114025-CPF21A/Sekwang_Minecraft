package com.sekwang.gun;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GunPlugin extends JavaPlugin {
    
    private GunManager gunManager;
    
    @Override
    public void onEnable() {
        gunManager = new GunManager(this);
        getServer().getPluginManager().registerEvents(new GunListener(gunManager, this), this);
        getLogger().info("GunPlugin enabled! Use /gun to get a gun.");
    }
    
    @Override
    public void onDisable() {
        getLogger().info("GunPlugin disabled!");
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("gun")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Only players can use this command!");
                return true;
            }
            
            Player player = (Player) sender;
            
            if (args.length == 0) {
                player.sendMessage("§6=== Gun Plugin ===");
                player.sendMessage("§e/gun pistol §7- Get a pistol");
                player.sendMessage("§e/gun rifle §7- Get a rifle");
                player.sendMessage("§e/gun ammo §7- Get ammo (arrows)");
                return true;
            }
            
            switch (args[0].toLowerCase()) {
                case "pistol":
                    player.getInventory().addItem(gunManager.createPistol());
                    player.sendMessage("§aYou received a Pistol!");
                    break;
                case "rifle":
                    player.getInventory().addItem(gunManager.createRifle());
                    player.sendMessage("§aYou received a Rifle!");
                    break;
                case "ammo":
                    player.getInventory().addItem(gunManager.createAmmo(64));
                    player.sendMessage("§aYou received 64 ammo!");
                    break;
                default:
                    player.sendMessage("§cUnknown gun type!");
            }
            return true;
        }
        return false;
    }
}
