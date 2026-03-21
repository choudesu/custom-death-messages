package com.github.choudesu.customdeathmessages;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ComboTracker {

    private final CustomDeathMessages plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final Map<UUID, ComboEntry> activeCombo = new HashMap<>();

    public ComboTracker(CustomDeathMessages plugin) {
        this.plugin = plugin;
    }

    /**
     * Records a death and returns the new combo count.
     * Count == 1 means a fresh streak (no milestone announced yet).
     * Count >= 2 means the caller should announce a milestone.
     */
    public int recordDeath(Player victim, String killerId, Component killerName) {
        ComboEntry existing = activeCombo.get(victim.getUniqueId());

        if (existing != null) {
            existing.expiryTask.cancel();

            if (existing.killerId.equals(killerId)) {
                // Same killer — extend streak
                existing.count++;
                existing.killerName = killerName;
                existing.expiryTask = scheduleExpiry(victim, existing);
                return existing.count;
            } else {
                // Different killer — silently reset
                activeCombo.remove(victim.getUniqueId());
            }
        }

        // Start a fresh streak
        ComboEntry entry = new ComboEntry(killerId, killerName);
        entry.expiryTask = scheduleExpiry(victim, entry);
        activeCombo.put(victim.getUniqueId(), entry);
        return 1;
    }

    private BukkitTask scheduleExpiry(Player victim, ComboEntry entry) {
        long windowTicks = plugin.getConfigManager().getComboConfig().windowTicks();
        return plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            ComboEntry current = activeCombo.remove(victim.getUniqueId());
            if (current == null || current.count < 2) return;

            ConfigManager.ComboConfig cfg = plugin.getConfigManager().getComboConfig();
            Component playerName = resolvePlayerName(victim);

            // Server-wide combo-breaker broadcast
            Component breakerMsg = miniMessage.deserialize(
                    cfg.breakerBroadcast(),
                    TagResolver.builder()
                            .resolver(Placeholder.component("player", playerName))
                            .resolver(Placeholder.unparsed("combo", String.valueOf(current.count)))
                            .build()
            );
            plugin.getServer().broadcast(breakerMsg);

            // Private congrats to the victim (only if still online)
            if (victim.isOnline()) {
                Component privateMsg = miniMessage.deserialize(
                        cfg.breakerPrivate(),
                        Placeholder.unparsed("combo", String.valueOf(current.count))
                );
                victim.sendMessage(privateMsg);
            }
        }, windowTicks);
    }

    private Component resolvePlayerName(Player player) {
        if (plugin.getConfigManager().isUseEssentialsNicknames()
                && plugin.getEssentialsHook().isAvailable()) {
            return plugin.getEssentialsHook().getDisplayName(player);
        }
        return player.displayName();
    }

    /**
     * Returns a stable killer ID for combo matching.
     * Players use their UUID; mobs use their entity type name.
     */
    public static String resolveKillerId(Entity damager) {
        if (damager instanceof Player p) {
            return p.getUniqueId().toString();
        }
        return damager.getType().name();
    }

    /** Cancels all pending expiry tasks — call on plugin disable. */
    public void clearAll() {
        activeCombo.values().forEach(e -> e.expiryTask.cancel());
        activeCombo.clear();
    }

    // ── Inner class ───────────────────────────────────────────────────────────

    private static class ComboEntry {
        final String killerId;
        Component killerName;
        int count = 1;
        BukkitTask expiryTask;

        ComboEntry(String killerId, Component killerName) {
            this.killerId = killerId;
            this.killerName = killerName;
        }
    }
}
