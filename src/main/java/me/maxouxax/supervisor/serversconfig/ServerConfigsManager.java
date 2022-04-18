package me.maxouxax.supervisor.serversconfig;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import me.maxouxax.supervisor.database.DatabaseManager;
import me.maxouxax.supervisor.database.Databases;
import me.maxouxax.supervisor.database.nosql.DatabaseAccess;

import java.util.ArrayList;
import java.util.List;

public abstract class ServerConfigsManager {

    private String databaseName;
    private DatabaseAccess databaseAccess;
    private List<ServerConfig> serverConfigs;

    public ServerConfigsManager(String databaseName) {
        this.databaseName = databaseName;
        this.databaseAccess = (DatabaseAccess) DatabaseManager.getDatabaseAccess(Databases.MONGODB.getName());
    }

    public void loadConfigs() {
        MongoClient mongoClient = this.databaseAccess.getMongoClient();
        MongoDatabase database = mongoClient.getDatabase(databaseName);
        MongoCollection<? extends ServerConfig> collection = database.getCollection("server_config", getServerConfigImpl());
        this.serverConfigs = collection.find().into(new ArrayList<>());
    }

    public abstract <T extends ServerConfig> Class<T> getServerConfigImpl();

    public ServerConfig getServerConfig(String serverId) {
        if (serverConfigs.stream().anyMatch(serverConfig -> serverConfig.getServerId().equals(serverId))) {
            return serverConfigs.stream().filter(serverConfig -> serverConfig.getServerId().equals(serverId)).findFirst().orElse(null);
        } else {
            ServerConfig serverConfig = getDefault(serverId);
            serverConfigs.add(serverConfig);
            return serverConfig;
        }
    }

    public abstract ServerConfig getDefault(String serverId);

    public void saveServerConfig(ServerConfig serverConfig) {
        String serverId = serverConfig.getServerId();
        if (serverConfig.equals(getServerConfig(serverId))) return;

        MongoClient mongoClient = this.databaseAccess.getMongoClient();
        MongoDatabase database = mongoClient.getDatabase(databaseName);
        MongoCollection<ServerConfig> collection = database.getCollection("server_config", getServerConfigImpl());
        if (collection.countDocuments(Filters.eq("_id", serverId)) == 0) {
            collection.insertOne(serverConfig);
        } else {
            collection.replaceOne(Filters.eq("_id", serverId), serverConfig);
        }
    }

    public void saveServerConfig(String serverId) {
        ServerConfig serverConfig = getServerConfig(serverId);
        saveServerConfig(serverConfig);
    }

    public List<ServerConfig> getServerConfigs() {
        return serverConfigs;
    }

}
