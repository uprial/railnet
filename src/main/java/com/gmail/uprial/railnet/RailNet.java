package com.gmail.uprial.railnet;

import com.gmail.uprial.railnet.common.CustomLogger;
import com.gmail.uprial.railnet.config.InvalidConfigException;
import com.gmail.uprial.railnet.firework.FireworkEngine;
import com.gmail.uprial.railnet.listeners.*;
import com.gmail.uprial.railnet.populator.ChunkPopulator;
import com.gmail.uprial.railnet.populator.Populator;
import com.gmail.uprial.railnet.populator.mineshaft.MineshaftPopulator;
import com.gmail.uprial.railnet.populator.railway.RailWayPopulator;
import com.gmail.uprial.railnet.populator.whirlpool.WhirlpoolPopulator;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.gmail.uprial.railnet.RailNetCommandExecutor.COMMAND_NS;

public final class RailNet extends JavaPlugin {
    private final String CONFIG_FILE_NAME = "config.yml";
    private final File configFile = new File(getDataFolder(), CONFIG_FILE_NAME);

    private CustomLogger consoleLogger = null;

    private Populator populator = null;

    private FireworkEngine fireworkEngine = null;

    private RailNetCron cron = null;

    @Override
    public void onEnable() {
        saveDefaultConfig();

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
        chunkPopulators.add(new WhirlpoolPopulator(this, consoleLogger, railWayName));
        // Order does matter: populate chests in RailWay and Whirlpool.
        chunkPopulators.add(new MineshaftPopulator(this, consoleLogger));

        populator = new Populator(this, consoleLogger, chunkPopulators);

        getServer().getPluginManager().registerEvents(new ChunkListener(populator), this);
        getServer().getPluginManager().registerEvents(new NastyEndermanListener(consoleLogger), this);
        getServer().getPluginManager().registerEvents(new NastyShooterListener(this, consoleLogger), this);
        getServer().getPluginManager().registerEvents(new ExplosiveShooterListener(this, consoleLogger), this);
        getServer().getPluginManager().registerEvents(new StrongBlockListener(), this);
        getServer().getPluginManager().registerEvents(new AngryShooterListener(consoleLogger), this);
        getServer().getPluginManager().registerEvents(new NastyEnderDragonListener(this, consoleLogger), this);

        fireworkEngine = new FireworkEngine(this, consoleLogger);
        fireworkEngine.enableCraftBook();

        getServer().getPluginManager().registerEvents(new ExplosiveFireworkListener(fireworkEngine), this);

        getCommand(COMMAND_NS).setExecutor(new RailNetCommandExecutor(this));
        consoleLogger.info("Plugin enabled");
    }

    public void scheduleDelayed(final Runnable runnable, final long delay) {
        getServer().getScheduler().scheduleSyncDelayedTask(this, runnable, delay);
    }

    int repopulateLoaded(final String worldName, final int x, final int z, final int radius) {
        return populator.repopulateLoaded(worldName, x, z, radius);
    }

    int breakTerrain(final String worldName, final int x, final int y, final int z, final int radius) {
        final World world = getServer().getWorld(worldName);
        int counter = 0;
        if(world != null) {
            final ItemStack tool = new ItemStack(Material.NETHERITE_PICKAXE);
            //tool.addEnchantment(Enchantment.FORTUNE, 3);
            /*
                Since we're breaking blocks
                it's better to s tart from top layers that may affect lower levels.
             */
            for(int dy = radius; dy >= -radius; dy--) {
                for (int dx = -radius; dx <= radius; dx++) {
                    for (int dz = -radius; dz <= radius; dz++) {
                        if(world.getBlockAt(x + dx, y + dy, z + dz).breakNaturally(tool)) {
                            counter++;
                        }
                    }
                }
            }
        }

        return counter;
    }

    void populatePlayer(final Player player, final int density) {
        new MineshaftPopulator(this, consoleLogger).populatePlayer(player, density);
    }

    @Override
    public void onDisable() {
        // If onEnable() didn't finish, this variable is highly probably null.
        if(fireworkEngine != null) {
            fireworkEngine.disableCraftBook();
        }
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
