package com.gmail.uprial.railnet.populator.mineshaft;

import com.gmail.uprial.railnet.common.CustomLogger;
import com.gmail.uprial.railnet.common.Probability;
import com.gmail.uprial.railnet.common.WorldName;
import com.gmail.uprial.railnet.populator.CLT;
import com.gmail.uprial.railnet.populator.ChunkPopulator;
import com.gmail.uprial.railnet.populator.ItemConfig;
import com.google.common.collect.ImmutableMap;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Furnace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.minecart.StorageMinecart;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

import static com.gmail.uprial.railnet.common.Formatter.format;

public class MineshaftPopulator implements ChunkPopulator {
    private final CustomLogger customLogger;

    private final static Random RANDOM = new Random();

    public MineshaftPopulator(final CustomLogger customLogger) {
        this.customLogger = customLogger;
    }

    @Override
    public void populate(final Chunk chunk) {
        final int minY = chunk.getWorld().getMinHeight();
        final int maxY = chunk.getWorld().getMaxHeight();
        for(int y = minY; y < maxY; y++) {
            for(int x = 0; x < 16; x++) {
                for(int z = 0; z < 16; z++) {
                    populateBlock(chunk.getBlock(x, y, z));
                }
            }
        }

        for(final Entity entity : chunk.getEntities()) {
            if(entity instanceof StorageMinecart) {
                populateStorageMinecart((StorageMinecart)entity);
            }
        }
    }

    private interface BlockPopulator {
        void populate(final Block block);
    }

    private final Map<Material, BlockPopulator> blockPopulators = ImmutableMap.<Material, BlockPopulator>builder()
            .put(Material.CHEST, this::populateChest)
            .put(Material.TRAPPED_CHEST, this::populateChest)
            .put(Material.FURNACE, this::populateFurnace)
            .put(Material.BLAST_FURNACE, this::populateFurnace)
            .build();

    private void populateBlock(final Block block) {
        final BlockPopulator blockPopulator = blockPopulators.get(block.getType());
        if(blockPopulator != null) {
            blockPopulator.populate(block);
        }
    }

    private final Map<String,Integer> worldDensity = ImmutableMap.<String,Integer>builder()
            .put(WorldName.NETHER, 1)
            .put(WorldName.END, 2)
            .build();

    private int getWorldDensity(final World world) {
        return worldDensity.getOrDefault(world.getName().toLowerCase(Locale.ROOT), 0);
    }

    /*
        According to https://minecraft.wiki/w/Food,
        food with good saturation, which can't be found in chests.
     */
    //private final Material chestIdempotencyMarker = Material.COOKED_MUTTON;

    private final ItemConfig netheriteClothConfig =  new ItemConfig()
            // Survival maximum level is 4, here it's 5
            .ench(Enchantment.PROTECTION, 0, 5)
            .ench(Enchantment.VANISHING_CURSE)
            .trim(TrimMaterial.NETHERITE, TrimPattern.RIB);

    private final ItemConfig goldenClothConfig =  new ItemConfig()
            // Survival maximum level is 4, here it's 5
            .ench(Enchantment.PROTECTION, 3, 5)
            .ench(Enchantment.THORNS, 0, 3)
            .trim(TrimMaterial.GOLD, TrimPattern.RIB);

    private final ItemConfig netheriteToolConfig =  new ItemConfig()
            // Survival maximum level is 5, here it's 10
            .ench(Enchantment.EFFICIENCY, 0, 10)
            .ench(Enchantment.VANISHING_CURSE);

    private final ItemConfig goldenToolConfig =  new ItemConfig()
            // Survival maximum level is 5, here it's 10
            .ench(Enchantment.EFFICIENCY, 5, 10)
            .ench(Enchantment.FORTUNE, 0, 3);

    private final ItemConfig netheriteSwordConfig =  new ItemConfig()
            // Survival maximum level is 5, here it's 10
            .ench(Enchantment.SHARPNESS, 0, 10)
            .ench(Enchantment.VANISHING_CURSE);

    private final ItemConfig goldenSwordConfig =  new ItemConfig()
            // Survival maximum level is 5, here it's 10
            .ench(Enchantment.SHARPNESS, 5, 10)
            .ench(Enchantment.LOOTING, 0, 3)
            .ench(Enchantment.KNOCKBACK, 0, 2)
            .ench(Enchantment.FIRE_ASPECT, 0, 2)
            .ench(Enchantment.SWEEPING_EDGE, 0, 3);

