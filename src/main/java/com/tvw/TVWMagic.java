package com.tvw;

import com.tvw.utils.CooldownManager;
import com.tvw.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.*;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.TimeUnit;

public final class TVWMagic extends JavaPlugin implements Listener, CommandExecutor {

    public void onEnable() {
        Bukkit.getServer().getPluginManager().registerEvents(this, this);
        this.getCommand("getbook").setExecutor(this);
    }

    public void onDisable() {

    }

    public static void giveSpellbook(Player player, int amount) {
        ItemStack stack = new ItemStack(Material.BOOK, amount);
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(Utils.color("&a&lAncient Spellbook"));
        meta.setLore(Utils.color(Arrays.asList("&bAncient spellbook used by the Templars and Warlocks.")));
        stack.setItemMeta(meta);
        player.getInventory().addItem(stack);
    }

    @EventHandler
    public void onOpen(PlayerInteractEvent event) {
        if (event.getItem() != null && event.getItem().getItemMeta().hasDisplayName() && event.getItem().getItemMeta().getDisplayName().equalsIgnoreCase(Utils.color("&a&lAncient Spellbook")) && event.getAction().equals(Action.RIGHT_CLICK_BLOCK) || event.getAction().equals(Action.RIGHT_CLICK_AIR)) {
            Team team = event.getPlayer().getScoreboard().getPlayerTeam(event.getPlayer());

            if (team.getDisplayName().equalsIgnoreCase("Templars")) {
                Inventory templarInventory = Bukkit.createInventory(null, 27, Utils.color("&1Templar Spellbook"));
                templarInventory.addItem(createGuiItem(Material.DIAMOND_SWORD, Utils.color("&c&lSuperhuman"), Utils.color("&cGives you Strength II")));
                templarInventory.addItem(createGuiItem(Material.APPLE, Utils.color("&c&lRegeneration"), Utils.color("&cGives you Regeneration II")));
                templarInventory.addItem(createGuiItem(Material.FLINT_AND_STEEL, Utils.color("&c&lFire-Walker"), Utils.color("&cGives you Fire Resistance")));
                event.getPlayer().openInventory(templarInventory);
            } else if (team.getDisplayName().equalsIgnoreCase("Warlocks")) {
                Inventory warlockInventory = Bukkit.createInventory(null, 27, Utils.color("&cWarlock Spellbook"));
                warlockInventory.addItem(createGuiItem(Material.TIPPED_ARROW, Utils.color("&0&lHarming Arrow"), Utils.color("&3Shoot a free full charge harming arrow.")));
                warlockInventory.addItem(createGuiItem(Material.ARROW, Utils.color("&2&lPoison Arrow"), Utils.color("&3Shoot a free full charge poison arrow.")));
                warlockInventory.addItem(createGuiItem(Material.SPECTRAL_ARROW, Utils.color("&3&lSpectral Arrow"), Utils.color("&3Shoot a free full charge spectral arrow.")));
                event.getPlayer().openInventory(warlockInventory);
            } else {
                event.getPlayer().sendMessage(Utils.color("&c&lYou have no team!"));
            }
            event.setCancelled(true);
        }
        if (event.getItem() != null && event.getItem().hasItemMeta() && event.getItem().getItemMeta().getDisplayName().equalsIgnoreCase(Utils.color("&a&lAncient Spellbook")) && event.getAction().equals(Action.LEFT_CLICK_AIR) || event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
            if (activeEffect.containsKey(event.getPlayer())) {

                if (activeEffect.get(event.getPlayer()) == "Spectral") {
                    Vector playerDirection = event.getPlayer().getLocation().getDirection();
                    long timeLeft = System.currentTimeMillis() - cooldownManager.getCooldown(event.getPlayer().getUniqueId());
                    if (TimeUnit.MILLISECONDS.toSeconds(timeLeft) >= 30) {
                        cooldownManager.setCooldown(event.getPlayer().getUniqueId(), System.currentTimeMillis());
                        SpectralArrow arrow = event.getPlayer().getWorld().spawnArrow(event.getPlayer().getEyeLocation(), playerDirection, 2.0f, 0f, SpectralArrow.class);
                        arrow.setPickupStatus(AbstractArrow.PickupStatus.CREATIVE_ONLY);
                    }
                }
                if (activeEffect.get(event.getPlayer()) == "Harming") {
                    Vector playerDirection1 = event.getPlayer().getLocation().getDirection();
                    long timeLeft1 = System.currentTimeMillis() - cooldownManager.getCooldown(event.getPlayer().getUniqueId());
                    if (TimeUnit.MILLISECONDS.toSeconds(timeLeft1) >= 2) {
                        cooldownManager.setCooldown(event.getPlayer().getUniqueId(), System.currentTimeMillis());
                        Arrow arrow = event.getPlayer().getWorld().spawnArrow(event.getPlayer().getEyeLocation(), playerDirection1, 2.0f, 0f, TippedArrow.class);
                        arrow.setBasePotionData(new PotionData(PotionType.INSTANT_DAMAGE));
                        arrow.setPickupStatus(AbstractArrow.PickupStatus.CREATIVE_ONLY);

                    }
                }
                if (activeEffect.get(event.getPlayer()) == "Poison") {
                    Vector playerDirection2 = event.getPlayer().getLocation().getDirection();
                    long timeLeft2 = System.currentTimeMillis() - cooldownManager.getCooldown(event.getPlayer().getUniqueId());
                    if (TimeUnit.MILLISECONDS.toSeconds(timeLeft2) >= 2) {
                        cooldownManager.setCooldown(event.getPlayer().getUniqueId(), System.currentTimeMillis());
                        Arrow arrow = event.getPlayer().getWorld().spawnArrow(event.getPlayer().getEyeLocation(), playerDirection2, 2.0f, 0f, TippedArrow.class);
                        ItemStack stack = new ItemStack(Material.TIPPED_ARROW);
                        PotionMeta meta = (PotionMeta) stack.getItemMeta();
                        meta.setBasePotionData(new PotionData(PotionType.POISON));
                        meta.addCustomEffect(new PotionEffect(PotionEffectType.POISON, 11 * 20, 1), true);
                        stack.setItemMeta(meta);
                        arrow.setBasePotionData(meta.getBasePotionData());
                        arrow.setPickupStatus(AbstractArrow.PickupStatus.CREATIVE_ONLY);

                    }
                }
            }
        }
    }

