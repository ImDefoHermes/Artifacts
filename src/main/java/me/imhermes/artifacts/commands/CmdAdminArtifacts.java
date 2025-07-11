package me.imhermes.artifacts.commands;

import me.imhermes.artifacts.Artifacts;
import me.imhermes.artifacts.listeners.ModifiablePersistentDataContainers;
import me.imhermes.artifacts.util.Volume;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Stream;

import static me.imhermes.artifacts.util.Util.namespacedKey;

public class CmdAdminArtifacts implements CommandExecutor, TabExecutor {
    public CmdAdminArtifacts() {
        Objects.requireNonNull(Bukkit.getPluginCommand("admin-artifacts")).setExecutor(this);
    }
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] args) {
        if (args.length < 1) {
            sender.sendMessage(Artifacts.errorMessage("Not enough arguments"));
            return false;
        }
        switch (args[0].toLowerCase()) {
            case "give" -> {
                if (!sender.hasPermission("artifacts.give")) {
                    sender.sendMessage(Artifacts.errorMessage("Insufficient permissions"));
                    return false;
                }
                if (args.length < 2) {
                    sender.sendMessage(Artifacts.errorMessage("Missing user to give artifact"));
                    return false;
                }
                if (args.length < 3) {
                    sender.sendMessage(Artifacts.errorMessage("Missing artifact name"));
                    return false;
                }
                Player p = Bukkit.getPlayer(args[1]);
                if (p == null) {
                    sender.sendMessage(Artifacts.errorMessage("This player is not online"));
                    return false;
                }
                ConfigurationSection configurationSection = Artifacts.instance().getConfig().getConfigurationSection("artifacts");
                if (configurationSection == null) {
                    sender.sendMessage(Artifacts.errorMessage("There are no artifacts"));
                    return false;
                }
                if (configurationSection.contains(args[2])) {
                    ItemStack artifactItem = configurationSection.getItemStack(args[2]);
                    if (artifactItem == null) {
                        sender.sendMessage(Artifacts.errorMessage("This artifact isn't an item?"));
                        return false;
                    }
                    p.getInventory().addItem(artifactItem);
                    p.sendMessage(Artifacts.defaultMessage("An artifact has been added to your inventory"));
                    return true;
                } else {
                    sender.sendMessage(Artifacts.errorMessage("This artifact doesn't exist"));
                    return false;
                }
            }
            case "remove" -> {
                if (!sender.hasPermission("artifacts.remove")) {
                    sender.sendMessage(Artifacts.errorMessage("Insufficient permissions"));
                    return false;
                }
                if (args.length < 2) {
                    sender.sendMessage(Artifacts.errorMessage("Missing user to remove artifact"));
                    return false;
                }
                if (args.length < 3) {
                    sender.sendMessage(Artifacts.errorMessage("Missing artifact name"));
                    return false;
                }
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[1]);
                if (!offlinePlayer.isOnline() && !offlinePlayer.hasPlayedBefore()) {
                    sender.sendMessage(Artifacts.errorMessage("This player has never joined before"));
                    return false;
                }
                PersistentDataContainer pdc = ModifiablePersistentDataContainers.getModifiablePersistentDataContainer(offlinePlayer);
                List<String> artifactIds = new ArrayList<>(pdc.getOrDefault(namespacedKey("artifacts"), PersistentDataType.LIST.strings(), new ArrayList<>()));

                artifactIds.remove(args[2]);

                pdc.set(namespacedKey("artifacts"), PersistentDataType.LIST.strings(), artifactIds);
                ModifiablePersistentDataContainers.set(offlinePlayer.getUniqueId(), pdc);
                return true;
            }
            case "add" -> {
                if (!sender.hasPermission("artifacts.add")) {
                    sender.sendMessage(Artifacts.errorMessage("Insufficient permissions"));
                    return false;
                }
                if (args.length < 2) {
                    sender.sendMessage(Artifacts.errorMessage("Missing user to give artifact"));
                    return false;
                }
                if (args.length < 3) {
                    sender.sendMessage(Artifacts.errorMessage("Missing artifact name"));
                    return false;
                }
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[1]);
                if (!offlinePlayer.isOnline() && !offlinePlayer.hasPlayedBefore()) {
                    sender.sendMessage(Artifacts.errorMessage("This player has never joined before"));
                    return false;
                }
                PersistentDataContainer pdc = ModifiablePersistentDataContainers.getModifiablePersistentDataContainer(offlinePlayer);
                List<String> artifactIds = new ArrayList<>(pdc.getOrDefault(namespacedKey("artifacts"), PersistentDataType.LIST.strings(), new ArrayList<>()));

                if (args[2].equalsIgnoreCase("*")) {
                    ConfigurationSection configurationSection = Artifacts.instance().getConfig().getConfigurationSection("artifacts");
                    if (configurationSection == null) {
                        sender.sendMessage(Artifacts.errorMessage("There are no artifacts"));
                        return false;
                    }
                    configurationSection.getKeys(false).stream()
                            .filter(id -> !artifactIds.contains(id))
                            .forEach(artifactIds::add);
                    pdc.set(namespacedKey("artifacts"), PersistentDataType.LIST.strings(), artifactIds);
                    ModifiablePersistentDataContainers.set(offlinePlayer.getUniqueId(), pdc);
                    sender.sendMessage(Artifacts.defaultMessage("Added all available artifacts to %s".formatted(offlinePlayer.getName())));
                    return true;
                } else {

                    artifactIds.add(args[2]);

                    pdc.set(namespacedKey("artifacts"), PersistentDataType.LIST.strings(), artifactIds);
                    ModifiablePersistentDataContainers.set(offlinePlayer.getUniqueId(), pdc);
                    sender.sendMessage(Artifacts.defaultMessage("Added artifact %s to %s".formatted(args[2], offlinePlayer.getName())));
                }
                return true;
            }
            case "reload", "reloadconfig" -> {
                if (!sender.hasPermission("artifacts.reload")) {
                    sender.sendMessage(Artifacts.errorMessage("Insufficient permissions"));
                    return false;
                }
                Artifacts.instance().reloadConfig();
                sender.sendMessage(Artifacts.defaultMessage("Reloaded Config"));
                return true;
            }
            case "list" -> {
                if (args.length < 2) {
                    Component artifactsToText = Component.text("All artifacts:")
                            .color(TextColor.color(0xFFAA55));
                    ConfigurationSection configurationSection = Artifacts.instance().getConfig().getConfigurationSection("artifacts");
                    List<String> artifactIds;
                    if (configurationSection == null) {
                        artifactIds = List.of();
                    } else {
                        artifactIds = new ArrayList<>(configurationSection.getKeys(false));
                    }
                    for (String artifactId : artifactIds) {
                        artifactsToText = artifactsToText.appendNewline().append(Component.text("-")
                                .color(TextColor.color(0xAAAAAA))
                                .appendSpace()
                                .append(Component.text(artifactId)
                                        .color(TextColor.color(0xFFAA00))
                                )
                        );
                    }
                    return true;
                }
                if (!sender.hasPermission("artifacts.list")) {
                    sender.sendMessage(Artifacts.errorMessage("Insufficient permissions"));
                    return false;
                }
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[1]);
                if (!offlinePlayer.isOnline() && !offlinePlayer.hasPlayedBefore()) {
                    sender.sendMessage(Artifacts.errorMessage("This player has never joined before"));
                    return false;
                }
                PersistentDataContainer pdc = ModifiablePersistentDataContainers.getModifiablePersistentDataContainer(offlinePlayer);
                List<String> artifactIds = pdc.getOrDefault(namespacedKey("artifacts"), PersistentDataType.LIST.strings(), List.of());

                Component artifactsToText = Component.text("Artifacts of %s:".formatted(offlinePlayer.getName()))
                        .color(TextColor.color(0xFFAA55));
                for (String artifactId : artifactIds) {
                    artifactsToText = artifactsToText.appendNewline().append(Component.text("-")
                            .color(TextColor.color(0xAAAAAA))
                            .appendSpace()
                            .append(Component.text(artifactId)
                                    .color(TextColor.color(0xFFAA00))
                            )
                    );
                }
                sender.sendMessage(artifactsToText);
                return true;
            }
            case "set" -> {
                if (!sender.hasPermission("artifacts.set")) {
                    sender.sendMessage(Artifacts.errorMessage("Insufficient permissions"));
                    return false;
                }
                if (sender instanceof Player p) {
                    if (args.length < 2) {
                        sender.sendMessage(Artifacts.errorMessage("Missing artifact id to set this item to"));
                        return false;
                    }
                    String artifactId = args[1].toLowerCase();
                    ItemStack itemInHand = p.getInventory().getItemInMainHand();
                    itemInHand.editMeta(itemMeta -> itemMeta.getPersistentDataContainer().set(namespacedKey("artifact"), PersistentDataType.STRING, artifactId));
                    ItemStack alreadyArtifact = Artifacts.instance().getConfig().getItemStack("artifacts." + artifactId);
                    Artifacts.instance().getConfig().set("artifacts." + artifactId, itemInHand);
                    Artifacts.instance().saveConfig();
                    if (Arrays.stream(args).noneMatch("-noAutoModify"::equalsIgnoreCase) && alreadyArtifact != null) {
                        itemInHand.editPersistentDataContainer(pdc -> pdc.set(namespacedKey("artifact"), PersistentDataType.STRING, artifactId));
                    }
                    if (Arrays.stream(args).noneMatch("-noRemove"::equalsIgnoreCase)) {
                        p.getInventory().remove(itemInHand);
                    }
                    if (Arrays.stream(args).noneMatch("-noReturn"::equalsIgnoreCase) && alreadyArtifact != null) {
                        p.getInventory().addItem(alreadyArtifact);
                    }
                    p.sendMessage(Artifacts.defaultMessage("Set artifact of %s to item in your main hand".formatted(artifactId)));
                    return true;
                } else {
                    sender.sendMessage(Artifacts.errorMessage("This command is only executable by players"));
                }
            }
            case "delete", "unset" -> {
                if (!sender.hasPermission("artifacts.set")) {
                    sender.sendMessage(Artifacts.errorMessage("Insufficient permissions"));
                    return false;
                }
                if (sender instanceof Player p) {
                    if (args.length < 2) {
                        sender.sendMessage(Artifacts.errorMessage("Missing artifact id to delete"));
                        return false;
                    }
                    String artifactId = args[1].toLowerCase();
                    Artifacts.instance().getConfig().set("artifacts." + artifactId, null);
                    Artifacts.instance().saveConfig();
                    p.sendMessage(Artifacts.defaultMessage("Removed artifact of id %s".formatted(artifactId)));
                    return true;
                } else {
                    sender.sendMessage(Artifacts.errorMessage("This command is only executable by players"));
                }
            }
            case "special" -> {
                if (!sender.hasPermission("artifacts.special")) {
                    sender.sendMessage(Artifacts.errorMessage("Insufficient permissions"));
                    return false;
                }
                sender.sendMessage(MiniMessage.miniMessage().deserialize("""
                        <b><gray>Artifacts with special ids:</gray></b>
                        <aqua>bunny, strength_3, strength_2, strength_1, key_finder, miner, electric_miner, diamond_scavenger, gold_scavenger, iron_scavenger, coal_scavenger, hunter, lightning, skeleton_shortbow, golden_acorn, ghost</aqua>"""));
                return true;
            }
            case "mine" -> {
                if (!(sender instanceof Player p)) {
                    sender.sendMessage(Artifacts.errorMessage("Only players can execute this command"));
                    return false;
                }
                if (!sender.hasPermission("artifacts.mine")) {
                    sender.sendMessage(Artifacts.errorMessage("Insufficient permissions"));
                    return false;
                }
                if (args.length < 2) {
                    sender.sendMessage(Artifacts.errorMessage("Not enough arguments"));
                    return false;
                }
                String mineName = args[1];
                if (Arrays.stream(args).anyMatch("-r"::equalsIgnoreCase)) {
                    Artifacts.instance().getConfig().set("mines." + mineName, null);
                    return true;
                }
                if (args.length < 8) {
                    sender.sendMessage(Artifacts.errorMessage("Not enough arguments"));
                    return false;
                }
                try {
                    int x0 = Integer.parseInt(args[2]);
                    int y0 = Integer.parseInt(args[3]);
                    int z0 = Integer.parseInt(args[4]);

                    int x1 = Integer.parseInt(args[5]);
                    int y1 = Integer.parseInt(args[6]);
                    int z1 = Integer.parseInt(args[7]);

                    int minX = Math.min(x0, x1);
                    int minY = Math.min(y0, y1);
                    int minZ = Math.min(z0, z1);

                    int maxX = Math.max(x0, x1);
                    int maxY = Math.max(y0, y1);
                    int maxZ = Math.max(z0, z1);

                    Volume volume = new Volume(p.getWorld(), new Vector(minX, minY, minZ), new Vector(maxX, maxY, maxZ));
                    Artifacts.instance().getConfig().set("mines." + mineName + ".world", volume.world().getName());
                    Artifacts.instance().getConfig().set("mines." + mineName + ".min", volume.min());
                    Artifacts.instance().getConfig().set("mines." + mineName + ".max", volume.max());
                    Artifacts.instance().saveConfig();
                } catch (NumberFormatException e) {
                    sender.sendMessage(Artifacts.errorMessage("Invalid arguments"));
                }
                return true;
            }
            default -> sender.sendMessage(Artifacts.errorMessage("This is not a valid argument"));
        }
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length > 1) {
            if (args[0].equalsIgnoreCase("set")) {
                return Stream.of(
                                "-noReturn",
                                "-noRemove",
                                "-noAutoModify"
                        )
                        .filter(arg -> arg.startsWith(args[1]))
                        .toList();
            }
        }
        if (args[0].equalsIgnoreCase("mine")) {
            if (sender instanceof Player p) {
                Block targetBlock = p.getTargetBlockExact(4);
                if (targetBlock == null) return List.of();
                Vector position = targetBlock.getLocation().toVector();
                return switch (args.length) {
                    case 3, 6 -> List.of("%s %s %s".formatted(position.getBlockX(), position.getBlockY(), position.getBlockZ()));
                    case 4, 7 -> List.of("%s %s".formatted(position.getBlockY(), position.getBlockZ()));
                    case 5, 8 -> List.of(String.valueOf(position.getBlockZ()));
                    default -> List.of();
                };
            }
        }
        return switch (args.length) {
            case 1 -> Stream.of(
                    "remove",
                            "add",
                            "give",
                            "reload",
                            "list",
                            "set",
                            "delete",
                            "unset",
                            "help",
                            "special",
                            "mine"
                    )
                    .filter(arg -> arg.startsWith(args[0]))
                    .toList();
            case 2 -> switch (args[0].toLowerCase()) {
                case "remove", "add", "give", "list" -> Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(arg -> arg.startsWith(args[1]))
                        .toList();
                case "delete", "unset" -> {
                    ConfigurationSection configurationSection = Artifacts.instance().getConfig().getConfigurationSection("artifacts");
                    if (configurationSection == null) {
                        yield List.of();
                    }
                    yield new ArrayList<>(configurationSection.getKeys(false));
                }
                default -> List.of();
            };
            case 3 -> switch (args[0].toLowerCase()) {
                case "remove" -> {
                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[1]);
                    if (!offlinePlayer.isOnline() && !offlinePlayer.hasPlayedBefore()) yield List.of();
                    PersistentDataContainer pdc = ModifiablePersistentDataContainers.getModifiablePersistentDataContainer(offlinePlayer);
                    List<String> artifactIds = pdc.getOrDefault(namespacedKey("artifacts"), PersistentDataType.LIST.strings(), List.of());
                    yield artifactIds.stream()
                            .filter(arg -> arg.startsWith(args[2]))
                            .toList();
                }
                case "add", "give" -> {
                    ConfigurationSection configurationSection = Artifacts.instance().getConfig().getConfigurationSection("artifacts");
                    if (configurationSection == null) {
                        yield List.of();
                    }
                    yield new ArrayList<>(configurationSection.getKeys(false));
                }
                default -> List.of();
            };
            default -> List.of();
        };
    }
}