    /*
        Ideated from:
            https://minecraft.wiki/w/Rarity
            https://minecraft.wiki/w/Spawn_Egg

        Removed ideas:
            Ender pearls motivate player to fight endermans.
            .put(Material.ENDER_PEARL,

            End crystals require a block of obsidian beneath,
            which removes all potential fun of placing them.
            .put(Material.END_CRYSTAL, new CLT(7.5D, 1))

            Bedrock has no real usage, but may bring potential damage to the world.
            .put(Material.BEDROCK,

            Golden carrots and apples are funny for 1st time,
            but they are neither food with good saturation for everyday life
            nor provide enough regeneration in a fight.
            .put(Material.GOLDEN_APPLE, new CLT(7.5D, 1))
            .put(Material.GOLDEN_CARROT, new CLT(7.5D, 1))

     */
    private final Map<Material, CLT> chestLootTable = ImmutableMap.<Material, CLT>builder()
            //.put(chestIdempotencyMarker, new CLT(MAX_PERCENT))

            /*
                Obtaining these resources isn't worth its time,
                but as a gift it's a lot of fun.
             */
            .put(Material.TNT, new CLT(10.0D, 2))
            .put(Material.OBSIDIAN, new CLT(10.0D, 2))

            .put(Material.DIAMOND, new CLT(7.5D, 1))

            // https://hub.spigotmc.org/javadocs/spigot/org/bukkit/potion/PotionEffectType.html
            .put(Material.POTION, new CLT(100.0D, new ItemConfig().effects(
                    ImmutableMap.<PotionEffectType, Integer>builder()
                            .put(PotionEffectType.BLINDNESS, 0) // negative
                            .put(PotionEffectType.DARKNESS, 0) // negative
                            .put(PotionEffectType.FIRE_RESISTANCE, 0)
                            .put(PotionEffectType.GLOWING, 0)
                            .put(PotionEffectType.HASTE, 4)
                            .put(PotionEffectType.HEALTH_BOOST, 4)
                            .put(PotionEffectType.HUNGER, 4) // negative
                            .put(PotionEffectType.INVISIBILITY, 0)
                            .put(PotionEffectType.JUMP_BOOST, 2)
                            .put(PotionEffectType.LEVITATION, 2) // negative
                            .put(PotionEffectType.NAUSEA, 0) // negative
                            .put(PotionEffectType.NIGHT_VISION, 0)
                            .put(PotionEffectType.POISON, 2) // negative
                            .put(PotionEffectType.REGENERATION, 0)
                            .put(PotionEffectType.RESISTANCE, 2)
                            .put(PotionEffectType.SATURATION, 0)
                            .put(PotionEffectType.SLOW_FALLING, 4)
                            .put(PotionEffectType.SPEED, 4)
                            .put(PotionEffectType.STRENGTH, 2)
                            .put(PotionEffectType.WATER_BREATHING, 0)
                            .build())
            ))

            .put(Material.GOLDEN_HELMET, new CLT(5.0D, goldenClothConfig
                    .ench(Enchantment.RESPIRATION, 0, 3)
                    .ench(Enchantment.AQUA_AFFINITY, 0, 1)))
            .put(Material.GOLDEN_CHESTPLATE, new CLT(5.0D, goldenClothConfig))
            .put(Material.GOLDEN_LEGGINGS, new CLT(5.0D, goldenClothConfig
                    .ench(Enchantment.SWIFT_SNEAK, 0, 3)))
            .put(Material.GOLDEN_BOOTS, new CLT(5.0D, goldenClothConfig
                    .ench(Enchantment.FEATHER_FALLING, 0, 4)
                    .ench(Enchantment.DEPTH_STRIDER, 0, 3)))

            .put(Material.GOLDEN_PICKAXE, new CLT(4.0D, goldenToolConfig))
            .put(Material.GOLDEN_SWORD, new CLT(4.0D, goldenSwordConfig))

            .put(Material.ENCHANTED_GOLDEN_APPLE, new CLT(3.0D))
            .put(Material.TOTEM_OF_UNDYING, new CLT(3.0D))
            .put(Material.SPAWNER, new CLT(3.0D))

