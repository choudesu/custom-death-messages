package com.github.choudesu.customdeathmessages;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

public class ConfigManager {

    public record ComboConfig(
            boolean enabled,
            long windowTicks,
            TreeMap<Integer, String> milestones,
            String milestoneBroadcast,
            String breakerBroadcast,
            String breakerPrivate) {}

    private final CustomDeathMessages plugin;
    private final Random random = new Random();

    private final Map<DamageCause, List<String>> messages = new EnumMap<>(DamageCause.class);
    private List<String> defaultMessages = new ArrayList<>();
    private boolean broadcastGlobally;
    private boolean useEssentialsNicknames;
    private ComboConfig comboConfig;

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

        loadComboConfig();

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

    public ComboConfig getComboConfig() {
        return comboConfig;
    }

    /** Returns the milestone label for the given combo count, clamping to the highest defined entry. */
    public String getMilestoneLabel(int count) {
        TreeMap<Integer, String> milestones = comboConfig.milestones();
        if (milestones.isEmpty()) return "Multi Kill";
        Map.Entry<Integer, String> entry = milestones.floorEntry(count);
        if (entry == null) return milestones.firstEntry().getValue();
        return entry.getValue();
    }

    private void loadComboConfig() {
        var config = plugin.getConfig();
        ConfigurationSection combo = config.getConfigurationSection("combo");
        if (combo == null) {
            comboConfig = new ComboConfig(false, 0, new TreeMap<>(), "", "", "");
            return;
        }

        boolean enabled = combo.getBoolean("enabled", true);
        long windowTicks = combo.getLong("window-seconds", 120) * 20L;

        TreeMap<Integer, String> milestones = new TreeMap<>();
        ConfigurationSection ms = combo.getConfigurationSection("milestones");
        if (ms != null) {
            for (String key : ms.getKeys(false)) {
                try {
                    milestones.put(Integer.parseInt(key), ms.getString(key, key));
                } catch (NumberFormatException ignored) {
                    plugin.getLogger().warning("Invalid milestone key in config: '" + key + "' — skipping.");
                }
            }
        }

        String milestoneBroadcast = combo.getString("milestone-broadcast",
                "<death_message> <dark_gray>—</dark_gray> <milestone> <gray>(<combo>x)</gray>");
        String breakerBroadcast = combo.getString("breaker-broadcast",
                "<gray><aqua><player></aqua> survived a <bold><combo>x</bold> death streak!</gray>");
        String breakerPrivate = combo.getString("breaker-private",
                "<green>You survived a <bold><combo>x</bold> death streak! Well done.</green>");

        comboConfig = new ComboConfig(enabled, windowTicks, milestones, milestoneBroadcast, breakerBroadcast, breakerPrivate);
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
