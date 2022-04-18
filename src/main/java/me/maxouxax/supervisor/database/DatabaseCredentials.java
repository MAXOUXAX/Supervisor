package me.maxouxax.supervisor.database;

public class DatabaseCredentials {

    private String type;
    private String host;
    private String user;
    private String password;
    private String databaseName;
    private String uri;
    private int port;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isValid() {
        return !(host.equals("host") || databaseName.equals("database name"));
    }

    public String toURI() {
        return uri.replace("{host}", host).replace("{port}", port + "").replace("{databaseName}", databaseName).replace("{user}", user).replace("{password}", password);
    }

}
