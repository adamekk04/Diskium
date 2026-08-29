package org.diskium.management;

import io.papermc.paper.plugin.configuration.PluginMeta;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

public class PluginManagement {
    public static boolean tempDisablePlugin(Plugin pl) {
        if (Arrays.asList(getPlugins()).contains(pl)) {
            Bukkit.getPluginManager().disablePlugin(pl);
            return true;
        }
        return false;
    }

    public static boolean permDisablePlugin(Plugin pl) {
        if (Arrays.asList(getPlugins()).contains(pl)) {
            if (pl == null) return false;
            try {
                File oldFile = new File(pl.getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
                File newFile = new File(oldFile.getPath() + "diskiumdisable");
                return oldFile.renameTo(newFile);
            } catch (URISyntaxException e) {
                return false;
            }
        }
        return false;
    }

    public static boolean tempEnablePlugin(Plugin pl) {
        if (Arrays.asList(getPlugins()).contains(pl)) {
            Bukkit.getPluginManager().enablePlugin(pl);
            return true;
        }
        return false;
    }

    public static boolean permEnablePlugin(Plugin pl) {
        if (Arrays.asList(getPlugins()).contains(pl)) {
            try {
                File oldFile = new File(pl.getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
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

    public static boolean deleteFolder(Plugin pl) {
        if (Arrays.asList(getPlugins()).contains(pl)) {
            File file = pl.getDataFolder();
            return file.delete(); // TODO: Provide more info when something fails
        }
        return false;
    }

    public static boolean deletePlugin(Plugin pl) {
        if (Arrays.asList(getPlugins()).contains(pl)) {
            try {
                File file = new File(pl.getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
                return file.delete(); // TODO: Provide more info when something fails
            } catch (URISyntaxException e) {
                return false;
            }
        }
        return false;
    }

    public static boolean hasFolder(String pl, File dir) {
        return new File(dir.getParentFile(), pl).exists();
    }

    public static String info(Plugin pl) {
        if (Arrays.asList(getPlugins()).contains(pl)) {
            PluginMeta meta = pl.getPluginMeta();
            String authors;
            if (meta.getAuthors().size() == 1) authors = "Author: ";
            else authors = "Authors: ";
            for (String author : meta.getAuthors()) {
                authors = authors + author + ", "; // TODO: Use StringBuilder
            }
            authors = authors.substring(0, authors.length() - 2);
            return "Name: " + meta.getName() + "\n" +
                    "Version: " + meta.getVersion() + "\n" +
                    authors +
                    "\nWebsite: " + meta.getWebsite(); // TODO: Add "Is on tasklist: true/false" & "Is enabled: ture/false"
        }
        return null;
    }

    public static Plugin[] getPlugins() {
        return Bukkit.getPluginManager().getPlugins();
    }

    public static String[] getPluginNames(File dir) {
        List<String> plugins = new ArrayList<>();
        File[] files = dir.listFiles((file, name) -> name.endsWith(".jar"));

        if (files == null) return new String[0];

        for (File file : files) {
            try (JarFile jar = new JarFile(file)) {
                ZipEntry entry = jar.getJarEntry("plugin.yml");
                if (entry == null) entry = jar.getJarEntry("paper-plugin.yml");
                if (entry == null) continue; // TODO: Provide more info
                try (InputStream input = jar.getInputStream(entry); InputStreamReader reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
                    YamlConfiguration config = YamlConfiguration.loadConfiguration(reader);
                    plugins.add(config.getString("name"));
                }
            } catch (IOException e) {
                // TODO: Provide more info
            }
        }

        return plugins.toArray(String[]::new);
    }
}
