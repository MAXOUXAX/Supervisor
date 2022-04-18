package me.maxouxax.supervisor.manager;

import me.maxouxax.supervisor.Supervisor;
import me.maxouxax.supervisor.supervised.Supervised;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import org.jetbrains.annotations.NotNull;

public class GlobalListener implements EventListener {

    private final Supervised supervised;
    private final Supervisor supervisor;

    public GlobalListener(Supervised supervised, Supervisor supervisor) {
        this.supervised = supervised;
        this.supervisor = supervisor;
    }

    @Override
    public void onEvent(@NotNull GenericEvent event) {
        if (event instanceof SlashCommandInteractionEvent) onCommand((SlashCommandInteractionEvent) event);
        if (event instanceof GenericComponentInteractionCreateEvent) onInteraction((GenericComponentInteractionCreateEvent) event);
    }

    private void onCommand(SlashCommandInteractionEvent event) {
        supervisor.getCommandManager().executeDiscordCommand(supervised, event.getName(), event);
    }

    private void onInteraction(GenericComponentInteractionCreateEvent event) {
        supervisor.getCommandManager().executeDiscordInteraction(supervised, event.getId(), event);
    }

}
