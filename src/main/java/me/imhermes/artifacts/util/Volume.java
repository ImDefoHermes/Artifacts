package me.imhermes.artifacts.util;

import org.bukkit.World;
import org.bukkit.util.Vector;

public record Volume(World world, Vector min, Vector max) {}
