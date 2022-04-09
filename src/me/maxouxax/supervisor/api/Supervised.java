package me.maxouxax.supervisor.api;

import me.maxouxax.supervisor.Supervisor;
import me.maxouxax.supervisor.serversconfig.ServerConfigsManager;
import net.dv8tion.jda.api.JDA;

/**
 * Represents the Supervised BOT
 */
public abstract class Supervised {

    private ServerConfigsManager serverConfigsManager;
    public JDA jda;

    /**
     * This method will be called when loading the BOT in, right before starting it
     */
    public abstract void load();

    /**
     * This method will be called when the BOT is being started
     */
    public void start() {
        this.serverConfigsManager.loadConfigs();
        Supervisor.getInstance().getCommandManager().updateCommands(this);
    }

    /**
     * This method will be called when the BOT is being stopped
     */
    public abstract void stop();

    /**
     * This method will be called when the BOT is being reloaded
     */
    public abstract void reload();

    public ServerConfigsManager getServerConfigsManager() {
        return serverConfigsManager;
    }

}
