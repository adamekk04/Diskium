package org.diskium.management;

import io.papermc.paper.plugin.configuration.PluginMeta;
import java.io.*;
import java.net.URISyntaxException;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.diskium.MultiplatformLogger;
import org.diskium.objects.BackupObj;
import org.diskium.objects.TaskObj;
import org.diskium.utils.FileUtils;
import org.diskium.utils.TasksUtils;

public class PluginManagement {

    /**
      * Temporarily disables plugin through {@code Bukkit#getPluginManager#disablePlugin}
      *
      * @param pl Plugin to disable.
      */
    public static void tempDisablePlugin(Plugin pl) {
        Bukkit.getPluginManager().disablePlugin(pl);
    }

    /**
      * Permanently disables plugin by adding 'diskiumdisable' after its name.
      *
      * @param pl Plugin to permanently disable.
      */
    public static void permDisablePlugin(Plugin pl) {
        if (pl == null) {
            return;
        }

        try {
            File oldFile =
                    new File(pl.getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
            File newFile = new File(oldFile.getPath() + "diskiumdisable");
            oldFile.renameTo(newFile);

        } catch (URISyntaxException e) {
            MultiplatformLogger.error("Something went wrong while permanently disabling plugin." + e);
        }
    }

    /**
      * Enables plugin by removing subfix 'diskiumdisable' after its name or simply enabling it through BukkitAPI.
      *
      * @param s Name of the plugin it should enable.
      */
    public static void enablePlugin(String s) {
        Plugin plugin = Bukkit.getPluginManager().getPlugin(s);

        if (plugin != null) {
            Bukkit.getPluginManager().enablePlugin(plugin);
            return;
        }

        try {
            File oldFile = getPlugin(s);

            if (oldFile == null || !oldFile.toString().endsWith("diskiumdisable")) {
                return;
            }

            String oldPath = oldFile.getPath();
            String newPath = oldPath.substring(0, oldPath.length() - 14);
            File newFile = new File(newPath);

            if (!oldFile.renameTo(newFile)) {
                return;
            }

            Plugin pl = Bukkit.getPluginManager().loadPlugin(newFile);
            if (pl != null) {
                Bukkit.getPluginManager().enablePlugin(pl);
            }
        } catch (Exception e) {
            MultiplatformLogger.error("Something went wrong while enabling plugin." + e);
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
                File file =
                        new File(pl.getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
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
        if (!Arrays.asList(Bukkit.getPluginManager().getPlugins()).contains(pl)) {
            return null;
        }

        PluginMeta meta = pl.getPluginMeta();
        List<String> authors = meta.getAuthors();

        TextComponent textComponent =
                Component.text("Name: ", NamedTextColor.DARK_GREEN)
                        .append(Component.text(pl.getName(), NamedTextColor.WHITE))
                        .append(Component.text("\nVersion: ", NamedTextColor.DARK_GREEN))
                        .append(Component.text(meta.getVersion(), NamedTextColor.WHITE))
                        .append(
                                Component.text(
                                        meta.getAuthors().size() == 1 ? "\nAuthor: " : "\nAuthors: ",
                                        NamedTextColor.DARK_GREEN))
                        .append(
                                Component.text(
                                        !authors.isEmpty() ? meta.getAuthors().getFirst() : "None",
                                        NamedTextColor.WHITE));

        if (!authors.isEmpty()) {
            authors.removeFirst();
        }

        for (String author : authors) {
            textComponent =
                    textComponent
                            .append(Component.text(", ", NamedTextColor.WHITE))
                            .append(Component.text(author, NamedTextColor.WHITE));
        }

        String website = meta.getWebsite();

        textComponent =
                textComponent
                        .append(Component.text("\nWebsite: ", NamedTextColor.DARK_GREEN))
                        .append(Component.text(website == null ? "None" : website, NamedTextColor.WHITE))
                        .append(Component.text("\nIs on tasklist: ", NamedTextColor.DARK_GREEN));

        TaskObj[] tasks = TasksUtils.getTasks();

        if (tasks != null) {
            try {
                File file =
                        new File(pl.getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
                tasks = Arrays.stream(tasks).filter(task -> task.getFile() == file).toArray(TaskObj[]::new);

                textComponent =
                        textComponent.append(Component.text(tasks.length == 1, NamedTextColor.WHITE));
            } catch (URISyntaxException e) {
                MultiplatformLogger.error("Couldn't make URI while getting plugin's file.");
                textComponent = textComponent.append(Component.text(false, NamedTextColor.WHITE));
            }
        } else {
            textComponent = textComponent.append(Component.text(false, NamedTextColor.WHITE));
        }

        textComponent =
                textComponent.append(Component.text("\nIs on backuplist: ", NamedTextColor.DARK_GREEN));

        BackupObj[] backups = TasksUtils.getBackups();

        if (backups != null) {
            try {
                File file =
                        new File(pl.getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
                backups =
                        Arrays.stream(backups)
                                .filter(backup -> backup.getFile() == file)
                                .toArray(BackupObj[]::new);

                textComponent =
                        textComponent.append(Component.text(backups.length == 1, NamedTextColor.WHITE));
            } catch (URISyntaxException e) {
                MultiplatformLogger.error("Couldn't make URI while deleting plugin's file.");
                textComponent = textComponent.append(Component.text(false, NamedTextColor.WHITE));
            }
        } else {
            textComponent = textComponent.append(Component.text(false, NamedTextColor.WHITE));
        }

        textComponent =
                textComponent
                        .append(Component.text("\nIs enabled: ", NamedTextColor.DARK_GREEN))
                        .append(Component.text(pl.isEnabled(), NamedTextColor.WHITE));

        return textComponent;
    }

    /**
     * Reads plugin's meta from file.
     * @param file Jar file of plugin.
     * @return PluginMeta of the jar.
     */
    public static PluginMeta readPluginMeta(File file) {
        try (JarFile jar = new JarFile(file)) {
            JarEntry entry = jar.getJarEntry("plugin.yml");

            if (entry == null) {
                entry = jar.getJarEntry("paper-plugin.yml");
            } if (entry == null) {
                return null;
            }

            try (InputStream input = jar.getInputStream(entry)) {
                return new PluginDescriptionFile(input);
            }
        } catch (Exception e) {
            MultiplatformLogger.error("Cannot read PluginMeta of " + file.getName(), e);
            return null;
        }
    }

    /**
      * Gets all plugins
      *
      * @return Array of all plugins
      */
    public static String[] getPlugins(Status[] statuses) {
        List<String> plugins = new ArrayList<>();

        if (Arrays.stream(statuses).toList().contains(Status.ENABLED)) {
            Arrays.stream(Bukkit.getPluginManager().getPlugins())
                    .filter(Plugin::isEnabled)
                    .forEach(plugin -> plugins.add(plugin.getName()));
        }

        if (Arrays.stream(statuses).toList().contains(Status.DISABLED)) {
            Arrays.stream(Bukkit.getPluginManager().getPlugins())
                    .filter(plugin -> !plugin.isEnabled())
                    .forEach(plugin -> plugins.add(plugin.getName()));
        }

        if (Arrays.stream(statuses).toList().contains(Status.RENAMED)) {
            Arrays.stream(Bukkit.getPluginsFolder()
                    .listFiles(file -> file.toString().endsWith("diskiumdisable")))
                    .forEach(plugin -> {
                        PluginMeta meta = readPluginMeta(plugin);

                        if (meta == null) {
                            return;
                        }

                        plugins.add(meta.getName());
                    });
        }

        return plugins.toArray(String[]::new);
    }

    /**
     * Gets a plugin from its name.
     * @param s Name of the plugin.
     * @return Plugin from its name as a file.
     */
    private static File getPlugin(String s) {
        File[] files = Bukkit.getPluginsFolder().listFiles(file -> {
            PluginMeta meta = readPluginMeta(file);

            if (meta == null) {
                MultiplatformLogger.warn("PluginMeta of " + s + " is null, skipping.");
                return false;
            }

            return meta.getName().equalsIgnoreCase(s);
        });

        if (files.length == 0) {
            return null;
        }

        return files[0];
    }

    public enum Status {
        ENABLED,
        DISABLED,
        RENAMED
    }
}
