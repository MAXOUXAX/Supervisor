package me.maxouxax.supervisor.commands;

import me.maxouxax.supervisor.Supervisor;
import me.maxouxax.supervisor.commands.slashannotations.DiscordInteraction;
import me.maxouxax.supervisor.commands.slashannotations.Option;
import me.maxouxax.supervisor.commands.slashannotations.Subcommand;
import me.maxouxax.supervisor.commands.slashannotations.SubcommandGroup;
import me.maxouxax.supervisor.supervised.Supervised;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.interactions.commands.build.*;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

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
            SlashCommandData commandData = Commands.slash(discordCommand.name(), discordCommand.description());

            List<Method> methods = List.of(discordCommand.getClass().getMethods());

            HashMap<String, SubcommandGroupData> subcommandGroups = new HashMap<>();

            for (Method method : methods) {
                if (method.isAnnotationPresent(Subcommand.class)) {
                    Subcommand subcommandAnnotation = method.getAnnotation(Subcommand.class);
                    SubcommandData subcommandData = new SubcommandData(subcommandAnnotation.name(), subcommandAnnotation.description());

                    subcommandData.addOptions(getOptionsFromMethod(method));

                    if (method.isAnnotationPresent(SubcommandGroup.class)) {
                        SubcommandGroup subcommandGroupAnnotation = method.getAnnotation(SubcommandGroup.class);
                        SubcommandGroupData subcommandGroupData = subcommandGroups.getOrDefault(subcommandGroupAnnotation.name(), new SubcommandGroupData(subcommandGroupAnnotation.name(), subcommandGroupAnnotation.description()));
                        subcommandGroupData.addSubcommands(subcommandData);
                    } else {
                        commandData.addSubcommands(subcommandData);
                    }
                } else {
                    commandData.addOptions(getOptionsFromMethod(method));
                }
            }
            commandData.addSubcommandGroups(subcommandGroups.values());
            commands.add(commandData);
        });
        supervised.getJda().updateCommands().addCommands(commands).queue();
    }

    private List<OptionData> getOptionsFromMethod(Method method) {
        List<Option> options = List.of(method.getAnnotationsByType(Option.class));
        List<OptionData> optionDataList = new ArrayList<>();

        for (Option option : options) {
            OptionData optionData = new OptionData(option.type(), option.name(), option.description(), option.required());
            optionDataList.add(optionData);
        }

        return optionDataList;
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

    public void executeDiscordCommand(Supervised supervised, String command, SlashCommandInteractionEvent
            slashCommandEvent) {
        discordCommands.get(supervised).stream().filter(discordCommand -> discordCommand.name().equalsIgnoreCase(command)).findFirst().ifPresent(discordCommand -> {
            if (!slashCommandEvent.isFromGuild() || discordCommand.power() > getUserPower(supervised, slashCommandEvent.getGuild(), slashCommandEvent.getUser())) {
                slashCommandEvent.reply("Vous ne pouvez pas utiliser cette commande.").setEphemeral(true).queue();
                return;
            }

            try {
                String subcommand = slashCommandEvent.getSubcommandName();
                String subcommandGroup = slashCommandEvent.getSubcommandGroup();
                if (subcommand != null) {
                    Method method = discordCommand.getMethod(subcommandGroup, subcommand);
                    if (method != null) {
                        method.invoke(slashCommandEvent.getChannel(), slashCommandEvent.getMember(), slashCommandEvent);
                    } else {
                        throw new NoSuchMethodException("La commande " + command + " n'a pas de sous-commande " + subcommand + " dans le groupe " + subcommandGroup);
                    }
                } else {
                    discordCommand.onRootCommand(slashCommandEvent.getChannel(), slashCommandEvent.getMember(), slashCommandEvent);
                }
            } catch (Exception e) {
                supervisor.getLogger().error("La commande " + discordCommand.name() + " a rencontré un problème lors de son exécution. (" + e.getMessage() + ")");
                supervisor.getErrorHandler().handleException(e);
            }
        });
    }

    private long getUserPower(Supervised supervised, Guild guild, User user) {
        return supervised.getServerConfigsManager().getServerConfig(guild.getId()).getPowerFromUser(user.getId());
    }

    public void executeDiscordInteraction(Supervised supervised, String id, GenericComponentInteractionCreateEvent
            event) {
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