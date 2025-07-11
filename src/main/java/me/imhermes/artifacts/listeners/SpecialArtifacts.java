package me.imhermes.artifacts.listeners;

import io.papermc.paper.event.player.PrePlayerAttackEntityEvent;
import me.imhermes.artifacts.Artifacts;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static me.imhermes.artifacts.util.Util.*;

public class SpecialArtifacts implements Listener {
    static List<Player> fromElectric = new ArrayList<>();
    static Map<UUID, List<UUID>> attacked = new HashMap<>();
    public SpecialArtifacts() {
        Bukkit.getPluginManager().registerEvents(this, Artifacts.instance());
        Bukkit.getScheduler().runTaskTimer(Artifacts.instance(), () -> Bukkit.getOnlinePlayers().forEach(p -> {
            List<String> enabled = getEnabledArtifacts(p);
            if (enabled.contains("bunny")) {
                if (effectAmplifier(p, PotionEffectType.JUMP_BOOST) < 2) {
                    p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 20, 1, true, false, true));
                    p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20, 0, true, false, true));
                }
            }
            if (enabled.contains("strength_3")) {
                if (effectAmplifier(p, PotionEffectType.STRENGTH) < 3) {
                    p.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 20, 2, true, false, true));
                }
            } else if (enabled.contains("strength_2")) {
                if (effectAmplifier(p, PotionEffectType.STRENGTH) < 2) {
                    p.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 20, 1, true, false, true));
                }
            } else if (enabled.contains("strength_1")) {
                if (effectAmplifier(p, PotionEffectType.STRENGTH) < 1) {
                    p.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 20, 0, true, false, true));
                }
            }
        }), 1, 0);
    }
    @EventHandler
    public void mineBlock(@NotNull BlockBreakEvent e) {
        Player p = e.getPlayer();
        Block block = e.getBlock();
        if (fromElectric.contains(p)) {
            return;
        }
        Location dropLocation = block.getLocation().toCenterLocation();
        List<String> enabled = getEnabledArtifacts(p);
        if (enabled.contains("key_finder")) {
            if (Math.random() < Artifacts.instance().getConfig().getDouble("artifact_usages.key_finder.chance", 0.1)) {
                Artifacts.instance().getConfig().getStringList("artifact_usages.key_finder.commands").stream()
                        .map(s -> s.replaceAll("<p>", p.getName()))
                        .forEachOrdered(cmd -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd));
            }
        }
        if (enabled.contains("miner")) {
            Vector direction = p.getLocation().getDirection().normalize();
            fromElectric.add(p);
            for (int i = 0; i < 4; i++) {
                Block block1 = block.getLocation().add(direction.clone().multiply(i)).getBlock();
                if (block1.getType().isAir()) continue;
                p.breakBlock(block1);
            }
            fromElectric.remove(p);
        }
        if (enabled.contains("electric_miner")) {
            dropLocation.getWorld().strikeLightningEffect(dropLocation);
            Artifacts.instance().mines().values().stream()
                            .filter(volume -> dropLocation.getWorld() == volume.world() && dropLocation.toVector().isInAABB(volume.min(), volume.max()))
                                    .findFirst()
                    .ifPresent(volume -> {
                        fromElectric.add(p);
                        for (int x = volume.min().getBlockX(); x <= volume.max().getBlockX(); x++) {
                            for (int y = volume.min().getBlockY(); y <= volume.max().getBlockY(); y++) {
                                for (int z = volume.min().getBlockZ(); z <= volume.max().getBlockZ(); z++) {
                                    p.breakBlock(p.getWorld().getBlockAt(x, y, z));
                                }
                            }
                        }
                        fromElectric.remove(p);
                    });
        }
        if (enabled.contains("diamond_scavenger")) {
            String cmd = Artifacts.instance().getConfig().getString("artifact_usages.scavenger.diamond");
            if (cmd != null) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replaceAll("<p>", p.getName()));
            }
        } else if (enabled.contains("gold_scavenger")) {
            String cmd = Artifacts.instance().getConfig().getString("artifact_usages.scavenger.gold");
            if (cmd != null) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replaceAll("<p>", p.getName()));
            }
        } else if (enabled.contains("iron_scavenger")) {
            String cmd = Artifacts.instance().getConfig().getString("artifact_usages.scavenger.iron");
            if (cmd != null) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replaceAll("<p>", p.getName()));
            }
        } else if (enabled.contains("coal_scavenger")) {
            String cmd = Artifacts.instance().getConfig().getString("artifact_usages.scavenger.coal");
            if (cmd != null) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replaceAll("<p>", p.getName()));
            }
        }
    }
    @EventHandler
    public void damage(@NotNull EntityDamageEvent e) {
        DamageSource damageSource = e.getDamageSource();
        Entity causing = damageSource.getCausingEntity();
        if (causing == null) return;
        if (causing.getPersistentDataContainer().getOrDefault(namespacedKey("can_not_damage"), PersistentDataType.LIST.strings(), List.of()).contains(e.getEntity().getUniqueId().toString())) {
            e.setCancelled(true);
            return;
        }
        if (causing instanceof Player p) {
            List<String> enabled = getEnabledArtifacts(p);
            if (enabled.contains("hunter")) {
                if (damageSource.getDamageType() == DamageType.ARROW) {
                    if (Math.random() < Artifacts.instance().getConfig().getDouble("artifact_usages.hunter.chance", 0.5)) {
                        if (e.getEntity() instanceof Damageable damaged) {
                            damaged.damage(99999, damageSource);
                        }
                    }
                }
            }
            if (enabled.contains("lightning")) {
                if (damageSource.getDamageType() == DamageType.PLAYER_ATTACK) {
                    if (Math.random() < Artifacts.instance().getConfig().getDouble("artifact_usages.lightning.chance", 0.5)) {
                        Entity damaged = e.getEntity();
                        damaged.getWorld().spawn(damaged.getLocation(), LightningStrike.class, lightning -> {
                            PersistentDataContainer pdc = lightning.getPersistentDataContainer();
                            pdc.set(namespacedKey("can_not_damage"), PersistentDataType.LIST.strings(), List.of(p.getUniqueId().toString()));
                        });
                    }
                }
            }
            List<UUID> alreadyAttacked = new ArrayList<>(attacked.getOrDefault(p.getUniqueId(), List.of()));
            alreadyAttacked.add(e.getEntity().getUniqueId());
            attacked.put(p.getUniqueId(), alreadyAttacked);
        }
    }
    @EventHandler
    public void damage(@NotNull PlayerInteractEvent e) {
        Player p = e.getPlayer();
        List<String> enabled = getEnabledArtifacts(p);
        if (enabled.contains("skeleton_shortbow")) {
            if (e.getAction() == Action.LEFT_CLICK_AIR) {
                ItemStack bow = e.getItem();
                if (bow == null) return;
                if (p.getCooldown(bow) > 0) return;
                if (p.getInventory().contains(Material.ARROW, 1)) {
                    ItemStack arrowItemStack = p.getInventory().getItem(p.getInventory().first(Material.ARROW));
                    if (arrowItemStack != null) {
                        Arrow arrow = p.getWorld().spawnArrow(p.getEyeLocation(), p.getLocation().getDirection(), 3f, 4, Arrow.class);
                        arrow.setShooter(p);
                        arrowItemStack.subtract();
                        bow.damage(1, p);
                        p.setCooldown(bow, 20);
                    }
                } else {
                    ItemStack arrowItemStack = p.getInventory().getItem(p.getInventory().first(Material.TIPPED_ARROW));
                    if (arrowItemStack != null) {
                        Arrow arrow = p.getWorld().spawnArrow(p.getEyeLocation(), p.getLocation().getDirection(), 3f, 12, Arrow.class);
                        ((PotionMeta) arrowItemStack.getItemMeta()).getCustomEffects().forEach(effect -> arrow.addCustomEffect(effect, true));
                        arrow.setShooter(p);
                        arrowItemStack.subtract();
                        bow.damage(1, p);
                        p.setCooldown(bow, 20);
                    }
                }
            }
        }
    }
    @EventHandler
    public void hunger(@NotNull FoodLevelChangeEvent e) {
        HumanEntity p = e.getEntity();
        List<String> enabled = getEnabledArtifacts((Player) p);
        if (enabled.contains("golden_acorn")) {
            if (e.getFoodLevel() < p.getFoodLevel()) e.setCancelled(true);
        }
    }
    @EventHandler
    public void target(@NotNull EntityTargetLivingEntityEvent e) {
        if (e.getTarget() instanceof Player p) {
            List<String> enabled = getEnabledArtifacts(p);
            if (enabled.contains("ghost")) {
                e.setCancelled(true);
            }
        }
    }
    @EventHandler
    public void attack(@NotNull PrePlayerAttackEntityEvent e) {
        Player p = e.getPlayer();
        PersistentDataContainer pdc = e.getAttacked().getPersistentDataContainer();
        List<String> attackedBy = new ArrayList<>(pdc.getOrDefault(namespacedKey("attacked_by"), PersistentDataType.LIST.strings(), List.of()));
        if (!attackedBy.contains(p.getUniqueId().toString())) {
            attackedBy.add(p.getUniqueId().toString());
            pdc.set(namespacedKey("attacked_by"), PersistentDataType.LIST.strings(), attackedBy);
        }
    }
    @EventHandler
    public void death(@NotNull EntityDeathEvent e) {
        LivingEntity killed = e.getEntity();
        DamageSource damageSource = e.getDamageSource();
        if (damageSource.getCausingEntity() instanceof Player p) {
            List<String> enabled = getEnabledArtifacts(p);
            if (enabled.contains("hunter")) {
                if (damageSource.getDamageType() == DamageType.ARROW) {
                    if (Math.random() < Artifacts.instance().getConfig().getDouble("artifact_usages.hunter.chance", 0.5)) {
                        List<ItemStack> drops = e.getDrops();
                        drops.forEach(drop -> killed.getLocation().getWorld().dropItemNaturally(killed.getLocation(), drop));
                    }
                }
            }
        }
    }
}
