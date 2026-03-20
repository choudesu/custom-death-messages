package com.github.choudesu.customdeathmessages;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class CustomDeathMessages extends JavaPlugin implements CommandExecutor, TabCompleter {

    private ConfigManager configManager;
    private EssentialsHook essentialsHook;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.configManager = new ConfigManager(this);
        this.essentialsHook = new EssentialsHook(this);

        getServer().getPluginManager().registerEvents(new DeathMessageListener(this), this);

        var cmd = getCommand("customdeathmessages");
        if (cmd != null) {
            cmd.setExecutor(this);
            cmd.setTabCompleter(this);
        }

        getLogger().info("CustomDeathMessages enabled. Essentials integration: "
                + (essentialsHook.isAvailable() ? "active" : "not available"));
    }

    @Override
    public void onDisable() {
        getLogger().info("CustomDeathMessages disabled.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            reloadConfig();
            configManager.reload();
            sender.sendMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage()
                    .deserialize("<green>CustomDeathMessages config reloaded."));
            return true;
        }
        sender.sendMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage()
                .deserialize("<red>Usage: /" + label + " reload"));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return List.of("reload");
        }
        return List.of();
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public EssentialsHook getEssentialsHook() {
        return essentialsHook;
    }
}
