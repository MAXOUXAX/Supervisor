package me.maxouxax.supervisor.commands;

import me.maxouxax.supervisor.Supervisor;
import me.maxouxax.supervisor.commands.slashannotations.DiscordInteraction;
import me.maxouxax.supervisor.commands.slashannotations.Option;
import me.maxouxax.supervisor.commands.slashannotations.Subcommand;
import me.maxouxax.supervisor.commands.slashannotations.SubcommandGroup;
import me.maxouxax.supervisor.supervised.Supervised;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.interactions.commands.build.*;

import java.lang.reflect.Method;
import java.util.*;

public final class CommandManager {

    private final HashMap<Supervised, ArrayList<DiscordCommand>> discordCommands = new HashMap<>();
    private final HashMap<Supervised, ArrayList<DiscordInteraction>> discordInteractions = new HashMap<>();
    private final ArrayList<ConsoleCommand> consoleCommands = new ArrayList<>();
    private final Supervisor supervisor;

    public CommandManager() {
        this.supervisor = Supervisor.getInstance();
    }

    public void updateCommands(Supervised supervised) {
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

    public boolean executeConsoleCommand(String commandInput) {
        String[] commandSplit = commandInput.split(" ");
        Optional<ConsoleCommand> command = consoleCommands.stream().filter(consoleCommand -> consoleCommand.name().equalsIgnoreCase(commandSplit[0])).findFirst();
        command.ifPresent(consoleCommand -> {
            String[] args = new String[commandSplit.length - 1];
            System.arraycopy(commandSplit, 1, args, 0, commandSplit.length - 1);
            try {
                consoleCommand.onCommand(args);
            } catch (Exception e) {
                supervisor.getLogger().error("La commande " + consoleCommand.name() + " a rencontré un problème lors de son exécution. (" + e.getMessage() + ")");
            }
        });
        return command.isPresent();
    }

    public ArrayList<ConsoleCommand> getConsoleCommands() {
        return consoleCommands;
    }

    public HashMap<Supervised, ArrayList<DiscordCommand>> getDiscordCommands() {
        return discordCommands;
    }

    public void registerCommand(Supervised supervised, DiscordCommand command) {
        discordCommands.get(supervised).add(command);
    }

    public void registerConsoleCommand(ConsoleCommand consoleCommand) {
        consoleCommands.add(consoleCommand);
    }

    public ArrayList<DiscordCommand> getDiscordCommandsFromSupervised(Supervised supervised) {
        return discordCommands.get(supervised);
    }

    public void executeDiscordCommand(Supervised supervised, String command, SlashCommandInteractionEvent slashCommandEvent) {
        discordCommands.get(supervised).stream().filter(discordCommand -> discordCommand.name().equalsIgnoreCase(command)).findFirst().ifPresent(discordCommand -> {
            if (!slashCommandEvent.isFromGuild() || discordCommand.power() > getUserPower(supervised, slashCommandEvent.getGuild(), slashCommandEvent.getUser())) {
                slashCommandEvent.reply("Vous ne pouvez pas utiliser cette commande.").setEphemeral(true).queue();
                return;
            }

            try {
                discordCommand.onCommand(slashCommandEvent.getChannel(), slashCommandEvent.getMember(), slashCommandEvent);
            } catch (Exception e) {
                supervisor.getLogger().error("La commande " + discordCommand.name() + " a rencontré un problème lors de son exécution. (" + e.getMessage() + ")");
                supervisor.getErrorHandler().handleException(e);
            }
        });
    }

    private long getUserPower(Supervised supervised, Guild guild, User user) {
        return supervised.getServerConfigsManager().getServerConfig(guild.getId()).getPowerFromUser(user.getId());
    }

    public void executeDiscordInteraction(Supervised supervised, String id, GenericComponentInteractionCreateEvent event) {
        discordInteractions.get(supervised).stream().filter(discordInteraction -> discordInteraction.id().equalsIgnoreCase(id)).findFirst().ifPresent(discordInteraction -> {
            try {
                discordInteraction.onInteraction(event);
            } catch (Exception e) {
                supervisor.getLogger().error("L'interaction " + discordInteraction.id() + " a rencontré un problème lors de son exécution. (" + e.getMessage() + ")");
            }
        });
    }

    public void registerSupervised(Supervised supervised) {
        this.discordCommands.put(supervised, new ArrayList<>());
    }

}