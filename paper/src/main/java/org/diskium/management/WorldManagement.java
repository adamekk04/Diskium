package org.diskium.management;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.block.Block;

import java.util.Random;

public class WorldManagement {
    public static boolean exists(String name) {
        for (World world : Bukkit.getWorlds()) {
            if (world.getName().equalsIgnoreCase(name)) return true;
        }
        return false;
    }

    public static String getBlock(Location loc, boolean existing) {
        if (existing) {
            return loc.getWorld().getName() + ": " + loc.getBlock().getType().toString();
        } else {
            World newWorld = genWorld(loc.getWorld());
            String toReturn = loc.getWorld().getName() + ": " + newWorld.getBlockAt(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()).getType().toString();
            delWorld(newWorld);
            return toReturn;
        }
    }

    public static World genWorld(World template) {
        WorldCreator creator = new WorldCreator(template.getName() + getSalt());
        creator.copy(template);
        return creator.createWorld();
    }

    public static boolean delWorld(World world) {
        Bukkit.unloadWorld(world, false); // TODO: Check for side effects that unloading world midtick can have: https://jd.papermc.io/paper/26.2/org/bukkit/Bukkit.html#unloadWorld(org.bukkit.World,boolean)
        return world.getWorldFolder().delete();
    }

    public static String info(World world) {
        return "Name: " + world.getName() + "\nPlayers: " + world.getPlayers() + " (" + world.getPlayerCount() + ")\nSeed: " + world.getSeed() + "\nWorld border radius: " + world.getWorldBorder().getSize();
    }

    private static String getSalt() {
        String chars = "abcdefghijklmnopqrstuvwxyz1234567890";
        String toReturn = "";
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            toReturn = toReturn + chars.charAt(random.nextInt(chars.length()));
        }
        return toReturn;
    }
}
