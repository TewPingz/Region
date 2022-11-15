package me.tewpingz.region.command;

import lombok.RequiredArgsConstructor;
import me.tewpingz.region.RegionPlugin;
import me.tewpingz.region.profile.RegionProfile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

@RequiredArgsConstructor
public class RegionCommand implements CommandExecutor {

    private final RegionPlugin regionPlugin;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            this.handleRootCommand(sender, command, label, args);
            return true;
        }

        String argument = args[0];
        String[] shortArgs = args.length > 1 ? Arrays.copyOfRange(args, 1, args.length) : new String[0];

        switch (argument.toLowerCase()) {
            case "create" -> this.handleCreateCommand(sender, command, label, shortArgs);
            case "wand" -> this.handleWandCommand(sender, command, label, shortArgs);
        }

        return false;
    }

    private void handleRootCommand(CommandSender sender, Command command, String label, String[] args) {

    }

    private void handleCreateCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return;
        }
        Player player = (Player) sender;

        if (args.length == 0) {
            sender.sendMessage(Component.text("Usage: /%s create <name>").color(NamedTextColor.RED));
            return;
        }

        String name = args[0];

        if (name.length() >= 16) {
            sender.sendMessage(Component.text("That name is too long").color(NamedTextColor.RED));
            return;
        }

        RegionProfile profile = this.regionPlugin.getRegionProfileManager().getProfile(player.getUniqueId());

        if (!profile.isCuboidSelected()) {
            player.sendMessage(Component.text("Invalid cuboid selection, please use /cuboid wand and make a valid selection!").color(NamedTextColor.RED));
            return;
        }

        this.regionPlugin.getRegionManager().createRegion(name, profile.toRegionCuboid());
    }

    private void handleWandCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return;
        }
        Player player = (Player) sender;
        player.getInventory().addItem(this.regionPlugin.getRegionManager().getSelectionWand());
    }
}
