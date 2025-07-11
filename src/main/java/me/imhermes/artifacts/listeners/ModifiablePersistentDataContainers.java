package me.imhermes.artifacts.listeners;

import io.papermc.paper.persistence.PersistentDataContainerView;
import me.imhermes.artifacts.Artifacts;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import static me.imhermes.artifacts.util.Util.namespacedKey;

public class ModifiablePersistentDataContainers implements Listener {
    public ModifiablePersistentDataContainers() {
        Bukkit.getPluginManager().registerEvents(this, Artifacts.instance());
    }
    @EventHandler
    public void modify(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        World world = Bukkit.getWorld("world");
        if (world == null) return;
        PersistentDataContainer savedPersistentDataContainers = world.getPersistentDataContainer().getOrDefault(namespacedKey("saved_player_pdcs"), PersistentDataType.TAG_CONTAINER, world.getPersistentDataContainer().getAdapterContext().newPersistentDataContainer());
        PersistentDataContainer savedPlayerData = savedPersistentDataContainers.get(namespacedKey(p.getUniqueId().toString()), PersistentDataType.TAG_CONTAINER);
        if (savedPlayerData != null) {
            savedPlayerData.copyTo(p.getPersistentDataContainer(), true);
            savedPlayerData.remove(namespacedKey(p.getUniqueId().toString()));
        }
    }

    public static void set(UUID uuid, PersistentDataContainerView pdc) {
        World world = Bukkit.getWorld("world");
        if (world == null) return;
        PersistentDataContainer savedPersistentDataContainers = world.getPersistentDataContainer().getOrDefault(namespacedKey("saved_player_pdcs"), PersistentDataType.TAG_CONTAINER, world.getPersistentDataContainer().getAdapterContext().newPersistentDataContainer());
        PersistentDataContainer newPdc = pdc.getAdapterContext().newPersistentDataContainer();
        pdc.copyTo(newPdc, true);
        savedPersistentDataContainers.set(namespacedKey(uuid.toString()), PersistentDataType.TAG_CONTAINER, newPdc);
    }
    public static PersistentDataContainer getModifiablePersistentDataContainer(@NotNull OfflinePlayer offlinePlayer) {
        World world = Bukkit.getWorld("world");
        assert world != null: "world is null";
        Player p = offlinePlayer.getPlayer();
        if (p != null) {
            return p.getPersistentDataContainer();
        } else {
            PersistentDataContainer savedPersistentDataContainers = world.getPersistentDataContainer().getOrDefault(namespacedKey("saved_player_pdcs"), PersistentDataType.TAG_CONTAINER, world.getPersistentDataContainer().getAdapterContext().newPersistentDataContainer());
            if (savedPersistentDataContainers.has(namespacedKey(offlinePlayer.getUniqueId().toString()), PersistentDataType.TAG_CONTAINER)) {
                return savedPersistentDataContainers.get(namespacedKey(offlinePlayer.getUniqueId().toString()), PersistentDataType.TAG_CONTAINER);
            } else {
                PersistentDataContainerView pdcv = offlinePlayer.getPersistentDataContainer();
                PersistentDataContainer newPdc = pdcv.getAdapterContext().newPersistentDataContainer();
                pdcv.copyTo(newPdc, true);
                return newPdc;
            }
        }
    }
}
