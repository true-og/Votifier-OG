package com.vexsoftware.votifier.cmd;

import com.vexsoftware.votifier.NuVotifierBukkit;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class NVReloadCmd implements CommandExecutor {

    private final NuVotifierBukkit plugin;

    public NVReloadCmd(NuVotifierBukkit plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender.hasPermission("nuvotifier.reload")) {
            sender.sendMessage(Component.text("Reloading NuVotifier...").color(NamedTextColor.GRAY));
            if (plugin.reload()) {
                sender.sendMessage(Component.text("NuVotifier has been reloaded!").color(NamedTextColor.DARK_GREEN));
            } else {
                sender.sendMessage(Component.text("Looks like there was a problem reloading NuVotifier, check the console!").color(NamedTextColor.DARK_RED));
            }
        } else {
            sender.sendMessage(Component.text("You do not have permission to do this!").color(NamedTextColor.DARK_RED));
        }
        return true;
    }
}
