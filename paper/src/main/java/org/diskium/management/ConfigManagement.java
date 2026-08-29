package org.diskium.management;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.diskium.Diskium;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ConfigManagement {
    public static Map<String, Object> getConfig(File dir) {
        FileConfiguration config = YamlConfiguration.loadConfiguration(new File(dir, "config.yml"));
        Map<String, Object> keys = new HashMap<>();
        for (String key : config.getKeys(false)) {
            keys.put(key, config.get(key));
        }
        return keys;
    }

    public static Object getSingleConfig(String name) {
        return Diskium.getInstance().getConfig().getBoolean(name);
    }

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