    private ItemStack createGuiItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        ArrayList<String> metaLore = new ArrayList<String>();

        for (String loreComments : lore) {
            metaLore.add(loreComments);
        }

        meta.setLore(metaLore);
        item.setItemMeta(meta);
        return item;
    }

    List<Player> activeEffects = new ArrayList<>();
    HashMap<Player, String> activeEffect = new HashMap<>();

    @EventHandler
    public void clickEvent(InventoryClickEvent event) {
        if (event.getView().getTitle().equalsIgnoreCase(Utils.color("&1Templar Spellbook"))) {
            event.setCancelled(true);
            if (event.getCurrentItem() != null) {
                if (event.getCurrentItem().hasItemMeta() && event.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase(Utils.color("&c&lSuperhuman"))) {
                    if (activeEffects.contains(event.getWhoClicked())) {
                        event.getWhoClicked().sendMessage(Utils.color("&c&lSpell Deselected"));
                        event.getWhoClicked().removePotionEffect(PotionEffectType.INCREASE_DAMAGE);
                        event.getWhoClicked().closeInventory();
                        activeEffects.remove(event.getWhoClicked());
                    } else {
                        event.getWhoClicked().closeInventory();
                        event.getWhoClicked().sendMessage(Utils.color("&a&lEffect selected."));
                        event.getWhoClicked().addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, Integer.MAX_VALUE, 1));
                        activeEffects.add((Player) event.getWhoClicked());
                    }
                }
                if (event.getCurrentItem().hasItemMeta() && event.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase(Utils.color("&c&lRegeneration"))) {
                    if (activeEffects.contains(event.getWhoClicked())) {
                        event.getWhoClicked().sendMessage(Utils.color("&c&lSpell Deselected"));
                        event.getWhoClicked().removePotionEffect(PotionEffectType.REGENERATION);
                        event.getWhoClicked().closeInventory();
                        activeEffects.remove(event.getWhoClicked());
                    } else {
                        event.getWhoClicked().closeInventory();
                        event.getWhoClicked().sendMessage(Utils.color("&a&lEffect selected."));
                        event.getWhoClicked().addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, Integer.MAX_VALUE, 1));
                        activeEffects.add((Player) event.getWhoClicked());
                    }
                }
                if (event.getCurrentItem().hasItemMeta() && event.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase(Utils.color("&c&lFire-Walker"))) {
                    if (activeEffects.contains(event.getWhoClicked())) {
                        event.getWhoClicked().sendMessage(Utils.color("&c&lSpell Deselected"));
                        event.getWhoClicked().removePotionEffect(PotionEffectType.FIRE_RESISTANCE);
                        event.getWhoClicked().closeInventory();
                        activeEffects.remove(event.getWhoClicked());
                    } else {
                        event.getWhoClicked().closeInventory();
                        event.getWhoClicked().sendMessage(Utils.color("&a&lEffect selected."));
                        event.getWhoClicked().addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 1));
                        activeEffects.add((Player) event.getWhoClicked());
                    }
                }
            }
        } else if (event.getView().getTitle().equalsIgnoreCase(Utils.color("&cWarlock Spellbook"))) {
            event.setCancelled(true);
            if (event.getCurrentItem() != null) {
                if (event.getCurrentItem().hasItemMeta() && event.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase(Utils.color("&3&lSpectral Arrow"))) {
                    if (activeEffect.containsKey(event.getWhoClicked())) {
                        event.getWhoClicked().sendMessage(Utils.color("&c&lSpell Deselected"));
                        event.getWhoClicked().closeInventory();
                        activeEffect.remove((Player) event.getWhoClicked());
                    } else {
                        event.getWhoClicked().closeInventory();
                        event.getWhoClicked().sendMessage(Utils.color("&a&lSpell selected."));
                        activeEffect.put((Player) event.getWhoClicked(), "Spectral");
                    }
                }
                if (event.getCurrentItem().hasItemMeta() && event.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase(Utils.color("&0&lHarming Arrow"))) {
                    if (activeEffect.containsKey(event.getWhoClicked())) {
                        event.getWhoClicked().sendMessage(Utils.color("&c&lSpell Deselected"));
                        event.getWhoClicked().closeInventory();
                        activeEffect.remove((Player) event.getWhoClicked());
                    } else {
                        event.getWhoClicked().closeInventory();
                        event.getWhoClicked().sendMessage(Utils.color("&a&lSpell selected."));
                        activeEffect.put((Player) event.getWhoClicked(), "Harming");
                    }
                }
                if (event.getCurrentItem().hasItemMeta() && event.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase(Utils.color("&2&lPoison Arrow"))) {
                    if (activeEffect.containsKey(event.getWhoClicked())) {
                        event.getWhoClicked().sendMessage(Utils.color("&c&lSpell Deselected"));
                        event.getWhoClicked().closeInventory();
                        activeEffect.remove((Player) event.getWhoClicked());
                    } else {
                        event.getWhoClicked().closeInventory();
                        event.getWhoClicked().sendMessage(Utils.color("&a&lSpell selected."));
                        activeEffect.put((Player) event.getWhoClicked(), "Poison");
                    }
                }
            }
        }
    }

    private final CooldownManager cooldownManager = new CooldownManager();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("getbook")) {
            Player player = (Player) sender;
            long timeLeft = System.currentTimeMillis() - cooldownManager.getCooldown(player.getUniqueId());
            if (TimeUnit.MILLISECONDS.toSeconds(timeLeft) >= 15) {
                giveSpellbook(player, 1);
                cooldownManager.setCooldown(player.getUniqueId(), (int) System.currentTimeMillis());
            } else {
                player.sendMessage(Utils.color("&cStill on cooldown!"));
            }
        }
        return true;
    }
}
