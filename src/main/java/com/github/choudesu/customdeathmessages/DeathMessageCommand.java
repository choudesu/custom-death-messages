package com.github.choudesu.customdeathmessages;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DeathMessageCommand implements CommandExecutor, TabCompleter {

    private final CustomDeathMessages plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public DeathMessageCommand(CustomDeathMessages plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(miniMessage.deserialize("<red>Usage: /" + label + " <set [player] <message>|clear [player]>"));
            return true;
        }

        if (args[0].equalsIgnoreCase("set")) {
            // Admin path: /deathmessage set <player> <message...>
            if (args.length >= 3
                    && sender.hasPermission("customdeathmessages.deathmessage.admin")
                    && plugin.getServer().getPlayerExact(args[1]) != null) {
                Player target = plugin.getServer().getPlayerExact(args[1]);
                String message = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                plugin.getPlayerMessageManager().setCustomMessage(target.getUniqueId(), message);
                sender.sendMessage(miniMessage.deserialize("<green>Set custom death message for " + target.getName() + "."));
                return true;
            }

            // Self path: /deathmessage set <message...>
            if (!(sender instanceof Player player)) {
                sender.sendMessage(miniMessage.deserialize("<red>Usage: /" + label + " set <player> <message>"));
                return true;
            }
            if (!player.hasPermission("customdeathmessages.deathmessage.set")) {
                player.sendMessage(miniMessage.deserialize("<red>You do not have permission to do that."));
                return true;
            }
            if (args.length < 2) {
                player.sendMessage(miniMessage.deserialize("<red>Usage: /" + label + " set <message>"));
                return true;
            }
            String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
            plugin.getPlayerMessageManager().setCustomMessage(player.getUniqueId(), message);
            player.sendMessage(miniMessage.deserialize("<green>Your custom death message has been set."));
            player.sendMessage(Component.text("Tip: use <player>, <killer>, <world> as placeholders.", NamedTextColor.GRAY));
            return true;
        }

        if (args[0].equalsIgnoreCase("clear")) {
            // Admin path: /deathmessage clear <player>
            if (args.length >= 2) {
                if (!sender.hasPermission("customdeathmessages.deathmessage.admin")) {
                    sender.sendMessage(miniMessage.deserialize("<red>You do not have permission to do that."));
                    return true;
                }
                Player target = plugin.getServer().getPlayerExact(args[1]);
                if (target == null) {
                    sender.sendMessage(miniMessage.deserialize("<red>Player not found: " + args[1]));
                    return true;
                }
                plugin.getPlayerMessageManager().clearCustomMessage(target.getUniqueId());
                sender.sendMessage(miniMessage.deserialize("<green>Cleared custom death message for " + target.getName() + "."));
                return true;
            }

            // Self path: /deathmessage clear
            if (!(sender instanceof Player player)) {
                sender.sendMessage(miniMessage.deserialize("<red>Usage: /" + label + " clear <player>"));
                return true;
            }
            if (!player.hasPermission("customdeathmessages.deathmessage.clear")) {
                player.sendMessage(miniMessage.deserialize("<red>You do not have permission to do that."));
                return true;
            }
            plugin.getPlayerMessageManager().clearCustomMessage(player.getUniqueId());
            player.sendMessage(miniMessage.deserialize("<green>Your custom death message has been cleared."));
            return true;
        }

        sender.sendMessage(miniMessage.deserialize("<red>Usage: /" + label + " <set [player] <message>|clear [player]>"));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return List.of("set", "clear").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length == 2 && sender.hasPermission("customdeathmessages.deathmessage.admin")) {
            String prefix = args[1].toLowerCase();
            return plugin.getServer().getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(prefix))
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}
