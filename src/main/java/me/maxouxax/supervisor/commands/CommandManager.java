package me.maxouxax.supervisor.commands;

import me.maxouxax.supervisor.Supervisor;
import me.maxouxax.supervisor.commands.handlers.ConsoleCommandHandler;
import me.maxouxax.supervisor.commands.handlers.DiscordCommandHandler;
import me.maxouxax.supervisor.supervised.Supervised;

import java.util.ArrayList;

public class CommandManager {

    private final Supervisor supervisor;
    private final ArrayList<CommandHandler> commandHandlers;

    public CommandManager() {
        this.supervisor = Supervisor.getInstance();
        this.commandHandlers = new ArrayList<>();
        registerCommandHandler(new ConsoleCommandHandler());
        registerCommandHandler(new DiscordCommandHandler());
    }

    /**
     * Register a command handler
     * Useful if you're trying to add a new command handler
     *
     * @param commandHandler The command handler to register
     */
    public void registerCommandHandler(CommandHandler commandHandler) {
        this.commandHandlers.add(commandHandler);
    }

    /**
     * Registers a command with a null Supervised
     * This method only works if the handler of the command doesn't take into account the supervised, like the default {@link ConsoleCommandHandler}
     *
     * @param command The command to register
     */
    public void registerCommand(Command command) {
        commandHandlers.forEach(commandHandler -> commandHandler.registerCommand(null, command));
    }

    /**
     * Registers a command to a supervised
     *
     * @param supervised The supervised to register the command to
     * @param command    The command to register
     */
    public void registerCommand(Supervised supervised, Command command) {
        commandHandlers.forEach(commandHandler -> commandHandler.registerCommand(supervised, command));
    }

}