            .put(Material.NETHERITE_HELMET, new CLT(2.0D, netheriteClothConfig
                    .ench(Enchantment.RESPIRATION, 0, 3)
                    .ench(Enchantment.AQUA_AFFINITY, 0, 1)))
            .put(Material.NETHERITE_CHESTPLATE, new CLT(2.0D, netheriteClothConfig))
            .put(Material.NETHERITE_LEGGINGS, new CLT(2.0D, netheriteClothConfig
                    .ench(Enchantment.SWIFT_SNEAK, 0, 3)))
            .put(Material.NETHERITE_BOOTS, new CLT(2.0D, netheriteClothConfig
                    .ench(Enchantment.FEATHER_FALLING, 0, 4)
                    .ench(Enchantment.DEPTH_STRIDER, 0, 3)))

            .put(Material.NETHERITE_PICKAXE, new CLT(1.5D, netheriteToolConfig))
            .put(Material.NETHERITE_SWORD, new CLT(1.5D, netheriteSwordConfig))

            .put(Material.CREEPER_SPAWN_EGG, new CLT(1.0D))
            .put(Material.ZOMBIE_SPAWN_EGG, new CLT(1.0D))
            .put(Material.SKELETON_SPAWN_EGG, new CLT(1.0D))
            .put(Material.SPIDER_SPAWN_EGG, new CLT(1.0D))

            .put(Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE, new CLT(1.0D))
            .put(Material.OMINOUS_BOTTLE, new CLT(1.0D, new ItemConfig().amplify(4)))

            .put(Material.SLIME_SPAWN_EGG, new CLT(0.5D))
            .put(Material.MOOSHROOM_SPAWN_EGG, new CLT(0.5D))
            .put(Material.BLAZE_SPAWN_EGG, new CLT(0.5D))

            .put(Material.SHULKER_SPAWN_EGG, new CLT(0.25D))
            .put(Material.WITHER_SKELETON_SPAWN_EGG, new CLT(0.25D))
            .put(Material.GHAST_SPAWN_EGG, new CLT(0.25D))
            .put(Material.EVOKER_SPAWN_EGG, new CLT(0.25D))

            // Just for fun
            .put(Material.SKELETON_SKULL, new CLT(0.25D))
            .put(Material.CREEPER_HEAD, new CLT(0.25D))
            .put(Material.PIGLIN_HEAD, new CLT(0.25D))
            .put(Material.PLAYER_HEAD, new CLT(0.25D))
            .put(Material.ZOMBIE_HEAD, new CLT(0.25D))

            .put(Material.SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE, new CLT(0.2D))
            .put(Material.VEX_ARMOR_TRIM_SMITHING_TEMPLATE, new CLT(0.2D))
            .put(Material.WILD_ARMOR_TRIM_SMITHING_TEMPLATE, new CLT(0.2D))
            .put(Material.COAST_ARMOR_TRIM_SMITHING_TEMPLATE, new CLT(0.2D))
            .put(Material.DUNE_ARMOR_TRIM_SMITHING_TEMPLATE, new CLT(0.2D))
            .put(Material.WAYFINDER_ARMOR_TRIM_SMITHING_TEMPLATE, new CLT(0.2D))
            .put(Material.RAISER_ARMOR_TRIM_SMITHING_TEMPLATE, new CLT(0.2D))
            .put(Material.SHAPER_ARMOR_TRIM_SMITHING_TEMPLATE, new CLT(0.2D))
            .put(Material.HOST_ARMOR_TRIM_SMITHING_TEMPLATE, new CLT(0.2D))
            .put(Material.WARD_ARMOR_TRIM_SMITHING_TEMPLATE, new CLT(0.2D))
            .put(Material.SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE, new CLT(0.2D))
            .put(Material.TIDE_ARMOR_TRIM_SMITHING_TEMPLATE, new CLT(0.2D))
            .put(Material.SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE, new CLT(0.2D))
            .put(Material.RIB_ARMOR_TRIM_SMITHING_TEMPLATE, new CLT(0.2D))
            .put(Material.EYE_ARMOR_TRIM_SMITHING_TEMPLATE, new CLT(0.2D))
            .put(Material.SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE, new CLT(0.2D))
            .put(Material.FLOW_ARMOR_TRIM_SMITHING_TEMPLATE, new CLT(0.2D))
            .put(Material.BOLT_ARMOR_TRIM_SMITHING_TEMPLATE, new CLT(0.2D))

