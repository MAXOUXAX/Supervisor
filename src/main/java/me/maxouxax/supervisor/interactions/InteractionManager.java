package me.maxouxax.supervisor.interactions;

import me.maxouxax.supervisor.Supervisor;
import me.maxouxax.supervisor.interactions.annotations.Option;
import me.maxouxax.supervisor.interactions.annotations.Subcommand;
import me.maxouxax.supervisor.interactions.annotations.SubcommandGroup;
import me.maxouxax.supervisor.interactions.commands.ConsoleCommand;
import me.maxouxax.supervisor.interactions.commands.DiscordCommand;
import me.maxouxax.supervisor.interactions.message.DiscordMessageInteraction;
import me.maxouxax.supervisor.interactions.modals.DiscordModalInteraction;
import me.maxouxax.supervisor.supervised.Supervised;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.interactions.commands.build.*;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public final class InteractionManager {

    private final HashMap<Supervised, ArrayList<DiscordCommand>> discordCommands = new HashMap<>();
    private final HashMap<Supervised, ArrayList<DiscordMessageInteraction>> discordMessageInteractions = new HashMap<>();
    private final HashMap<Supervised, ArrayList<DiscordModalInteraction>> discordModalInteractions = new HashMap<>();

    private final ArrayList<ConsoleCommand> consoleCommands = new ArrayList<>();
    private final Supervisor supervisor;

    public InteractionManager() {
        this.supervisor = Supervisor.getInstance();
    }

    public void updateCommands(Supervised supervised) {
        List<CommandData> commands = new ArrayList<>();
        discordCommands.get(supervised).forEach(discordCommand -> {
            supervisor.getLogger().debug("-".repeat(50));
            supervisor.getLogger().debug("Registering command /" + discordCommand.name());
            SlashCommandData commandData = Commands.slash(discordCommand.name(), discordCommand.description());

            List<Method> methods = List.of(discordCommand.getClass().getMethods());

            HashMap<String, SubcommandGroupData> subcommandGroups = new HashMap<>();

            supervisor.getLogger().debug("Scanning methods for subcommands");
            for (Method method : methods) {
                if (method.isAnnotationPresent(Subcommand.class)) {
                    Subcommand subcommandAnnotation = method.getAnnotation(Subcommand.class);
                    SubcommandData subcommandData = new SubcommandData(subcommandAnnotation.name(), subcommandAnnotation.description());
                    supervisor.getLogger().debug("Found subcommand /" + discordCommand.name() + " " + subcommandAnnotation.name());

                    subcommandData.addOptions(getOptionsFromMethod(method));

                    if (method.isAnnotationPresent(SubcommandGroup.class)) {
                        SubcommandGroup subcommandGroupAnnotation = method.getAnnotation(SubcommandGroup.class);
                        SubcommandGroupData subcommandGroupData = subcommandGroups.getOrDefault(subcommandGroupAnnotation.name(), new SubcommandGroupData(subcommandGroupAnnotation.name(), subcommandGroupAnnotation.description()));
                        if (!subcommandGroups.containsKey(subcommandGroupAnnotation.name())) {
                            subcommandGroups.put(subcommandGroupAnnotation.name(), subcommandGroupData);
                        }
                        supervisor.getLogger().debug("Subcommand " + subcommandAnnotation.name() + " is in group " + subcommandGroupAnnotation.name());
                        subcommandGroupData.addSubcommands(subcommandData);
                    } else {
                        supervisor.getLogger().debug("Subcommand " + subcommandAnnotation.name() + " is not in a group");
                        commandData.addSubcommands(subcommandData);
                    }
                } else {
                    supervisor.getLogger().debug("Method " + method.getName() + " is not a subcommand");
                    commandData.addOptions(getOptionsFromMethod(method));
                }
            }
            commandData.addSubcommandGroups(subcommandGroups.values());
            commands.add(commandData);
        });
        supervised.getJda().updateCommands().addCommands(commands).queue();
    }

    private List<OptionData> getOptionsFromMethod(Method method) {
        supervisor.getLogger().debug("Scanning method " + method.getName() + " for options");
        List<Option> options = List.of(method.getAnnotationsByType(Option.class));
        List<OptionData> optionDataList = new ArrayList<>();

        for (Option option : options) {
            OptionData optionData = new OptionData(option.type(), option.name(), option.description(), option.required());
            supervisor.getLogger().debug("Found option " + option.name() + " of type " + option.type() + " with description " + option.description() + " and required " + option.required());
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

    public void registerMessageInteraction(Supervised supervised, DiscordMessageInteraction interaction) {
        discordMessageInteractions.get(supervised).add(interaction);
    }

    public void registerModalInteraction(Supervised supervised, DiscordModalInteraction interaction) {
        discordModalInteractions.get(supervised).add(interaction);
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
                String subcommand = slashCommandEvent.getSubcommandName();
                String subcommandGroup = slashCommandEvent.getSubcommandGroup();
                if (subcommand != null) {
                    Method method = discordCommand.getMethod(subcommandGroup, subcommand);
                    if (method != null) {
                        method.invoke(discordCommand, slashCommandEvent.getChannel(), slashCommandEvent.getMember(), slashCommandEvent);
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

    public void executeDiscordMessageInteraction(Supervised supervised, String id, GenericComponentInteractionCreateEvent event) {
        discordMessageInteractions.get(supervised).stream().filter(discordInteraction -> discordInteraction.id().equalsIgnoreCase(id)).findFirst().ifPresent(discordInteraction -> {
            try {
                discordInteraction.onInteraction(event);
            } catch (Exception e) {
                supervisor.getLogger().error("L'interaction " + discordInteraction.id() + " a rencontré un problème lors de son exécution. (" + e.getMessage() + ")");
            }
        });
    }

    public void executeDiscordModalInteraction(Supervised supervised, String id, ModalInteractionEvent event) {
        discordModalInteractions.get(supervised).stream().filter(discordInteraction -> discordInteraction.id().equalsIgnoreCase(id)).findFirst().ifPresent(discordModalInteraction -> {
            try {
                discordModalInteraction.onModalSubmit(event);
            } catch (Exception e) {
                supervisor.getLogger().error("L'interaction " + discordModalInteraction.id() + " a rencontré un problème lors de son exécution. (" + e.getMessage() + ")");
            }
        });
    }

    public void registerSupervised(Supervised supervised) {
        this.discordCommands.put(supervised, new ArrayList<>());
    }

}