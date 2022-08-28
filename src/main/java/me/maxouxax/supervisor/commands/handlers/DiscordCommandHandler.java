package me.maxouxax.supervisor.commands.handlers;

import me.maxouxax.supervisor.Supervisor;
import me.maxouxax.supervisor.commands.Command;
import me.maxouxax.supervisor.commands.CommandHandler;
import me.maxouxax.supervisor.commands.DiscordCommand;
import me.maxouxax.supervisor.commands.slashannotations.Option;
import me.maxouxax.supervisor.commands.slashannotations.Subcommand;
import me.maxouxax.supervisor.commands.slashannotations.SubcommandGroup;
import me.maxouxax.supervisor.supervised.Supervised;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.*;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class DiscordCommandHandler implements CommandHandler {

    private final Supervisor supervisor = Supervisor.getInstance();
    private final HashMap<Supervised, ArrayList<DiscordCommand>> discordCommands = new HashMap<>();

    @Override
    public void registerListeners(Supervised supervised) {
        List<CommandData> commands = new ArrayList<>();
        discordCommands.get(supervised).forEach(discordCommand -> {
            try {
                Method method = discordCommand.getClass().getMethod("onCommand", TextChannel.class, Member.class, SlashCommandInteractionEvent.class);
                List<OptionData> options = Arrays.stream(method.getAnnotationsByType(Option.class)).map(option -> new OptionData(option.type(), option.name(), option.description(), option.isRequired())).toList();
                List<SubcommandData> subcommands = Arrays.stream(method.getAnnotationsByType(Subcommand.class)).map(subcommand -> new SubcommandData(subcommand.name(), subcommand.description())).toList();
                List<SubcommandGroupData> subcommandGroups = Arrays.stream(method.getAnnotationsByType(SubcommandGroup.class)).map(subcommandGroup -> new SubcommandGroupData(subcommandGroup.name(), subcommandGroup.description())).toList();

                SlashCommandData commandData = Commands.slash(discordCommand.name(), discordCommand.description());
                if (options.size() != 0) commandData = commandData.addOptions(options);
                if (subcommands.size() != 0) commandData = commandData.addSubcommands(subcommands);
                if (subcommandGroups.size() != 0) commandData = commandData.addSubcommandGroups(subcommandGroups);

                commands.add(commandData);
            } catch (NoSuchMethodException e) {
                // That should never happen
            }
        });
        supervised.getJda().updateCommands().addCommands(commands).queue();
    }

    @Override
    public void registerSupervised(Supervised supervised) {
        this.discordCommands.put(supervised, new ArrayList<>());
    }

    @Override
    public void registerCommand(Supervised supervised, Command command) {
        if (!(command instanceof DiscordCommand discordCommand)) return;

        discordCommands.get(supervised).add(discordCommand);
    }

    @Override
    public void executeCommand(Supervised supervised, Command command, Object commandData) {
        if (!(command instanceof DiscordCommand discordCommand)) return;
        if (!(commandData instanceof SlashCommandInteractionEvent event)) return;

        if (!event.isFromGuild() || discordCommand.power() > getUserPower(supervised, event.getGuild(), event.getUser())) {
            event.reply("Vous ne pouvez pas utiliser cette commande.").setEphemeral(true).queue();
            return;
        }
        try {
            discordCommand.onCommand(event.getTextChannel(), event.getMember(), event);
        } catch (Exception e) {
            supervisor.getLogger().error("La commande " + discordCommand.name() + " a rencontré un problème lors de son exécution. (" + e.getMessage() + ")");
            supervisor.getErrorHandler().handleException(e);
        }
    }

    private long getUserPower(Supervised supervised, Guild guild, User user) {
        return supervised.getServerConfigsManager().getServerConfig(guild.getId()).getPowerFromUser(user.getId());
    }

    @Override
    public ArrayList<Command> getCommandsFromSupervised(Supervised supervised) {
        return null;
    }
}
