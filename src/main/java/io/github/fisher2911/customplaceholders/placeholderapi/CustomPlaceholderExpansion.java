package io.github.fisher2911.customplaceholders.placeholderapi;

import io.github.fisher2911.customplaceholders.CustomPlaceholders;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CustomPlaceholderExpansion extends PlaceholderExpansion {

    private boolean registered;
    private final CustomPlaceholders plugin;
    private final String identifier;
    private final Map<String, String> values;
    private final Set<String> removed = new HashSet<>();
    private static final ExecutorService threads = Executors.newSingleThreadExecutor();

    public CustomPlaceholderExpansion(final CustomPlaceholders plugin,
                                      final String identifier,
                                      final Map<String, String> values) {
        this.plugin = plugin;
        this.identifier = identifier;
        this.values = values;
    }

    public Map<String, String> entrySet() {
        return this.values;
    }

    @Override
    public boolean register() {
        if (registered) {
            return false;
        }

        this.registered = super.register();
        return this.registered;
    }

    public void addPlaceholderValue(final String key, final String value) {
        this.values.put(key, value);
    }

    public void removePlaceholderValue(final String key) {
        this.values.remove(key);
        this.removed.add(key);
    }

    public String getPlaceholderValue(final String key) {
        return this.values.getOrDefault(key, "");
    }

    /**
     * Because this is an internal class,
     * you must override this method to let PlaceholderAPI know to not unregister your expansion class when
     * PlaceholderAPI is reloaded
     *
     * @return true to persist through reloads
     */
    @Override
    public boolean persist(){
        return true;
    }

    /**
     * Because this is a internal class, this check is not needed
     * and we can simply return {@code true}
     *
     * @return Always true since it's an internal class.
     */
    @Override
    public boolean canRegister(){
        return true;
    }

    /**
     * The name of the person who created this expansion should go here.
     * <br>For convenience do we return the author from the plugin.yml
     *
     * @return The name of the author as a String.
     */
    @Override
    public @NotNull String getAuthor(){
        return plugin.getDescription().getAuthors().toString();
    }

    /**
     * The placeholder identifier should go here.
     * <br>This is what tells PlaceholderAPI to call our onRequest
     * method to obtain a value if a placeholder starts with our
     * identifier.
     * <br>The identifier has to be lowercase and can't contain _ or %
     *
     * @return The identifier in {@code %<identifier>_<value>%} as String.
     */
    @Override
    public @NotNull String getIdentifier(){
        return this.identifier;
    }

    /**
     * This is the version of the expansion.
     * <br>You don't have to use numbers, since it is set as a String.
     *
     * For convienience do we return the version from the plugin.yml
     *
     * @return The version as a String.
     */
    @Override
    public @NotNull String getVersion(){
        return plugin.getDescription().getVersion();
    }

    /**
     * This is the method called when a placeholder with our identifier
     * is found and needs a value.
     * <br>We specify the value identifier in this method.
     * <br>Since version 2.9.1 can you use OfflinePlayers in your requests.
     *
     * @param  player
     *         A {@link org.bukkit.Player Player}.
     * @param  identifier
     *         A String containing the identifier/value.
     *
     * @return possibly-null String of the requested identifier.
     */
    @Override
    public String onPlaceholderRequest(Player player, @NotNull String identifier){
        if (player == null) {
            return "";
        }

        return this.values.get(identifier);

    }

    public void close() {
        this.threads.shutdown();
    }

    public void save() {
        final Map<String, String> currentCopy = new HashMap<>(this.values);
        final Set<String> removedCopy = new HashSet<>(this.removed);

        threads.submit(() -> {
            final File file = Path.of(this.plugin.getDataFolder().getPath(),
                    "placeholders",
                    this.identifier + ".yml").toFile();

            if (!file.exists()) {
                Path.of(this.plugin.getDataFolder().getPath(), "placeholders").toFile().mkdirs();
                try {
                    file.createNewFile();
                } catch (final IOException exception) {
                    exception.printStackTrace();
                }
            }

            final YamlConfiguration config = YamlConfiguration.
                    loadConfiguration(file);

            if (currentCopy.isEmpty()) {
                file.delete();
                return;
            }

            for (final String string : removedCopy) {
                config.set(string, null);
            }

            for (final Map.Entry<String, String> entry : currentCopy.entrySet()) {
                config.set(entry.getKey(), entry.getValue());
            }
            removed.clear();

            try {
                config.save(file);
            } catch (final IOException exception) {
                exception.printStackTrace();
            }
        });
    }

}