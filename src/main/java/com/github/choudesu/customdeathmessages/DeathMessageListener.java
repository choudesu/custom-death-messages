package com.github.choudesu.customdeathmessages;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.projectiles.ProjectileSource;

public class DeathMessageListener implements Listener {

    private final CustomDeathMessages plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public DeathMessageListener(CustomDeathMessages plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        // If the original message is already null, another plugin suppressed it — respect that.
        if (event.deathMessage() == null) {
            return;
        }

        Player victim = event.getPlayer();
        var damage = victim.getLastDamageCause();
        var cause = damage != null
                ? damage.getCause()
                : org.bukkit.event.entity.EntityDamageEvent.DamageCause.CUSTOM;

        String template = plugin.getConfigManager().getRandomMessage(cause);

        // Empty list configured → suppress the death message.
        if (template == null) {
            event.deathMessage(null);
            return;
        }

        // Resolve {player} display name
        Component playerName = resolvePlayerName(victim);

        // Resolve {killer} — could be a player, mob, or "Unknown"
        Component killerName = resolveKillerName(victim);

        // Build the component from the MiniMessage template with named placeholders.
        Component message = miniMessage.deserialize(
                template,
                TagResolver.builder()
                        .resolver(Placeholder.component("player", playerName))
                        .resolver(Placeholder.component("killer", killerName))
                        .resolver(Placeholder.unparsed("world", victim.getWorld().getName()))
                        .build()
        );

        // Resolve combo count first so we can decide which message to send.
        int comboCount = 0;
        if (plugin.getConfigManager().getComboConfig().enabled()) {
            Entity rawDamager = resolveRawDamager(victim);
            if (rawDamager != null) {
                String killerId = ComboTracker.resolveKillerId(rawDamager);
                comboCount = plugin.getComboTracker().recordDeath(victim, killerId, killerName);
            }
        }

        if (comboCount >= 2) {
            // Milestone broadcast replaces the regular death message entirely.
            event.deathMessage(null);
            String milestone = plugin.getConfigManager().getMilestoneLabel(comboCount);
            broadcastMilestone(message, milestone, comboCount);
        } else if (plugin.getConfigManager().isBroadcastGlobally()) {
            event.deathMessage(message);
        } else {
            // Suppress the global broadcast and manually send to the victim's world only.
            event.deathMessage(null);
            for (Player p : victim.getWorld().getPlayers()) {
                p.sendMessage(message);
            }
            victim.getServer().getConsoleSender().sendMessage(message);
        }
    }

    // ── Combo helpers ─────────────────────────────────────────────────────────

    /** Returns the raw killing entity (unwrapping projectiles), or null if non-entity damage. */
    private Entity resolveRawDamager(Player victim) {
        var damage = victim.getLastDamageCause();
        if (!(damage instanceof org.bukkit.event.entity.EntityDamageByEntityEvent ede)) return null;
        Entity damager = ede.getDamager();
        if (damager instanceof Projectile projectile) {
            ProjectileSource source = projectile.getShooter();
            if (source instanceof Entity shooter) {
                damager = shooter;
            }
        }
        return damager;
    }

    private void broadcastMilestone(Component deathMessage, String milestone, int count) {
        String template = plugin.getConfigManager().getComboConfig().milestoneBroadcast();
        Component msg = miniMessage.deserialize(
                template,
                TagResolver.builder()
                        .resolver(Placeholder.component("death_message", deathMessage))
                        .resolver(Placeholder.component("milestone", miniMessage.deserialize(milestone)))
                        .resolver(Placeholder.unparsed("combo", String.valueOf(count)))
                        .build()
        );
        plugin.getServer().broadcast(msg);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Component resolvePlayerName(Player player) {
        if (plugin.getConfigManager().isUseEssentialsNicknames()
                && plugin.getEssentialsHook().isAvailable()) {
            return plugin.getEssentialsHook().getDisplayName(player);
        }
        return player.displayName();
    }

    private Component resolveKillerName(Player victim) {
        var damage = victim.getLastDamageCause();
        if (damage == null) {
            return Component.text("Unknown");
        }

        // Direct entity killer
        if (damage instanceof org.bukkit.event.entity.EntityDamageByEntityEvent ede) {
            Entity damager = ede.getDamager();

            // Unwrap projectiles to find the shooter
            if (damager instanceof Projectile projectile) {
                ProjectileSource source = projectile.getShooter();
                if (source instanceof Entity shooter) {
                    damager = shooter;
                }
            }

            if (damager instanceof Player killerPlayer) {
                return resolvePlayerName(killerPlayer);
            }

            // Non-player entity — use their custom name or type name
            Component customName = damager.customName();
            if (customName != null) {
                return customName;
            }
            // Pretty-print the entity type (e.g. ZOMBIE → Zombie)
            String typeName = damager.getType().name()
                    .replace('_', ' ')
                    .toLowerCase();
            typeName = Character.toUpperCase(typeName.charAt(0)) + typeName.substring(1);
            String article = "aeiouAEIOU".indexOf(typeName.charAt(0)) >= 0 ? "an" : "a";
            return Component.text(article + " " + typeName);
        }

        return Component.text("Unknown");
    }
}
