package com.gmail.uprial.railnet.common;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;

public class Formatter {
    public static String format(final Block block) {
        return String.format("%s[%s:%d:%d:%d]",
                block.getType(),
                block.getWorld().getName(),
                block.getX(), block.getY(), block.getZ());
    }

    public static String format(final Entity entity) {
        return String.format("%s[%s:%.0f:%.0f:%.0f]",
                entity.getType(),
                entity.getWorld().getName(),
                entity.getLocation().getX(), entity.getLocation().getY(), entity.getLocation().getZ());
    }

    public static String format(final Location location) {
        return String.format("%s:%.0f:%.0f:%.0f",
                location.getWorld().getName(),
                location.getX(), location.getY(), location.getZ());
    }
}
