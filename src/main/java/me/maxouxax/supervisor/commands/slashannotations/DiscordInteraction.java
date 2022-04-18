package me.maxouxax.supervisor.commands.slashannotations;

import me.maxouxax.supervisor.commands.Identifier;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;

public interface DiscordInteraction extends Identifier {

    String id();

    void onInteraction(GenericComponentInteractionCreateEvent event);

}
