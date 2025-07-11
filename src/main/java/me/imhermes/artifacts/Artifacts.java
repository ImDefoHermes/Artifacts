package me.imhermes.artifacts;

import me.imhermes.artifacts.commands.CmdAdminArtifacts;
import me.imhermes.artifacts.commands.CmdArtifact;
import me.imhermes.artifacts.listeners.*;
import me.imhermes.artifacts.util.Volume;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;


public final class Artifacts extends JavaPlugin {

    static Artifacts instance;
    @Override
    public void onEnable() {
        saveDefaultConfig();
        instance = this;
        new CmdAdminArtifacts();
        new CmdArtifact();

        new SpecialArtifacts();
        new ModifiablePersistentDataContainers();
        new EnableArtifacts();
    }
    public Map<String, Volume> mines() {
        ConfigurationSection configurationSection = getConfig().getConfigurationSection("mines");
        if (configurationSection == null) return Map.of();
        Map<String, Volume> mines = new HashMap<>();
        configurationSection.getKeys(false).forEach(k -> {
            ConfigurationSection mineConfig = getConfig().getConfigurationSection("mines." + k);
            if (mineConfig == null) return;
            String worldName = mineConfig.getString("world");
            Vector min = mineConfig.getVector("min");
            Vector max = mineConfig.getVector("max");
            if (worldName == null || min == null || max == null) return;
            World world = Bukkit.getWorld(worldName);
            if (world == null) return;
            mines.put(k, new Volume(world, min, max));
        });
        return mines;
    }
    @Override
    public void onDisable() {
        if (getConfig().getBoolean("whitelist-on-disable")) {
            Bukkit.setWhitelist(true);
            Bukkit.broadcast(defaultMessage("Whitelist enabled due to plugin being disabled"));
        }
        // Plugin shutdown logic
    }

    public static @NotNull Component defaultMessage(String message) {
        return MiniMessage.miniMessage().deserialize(instance().getConfig().getString("prefix.message", "<b><light_purple>[Artifacts]</light_purple></b>")).appendSpace().append(Component.text(message).color(TextColor.color(0xFFAAFF)));
    }

    public static @NotNull Component errorMessage(String message) {
        return MiniMessage.miniMessage().deserialize(instance().getConfig().getString("prefix.error", "<b><red>[Artifacts Error]</red></b>")).appendSpace().append(Component.text(message).color(TextColor.color(0xFFAAAA)));
    }

    public static Artifacts instance() {
        return instance;
    }
}
