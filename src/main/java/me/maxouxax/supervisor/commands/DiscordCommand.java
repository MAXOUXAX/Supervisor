package me.maxouxax.supervisor.commands;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public interface DiscordCommand extends Command {

    void onCommand(TextChannel textChannel, Member member, SlashCommandInteractionEvent messageContextInteractionEvent);

    int power();

}
