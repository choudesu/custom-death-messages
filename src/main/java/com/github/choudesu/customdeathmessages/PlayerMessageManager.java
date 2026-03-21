package com.github.choudesu.customdeathmessages;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerMessageManager {

    private final CustomDeathMessages plugin;
    private final File file;
    private final Map<UUID, String> messages = new HashMap<>();

    public PlayerMessageManager(CustomDeathMessages plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "player_messages.yml");
        load();
    }

    private void load() {
        messages.clear();
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        for (String key : config.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                String message = config.getString(key);
                if (message != null) {
                    messages.put(uuid, message);
                }
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Skipping malformed UUID key in player_messages.yml: '" + key + "'");
            }
        }
    }

    private void save() {
        YamlConfiguration config = new YamlConfiguration();
        messages.forEach((uuid, message) -> config.set(uuid.toString(), message));
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save player_messages.yml: " + e.getMessage());
        }
    }

    public String getCustomMessage(UUID playerUuid) {
        return messages.get(playerUuid);
    }

    public void setCustomMessage(UUID playerUuid, String message) {
        messages.put(playerUuid, message);
        save();
    }

    public void clearCustomMessage(UUID playerUuid) {
        messages.remove(playerUuid);
        save();
    }

    public void reload() {
        load();
    }
}
