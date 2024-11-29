package com.gmail.uprial.railnet.populator.whirlpool;

import com.gmail.uprial.railnet.common.CustomLogger;
import com.gmail.uprial.railnet.common.Probability;
import com.gmail.uprial.railnet.populator.ChunkPopulator;
import com.gmail.uprial.railnet.populator.VirtualChunk;
import com.gmail.uprial.railnet.populator.ItemConfig;
import com.gmail.uprial.railnet.populator.mineshaft.MineshaftPopulator;
import com.gmail.uprial.railnet.populator.railway.RailWayPopulator;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class WhirlpoolPopulator implements ChunkPopulator {
    private final CustomLogger customLogger;

    VirtualChunk vc;

    public WhirlpoolPopulator(final CustomLogger customLogger) {
        this.customLogger = customLogger;
    }

    private final static double FISHING_ROD_PROBABILITY = 10.0D;
    private final ItemConfig fishingRodItemConfig = new ItemConfig()
            .ench(Enchantment.LUCK_OF_THE_SEA, 0, 3)
            .ench(Enchantment.LURE, 0, 3)
            .ench(Enchantment.UNBREAKING, 0, 3);

    @Override
    public void populate(final Chunk chunk) {
        if(isAppropriate(chunk)) {
            vc = new VirtualChunk("Whirlpool", chunk, BlockFace.NORTH);

            int minX = 1;
            int minY = vc.getSeaLevel();
            int minZ = 1;
            for(int x = 1; x < 15; x++) {
                for(int z = 1; z < 15; z++) {
                    int y = vc.getSeaLevel();
                    // +1 layer for a chest
                    while((y > vc.getMinHeight() + 1) && (isWaterLayer(x, y, z))) {
                        y--;
                    }
                    // Don't overlap with other structures
                    while((y < vc.getSeaLevel()) && (isConflicting(x, y, z))) {
                        y++;
                    }
                    if(y < minY) {
                        minX = x;
                        minY = y;
                        minZ = z;
                    }
                }
            }

            if(minY >= vc.getSeaLevel()) {
                if(customLogger.isDebugMode()) {
                    // No water
                    customLogger.debug(String.format("Whirlpool %s:%d:%d can't be populated",
                            chunk.getWorld().getName(), chunk.getX(), chunk.getZ()));
                }
                return;
            }

            for(int dx = -1; dx <= 1; dx++) {
                for(int dz = -1; dz <= 1; dz++) {
                    int y = minY;
                    // +1 layer for a chest
                    while((y > vc.getMinHeight() + 1) && isWater(minX + dx, y,  minZ + dz)) {
                        y--;
                    }
                    // Don't overlap with other structures
                    while((y < vc.getSeaLevel()) && (isConflicting(minX + dx, y,  minZ + dz))) {
                        y++;
                    }

                    if(y < vc.getSeaLevel()) {
                        if (vc.get(minX + dx, y, minZ + dz).getType().equals(Material.MAGMA_BLOCK)) {
                            if (customLogger.isDebugMode()) {
                                // Idempotency marker
                                customLogger.debug(String.format("Whirlpool %s:%d:%d is already populated",
                                        chunk.getWorld().getName(), chunk.getX(), chunk.getZ()));
                            }
                            return;
                        }

                        vc.applyPhysicsOnce();
                        vc.set(minX + dx, y, minZ + dz, Material.MAGMA_BLOCK);

                        if ((dx == 0) && (dz == 0)
                                // Sacrifice a chest when overlaps with other structures
                                && (!isConflicting(minX + dx, y - 1, minZ + dz))) {
                            final Block block = vc.set(minX + dx, y - 1, minZ + dz, Material.CHEST);

                            final Inventory inventory = ((Chest) block.getState()).getBlockInventory();
                            final int i = 0;

                            if(Probability.PASS(FISHING_ROD_PROBABILITY)) {
                                inventory.setItem(i, new ItemStack(Material.FISHING_ROD, 1));

                                // The fresh getItem() is needed to properly update the amount
                                fishingRodItemConfig.apply(inventory.getItem(i));
                            }

                            /*
                                The deepest water the more loot in the chest.

                                One more population will happen if no idempotency marker is set.
                             */
                            int density = (int)Math.floor((vc.getSeaLevel() - y) / 10.0D);
                            new MineshaftPopulator(customLogger).populateChest(block, density);
                        }
                    }
                }
            }

            if(customLogger.isDebugMode()) {
                customLogger.debug(String.format("Whirlpool %s:%d:%d populated",
                        chunk.getWorld().getName(), chunk.getX(), chunk.getZ()));
            }
        }
    }

    private boolean isWater(final int x, final int y, final int z) {
        return vc.get(x, y, z).getType().equals(Material.WATER);
    }

    private boolean isWaterLayer(final int x, final int y, final int z) {
        for(int dx = -1; dx <= 1; dx++) {
            for(int dz = -1; dz <= 1; dz++) {
                if(!isWater(x + dx, y, z + dz)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isConflicting(final int x, final int y, final int z) {
        return RailWayPopulator.isBorderBlock(vc.get(x, y, z).getType());
    }

    final static String world = "world";
    final static int xRate = 13;
    final static int zRate = 7;

    private boolean isAppropriate(final Chunk chunk) {
        return (chunk.getWorld().getName().equalsIgnoreCase(world))
                && (Math.abs(chunk.getWorld().getSeed()) % xRate == Math.abs(chunk.getX()) % xRate)
                && (Math.abs(chunk.getWorld().getSeed()) % zRate == Math.abs(chunk.getZ()) % zRate);
    }
}