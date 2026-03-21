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
    private ComboTracker comboTracker;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.configManager = new ConfigManager(this);
        this.essentialsHook = new EssentialsHook(this);
        this.comboTracker = new ComboTracker(this);

        getServer().getPluginManager().registerEvents(new DeathMessageListener(this), this);

        var cmd = getCommand("customdeathmessages");
        if (cmd != null) {
            cmd.setExecutor(this);
            cmd.setTabCompleter(this);
        }

        getLogger().info("  ____          _                   ____             _   _     ");
        getLogger().info(" / ___|   _ ___| |_ ___  _ __ ___  |  _ \\  ___  __ _| |_| |__  ");
        getLogger().info("| |  | | | / __| __/ _ \\| '_ ` _ \\ | | | |/ _ \\/ _` | __| '_ \\ ");
        getLogger().info("| |__| |_| \\__ \\ || (_) | | | | | || |_| |  __/ (_| | |_| | | |");
        getLogger().info(" \\____\\__,_|___/\\__\\___/|_| |_| |_||____/ \\___|\\__,_|\\__|_| |_|");
        getLogger().info("  __  __                                                         ");
        getLogger().info(" |  \\/  | ___  ___ ___  __ _  __ _  ___  ___                   ");
        getLogger().info(" | |\\/| |/ _ \\/ __/ __|/ _` |/ _` |/ _ \\/ __|                  ");
        getLogger().info(" | |  | |  __/\\__ \\__ \\ (_| | (_| |  __/\\__ \\                  ");
        getLogger().info(" |_|  |_|\\___||___/___/\\__,_|\\__, |\\___||___/                  ");
        getLogger().info("                              |___/                              ");
        getLogger().info("CustomDeathMessages enabled. Essentials integration: "
                + (essentialsHook.isAvailable() ? "active" : "not available"));
    }

    @Override
    public void onDisable() {
        if (comboTracker != null) comboTracker.clearAll();
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

    public ComboTracker getComboTracker() {
        return comboTracker;
    }
}
