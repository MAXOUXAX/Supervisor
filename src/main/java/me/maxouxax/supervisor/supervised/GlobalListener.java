package me.maxouxax.supervisor.supervised;

import me.maxouxax.supervisor.Supervisor;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
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
        if (event instanceof GenericComponentInteractionCreateEvent) onMessageInteraction((GenericComponentInteractionCreateEvent) event);
        if (event instanceof ModalInteractionEvent) onModalInteraction((ModalInteractionEvent) event);
    }

    private void onCommand(SlashCommandInteractionEvent event) {
        supervisor.getInteractionManager().executeDiscordCommand(supervised, event.getName(), event);
    }

    private void onMessageInteraction(GenericComponentInteractionCreateEvent event) {
        supervisor.getInteractionManager().executeDiscordMessageInteraction(supervised, event.getComponentId(), event);
    }

    private void onModalInteraction(ModalInteractionEvent event) {
        supervisor.getInteractionManager().executeDiscordModalInteraction(supervised, event.getModalId(), event);
    }

}
