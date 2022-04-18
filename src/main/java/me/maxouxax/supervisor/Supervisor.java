package me.maxouxax.supervisor;

import me.maxouxax.supervisor.commands.CommandManager;
import me.maxouxax.supervisor.commands.register.console.CommandConsoleStop;
import me.maxouxax.supervisor.database.DatabaseManager;
import me.maxouxax.supervisor.supervised.Supervised;
import me.maxouxax.supervisor.supervised.SupervisedManager;
import me.maxouxax.supervisor.utils.ErrorHandler;
import org.slf4j.Logger;

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
    private final Logger logger;
    private final ErrorHandler errorHandler;
    private final SupervisedManager supervisedManager;
    private final CommandManager commandManager;
    private final String version;
    private boolean running;

    public Supervisor() throws IllegalArgumentException, NullPointerException, SQLException, IOException {
        instance = this;
        this.logger = org.slf4j.LoggerFactory.getLogger(Supervisor.class);
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

        loadConfigs();

        logger.info("Loading command manager...");
        this.commandManager = new CommandManager();
        logger.info("Registering stop command...");
        this.commandManager.registerConsoleCommand(new CommandConsoleStop());

        logger.info("Initial components loaded, loading supervised manager...");
        this.supervisedManager = new SupervisedManager();

        logger.info("Enabling all supervised bots... (this may take a while)");
        this.supervisedManager.enableAllSupervised();
        logger.info("All supervised bots enabled!");

        logger.info("--------------- STARTING ---------------");

        logger.info("Done! (" + (new Date().getTime() - date.getTime()) + "ms)");
    }

    public static void main(String[] args) {
        try {
            Supervisor supervisor = new Supervisor();
            Thread thread = new Thread(supervisor, "Supervisor");
            thread.start();
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
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

        while (running) {
            if (scanner.hasNextLine()) {
                String commandInput = scanner.nextLine();
                if (!commandManager.executeConsoleCommand(commandInput)) {
                    logger.warn("Unknown command: " + commandInput);
                }
            }
        }

        this.supervisedManager.getSupervised().forEach(Supervised::onDisable);
        logger.info("--------------- STOPPING ---------------");
        logger.info("> Shutdown in progress...");
        scanner.close();
        logger.info("> Scanner closed!");
        DatabaseManager.closeDatabasesConnection();
        logger.info("> Closed database connection!");
        logger.info("--------------- STOPPING ---------------");
        logger.info("Arrêt du BOT réussi");
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

}
