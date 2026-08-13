package org.diskium.management;

import org.bukkit.Bukkit;
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

    public static String getBlock(String argWorld, String stringX, String stringY, String stringZ, boolean existing) {
        int x;
        int y;
        int z;
        String toReturn = null;
        try {
            x = Integer.parseInt(stringX);
            y = Integer.parseInt(stringY);
            z = Integer.parseInt(stringZ);
        } catch (NumberFormatException e) {
            return "You need to enter numbers as coordinates";
        }
        if (existing) {
            if (argWorld.equalsIgnoreCase("allworlds")) {
                for (World world : Bukkit.getWorlds()) {
                    Block block = world.getBlockAt(x, y, z);
                    toReturn = toReturn + world.getName() + ": " + block.getType().toString() + "\n";
                }
                return toReturn;
            }
            return argWorld + ": " + Bukkit.getWorld(argWorld).getBlockAt(x, y, z);
        } else {
            if (argWorld.equalsIgnoreCase("allwordls")) {
                for (World world : Bukkit.getWorlds()) {
                    World newWorld = genWorld(world);
                    toReturn = toReturn + world.getName() + ": " + newWorld.getBlockAt(x, y, z).getType().toString() + "\n";
                    delWorld(newWorld);
                }
                return toReturn;
            }
            World originalWorld = Bukkit.getWorld(argWorld);
            World newWorld = genWorld(originalWorld);
            toReturn = argWorld + ": " + newWorld.getBlockAt(x, y, z).getType().toString();
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
