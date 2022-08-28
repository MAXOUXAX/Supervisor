package me.maxouxax.supervisor;

import me.maxouxax.supervisor.commands.CommandManager;
import me.maxouxax.supervisor.commands.register.console.CommandConsoleStop;
import me.maxouxax.supervisor.database.DatabaseManager;
import me.maxouxax.supervisor.supervised.SupervisedManager;
import me.maxouxax.supervisor.utils.ErrorHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.Date;
import java.util.Scanner;

public class Supervisor implements Runnable {

    private static Supervisor instance;
    private final Scanner scanner = new Scanner(System.in);
    private Logger logger;
    private ErrorHandler errorHandler;
    private SupervisedManager supervisedManager;
    private CommandManager commandManager;
    private String version;
    private boolean running;

    public void startSupervisor() throws SQLException, IOException {
        instance = this;
        this.logger = LoggerFactory.getLogger(Supervisor.class);
        String string = new File(Supervisor.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getName();
        string = string.replaceAll("Supervisor-", "")
                .replaceAll(".jar", "");
        this.version = string;

        Date date = new Date();

        logger.info("--------------- STARTING ---------------");
        logger.info("Supervisor v" + getVersion());
        logger.info("Loading error handler...");

        this.errorHandler = new ErrorHandler();

        logger.info("Loading configs...");

        try {
            loadConfigs();
        } catch (SQLException | IOException e) {
            logger.error("An error occured while loading configs: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }

        logger.info("Loading command manager...");
        this.commandManager = new CommandManager();
        logger.info("Registering stop command...");
        this.commandManager.registerCommand(new CommandConsoleStop());

        logger.info("Initial components loaded, loading supervised manager...");
        this.supervisedManager = new SupervisedManager();

        logger.info("Enabling all supervised bots... (this may take a while)");
        this.supervisedManager.enableAllSupervised();
        logger.info("All supervised bots enabled!");

        logger.info("--------------- STARTING ---------------");

        logger.info("Done! (" + (new Date().getTime() - date.getTime()) + "ms)");
    }

    public static void main(String[] args) {
        Supervisor supervisor = new Supervisor();
        Thread thread = new Thread(supervisor, "Supervisor");
        thread.start();
    }

    public static Supervisor getInstance() {
        return instance;
    }

    private void loadConfigs() throws SQLException, IOException {
        File configFolder = new File("configs/");

        File mariaDbConfig = new File(configFolder, "mariadb.yml");
        File mongoDbConfig = new File(configFolder, "mongodb.yml");

        if (!configFolder.exists()) {
            logger.info("Config folder not found, creating...");
            configFolder.mkdirs();
        }

        if (!mariaDbConfig.exists()) {
            logger.info("MariaDB config not found, copying default config...");
            copyDefaultConfig(mariaDbConfig);
        }
        if (!mongoDbConfig.exists()) {
            logger.info("MongoDB config not found, copying default config...");
            copyDefaultConfig(mongoDbConfig);
        }

        logger.info("Connecting to MariaDB...");
        DatabaseManager.initDatabaseConnection(mariaDbConfig);
        logger.info("Connected to MariaDB!");
        logger.info("Connecting to MongoDB...");
        DatabaseManager.initDatabaseConnection(mongoDbConfig);
        logger.info("Connected to MongoDB!");
    }

    private void copyDefaultConfig(File configFile) throws IOException {
        InputStream defaultConfig = getClass().getClassLoader().getResourceAsStream("configs/" + configFile.getName());
        if (defaultConfig != null) {
            try {
                Files.copy(defaultConfig, configFile.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
            defaultConfig.close();
        }
    }

    @Override
    public void run() {
        running = true;

        try {
            startSupervisor();
        } catch (Exception e) {
            logger.error("Supervisor encountered an error while starting, forcibly shutting down...");
            System.exit(1);
        }

        logger.info("--------------- STOPPING ---------------");
        logger.info("Supervisor is being stopped...");
        logger.info("Closing scanner to prevent further input...");
        scanner.close();
        logger.info("Disabling all supervised bots...");
        this.supervisedManager.disableAllSupervised();
        logger.info("All supervised bots disabled!");
        logger.info("Closing database connections...");
        DatabaseManager.closeDatabasesConnection();
        logger.info("Database connections closed!");
        logger.info("--------------- STOPPING ---------------");
        System.exit(0);
    }

    public Logger getLogger() {
        return logger;
    }

    public ErrorHandler getErrorHandler() {
        return errorHandler;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }

    public String getVersion() {
        return version;
    }

    public SupervisedManager getSupervisedManager() {
        return supervisedManager;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public boolean isRunning() {
        return running;
    }

}
