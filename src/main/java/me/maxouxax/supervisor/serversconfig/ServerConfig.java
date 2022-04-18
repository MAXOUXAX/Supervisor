package me.maxouxax.supervisor.serversconfig;

public abstract class ServerConfig {

    public ServerConfig() {
    }

    public abstract ServerConfig getDefault(String serverId);

    public abstract String getServerId();

    public abstract long getPowerFromUser(String userId);

}
