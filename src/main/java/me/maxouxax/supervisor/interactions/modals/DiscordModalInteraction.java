package me.maxouxax.supervisor.interactions.modals;

import me.maxouxax.supervisor.interactions.DiscordInteraction;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;

public interface DiscordModalInteraction extends DiscordInteraction {

    void onModalSubmit(ModalInteractionEvent event);

}
