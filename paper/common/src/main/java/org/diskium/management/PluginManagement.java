package org.diskium.management;

import io.papermc.paper.plugin.configuration.PluginMeta;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.diskium.MultiplatformLogger;
import org.diskium.objects.BackupObj;
import org.diskium.objects.TaskObj;
import org.diskium.utils.FileUtils;
import org.diskium.utils.TasksUtils;

import java.io.*;
import java.net.URISyntaxException;
import java.util.*;

public class PluginManagement {

/**
     * Temporarily disables plugin through {@code Bukkit#getPluginManager#disablePlugin}
     *
     * @param pl Plugin to disable.
     */
    public static void tempDisablePlugin(Plugin pl) {
        if (Arrays.asList(getPlugins()).contains(pl)) {
            Bukkit.getPluginManager().disablePlugin(pl);
        }
    }

/**
     * Permanently disables plugin by adding 'diskiumdisable' after its name.
     *
     * @param pl Plugin to permanently disable.
     */
    public static void permDisablePlugin(Plugin pl) {
        if (Arrays.asList(getPlugins()).contains(pl)) {
            if (pl == null) return;
            try {
                File oldFile = new File(pl.getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
                File newFile = new File(oldFile.getPath() + "diskiumdisable");
                oldFile.renameTo(newFile);
            } catch (URISyntaxException e) {
                MultiplatformLogger .error("Something went wrong while permanently disabling plugin." + e);
            }
        }
    }

/**
     * Enables plugin by removing subfix 'diskiumdisable' after its name.
     *
     * @param pl Plugin it should enable.
     */
    public static void permEnablePlugin(Plugin pl) {
        if (Arrays.asList(getPlugins()).contains(pl)) {
            try {
                File oldFile = new File(pl.getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
                if (!oldFile.getPath().endsWith("diskiumdisable")) return;
                String oldPath = oldFile.getPath();
                String newPath = oldPath.substring(0, oldPath.length() - 14);
                File newFile = new File(newPath);
                oldFile.renameTo(newFile);
            } catch (URISyntaxException e) {
                MultiplatformLogger.error("Something went wrong while enabling plugin." + e);
            }
        }
    }

/**
     * Deletes the .jar file or/and the folder of given plugin.
     *
     * @param pl The target plugin.
     * @param plFile Whether the .jar file of the plugin should be deleted.
     * @param folder Whether the folder of the plugin should be deleted.
     * @return Status of the method
     */
    public static boolean del(Plugin pl, boolean plFile, boolean folder) {
        boolean buffer = true;

        if (plFile) {
            try {
                File file = new File(pl.getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
                buffer = FileUtils.safeDel(file, FileUtils.DelSpecifier.PLUGINS);
            } catch (URISyntaxException e) {
                MultiplatformLogger.error("Couldn't make URI while deleting plugin.");
                return false;
            }
        }
        if (folder && hasFolder(pl)) {
            buffer = FileUtils.safeDel(pl.getDataFolder(), FileUtils.DelSpecifier.PLUGINS);
        }

        return buffer;
    }

/**
     * Checks, if a given plugin has a folder.
     *
     * @param pl Plugin target.
     * @return Whether the plugin has a folder.
     */
    public static boolean hasFolder(Plugin pl) {
        return pl.getDataFolder().exists();
    }

/**
     * Creates a TextComponent with information about a given plugin.
     *
     * @param pl Target plugin to fetch its information
     * @return A TextComponent with all information about a plugin. Ready to send.
     */
    public static TextComponent info(Plugin pl) {
        if (!Arrays.asList(getPlugins()).contains(pl)) {
            return null;
        }

        PluginMeta meta = pl.getPluginMeta();
        List<String> authors = meta.getAuthors();

        TextComponent textComponent = Component.text("Name: ", NamedTextColor.DARK_GREEN)
                .append(Component.text(pl.getName(), NamedTextColor.WHITE))
                .append(Component.text("\nVersion: ", NamedTextColor.DARK_GREEN))
                .append(Component.text(meta.getVersion(), NamedTextColor.WHITE))
                .append(Component.text(meta.getAuthors().size() == 1 ? "\nAuthor: " : "\nAuthors: ", NamedTextColor.DARK_GREEN))
                .append(Component.text(!authors.isEmpty() ? meta.getAuthors().getFirst() : "None", NamedTextColor.WHITE));

        if (!authors.isEmpty()) {
            authors.removeFirst();
        }

        for (String author : authors) {
            textComponent = textComponent.append(Component.text(", ", NamedTextColor.WHITE))
                    .append(Component.text(author, NamedTextColor.WHITE));
        }

        String website = meta.getWebsite();

        textComponent = textComponent.append(Component.text("\nWebsite: ", NamedTextColor.DARK_GREEN))
                .append(Component.text(website == null ? "None" : website, NamedTextColor.WHITE))
                .append(Component.text("\nIs on tasklist: ", NamedTextColor.DARK_GREEN));

        TaskObj[] tasks = TasksUtils.getTasks();

        if (tasks != null) {
            try {
                File file = new File(pl.getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
                tasks = Arrays.stream(tasks).filter(task -> task.getFile() == file).toArray(TaskObj[]::new);

                textComponent = textComponent.append(Component.text(tasks.length == 1, NamedTextColor.WHITE));
            } catch (URISyntaxException e) {
                MultiplatformLogger.error("Couldn't make URI while getting plugin's file.");
                textComponent = textComponent.append(Component.text(false, NamedTextColor.WHITE));
            }
        } else {
            textComponent = textComponent.append(Component.text(false, NamedTextColor.WHITE));
        }

        textComponent = textComponent.append(Component.text("\nIs on backuplist: ", NamedTextColor.DARK_GREEN));

        BackupObj[] backups = TasksUtils.getBackups();

        if (backups != null) {
            try {
                File file = new File(pl.getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
                backups = Arrays.stream(backups).filter(backup -> backup.getFile() == file).toArray(BackupObj[]::new);

                textComponent = textComponent.append(Component.text(backups.length == 1, NamedTextColor.WHITE));
            } catch (URISyntaxException e) {
                MultiplatformLogger.error("Couldn't make URI while deleting plugin's file.");
                textComponent = textComponent.append(Component.text(false, NamedTextColor.WHITE));
            }
        } else {
            textComponent = textComponent.append(Component.text(false, NamedTextColor.WHITE));
        }

        textComponent = textComponent.append(Component.text("\nIs enabled: ", NamedTextColor.DARK_GREEN))
                .append(Component.text(pl.isEnabled(), NamedTextColor.WHITE));

        return textComponent;
    }

/**
     * Gets all plugins
     *
     * @return Array of all plugins
     */
    public static Plugin[] getPlugins() {
        return Bukkit.getPluginManager().getPlugins();
    }
}