            // Something insane
            .put(Material.ENDER_DRAGON_SPAWN_EGG, new CLT(0.1D))
            .put(Material.WITHER_SPAWN_EGG, new CLT(0.1D))
            .put(Material.WARDEN_SPAWN_EGG, new CLT(0.1D))

            .build();

    private void stackInventoryOnce(final String title, final Inventory inventory) {
        final Map<Material,Integer> map = new HashMap<>();

        /*
            getContents() returns a list of nulls
            even when the content isn't actually null,
            so I iterate the content by id.
         */
        for(int i = 0; i < inventory.getSize(); i++) {
            ItemStack itemStack = inventory.getItem(i);
            /*
                The current use case of calling stackInventoryOnce()
                when inventory.firstEmpty() didn't give a result
                makes itemStack not null,
                but the check is for further safety.
             */
            if(itemStack != null) {
                if(map.containsKey(itemStack.getType())) {
                    final int existingI = map.get(itemStack.getType());
                    final ItemStack existingItemStack = inventory.getItem(existingI);
                    /*
                        Even assuming itemStack might be null in the future,
                        existingItemStack can't be null
                        because we put existingI in the map after checking for null.
                     */
                    if(existingItemStack.getAmount() < existingItemStack.getMaxStackSize()) {
                        final int diff = Math.min(
                                existingItemStack.getMaxStackSize() - existingItemStack.getAmount(),
                                itemStack.getAmount()
                        );

                        if (customLogger.isDebugMode()) {
                            customLogger.debug(String.format("%s merged: %d %s from item #%d moved to item #%d",
                                    title, diff, itemStack.getType(), i, existingI));
                        }

                        existingItemStack.setAmount(existingItemStack.getAmount() + diff);
                        if(itemStack.getAmount() > diff) {
                            itemStack.setAmount(itemStack.getAmount() - diff);
                        } else {
                            inventory.setItem(i, null);
                            return;
                        }
                    }
                }

                map.put(itemStack.getType(), i);
            }
        }

        if (customLogger.isDebugMode()) {
            customLogger.debug(String.format("%s doesn't have space", title));
        }
    }

    private final static double MULTIPLY_PROBABILITY = 10.0D;
    private void populateInventory(final String title, final Inventory inventory, final int density) {
        /*
            getContents() returns a list of nulls
            even when the content isn't actually null,
            so I iterate the content by id.
         */

        /*
        for(int i = 0; i < inventory.getSize(); i++) {
            final ItemStack itemStack = inventory.getItem(i);
            if((itemStack != null) && (itemStack.getType().equals(chestIdempotencyMarker))) {
                if(customLogger.isDebugMode()) {
                    customLogger.debug(String.format("%s item #%d already has an idempotency marker", title, i));
                }
                return;
            }
        }
        */

        for(int i = 0; i < inventory.getSize(); i++) {
            final ItemStack itemStack = inventory.getItem(i);
            if((itemStack != null) && (itemStack.getMaxStackSize() > 1) && (Probability.PASS(MULTIPLY_PROBABILITY, density))) {
                setAmount(String.format("%s item #%d", title, i),
                        itemStack.getAmount(), itemStack, 1, CLT.MAX_POWER);
            }
        }

        for(Map.Entry<Material, CLT> entry : chestLootTable.entrySet()) {
            if(Probability.PASS(entry.getValue().getProbability(), density)) {
                int i = inventory.firstEmpty();
                if(i == -1) {
                    // There are no empty slots.
                    stackInventoryOnce(title, inventory);
                    i = inventory.firstEmpty();
                    if(i == -1) {
                        /*
                            There are no empty slots even after the stack.
                            Potentially, other random items may be added, but it isn't interesting.
                         */
                        break;
                    }
                }
                inventory.setItem(i, new ItemStack(entry.getKey(), 1));

                if(entry.getValue().getItemConfig() != null) {
                    // The fresh getItem() is needed to properly update the amount
                    entry.getValue().getItemConfig().apply(inventory.getItem(i));
                }

                setAmount(String.format("%s item #%d", title, i),
                        // The fresh getItem() is needed to properly update the amount
                        0, inventory.getItem(i), 0, entry.getValue().getMaxPower());
            }
        }
    }

