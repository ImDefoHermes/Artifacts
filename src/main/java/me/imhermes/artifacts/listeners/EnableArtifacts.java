package me.imhermes.artifacts.listeners;

import me.imhermes.artifacts.Artifacts;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static me.imhermes.artifacts.util.Util.*;

public class EnableArtifacts implements Listener {
    public EnableArtifacts() {
        Bukkit.getPluginManager().registerEvents(this, Artifacts.instance());
    }
    @EventHandler
    public void rightClick(@NotNull PlayerInteractEvent e) {
        ItemStack item = e.getItem();
        if (item == null) return;
        Player p = e.getPlayer();
        if (!e.getAction().isRightClick()) return;
        String artifact = item.getPersistentDataContainer().get(namespacedKey("artifact"), PersistentDataType.STRING);
        if (artifact == null) return;
        PersistentDataContainer pdc = p.getPersistentDataContainer();
        List<String> available = new ArrayList<>(pdc.getOrDefault(namespacedKey("artifacts"), PersistentDataType.LIST.strings(), List.of()));
        if (available.contains(artifact)) {
            p.playSound(p, "minecraft:entity.enderman.teleport", 1, 0.5f);
            p.sendMessage(Artifacts.defaultMessage("You already have this artifact enabled"));
            return;
        }
        available.add(artifact);
        pdc.set(namespacedKey("artifacts"), PersistentDataType.LIST.strings(), available);
        p.playSound(p, "minecraft:entity.player.burp", 1, 1);
        item.subtract();
    }
}
