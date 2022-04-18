package me.maxouxax.supervisor.serversconfig;

public abstract class ServerConfig {

    String serverId;

    public ServerConfig(String serverId) {
        this.serverId = serverId;
    }

    public ServerConfig() {
    }

    public abstract ServerConfig getDefault(String serverId, ServerConfigsManager serverConfigsManager);

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public abstract long getPowerFromUser(String userId);

}
