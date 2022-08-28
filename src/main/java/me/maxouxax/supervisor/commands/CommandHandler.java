package me.maxouxax.supervisor.commands;

import me.maxouxax.supervisor.supervised.Supervised;

import java.util.ArrayList;

public interface CommandHandler {

    /**
     * This should register the Supervised as an user of that handler
     *
     * @param supervised The supervised
     */
    void registerSupervised(Supervised supervised);

    /**
     * This should register the command to the supervised
     *
     * @param supervised The supervised to register the command to
     * @param command    The command to register
     */
    void registerCommand(Supervised supervised, Command command);

    /**
     * This should execute the command
     *
     * @param supervised  The supervised to execute the command to
     * @param command     The command to execute
     * @param commandData The data needed to execute the command
     */
    void executeCommand(Supervised supervised, Command command, Object commandData);

    /**
     * This should be called once after every command has been registered
     *
     * @param supervised The supervised to register the commands
     */
    void registerListeners(Supervised supervised);

    ArrayList<Command> getCommandsFromSupervised(Supervised supervised);

}
