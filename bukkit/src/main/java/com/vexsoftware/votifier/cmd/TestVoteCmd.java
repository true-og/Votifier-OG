package com.vexsoftware.votifier.cmd;

import com.vexsoftware.votifier.NuVotifierBukkit;
import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.net.VotifierSession;
import com.vexsoftware.votifier.util.ArgsToVote;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class TestVoteCmd implements CommandExecutor {

    private final NuVotifierBukkit plugin;

    public TestVoteCmd(NuVotifierBukkit plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender.hasPermission("nuvotifier.testvote")) {
            Vote v;
            try {
                v = ArgsToVote.parse(args);
            } catch (IllegalArgumentException e) {
                sender.sendMessage(Component.text("Error while parsing arguments to create test vote: " + e.getMessage()).color(NamedTextColor.DARK_RED));
                sender.sendMessage(Component.text("Usage hint: /testvote [username] [serviceName=?] [username=?] [address=?] [localTimestamp=?] [timestamp=?]").color(NamedTextColor.GRAY));
                return true;
            }

            plugin.onVoteReceived(v, VotifierSession.ProtocolVersion.TEST, "localhost.test");
            sender.sendMessage(Component.text("Test vote executed: " + v.toString()).color(NamedTextColor.GREEN));
        } else {
            sender.sendMessage(Component.text("You do not have permission to do this!").color(NamedTextColor.DARK_RED));
        }
        return true;
    }
}
