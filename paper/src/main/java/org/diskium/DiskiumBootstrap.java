package org.diskium;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;

class DiskiumBootstrap implements PluginBootstrap {

    @Override
    public void bootstrap(final BootstrapContext context) {
        context.getLogger().info("Diskium is entering bootstrap is now on");
        context.getLogger().info("Checking for tasks to do before server startup");
        if (!TasksManagement.taskFileExists(context.getDataDirectory().toFile())) {
            context.getLogger().error("Something went wrong while trying to get the file 'tasks.yml'");
        }
        context.getLogger().info("Found 'tasks.yml', trying to find tasks");
        TaskObj[] tasks = TasksManagement.getTasks(context.getDataDirectory().toFile());
        context.getLogger().info("Found " + tasks.length + "task(s) to do");
        TasksManagement.doTasks(tasks);
        context.getLogger().info("All bootstrap tasks done, Diskium is exiting bootstrap");
    }
}
