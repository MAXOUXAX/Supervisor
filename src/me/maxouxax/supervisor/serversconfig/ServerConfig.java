package me.maxouxax.supervisor.serversconfig;

import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;

public abstract class ServerConfig {

    @BsonProperty(value = "_id")
    @BsonId
    String serverId;

    ServerConfigsManager serverConfigsManager;

    abstract ServerConfig getDefault(String serverId, ServerConfigsManager serverConfigsManager);

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId, boolean save) {
        this.serverId = serverId;
        if (save) save();
    }

    /**
     * Do not use this method, use {@link ServerConfig#setServerId(String, boolean)} instead if you want to save the server config
     *
     * @param serverId The server id
     */
    private void setServerId(String serverId) {
        setServerId(serverId, false);
    }

    public void save() {
        serverConfigsManager.saveServerConfig(this);
    }

}
