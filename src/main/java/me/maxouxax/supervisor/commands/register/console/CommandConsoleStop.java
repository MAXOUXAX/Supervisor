package me.maxouxax.supervisor.commands.register.console;

import me.maxouxax.supervisor.Supervisor;
import me.maxouxax.supervisor.commands.ConsoleCommand;

public class CommandConsoleStop implements ConsoleCommand {

    private final Supervisor supervisor;

    public CommandConsoleStop() {
        this.supervisor = Supervisor.getInstance();
    }

    @Override
    public String name() {
        return "stop";
    }

    @Override
    public String description() {
        return "Permet d'arrêter le superviseur";
    }

    @Override
    public String help() {
        return "stop";
    }

    @Override
    public String example() {
        return "stop";
    }

    @Override
    public void onCommand(String[] args) {
        supervisor.setRunning(false);
    }

}
