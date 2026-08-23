package org.diskium;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.diskium.commands.MainCommand;

class DiskiumBootstrap implements PluginBootstrap {

    @Override
    public void bootstrap(final BootstrapContext context) {
        context.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            commands.registrar().register(MainCommand.register());
        });

        context.getLogger().info("Diskium is entering bootstrap is now on");
        context.getLogger().info("Checking for tasks to do before server startup");
        if (!TasksUtils.taskFileExists(context.getDataDirectory().toFile())) {
            context.getLogger().error("Something went wrong while trying to get the file 'tasks.yml'");
            return;
        }
        context.getLogger().info("Found 'tasks.yml', trying to find tasks");
        TaskObj[] tasks = TasksUtils.getTasks(context.getDataDirectory().toFile());
        context.getLogger().info("Found " + tasks.length + " task(s) to do");
        TasksUtils.doTasks(tasks);
        context.getLogger().info("All bootstrap tasks done, Diskium is exiting bootstrap");
    }
}
