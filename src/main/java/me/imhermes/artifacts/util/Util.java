package me.imhermes.artifacts.util;

import me.imhermes.artifacts.Artifacts;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class Util {
    private Util() {}
    private static final Map<String, NamespacedKey> namespacedKeyMap = new HashMap<>();
    public static NamespacedKey namespacedKey(String value) {
        if (namespacedKeyMap.containsKey(value)) {
            return namespacedKeyMap.get(value);
        }
        NamespacedKey namespacedKey = new NamespacedKey(Artifacts.instance(), value);
        namespacedKeyMap.put(value, namespacedKey);
        return namespacedKey;
    }

    public static String getArtifact(@NotNull ItemStack itemStack) {
        return itemStack.getPersistentDataContainer().get(namespacedKey("artifact"), PersistentDataType.STRING);
    }

    public static List<String> getEnabledArtifacts(OfflinePlayer p) {
        return new ArrayList<>(p.getPersistentDataContainer().getOrDefault(namespacedKey("enabled_artifacts"), PersistentDataType.LIST.strings(), List.of()));
    }
    public static int effectAmplifier(Player p, PotionEffectType type) {
        PotionEffect effect = p.getPotionEffect(type);
        if (effect == null) return -1;
        return effect.getAmplifier();
    }
}
