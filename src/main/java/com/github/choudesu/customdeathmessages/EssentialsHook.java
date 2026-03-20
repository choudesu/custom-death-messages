package com.github.choudesu.customdeathmessages;

import com.earth2me.essentials.Essentials;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class EssentialsHook {

    private Essentials essentials;

    public EssentialsHook(CustomDeathMessages plugin) {
        Plugin ess = plugin.getServer().getPluginManager().getPlugin("Essentials");
        if (ess instanceof Essentials) {
            this.essentials = (Essentials) ess;
        }
    }

    public boolean isAvailable() {
        return essentials != null;
    }

    /**
     * Returns the player's Essentials nickname as a Component (with colour codes resolved),
     * falling back to the player's display name if Essentials is unavailable or no nick is set.
     */
    public Component getDisplayName(Player player) {
        if (essentials != null) {
            try {
                com.earth2me.essentials.User user = essentials.getUser(player);
                if (user != null) {
                    String nick = user.getNickname();
                    if (nick != null && !nick.isEmpty()) {
                        // Nicknames stored by Essentials use legacy '§' colour codes.
                        return LegacyComponentSerializer.legacySection().deserialize(nick);
                    }
                }
            } catch (Exception ignored) {
                // If anything goes wrong, fall through to the default.
            }
        }
        return player.displayName();
    }
}
