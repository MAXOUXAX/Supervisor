package me.maxouxax.supervisor.interactions.commands;

public interface ConsoleCommand extends Command {

    void onCommand(String[] args);

    String help();

    String example();

}
