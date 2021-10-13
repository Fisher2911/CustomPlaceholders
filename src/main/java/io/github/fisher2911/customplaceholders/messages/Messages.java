package io.github.fisher2911.customplaceholders.messages;

import io.github.fisher2911.customplaceholders.CustomPlaceholders;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class Messages {

    public static final String PREFIX = "prefix";
    public static final String NO_PERMISSION = "no-permission";
    public static final String INVALID_COMMAND_USAGE = "invalid-command-usage";
    public static final String ADDED_PLACEHOLDER = "added-placeholder";
    public static final String REMOVED_PLACEHOLDER = "removed-placeholder";
    public static final String NOT_NUMBER = "not-number";
    public static final String ADDED_TO_PLACEHOLDER = "added-to-placeholder";
    public static final String REMOVED_FROM_PLACEHOLDER = "removed-from-placeholder";

    private final CustomPlaceholders plugin;

    public Messages(final CustomPlaceholders plugin) {
        this.plugin = plugin;
    }

    private String prefix = "";
    private final Map<String, String> messages = new HashMap<>();

    public String getMessage(final String key) {
        return this.messages.getOrDefault(key, "").replace("%prefix%", this.prefix);
    }

    public void load() {
        this.plugin.saveDefaultConfig();
        final File file = new File(this.plugin.getDataFolder(), "messages.yml");

        if (!file.exists()) {
            this.plugin.saveResource("messages.yml", false);
        }

        final YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        this.prefix = config.getString(PREFIX);

        for (final String key : config.getKeys(false)) {
            if (key.equals(prefix)) {
                continue;
            }
            this.messages.put(key,
                    ChatColor.translateAlternateColorCodes('&',
                            config.getString(key).replace("%prefix%", this.prefix)));
        }
    }



}
