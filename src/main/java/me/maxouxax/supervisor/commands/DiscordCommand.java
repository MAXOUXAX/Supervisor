package me.maxouxax.supervisor.commands;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public interface DiscordCommand extends Command {

    void onCommand(MessageChannelUnion messageChannelUnion, Member member, SlashCommandInteractionEvent slashCommandInteractionEvent);

    int power();

}
