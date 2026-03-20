package com.github.choudesu.customdeathmessages;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ConfigManager {

    private final CustomDeathMessages plugin;
    private final Random random = new Random();

    private final Map<DamageCause, List<String>> messages = new EnumMap<>(DamageCause.class);
    private List<String> defaultMessages = new ArrayList<>();
    private boolean broadcastGlobally;
    private boolean useEssentialsNicknames;

    public ConfigManager(CustomDeathMessages plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        messages.clear();

        var config = plugin.getConfig();

        broadcastGlobally = config.getBoolean("settings.broadcast-globally", true);
        useEssentialsNicknames = config.getBoolean("settings.use-essentials-nicknames", true);

        ConfigurationSection section = config.getConfigurationSection("messages");
        if (section == null) {
            plugin.getLogger().warning("No 'messages' section found in config.yml!");
            return;
        }

        for (String key : section.getKeys(false)) {
            List<String> list = getStringList(section, key);

            if (key.equalsIgnoreCase("default")) {
                defaultMessages = list;
                continue;
            }

            try {
                DamageCause cause = DamageCause.valueOf(key.toUpperCase());
                messages.put(cause, list);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Unknown damage cause in config: '" + key + "' — skipping.");
            }
        }
    }

    /** Returns a random message template for the given cause, or from the default list. */
    public String getRandomMessage(DamageCause cause) {
        List<String> list = messages.getOrDefault(cause, defaultMessages);
        if (list == null || list.isEmpty()) {
            list = defaultMessages;
        }
        if (list == null || list.isEmpty()) {
            return null; // suppress message
        }
        return list.get(random.nextInt(list.size()));
    }

    public boolean isBroadcastGlobally() {
        return broadcastGlobally;
    }

    public boolean isUseEssentialsNicknames() {
        return useEssentialsNicknames;
    }

    // Accepts both a plain string and a list under the same key.
    private List<String> getStringList(ConfigurationSection section, String key) {
        if (section.isList(key)) {
            return section.getStringList(key);
        }
        String value = section.getString(key);
        if (value == null) return new ArrayList<>();
        List<String> result = new ArrayList<>();
        result.add(value);
        return result;
    }
}
