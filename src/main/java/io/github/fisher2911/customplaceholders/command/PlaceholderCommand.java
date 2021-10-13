package io.github.fisher2911.customplaceholders.command;

import io.github.fisher2911.customplaceholders.CustomPlaceholders;
import io.github.fisher2911.customplaceholders.messages.Messages;
import io.github.fisher2911.customplaceholders.placeholderapi.CustomPlaceholderExpansion;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class PlaceholderCommand implements CommandExecutor, TabExecutor {

    private final CustomPlaceholders plugin;
    private final Messages messages;
    private final String invalidUsage;

    public PlaceholderCommand(final CustomPlaceholders plugin) {
        this.plugin = plugin;
        this.messages = plugin.getMessages();
        this.invalidUsage = this.messages.getMessage(Messages.INVALID_COMMAND_USAGE);
    }

    @Override
    public boolean onCommand(@NotNull final CommandSender sender,
                             @NotNull final Command command,
                             @NotNull final String label,
                             @NotNull final String[] args) {

        if (!sender.hasPermission(CustomPlaceholders.PERMISSION)) {
            sender.sendMessage(this.invalidUsage);
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage(this.invalidUsage);
            return true;
        }


        final String identifier = args[0];
        final String action = args[1];
        final String key = args[2];

        final CustomPlaceholderExpansion expansion = this.plugin.getOrNewExpansion(identifier);
        if (args.length == 3) {
            switch (action.toLowerCase()) {
                case "add" -> {
                    this.setValue(key, sender, args, expansion, "add");
                    return true;
                }
                case "remove" -> {
                    this.setValue(key, sender, args, expansion, "remove");
                    return true;
                }

                case "delete" -> {
                    final String value = expansion.getPlaceholderValue(key);
                    expansion.removePlaceholderValue(key);
                    sender.sendMessage(this.messages.getMessage(Messages.REMOVED_PLACEHOLDER).
                            replace("%id%", identifier).
                            replace("%key%", key).
                            replace("%value%", value));
                    return true;
                }
            }
            sender.sendMessage(this.invalidUsage);
            return true;
        }

        if (args.length == 4) {
            final String value = args[3];
            switch (action.toLowerCase()) {
                case "set" -> {
                    expansion.addPlaceholderValue(key, value);
                    sender.sendMessage(this.messages.getMessage(Messages.ADDED_PLACEHOLDER).
                            replace("%id%", identifier).
                            replace("%key%", key).
                            replace("%value%", value));
                }
                case "delete" -> {
                    expansion.removePlaceholderValue(key);
                    sender.sendMessage(this.messages.getMessage(Messages.REMOVED_PLACEHOLDER).
                            replace("%id%", identifier).
                            replace("%key%", key).
                            replace("%value%", value));
                }
                case "add" -> this.setValue(key, sender, args, expansion, "add");
                case "remove" -> this.setValue(key, sender, args, expansion, "remove");

                default -> sender.sendMessage(this.invalidUsage);
            }
        }

        this.plugin.addExpansion(expansion);
        expansion.save();

        return true;
    }

    private void setValue(final String key,
                          final CommandSender sender,
                          final String[] args,
                          final CustomPlaceholderExpansion expansion,
                          final String type) {

        final String possibleNumber = expansion.getPlaceholderValue(key);

        if (!this.isNumber(sender, possibleNumber, possibleNumber)) {
            return;
        }

        final int numValue = Integer.parseInt(possibleNumber);

        int add = 1;
        if (args.length == 4) {

            if (!this.isNumber(sender, args[3], args[3])) {
                return ;
            }

            add = Integer.parseInt(args[3]);
        }
        switch (type.toLowerCase()) {
            case "add" -> {
                expansion.addPlaceholderValue(key, String.valueOf(numValue + add));
                sender.sendMessage(this.messages.getMessage(
                        Messages.ADDED_TO_PLACEHOLDER).
                        replace("%amount%", String.valueOf(add)).
                        replace("%id%", expansion.getIdentifier()).
                        replace("%key%", key));
            }
            case "remove" -> {
                expansion.addPlaceholderValue(key, String.valueOf(numValue - add));
                sender.sendMessage(this.messages.getMessage(
                                Messages.REMOVED_FROM_PLACEHOLDER).
                        replace("%amount%", String.valueOf(add)).
                        replace("%id%", expansion.getIdentifier()).
                        replace("%key%", key));
            }
        }
    }

    private boolean isNumber(final CommandSender sender, final String string, final String value) {
        if (!NumberUtils.isNumber(string)) {
            sender.sendMessage(this.messages.getMessage(Messages.NOT_NUMBER).
                    replace("%value%", value));
            return false;
        }
        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull final CommandSender sender,
                                      @NotNull final Command command,
                                      @NotNull final String alias,
                                      @NotNull final String[] args) {

        final List<String> allowed = new ArrayList<>();


        if (args.length == 1) {
            final Set<String> possible = this.plugin.getAllIdentifiers();
            final String arg = args[0];
            for (final String string : possible) {
                if (string.startsWith(arg.toLowerCase(Locale.ROOT))) {
                    allowed.add(string);
                }
            }
        }

        if (args.length == 2) {
            final List<String> tabs = List.of("set", "delete", "add", "remove");
            final String arg = args[1];
            for (final String string : tabs) {
                if (string.startsWith(arg.toLowerCase(Locale.ROOT))) {
                    allowed.add(string);
                }
            }
        }

        if (args.length == 3) {

            final String identifier = args[0];

            final String arg = args[2];

            final CustomPlaceholderExpansion expansion = this.plugin.getExpansion(identifier);

            if (expansion == null) {
                return allowed;
            }

            for (final String string : expansion.entrySet().keySet()) {
                if (string.startsWith(arg.toLowerCase(Locale.ROOT))) {
                    allowed.add(string);
                }
            }
        }

        return allowed;
    }
}
