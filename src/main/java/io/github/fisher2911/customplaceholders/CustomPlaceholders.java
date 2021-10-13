package io.github.fisher2911.customplaceholders;

import io.github.fisher2911.customplaceholders.command.PlaceholderCommand;
import io.github.fisher2911.customplaceholders.messages.Messages;
import io.github.fisher2911.customplaceholders.placeholderapi.CustomPlaceholderExpansion;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.FileFilter;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CustomPlaceholders extends JavaPlugin {

    public static final String PERMISSION = "customplaceholders.command";
    private Messages messages;
    private BukkitTask task;

    private final Map<String, CustomPlaceholderExpansion> placeholderExpansions = new HashMap<>();

    @Override
    public void onEnable() {
        this.messages = new Messages(this);
        this.load();
        startSaveTask();
        this.getCommand("customplaceholder").setExecutor(new PlaceholderCommand(this));
    }

    @Override
    public void onDisable() {
        this.task.cancel();
        this.saveAll();
    }

    private void startSaveTask() {
        this.saveDefaultConfig();
        final FileConfiguration config = this.getConfig();
        final long saveInterval = config.getLong("save-interval");
        this.task = Bukkit.getScheduler().runTaskTimer(this, this::saveAll,
        saveInterval * 20, saveInterval * 20);
    }

    public void saveAll() {
        this.placeholderExpansions.values().forEach(CustomPlaceholderExpansion::save);
    }

    public CustomPlaceholderExpansion getExpansion(final String id) {
        return this.placeholderExpansions.get(id);
    }

    public Set<String> getAllIdentifiers() {
        return this.placeholderExpansions.keySet();
    }

    public CustomPlaceholderExpansion getOrNewExpansion(final String id) {
        CustomPlaceholderExpansion expansion = this.getExpansion(id);

        if (expansion == null) {
            expansion = new CustomPlaceholderExpansion(this, id, new HashMap<>());
            this.addExpansion(expansion);
        }

        return expansion;
    }

    private void load() {
        final File folder = Path.of(
                this.getDataFolder().getPath(), "placeholders"
        ).toFile();

        if (!folder.exists()) {
            return;
        }

        final File[] files = folder.listFiles();

        if (files == null) {
            return;
        }

        for (final File file : files) {

            if (!file.exists()) {
                continue;
            }

            final Map<String, String> values = new HashMap<>();
            final CustomPlaceholderExpansion expansion =
                    new CustomPlaceholderExpansion(
                            this,
                            file.getName().replace(".yml", ""),
                            values
                    );

            final YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

            for (final String key : config.getKeys(false)) {
                values.put(key, config.getString(key));
            }

            this.addExpansion(expansion);
        }
        this.messages.load();
    }

    public void registerExpansion(final CustomPlaceholderExpansion expansion) {
        if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            expansion.register();
        }
    }

    public void addExpansion(final CustomPlaceholderExpansion expansion) {
        this.placeholderExpansions.put(expansion.getIdentifier(), expansion);
        this.registerExpansion(expansion);
    }

    public Messages getMessages() {
        return messages;
    }
}
