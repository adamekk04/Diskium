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
        context.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            commands.registrar().register(MainCommand.register(context.getDataDirectory().toFile()));
        });

        context.getLogger().info(ASCII.printASCII("Paper", context.getPluginMeta().getVersion(), ServerBuildInfo.buildInfo().minecraftVersionName()));

        context.getLogger().info("Checking for tasks to do before server startup");

        if (!TasksUtils.fileExists(context.getDataDirectory().toFile(), true)) {
            TasksUtils.create(context.getDataDirectory().toFile(), true);

            context.getLogger().info("tasks.txt not found, created new one.");
            return;
        }

        context.getLogger().info("Found tasks.txt, trying to find tasks");

        TaskObj[] tasks = TasksUtils.getTasks(context.getDataDirectory().toFile());

        if (tasks == null) {
            context.getLogger().info("No tasks found.");
            return;
        }

        context.getLogger().info("Found " + tasks.length + " task(s) to do");

        TasksUtils.complete(tasks);

        context.getLogger().info("All bootstrap tasks done.");
    }
}
