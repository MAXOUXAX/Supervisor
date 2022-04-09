package me.maxouxax.supervisor;

import me.maxouxax.supervisor.api.Supervised;
import me.maxouxax.supervisor.commands.CommandManager;
import me.maxouxax.supervisor.database.DatabaseManager;
import me.maxouxax.supervisor.manager.SupervisedManager;
import me.maxouxax.supervisor.utils.ErrorHandler;
import org.slf4j.Logger;

import java.io.File;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner;

public class Supervisor implements Runnable {

    private static Supervisor instance;
    private static boolean running;
    private final Scanner scanner = new Scanner(System.in);
    private final Logger logger;

    private final ErrorHandler errorHandler;
    private final SupervisedManager supervisedManager;
    private final CommandManager commandManager;
    private final String version;

    public Supervisor() throws IllegalArgumentException, NullPointerException, SQLException {
        instance = this;
        this.logger = org.slf4j.LoggerFactory.getLogger(Supervisor.class);
        this.errorHandler = new ErrorHandler();

        DatabaseManager.initDatabaseConnection(Paths.get("configs/database.yml"));
        DatabaseManager.initDatabaseConnection(Paths.get("configs/database_mongo.yml"));

        String string = new File(Supervisor.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getName();
        string = string.replaceAll("Supervisor-", "")
                .replaceAll(".jar", "");
        this.version = string;

        this.supervisedManager = new SupervisedManager();

        logger.info("--------------- STARTING ---------------");

        logger.info("> Generated new BOT instance");
        logger.info("> BOT thread started, loading libraries...");
        this.commandManager = new CommandManager();
        logger.info("> Libraries loaded! Loading JDA...");

        loadBots();
        logger.info("> JDA loaded!");

        logger.info("> The BOT is now good to go !");
        logger.info("--------------- STARTING ---------------");
    }

    private void loadBots() {
        //TODO: Load bots from JARs in bots/ folder
        ArrayList<Supervised> supervisedBots = this.supervisedManager.getSupervisedBots();
        supervisedBots.forEach(Supervised::load);
        /*TODO
         * For loop to load all bots
         * for every bot, run commandManager.updateCommands(Supervised)
         * which will update the commands for the bot
         * */
    }

    public static void main(String[] args) {
        try {
            Supervisor supervisor = new Supervisor();
            Thread thread = new Thread(supervisor, "Supervisor");
            thread.start();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        running = true;

        while (running) {
            if (scanner.hasNextLine()) {
                String commandInput = scanner.nextLine();
                commandManager.executeConsoleCommand(commandInput);
            }
        }

        this.supervisedManager.getSupervisedBots().forEach(Supervised::stop);
        logger.info("--------------- STOPPING ---------------");
        logger.info("> Shutdowning...");
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

    public static Supervisor getInstance() {
        return instance;
    }

}
