package org.diskium;

import org.bukkit.plugin.java.JavaPlugin;
import org.diskium.utils.TasksUtils;

import java.util.logging.Level;

public final class Diskium extends JavaPlugin {

    private static Diskium instance;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        instance = this;
        TasksUtils.setServerRoot(getServer().getWorldContainer());

        MultiplatformLogger.setLogger(new MultiplatformLogger.Logger() {
            @Override
            public void info(String message) {
                getLogger().info(message);
            }

            @Override
            public void warn(String message) {
                getLogger().warning(message);
            }

            @Override
            public void error(String message) {
                getLogger().severe(message);
            }

            @Override
            public void error(String message, Throwable throwable) {
                getLogger().log(Level.SEVERE, message, throwable);
            }
        });
    }

    @Override
    public void onDisable() {
        instance = null;
    }

    public static Diskium getInstance() {
        return instance;
    }
}
