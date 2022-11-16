package me.tewpingz.region.command;

import lombok.RequiredArgsConstructor;
import me.tewpingz.region.RegionPlugin;
import me.tewpingz.region.menu.RegionMenu;
import me.tewpingz.region.menu.RegionsMenu;
import me.tewpingz.region.model.Region;
import me.tewpingz.region.profile.RegionProfile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
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
            case "add" -> this.handleAddCommand(sender, command, label, shortArgs);
            case "remove" -> this.handleRemoveCommand(sender, command, label, shortArgs);
            case "whitelist" -> this.handleWhitelistCommand(sender, command, label, shortArgs);
            default -> this.handleRegionMenuCommand(sender, command, label, argument, shortArgs);
        }

        return false;
    }

    private void handleRootCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Component.text("Console cannot execute this command").color(NamedTextColor.RED));
            return;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("region.menu")) {
            sender.sendMessage(Component.text("You do not have permission to perform this command!").color(NamedTextColor.RED));
            return;
        }

        new RegionsMenu().open(player);
    }

    private void handleRegionMenuCommand(CommandSender sender, Command command, String label, String argumentLabel, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Component.text("Console cannot execute this command").color(NamedTextColor.RED));
            return;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("region.menu")) {
            sender.sendMessage(Component.text("You do not have permission to perform this command!").color(NamedTextColor.RED));
            return;
        }

        Region region = this.regionPlugin.getRegionManager().getRegionByName(argumentLabel);

        if (region == null) {
            sender.sendMessage(Component.text("There is no region with that name").color(NamedTextColor.RED));
            return;
        }

        new RegionMenu(region).open(player);
    }

    private void handleCreateCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Component.text("Console cannot execute this command").color(NamedTextColor.RED));
            return;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("region.create")) {
            sender.sendMessage(Component.text("You do not have permission to perform this command!").color(NamedTextColor.RED));
            return;
        }

        if (args.length == 0) {
            sender.sendMessage(Component.text("Usage: /%s create <name>".formatted(label)).color(NamedTextColor.RED));
            return;
        }

        String name = args[0];

        if (name.length() >= 16) {
            sender.sendMessage(Component.text("That name is too long").color(NamedTextColor.RED));
            return;
        }

        if (this.regionPlugin.getRegionManager().getRegionByName(name) != null) {
            sender.sendMessage(Component.text("There is already a region with that name").color(NamedTextColor.RED));
            return;
        }

        RegionProfile profile = this.regionPlugin.getRegionProfileManager().getProfile(player.getUniqueId());

        if (!profile.isCuboidSelected()) {
            player.sendMessage(Component.text("Invalid cuboid selection, please use /cuboid wand and make a valid selection!").color(NamedTextColor.RED));
            return;
        }

        this.regionPlugin.getRegionManager().createRegion(name, profile.toRegionCuboid());
        player.sendMessage(Component.text("You have successfully created a region with the name %s".formatted(name)).color(NamedTextColor.GREEN));
        profile.setSelectionOne(null);
        profile.setSelectionTwo(null);
    }

    private void handleWandCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Component.text("Console cannot execute this command").color(NamedTextColor.RED));
            return;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("region.create")) {
            sender.sendMessage(Component.text("You do not have permission to perform this command!").color(NamedTextColor.RED));
            return;
        }

        if (player.getInventory().contains(this.regionPlugin.getRegionManager().getSelectionWand())) {
            sender.sendMessage(Component.text("You already have a selection wand!").color(NamedTextColor.RED));
            return;
        }

        player.getInventory().addItem(this.regionPlugin.getRegionManager().getSelectionWand());
        player.sendMessage(Component.text("You have been given the wand!").color(NamedTextColor.GOLD));
    }

    private void handleAddCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("region.add")) {
            sender.sendMessage(Component.text("You do not have permission to perform this command!").color(NamedTextColor.RED));
            return;
        }

        if (args.length <= 1) {
            sender.sendMessage(Component.text("Usage: /%s add <name> <player>".formatted(label)).color(NamedTextColor.RED));
            return;
        }

        String regionName = args[0];
        Region region = this.regionPlugin.getRegionManager().getRegionByName(regionName);

        if (region == null) {
            sender.sendMessage(Component.text("There is no region with that name").color(NamedTextColor.RED));
            return;
        }

        String playerName = args[1];
        OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(playerName);

        if (!targetPlayer.hasPlayedBefore()) {
            sender.sendMessage(Component.text("That player has not joined before!").color(NamedTextColor.RED));
            return;
        }

        if (region.isWhitelisted(targetPlayer.getUniqueId())) {
            sender.sendMessage(Component.text("That player is already whitelisted!").color(NamedTextColor.RED));
            return;
        }

        region.addToWhitelist(targetPlayer.getUniqueId());
        sender.sendMessage(Component.text("You have added %s to the whitelist!".formatted(targetPlayer.getName())).color(NamedTextColor.GREEN));
    }

    private void handleRemoveCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("region.remove")) {
            sender.sendMessage(Component.text("You do not have permission to perform this command!").color(NamedTextColor.RED));
            return;
        }

        if (args.length <= 1) {
            sender.sendMessage(Component.text("Usage: /%s remove <name> <player>".formatted(label)).color(NamedTextColor.RED));
            return;
        }

        String regionName = args[0];
        Region region = this.regionPlugin.getRegionManager().getRegionByName(regionName);

        if (region == null) {
            sender.sendMessage(Component.text("There is no region with that name").color(NamedTextColor.RED));
            return;
        }

        String playerName = args[1];
        OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(playerName);

        if (!targetPlayer.hasPlayedBefore()) {
            sender.sendMessage(Component.text("That player has not joined before!").color(NamedTextColor.RED));
            return;
        }

        if (!region.isWhitelisted(targetPlayer.getUniqueId())) {
            sender.sendMessage(Component.text("That player is not whitelisted!").color(NamedTextColor.RED));
            return;
        }

        region.removeFromWhitelist(targetPlayer.getUniqueId());
        sender.sendMessage(Component.text("You have removed %s from the whitelist!".formatted(targetPlayer.getName())).color(NamedTextColor.GREEN));
    }

    private void handleWhitelistCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("region.whitelist")) {
            sender.sendMessage(Component.text("You do not have permission to perform this command!").color(NamedTextColor.RED));
            return;
        }

        if (args.length == 0) {
            sender.sendMessage(Component.text("Usage: /%s whitelist <name>".formatted(label)).color(NamedTextColor.RED));
            return;
        }

        String regionName = args[0];
        Region region = this.regionPlugin.getRegionManager().getRegionByName(regionName);

        if (region == null) {
            sender.sendMessage(Component.text("There is no region with that name").color(NamedTextColor.RED));
            return;
        }

        if (region.getWhitelisted().isEmpty()) {
            sender.sendMessage(Component.text("There is nobody whitelisted to that region!").color(NamedTextColor.RED));
            return;
        }

        sender.sendMessage(Component.text("Whitelisted players for %s".formatted(region.getName())).color(NamedTextColor.GOLD));
        region.getWhitelisted().forEach(uuid -> {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
            if (offlinePlayer.hasPlayedBefore()) {
                sender.sendMessage(Component.text(" - %s".formatted(offlinePlayer.getName())).color(NamedTextColor.YELLOW));
            }
        });
    }
}
