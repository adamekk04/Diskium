package org.diskium;

import io.papermc.paper.ServerBuildInfo;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.diskium.commands.MainCommand;
import org.diskium.objects.TaskObj;
import org.diskium.utils.ASCII;
import org.diskium.utils.TasksUtils;

class DiskiumBootstrap implements PluginBootstrap {

    @Override
    public void bootstrap(final BootstrapContext context) {
        context.getDataDirectory().toFile().mkdirs();

        TasksUtils.setFiles(
                context.getDataDirectory().toFile().getAbsoluteFile().getParentFile().getParentFile(),
                context.getDataDirectory().toFile());

        MultiplatformLogger.setLogger(new MultiplatformLogger.Logger() {
            @Override
            public void info(String message) {
                context.getLogger().info(message);
            }

            @Override
            public void warn(String message) {
                context.getLogger().warn(message);
            }

            @Override
            public void error(String message) {
                context.getLogger().error(message);
            }

            @Override
            public void error(String message, Throwable throwable) {
                context.getLogger().trace(message, throwable);
            }
        });

        context.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            commands.registrar().register(MainCommand.register(context.getDataDirectory().toFile()));
        });

        context.getLogger().info(ASCII.printASCII("Paper", context.getPluginMeta().getVersion(), ServerBuildInfo.buildInfo().minecraftVersionName()));
        context.getLogger().info("Checking for tasks to do before server startup.");

        if (!TasksUtils.fileExists(true)) {
            TasksUtils.createDirs(context.getDataDirectory().toFile(), true);

            context.getLogger().info("tasks.txt not found, created new one.");
            return;
        }

        TaskObj[] tasks = TasksUtils.getTasks();

        if (tasks == null) {
            context.getLogger().info("No tasks found.");
            return;
        }

        context.getLogger().info("Found " + tasks.length + " task(s).");

        TasksUtils.complete(tasks);

        context.getLogger().info("All bootstrap tasks done.");
    }
}
