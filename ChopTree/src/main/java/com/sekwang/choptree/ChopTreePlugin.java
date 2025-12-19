package com.sekwang.choptree;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class ChopTreePlugin extends JavaPlugin implements Listener {
    
    private static final Set<Material> LOG_TYPES = EnumSet.of(
        Material.OAK_LOG, Material.SPRUCE_LOG, Material.BIRCH_LOG,
        Material.JUNGLE_LOG, Material.ACACIA_LOG, Material.DARK_OAK_LOG,
        Material.MANGROVE_LOG, Material.CHERRY_LOG,
        Material.CRIMSON_STEM, Material.WARPED_STEM
    );
    
    private static final Set<Material> AXE_TYPES = EnumSet.of(
        Material.WOODEN_AXE, Material.STONE_AXE, Material.IRON_AXE,
        Material.GOLDEN_AXE, Material.DIAMOND_AXE, Material.NETHERITE_AXE
    );
    
    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("ChopTree enabled! Chop trees easily.");
    }
    
    @Override
    public void onDisable() {
        getLogger().info("ChopTree disabled!");
    }
    
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();
        ItemStack tool = player.getInventory().getItemInMainHand();
        
        if (!LOG_TYPES.contains(block.getType())) return;
        if (!AXE_TYPES.contains(tool.getType())) return;
        if (player.isSneaking()) return;
        
        Set<Block> treeLogs = new HashSet<>();
        findConnectedLogs(block, treeLogs, 256);
        
        for (Block log : treeLogs) {
            if (log.equals(block)) continue;
            log.breakNaturally(tool);
        }
        
        if (tool.getType().getMaxDurability() > 0) {
            org.bukkit.inventory.meta.Damageable meta = (org.bukkit.inventory.meta.Damageable) tool.getItemMeta();
            if (meta != null) {
                int damage = meta.getDamage() + treeLogs.size();
                if (damage >= tool.getType().getMaxDurability()) {
                    player.getInventory().setItemInMainHand(null);
                    player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
                } else {
                    meta.setDamage(damage);
                    tool.setItemMeta(meta);
                }
            }
        }
    }
    
    private void findConnectedLogs(Block start, Set<Block> logs, int maxLogs) {
        Queue<Block> queue = new LinkedList<>();
        queue.add(start);
        
        while (!queue.isEmpty() && logs.size() < maxLogs) {
            Block current = queue.poll();
            if (logs.contains(current)) continue;
            if (!LOG_TYPES.contains(current.getType())) continue;
            
            logs.add(current);
            
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = 0; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (dx == 0 && dy == 0 && dz == 0) continue;
                        Block neighbor = current.getRelative(dx, dy, dz);
                        if (!logs.contains(neighbor) && LOG_TYPES.contains(neighbor.getType())) {
                            queue.add(neighbor);
                        }
                    }
                }
            }
        }
    }
}
