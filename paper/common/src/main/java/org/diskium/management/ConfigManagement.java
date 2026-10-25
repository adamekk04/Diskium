package org.diskium.management;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.diskium.Diskium;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ConfigManagement {

/**
     * Loads leaf configuration entries from {@code config.yml}.
     *
     * @param dir the directory containing the configuration file
     * @return values keyed by their full configuration paths, excluding parent sections
     */
    public static Map<String, Object> getConfig(File dir) {
        FileConfiguration config = YamlConfiguration.loadConfiguration(new File(dir, "config.yml"));
        Map<String, Object> keys = new HashMap<>();

        for (String key : config.getKeys(true)) {
            keys.put(key, config.get(key));
        }

        keys.keySet().removeIf(key ->
                keys.keySet().stream().anyMatch(other -> other.startsWith(key + "."))
        );

        return keys;
    }

/**
     * Gets a single configuration.
     *
     * @param name Name of the configuration.
     * @return Object obtained from the config.
     */
    public static Object getSingleConfig(String name) {
        return Diskium.getInstance().getConfig().get(name);
    }

/**
     * Sets a new value to a configuration.
     *
     * @param configName Name of the configuration.
     * @param input New value of the configuration.
     * @return Status of the method.
     */
    public static boolean setSingleConfig(String configName, Object input) {
        if (!Diskium.getInstance().getConfig().contains(configName)) {
            return false;
        }

        Diskium.getInstance().getConfig().set(configName, input);
        Diskium.getInstance().saveConfig();
        Diskium.getInstance().getLogger().info("Set " + configName + " to " + input);
        return true;
    }
}
