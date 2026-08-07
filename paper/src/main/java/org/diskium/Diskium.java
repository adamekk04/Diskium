package org.diskium;

import org.bukkit.plugin.java.JavaPlugin;

public final class Diskium extends JavaPlugin {

    private static Diskium instance;

    @Override
    public void onEnable() {
        getDataFolder().mkdirs();
        saveDefaultConfig();
        getCommand("diskium").setExecutor(new DiskiumCommand());
    }

    @Override
    public void onDisable() {
        // nothing :(
    }

    public static Diskium getInstance(){
        return instance;
    }
}
