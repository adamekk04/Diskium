package org.diskium.management;

import io.papermc.paper.plugin.configuration.PluginMeta;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.diskium.Diskium;
import org.diskium.MultiplatformLogger;
import org.diskium.objects.BackupObj;
import org.diskium.objects.TaskObj;
import org.diskium.utils.FileUtils;
import org.diskium.utils.TasksUtils;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;

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

    public static void del(Plugin pl, boolean plFile, boolean folder) {
        if (plFile) {
            FileUtils.del(pl.getDataFolder());
        }
        if (folder && hasFolder(pl)) {
            try {
                File file = new File(pl.getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
                FileUtils.del(file);
            } catch (URISyntaxException e) {
                MultiplatformLogger.error("Couldn't make URI while deleting plugin.");
            }
        }
    }

    public static boolean hasFolder(Plugin pl) {
        return pl.getDataFolder().exists();
    }

    public static TextComponent info(Plugin pl) {
        if (!Arrays.asList(getPlugins()).contains(pl)) {
            return null;
        }

        PluginMeta meta = pl.getPluginMeta();

        TextComponent textComponent = Component.text("Name: ", NamedTextColor.DARK_GREEN)
                .append(Component.text(pl.getName(), NamedTextColor.WHITE))
                .append(Component.text("\nVersion: ", NamedTextColor.DARK_GREEN))
                .append(Component.text(meta.getVersion(), NamedTextColor.WHITE))
                .append(Component.text(meta.getAuthors().size() == 1 ? "\nAuthor: " : "\nAuthors: ", NamedTextColor.DARK_GREEN))
                .append(Component.text(meta.getAuthors().getFirst(), NamedTextColor.WHITE));

        List<String> authors = meta.getAuthors();
        authors.removeFirst();

        for (String author : authors) {
            textComponent.append(Component.text(", ", NamedTextColor.WHITE))
                    .append(Component.text(author, NamedTextColor.WHITE));
        }

        String website = meta.getWebsite();

        textComponent.append(Component.text("\nWebsite: ", NamedTextColor.DARK_GREEN))
                .append(Component.text(website == null ? "None" : website, NamedTextColor.WHITE))
                .append(Component.text("\nIs on tasklist: ", NamedTextColor.DARK_GREEN));

        TaskObj[] tasks = TasksUtils.getTasks(Diskium.getInstance().getDataFolder());

        if (tasks != null) {
            try {
                File file = new File(pl.getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
                tasks = Arrays.stream(tasks).filter(task -> task.getFile() == file).toArray(TaskObj[]::new);

                textComponent.append(Component.text(tasks.length == 1, NamedTextColor.WHITE));
            } catch (URISyntaxException e) {
                MultiplatformLogger.error("Couldn't make URI while getting plugin's file.");
                textComponent.append(Component.text(false, NamedTextColor.WHITE));
            }
        } else {
            textComponent.append(Component.text(false, NamedTextColor.WHITE));
        }

        textComponent.append(Component.text("\nIs on backuplist: ", NamedTextColor.DARK_GREEN));

        BackupObj[] backups = TasksUtils.getBackups(Diskium.getInstance().getDataFolder());

        if (backups != null) {
            try {
                File file = new File(pl.getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
                backups = Arrays.stream(backups).filter(backup -> backup.getFile() == file).toArray(BackupObj[]::new);

                textComponent.append(Component.text(backups.length == 1, NamedTextColor.WHITE));
            } catch (URISyntaxException e) {
                MultiplatformLogger.error("Couldn't make URI while deleting plugin's file.");
                textComponent.append(Component.text(false, NamedTextColor.WHITE));
            }
        } else {
            textComponent.append(Component.text(false, NamedTextColor.WHITE));
        }

        textComponent.append(Component.text("\nIs enabled: ", NamedTextColor.DARK_GREEN));

        return textComponent;
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
                if (entry == null) MultiplatformLogger.error("Couldn't find plugin.yml/paper-plugin.yml in " + file.getName());
                try (InputStream input = jar.getInputStream(entry); InputStreamReader reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
                    YamlConfiguration config = YamlConfiguration.loadConfiguration(reader);
                    plugins.add(config.getString("name"));
                }
            } catch (FileNotFoundException e) {
                MultiplatformLogger.error("Plugin file " + file.getName() + " not found.");
            } catch (ZipException e) {
                MultiplatformLogger.error("Plugin file " + file.getName() + " is maybe damaged, couldn't unzip it.");
            } catch (IOException e) {
                MultiplatformLogger.error("Something went wrong while getting name of plugin " + file.getName(), e);
            }
        }

        return plugins.toArray(String[]::new);
    }
}
