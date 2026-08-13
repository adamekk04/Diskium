package org.diskium.management;

import io.papermc.paper.plugin.configuration.PluginMeta;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Arrays;

public class PluginManagement {
    public static boolean tempDisablePlugin(String name) {
        if (Arrays.asList(getPlugins()).contains(name)) {
            Bukkit.getPluginManager().disablePlugin(Bukkit.getPluginManager().getPlugin(name));
            return true;
        }
        return false;
    }

    public static boolean permDisablePlugin(String name) {
        if (Arrays.asList(getPlugins()).contains(name)) {
            Plugin plugin = Bukkit.getPluginManager().getPlugin(name);
            if (plugin == null) {
                return false;
            }
            try {
                File oldFile = new File(plugin.getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
                File newFile = new File(oldFile.getPath() + "diskiumdisable");
                return oldFile.renameTo(newFile);
            } catch (URISyntaxException e) {
                return false;
            }
        }
        return false;
    }

    public static boolean tempEnablePlugin(String name) {
        if (Arrays.asList(getPlugins()).contains(name)) {
            Bukkit.getPluginManager().enablePlugin(Bukkit.getPluginManager().getPlugin(name));
            return true;
        }
        return false;
    }

    public static boolean permEnablePlugin(String name) {
        if (Arrays.asList(getPlugins()).contains(name)) {
            Plugin plugin = Bukkit.getPluginManager().getPlugin(name);
            try {
                File oldFile = new File(plugin.getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
                if (!oldFile.getPath().endsWith("diskiumdisable")) return false;
                String oldPath = oldFile.getPath();
                String newPath = oldPath.substring(0, oldPath.length() - 14);
                File newFile = new File(newPath);
                return oldFile.renameTo(newFile);
            } catch (URISyntaxException e) {
                return false;
            }
        }
        return false;
    }

    public static boolean deleteFolder(String name) {
        if (Arrays.asList(getPlugins()).contains(name)) {
            Plugin plugin = Bukkit.getPluginManager().getPlugin(name);
            File file = plugin.getDataFolder();
            return file.delete();
        }
        return false;
    }

    public static boolean deletePlugin(String name) {
        if (Arrays.asList(getPlugins()).contains(name)) {
            Plugin plugin = Bukkit.getPluginManager().getPlugin(name);
            try {
                File file = new File(plugin.getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
                return file.delete();
            } catch (URISyntaxException e) {
                return false;
            }
        }
        return false;
    }

    public static String pluginInfo(String name) {
        if (Arrays.asList(getPlugins()).contains(name)) {
            Plugin plugin = Bukkit.getPluginManager().getPlugin(name);
            PluginMeta meta = plugin.getPluginMeta();
            String authors;
            if (meta.getAuthors().size() == 1) authors = "Author: ";
            else authors = "Authors: ";
            for (String author : meta.getAuthors()) {
                authors = authors + author + ", ";
            }
            authors = authors.substring(0, authors.length() - 2);
            return "Name: " + meta.getName() + "\nVersion: " + meta.getVersion() + "\n" + authors + "\nWebsite: " + meta.getWebsite(); // TODO: Add "Is on tasklist: true/false" & "Is enabled: ture/false"
        }
        return null;
    }

    public static String[] getPlugins() {
        Plugin[] plugins = Bukkit.getPluginManager().getPlugins();
        String[] pluginsName = new String[plugins.length];
        for (int i = 0; i < plugins.length; i++) {
            pluginsName[i] = plugins[i].getName();
        }
        return pluginsName;
    }
}
