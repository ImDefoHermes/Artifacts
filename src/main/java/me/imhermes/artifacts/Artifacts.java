package me.imhermes.artifacts;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.plugin.java.JavaPlugin;

public final class Artifacts extends JavaPlugin {

    @Override
    public void onEnable() {

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static Component errorMessage(String message) {
        return Component.text("[Error] %s".formatted(message))
                .color(TextColor.color(0xFF5555));
    }

    public static Component defaultMessage(String message) {
        return Component.text("[Artifacts] %s".formatted(message))
                .color(TextColor.color(0x5555FF));
    }
}
