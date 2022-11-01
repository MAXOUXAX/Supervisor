package me.maxouxax.supervisor.interactions.commands;

import me.maxouxax.supervisor.interactions.annotations.Subcommand;
import me.maxouxax.supervisor.interactions.annotations.SubcommandGroup;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.lang.reflect.Method;

public interface DiscordCommand extends Command {

    void onRootCommand(MessageChannelUnion messageChannelUnion, Member member, SlashCommandInteractionEvent slashCommandInteractionEvent);

    int power();

    default Method getMethod(String subcommandGroup, String subcommand) {
        for (Method method : this.getClass().getMethods()) {
            if (method.isAnnotationPresent(Subcommand.class)) {
                Subcommand subcommandAnnotation = method.getAnnotation(Subcommand.class);
                if (subcommandAnnotation.name().equals(subcommand)) {
                    if (method.isAnnotationPresent(SubcommandGroup.class)) {
                        SubcommandGroup subcommandGroupAnnotation = method.getAnnotation(SubcommandGroup.class);
                        if (subcommandGroupAnnotation.name().equals(subcommandGroup)) {
                            return method;
                        }
                    }
                }
            }
        }
        return null;
    }

}
