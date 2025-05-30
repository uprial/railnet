package com.gmail.uprial.railnet;

import com.gmail.uprial.railnet.common.CustomLogger;
import com.gmail.uprial.railnet.common.WorldName;
import com.gmail.uprial.railnet.config.InvalidConfigException;
import com.gmail.uprial.railnet.listeners.*;
import com.gmail.uprial.railnet.populator.ChunkPopulator;
import com.gmail.uprial.railnet.populator.Populator;
import com.gmail.uprial.railnet.populator.dungeon.DungeonPopulator;
import com.gmail.uprial.railnet.populator.mineshaft.MineshaftPopulator;
import com.gmail.uprial.railnet.populator.railway.RailWayPopulator;
import com.gmail.uprial.railnet.populator.whirlpool.WhirlpoolPopulator;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;

import static com.gmail.uprial.railnet.RailNetCommandExecutor.COMMAND_NS;

public final class RailNet extends JavaPlugin {
    private final String CONFIG_FILE_NAME = "config.yml";
    private final File configFile = new File(getDataFolder(), CONFIG_FILE_NAME);

    private CustomLogger consoleLogger = null;

    private Populator populator = null;

    private RailNetCron cron = null;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        for(final String worldName : WorldName.getAll()) {
            if(getServer().getWorld(worldName) == null) {
                throw new RuntimeException(String.format("World '%s' not found", worldName));
            }
        }

        cron = new RailNetCron(this);

        consoleLogger = new CustomLogger(getLogger());
        final RailNetConfig railNetConfig = loadConfig(getConfig(), consoleLogger);

        final List<ChunkPopulator> chunkPopulators = new ArrayList<>();

        final String railWayName;
        if(railNetConfig.hasUndergroundRailways()) {
            // Order does matter: RailWay is top priority
            final RailWayPopulator railWayPopulator = new RailWayPopulator(this, consoleLogger);
            railWayName = railWayPopulator.getName();

            chunkPopulators.add(railWayPopulator);
        } else {
            railWayName = null;
        }
        chunkPopulators.add(new WhirlpoolPopulator(consoleLogger, railWayName));
        chunkPopulators.add(new DungeonPopulator(consoleLogger, railWayName));
        // Order does matter: populate chests in RailWay and Whirlpool.
        chunkPopulators.add(new MineshaftPopulator(this, consoleLogger, railNetConfig.hasDynamicLootDensity()));

        populator = new Populator(this, consoleLogger, chunkPopulators);

        getServer().getPluginManager().registerEvents(new ChunkListener(populator), this);
        getServer().getPluginManager().registerEvents(new StrongBlockListener(), this);

        getCommand(COMMAND_NS).setExecutor(new RailNetCommandExecutor(this));
        consoleLogger.info("Plugin enabled");
    }

    int repopulateLoaded(final String worldName, final int x, final int z, final int radius) {
        return populator.repopulateLoaded(worldName, x, z, radius);
    }

    void populatePlayer(final Player player, final int density) {
        new MineshaftPopulator(this, consoleLogger, false).populatePlayer(player, density);
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
        cron.cancel();
        consoleLogger.info("Plugin disabled");
    }

    @Override
    public void saveDefaultConfig() {
        if (!configFile.exists()) {
            saveResource(CONFIG_FILE_NAME, false);
        }
    }

    @Override
    public FileConfiguration getConfig() {
        return YamlConfiguration.loadConfiguration(configFile);
    }

    static RailNetConfig loadConfig(FileConfiguration config, CustomLogger customLogger) {
        return loadConfig(config, customLogger, null);
    }

    private static RailNetConfig loadConfig(FileConfiguration config, CustomLogger mainLogger, CustomLogger secondLogger) {
        RailNetConfig railNetConfig = null;

        try {
            final boolean isDebugMode = RailNetConfig.isDebugMode(config, mainLogger);
            mainLogger.setDebugMode(isDebugMode);
            if(secondLogger != null) {
                secondLogger.setDebugMode(isDebugMode);
            }

            railNetConfig = RailNetConfig.getFromConfig(config, mainLogger);
        } catch (InvalidConfigException e) {
            mainLogger.error(e.getMessage());
        }

        return railNetConfig;
    }
}
