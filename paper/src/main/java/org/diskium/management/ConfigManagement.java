package org.diskium.management;

import org.bukkit.configuration.file.FileConfiguration;
import org.diskium.Diskium;

import java.util.HashMap;
import java.util.Map;

public class ConfigManagement {
    public static Map<String, Boolean> getConfig(){
        FileConfiguration config = Diskium.getInstance().getConfig();
        Map<String, Boolean> keys = new HashMap<>();
        for (String key : config.getKeys(false)){
            keys.put(key, config.getBoolean(key));
        }
        return keys;
    }
    public static Boolean getSingleConfig(String name){
        return Diskium.getInstance().getConfig().getBoolean(name);
    }
    public static boolean setSingleConfig(String configName, boolean input){
        if (!Diskium.getInstance().getConfig().contains(configName)) {
            return false;
        }

        Diskium.getInstance().getConfig().set(configName, input);
        Diskium.getInstance().saveConfig();
        Diskium.getInstance().getLogger().info("Set " + configName + " to " + input);
        return true;
    }
}