    private void populateChest(final Block block) {
        populateChest(block, getWorldDensity(block.getWorld()));
    }

    public void populateChest(final Block block, final int density) {
        populateInventory(format(block), ((Chest)block.getState()).getBlockInventory(), density);

        if(customLogger.isDebugMode()) {
            customLogger.debug(String.format("%s populated with density %d", format(block), density));
        }
    }

    private void populateStorageMinecart(final StorageMinecart storageMinecart) {
        final int density = getWorldDensity(storageMinecart.getWorld());

        populateInventory(format(storageMinecart), storageMinecart.getInventory(), density);

        if(customLogger.isDebugMode()) {
            customLogger.debug(String.format("%s populated with density %d", format(storageMinecart), density));
        }
    }

    // Ideated from https://minecraft.wiki/w/Smelting
    // Material -> max power of drop
    private final Map<Material,Integer> furnaceResultTable = ImmutableMap.<Material,Integer>builder()
            .put(Material.GOLD_NUGGET, CLT.MAX_POWER)
            .put(Material.IRON_NUGGET, CLT.MAX_POWER)

            .put(Material.IRON_INGOT, CLT.MAX_POWER - 1)
            .put(Material.GOLD_INGOT, CLT.MAX_POWER - 1)

            .put(Material.REDSTONE, CLT.MAX_POWER - 2)
            .put(Material.LAPIS_LAZULI, CLT.MAX_POWER - 2)

            .put(Material.NETHERITE_SCRAP, 0)
            .put(Material.SPONGE, 0)
            .build();

    private final Map<Material,Integer> furnaceFuelTable = ImmutableMap.<Material,Integer>builder()
            .put(Material.COAL, CLT.MAX_POWER)
            .put(Material.COAL_BLOCK, CLT.MAX_POWER - 2)
            .put(Material.LAVA_BUCKET, 0)
            .build();


    private interface ItemStackGetter {
        ItemStack get();
    }

    private interface ItemStackSetter {
        void set(final ItemStack itemStack);
    }

    private void updateItemStack(final String title,
                                 final ItemStackGetter itemStackGetter,
                                 final ItemStackSetter itemStackSetter,
                                 final Map<Material,Integer> lootTable) {
        ItemStack itemStack = itemStackGetter.get();
        if(itemStack != null) {
            if(customLogger.isDebugMode()) {
                customLogger.debug(String.format("%s already has an idempotency marker", title));
            }
            return;
        }

        final Material material = getRandomSetItem(lootTable.keySet());
        itemStackSetter.set(new ItemStack(material, 1));
        // The sequence is needed to properly update the amount
        itemStack = itemStackGetter.get();
        setAmount(title, 0, itemStack, 0, lootTable.get(material));
    }

    private void populateFurnace(final Block block) {
        final Furnace furnace = (Furnace)block.getState();

        final FurnaceInventory inventory = furnace.getInventory();

        updateItemStack(String.format("%s fuel", format(block)),
                inventory::getFuel,
                inventory::setFuel,
                furnaceFuelTable);

        updateItemStack(String.format("%s result", format(block)),
                inventory::getResult,
                inventory::setResult,
                furnaceResultTable);
    }

    private void setAmount(final String title, final int oldAmount, final ItemStack itemStack,
                           final int minPower, final int maxPower) {
        final int newAmount =
                Math.min(
                        itemStack.getMaxStackSize(),
                        itemStack.getAmount() * CLT.getRandomAmount(minPower, maxPower)
                );

        itemStack.setAmount(newAmount);

        if (customLogger.isDebugMode()) {
            if(oldAmount == 0) {
                customLogger.debug(String.format("%s %s set to %d",
                        title, itemStack.getType(), newAmount));
            } else if(newAmount > oldAmount) {
                customLogger.debug(String.format("%s %s updated from %d to %d",
                        title, itemStack.getType(), oldAmount, newAmount));
            } else {
                customLogger.warning(String.format("%s %s kept as %d",
                        title, itemStack.getType(), newAmount));
            }
        }
    }

    private <T> T getRandomSetItem(final Set<T> set) {
        return  (new ArrayList<>(set)).get(RANDOM.nextInt(set.size()));
    }
}
