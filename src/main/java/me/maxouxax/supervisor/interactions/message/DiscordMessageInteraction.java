package me.maxouxax.supervisor.interactions.message;

import me.maxouxax.supervisor.interactions.DiscordInteraction;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;

public interface DiscordMessageInteraction extends DiscordInteraction {

    void onInteraction(GenericComponentInteractionCreateEvent event);

}
