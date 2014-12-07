package com.gmail.bitboxgaming.Gold2XP;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class MainClass extends JavaPlugin implements Listener {
    public static Economy econ = null;

    public static void removeInventoryItems(PlayerInventory inv, Material type, int amount) {
        for (ItemStack is : inv.getContents()) {
            if (is != null && is.getType() == type) {
                int newamount = is.getAmount() - amount;
                if (newamount > 0) {
                    is.setAmount(newamount);
                    break;
                } else {
                    inv.remove(is);
                    amount = -newamount;
                    if (amount == 0) break;
                }
            }
        }
    }

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getConfig().options().copyDefaults(true);
        saveConfig();
        if (getConfig().getBoolean("Use-Vault-To-Charge-Player") == false) {
            getServer().getLogger().severe(String.format("[%s] - No Vault found!", getDescription().getName()));
        } else if (getConfig().getBoolean("Use-Vault-To-Charge-Player") == true) {
            setupEconomy();
        }
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getPlayer() instanceof Player) {
            Player player = event.getPlayer();
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                Material rightClickedBlock = event.getClickedBlock().getType();
                Material block = Material.getMaterial(getConfig().getString("Block-To-Be-Right-Clicked"));
                Material item = Material.getMaterial(getConfig().getString("Item-To-Be-Consumed"));
                if (rightClickedBlock == block) {
                    if (player.getInventory().contains(item)) {
                        if (getConfig().getBoolean("Use-Vault-To-Charge-Player") == true && getServer().getPluginManager().isPluginEnabled("Vault")) {
                            EconomyResponse r = econ.withdrawPlayer(player, getConfig().getInt("Cost-Of-Exchange"));
                            if(r.transactionSuccess()) {
                                removeInventoryItems(player.getInventory(), item, getConfig().getInt("Number-Of-Items-To-Be-Consumed"));
                                player.updateInventory();
                                player.giveExpLevels(getConfig().getInt("Levels-To-Give"));
                                player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1, 10);
                            } else {
                                player.sendMessage(String.format("An error occured: %s", r.errorMessage));
                            }
                        } else {
                            return;
                        }
                    } else {
                        player.sendMessage(ChatColor.YELLOW + "You must have at least: " + ChatColor.GOLD + getConfig().getInt("Number-Of-Items-To-Be-Consumed") + " " + ChatColor.GOLD + getConfig().getString("Item-To-Be-Consumed").toLowerCase() + "(s)");
                    }
                }
            }
        }
    }
}
