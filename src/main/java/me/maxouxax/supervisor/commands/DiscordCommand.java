package me.maxouxax.supervisor.commands;

import me.maxouxax.supervisor.commands.slashannotations.Subcommand;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.lang.reflect.Method;

public interface DiscordCommand extends Command {

    void onRootCommand(MessageChannelUnion messageChannelUnion, Member member, SlashCommandInteractionEvent slashCommandInteractionEvent);

    int power();

    default Method getMethod(String subcommandGroup, String subcommand){
        for(Method method : this.getClass().getMethods()){
            if(method.isAnnotationPresent(Subcommand.class)){
                Subcommand subcommandAnnotation = method.getAnnotation(Subcommand.class);
                if(subcommandAnnotation.name().equals(subcommand)){
                    if(subcommandAnnotation.group().equals(subcommandGroup)){
                        return method;
                    }
                }
            }
        }
        return null;
    }

}
