![RailNet Logo](images/railnet-logo.png)

## Compatibility

Tested on Spigot-1.21.

## Introduction

Generate rails from spawn to mansion and monument, rare loot in chests and furnaces, allow Bazookas.

## Features

* Underground railways from a spawn location to a woodland mansion and an ocean monument with ladders from above at stations
* Uncommon, rare, and epic loot in chests with higher probability in mansions and bastions
* Fuel and ingots in furnaces
* Whirlpools in water with loot chests under magma blocks
* 0.1% of Endermans ignore carved pumpkins on players
* Skeletons have a chance to add both positive and negative effects to their arrows

That plugin combines all the fun I had with my friends that I didn't find in other plugins and didn't manage to split into other my plugins. If you like any changes - please request them specifically, and I'll create a separate configurable plugin.

#### Use cases

Make a bazooka:

![Recipes](https://raw.githubusercontent.com/uprial/railnet/master/images/bazookas.png)

Fire the bazooka:

![120 explosion radius](https://raw.githubusercontent.com/uprial/railnet/master/images/nuke-120r.png)

Find an entrance, take a minecart, enjoy your ride, discover a struct

![Find a monument](https://raw.githubusercontent.com/uprial/railnet/master/images/find-a-monument.png)


## Commands

`railnet reload` - reload config from disk

`repopulate-loaded <radius>` - repopulate loaded terrain around player

`railnet repopulate-loaded <world> <x> <z> <radius>` - repopulate loaded terrain

`railnet claim <density>` - generate player inventory like it's a chest

## Permissions

* Access to 'reload' command:
`railnet.reload` (default: op)
* Access to 'repopulate-loaded' command:
`railnet.repopulate-loaded` (default: op)

## Configuration
[Default configuration file](src/main/resources/config.yml)

## Author
I will be happy to add some features or fix bugs. My mail: uprial@gmail.com.

## Useful links
* [Project on GitHub](https://github.com/uprial/railnet)
* [Project on Bukkit Dev](https://legacy.curseforge.com/minecraft/bukkit-plugins/rails-chests-bazookas)
* [Project on Spigot](https://www.spigotmc.org/resources/rails-chests-bazookas.121505/)

## Related projects
* CustomCreatures: [Bukkit Dev](http://dev.bukkit.org/bukkit-plugins/customcreatures/), [GitHub](https://github.com/uprial/customcreatures), [Spigot](https://www.spigotmc.org/resources/customcreatures.68711/)
* CustomNukes: [Bukkit Dev](http://dev.bukkit.org/bukkit-plugins/customnukes/), [GitHub](https://github.com/uprial/customnukes), [Spigot](https://www.spigotmc.org/resources/customnukes.68710/)
* CustomRecipes: [Bukkit Dev](https://dev.bukkit.org/projects/custom-recipes), [GitHub](https://github.com/uprial/customrecipes/), [Spigot](https://www.spigotmc.org/resources/customrecipes.89435/)
* CustomVillage: [Bukkit Dev](http://dev.bukkit.org/bukkit-plugins/customvillage/), [GitHub](https://github.com/uprial/customvillage/), [Spigot](https://www.spigotmc.org/resources/customvillage.69170/)
* TakeAim: [Bukkit Dev](https://dev.bukkit.org/projects/takeaim), [GitHub](https://github.com/uprial/takeaim), [Spigot](https://www.spigotmc.org/resources/takeaim.68713/)
