package me.imhermes.artifacts.commands;

import me.imhermes.artifacts.Artifacts;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

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
            case "removeArtifact" -> {

            }
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length == 1) {
            return Stream.of(
                    "removeArtifact"
            )
                    .filter(args[0].toLowerCase()::startsWith)
                    .toList();
        }
        return List.of();
    }
}
