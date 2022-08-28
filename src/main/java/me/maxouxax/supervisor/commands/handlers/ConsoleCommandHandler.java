package me.maxouxax.supervisor.commands.handlers;

import me.maxouxax.supervisor.Supervisor;
import me.maxouxax.supervisor.commands.Command;
import me.maxouxax.supervisor.commands.CommandHandler;
import me.maxouxax.supervisor.commands.ConsoleCommand;
import me.maxouxax.supervisor.supervised.Supervised;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class ConsoleCommandHandler implements CommandHandler {

    private final Supervisor supervisor = Supervisor.getInstance();
    private final HashMap<Supervised, ArrayList<ConsoleCommand>> consoleCommands = new HashMap<>();

    @Override
    public void registerSupervised(Supervised supervised) {
        this.consoleCommands.put(supervised, new ArrayList<>());
    }

    @Override
    public void registerCommand(Supervised supervised, Command command) {
        if (!(command instanceof ConsoleCommand consoleCommand)) return;
        consoleCommands.get(supervised).add(consoleCommand);
    }

    @Override
    public void executeCommand(Supervised supervised, Command command, Object commandData) {
        if (!(command instanceof ConsoleCommand consoleCommand)) return;
        if (!(commandData instanceof String[] args)) return;
        try {
            consoleCommand.onCommand(args);
        } catch (Exception e) {
            supervisor.getLogger().error("La commande " + consoleCommand.name() + " a rencontré un problème lors de son exécution. (" + e.getMessage() + ")");
        }
    }

    @Override
    public void registerListeners(Supervised supervised) {
        Scanner scanner = new Scanner(System.in);
        while (supervisor.isRunning()) {
            if (scanner.hasNextLine()) {
                String commandInput = scanner.nextLine();
                String[] args = commandInput.split(" ");
                String commandName = args[0];
                String[] commandArgs = new String[args.length - 1];
                System.arraycopy(args, 1, commandArgs, 0, args.length - 1);

                ConsoleCommand consoleCommand = (ConsoleCommand) getCommandFromSupervised(supervised, commandName);
                if (consoleCommand == null) continue;

                try {
                    consoleCommand.onCommand(commandArgs);
                } catch (Exception e) {
                    supervisor.getLogger().error("La commande " + consoleCommand.name() + " a rencontré un problème lors de son exécution. (" + e.getMessage() + ")");
                }
            }
        }
    }

    @Override
    public ArrayList<Command> getCommandsFromSupervised(Supervised supervised) {
        return new ArrayList<>(consoleCommands.get(supervised));
    }

    @Override
    public Command getCommandFromSupervised(Supervised supervised, String commandName) {
        return consoleCommands.get(supervised).stream().filter(consoleCommand -> consoleCommand.name().equalsIgnoreCase(commandName)).findFirst().orElse(null);
    }

}
