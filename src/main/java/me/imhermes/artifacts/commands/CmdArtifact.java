package me.imhermes.artifacts.commands;

import me.imhermes.artifacts.Artifacts;
import me.imhermes.artifacts.util.Util;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static me.imhermes.artifacts.util.Util.namespacedKey;

public class CmdArtifact implements CommandExecutor, Listener {
    List<Inventory> inventories = new ArrayList<>();
    public CmdArtifact() {
        Objects.requireNonNull(Bukkit.getPluginCommand("artifact")).setExecutor(this);
        Bukkit.getPluginManager().registerEvents(this, Artifacts.instance());
    }
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] args) {
        if (sender instanceof Player p) {
            Inventory inv = createCenteredInventory(p);
            inventories.add(inv);
            p.openInventory(inv);
            // TODO: Make the command /artifact as a menu with the player's artifacts.
        }
        return false;
    }

    @EventHandler
    public void click(@NotNull InventoryClickEvent e) {
        if (!inventories.contains(e.getInventory())) return;
        e.setCancelled(true);
        HumanEntity p = e.getWhoClicked();
        PersistentDataContainer pdc = p.getPersistentDataContainer();
        List<String> enabledArtifacts = new ArrayList<>(pdc.getOrDefault(namespacedKey("enabled_artifacts"), PersistentDataType.LIST.strings(), List.of()));
        List<String> usableArtifacts = new ArrayList<>(pdc.getOrDefault(namespacedKey("artifacts"), PersistentDataType.LIST.strings(), List.of()));
        ItemStack itemStack = e.getCurrentItem();
        if (itemStack == null) return;
        String id = Util.getArtifact(itemStack);
        if (id == null) return;
        int maxArtifacts = 3;
        if (enabledArtifacts.contains(id)) {
            itemStack.editMeta(itemMeta -> itemMeta.setEnchantmentGlintOverride(false));
            enabledArtifacts.remove(id);
            pdc.set(namespacedKey("enabled_artifacts"), PersistentDataType.LIST.strings(), enabledArtifacts);
            p.sendMessage(Artifacts.defaultMessage("You have disabled this artifact"));
        } else {
            if (!usableArtifacts.contains(id)) {
                p.sendMessage(Artifacts.errorMessage("You don't have this artifact"));
                return;
            }
            if (enabledArtifacts.size() >= maxArtifacts) {
                p.sendMessage(Artifacts.defaultMessage("You have reached the maximum number of enabled artifacts"));
                return;
            }
            enabledArtifacts.add(id);
            pdc.set(namespacedKey("enabled_artifacts"), PersistentDataType.LIST.strings(), enabledArtifacts);
            p.sendMessage(Artifacts.defaultMessage("You have enabled this artifact"));

            itemStack.editMeta(itemMeta -> itemMeta.setEnchantmentGlintOverride(true));
        }
    }
    @EventHandler
    public void close(InventoryCloseEvent e) {
        inventories.remove(e.getInventory());
    }

    private Inventory createCenteredInventory(Player p) {
        ConfigurationSection configurationSection = Artifacts.instance().getConfig().getConfigurationSection("artifacts");
        ItemStack empty = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        empty.editMeta(itemMeta -> {
            itemMeta.itemName(Component.empty());
            itemMeta.setHideTooltip(true);
        });
        ItemStack unusable = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        unusable.editMeta(itemMeta -> itemMeta.itemName(Component.text("This artifact has not been found yet!")));
        if (configurationSection == null) {
            Inventory inv = Bukkit.createInventory(null, 9, Artifacts.errorMessage("No artifacts found in config"));
            for (int i = 0; i < inv.getSize(); i++) {
                inv.setItem(i, empty);
            }
            return inv;
        }
        Map<String, Object> map = configurationSection.getValues(false);

        int artifacts = map.size();
        int rows = Math.max(1, (int) Math.ceil(artifacts / 9.0));
        int size = rows * 9;

        Inventory inv = Bukkit.createInventory(p, size, Component.text("Artifacts"));

        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, empty);
        }
        List<ItemStack> artifactItems = map.values().stream()
                .filter(ItemStack.class::isInstance)
                .map(i -> (ItemStack) i)
                .toList();
        int placed = 0;
        for (int row = 0; row < rows; row++) {
            int itemsInRow = Math.min(artifacts - placed, 9);

            int start = (9 - itemsInRow) / 2;

            for (int i = 0; i < itemsInRow; i++) {

                int slot = row * 9 + start + i;
                if (itemsInRow % 2 == 0 && slot > i / 2) slot++;
                ItemStack itemStack = artifactItems.get(placed);
                if (p.getPersistentDataContainer().getOrDefault(namespacedKey("artifacts"), PersistentDataType.LIST.strings(), List.of()).contains(Util.getArtifact(itemStack))) {
                    itemStack.editMeta(itemMeta -> itemMeta.setEnchantmentGlintOverride(p.getPersistentDataContainer().getOrDefault(namespacedKey("enabled_artifacts"), PersistentDataType.LIST.strings(), List.of()).contains(Util.getArtifact(itemStack))));
                    inv.setItem(slot, itemStack);
                } else {
                    inv.setItem(slot, unusable);
                }
                placed++;

                if (placed >= artifacts) break;
            }
        }

        return inv;
    }


}
