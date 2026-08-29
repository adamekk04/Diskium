package org.diskium;

import org.bukkit.plugin.java.JavaPlugin;

public final class Diskium extends JavaPlugin {

    private static Diskium instance;

    @Override
    public void onEnable() {
        getDataFolder().mkdirs(); // TODO: Use return of this line
        saveDefaultConfig();
        instance = this;
    }

    @Override
    public void onDisable() {
        instance = null;
    }

    public static Diskium getInstance() {
        return instance;
    }
}
