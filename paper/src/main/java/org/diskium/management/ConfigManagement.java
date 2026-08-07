package org.diskium.management;

import org.bukkit.configuration.file.FileConfiguration;
import org.diskium.Diskium;

import java.util.HashMap;
import java.util.Map;

public class ConfigManagement {
    public static Map<String, Object> getConfig(){
        FileConfiguration config = Diskium.getInstance().getConfig();
        Map<String, Object> keys = new HashMap<>();
        for (String key : config.getKeys(false)){
            keys.put(key, config.get(key));
        }
        return keys;
    }
    public static Object getSingleConfig(String name){
        return Diskium.getInstance().getConfig().get(name);
    }
    public static void setSingleConfig(String configName, String[] input){
        if (!Diskium.getInstance().getConfig().contains(configName)) {
            Diskium.getInstance().getLogger().severe("Config " + configName + " does not exist");
            return;
        }
        String replacement = String.join(" ", input);
        Diskium.getInstance().getConfig().set(configName, replacement);
        Diskium.getInstance().saveConfig();
        Diskium.getInstance().getLogger().info("Set " + configName + " to " + replacement);
    }
}