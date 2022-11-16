package me.tewpingz.region.menu;

import fr.mrmicky.fastinv.FastInv;
import fr.mrmicky.fastinv.ItemBuilder;
import me.tewpingz.region.RegionPlugin;
import me.tewpingz.region.model.Region;
import me.tewpingz.region.profile.RegionProfile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.RegexPrompt;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RegionMenu extends FastInv {
    public RegionMenu(Region region) {
        super(3 * 9, region.getName());

        this.setItem(10, new ItemBuilder(Material.ANVIL).name(ChatColor.GOLD + "Rename").build(), event -> {
            event.getInventory().close();
            new Conversation(RegionPlugin.getInstance(), (Player)event.getWhoClicked(), new RenamePrompt(region.getId())).begin();
        });

        this.setItem(12, new ItemBuilder(Material.PLAYER_HEAD).name(ChatColor.GOLD + "Add Player").build(), event -> {
            event.getInventory().close();
            new Conversation(RegionPlugin.getInstance(), (Player)event.getWhoClicked(), new AddPlayerPrompt(region)).begin();
        });

        this.setItem(14, new ItemBuilder(Material.DIAMOND_HELMET).name(ChatColor.GOLD + "Remove Player").build(), event -> {
            event.getInventory().close();
            new Conversation(RegionPlugin.getInstance(), (Player)event.getWhoClicked(), new RemovePlayerPrompt(region)).begin();
        });

        this.setItem(16, new ItemBuilder(Material.COMPASS).name(ChatColor.GOLD + "Redefine location").build(), event -> {
            event.getInventory().close();

            Player player = (Player) event.getWhoClicked();
            RegionProfile profile = RegionPlugin.getInstance().getRegionProfileManager().getProfile(player.getUniqueId());

            if (profile.getSelectingFor() != -1) {
                player.sendMessage(Component.text("You are already selecting for another region!").color(NamedTextColor.RED));
                return;
            }

            profile.setSelectingFor(region.getId());
            player.sendMessage(Component.text("You are now selecting a region for %s".formatted(region.getName())).color(NamedTextColor.GREEN));
            player.sendMessage(Component.text("The next region you select using your wand will be auto set for this region").color(NamedTextColor.GREEN));
        });
    }

    private static class AddPlayerPrompt extends RegexPrompt {
        private final Region region;

        public AddPlayerPrompt(Region region) {
            super("^\\S+$");
            this.region = region;
        }

        @Override
        public @NotNull String getPromptText(@NotNull ConversationContext context) {
            return ChatColor.GOLD + "Please enter the name of the player you would like to add!";
        }

        @Override
        protected @Nullable Prompt acceptValidatedInput(@NotNull ConversationContext context, @NotNull String input) {
            Player player = ((Player) context.getForWhom());
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(input);

            if (!offlinePlayer.hasPlayedBefore()) {
                player.sendMessage(Component.text("That player has not joined the server before").color(NamedTextColor.RED));
                return null;
            }

            if (this.region.isWhitelisted(offlinePlayer.getUniqueId())) {
                player.sendMessage(Component.text("That player is already whitelisted").color(NamedTextColor.RED));
                return null;
            }

            this.region.addToWhitelist(offlinePlayer.getUniqueId());
            player.sendMessage(Component.text("You have added %s to the whitelist!".formatted(offlinePlayer.getName())).color(NamedTextColor.GREEN));
            return null;
        }
    }

    private static class RemovePlayerPrompt extends RegexPrompt {
        private final Region region;

        public RemovePlayerPrompt(Region region) {
            super("^\\S+$");
            this.region = region;
        }

        @Override
        public @NotNull String getPromptText(@NotNull ConversationContext context) {
            return ChatColor.GOLD + "Please enter the name of the player you would like to remove!";
        }

        @Override
        protected @Nullable Prompt acceptValidatedInput(@NotNull ConversationContext context, @NotNull String input) {
            Player player = ((Player) context.getForWhom());
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(input);

            if (!offlinePlayer.hasPlayedBefore()) {
                player.sendMessage(Component.text("That player has not joined the server before").color(NamedTextColor.RED));
                return null;
            }

            if (!this.region.isWhitelisted(offlinePlayer.getUniqueId())) {
                player.sendMessage(Component.text("That player is not whitelisted").color(NamedTextColor.RED));
                return null;
            }

            this.region.removeFromWhitelist(offlinePlayer.getUniqueId());
            player.sendMessage(Component.text("You have removed %s from the whitelist!".formatted(offlinePlayer.getName())).color(NamedTextColor.GREEN));
            return null;
        }
    }


    private static class RenamePrompt extends RegexPrompt {

        private final int regionId;

        public RenamePrompt(int regionId) {
            super("^\\S+$");
            this.regionId = regionId;
        }

        @Override
        public @NotNull String getPromptText(@NotNull ConversationContext context) {
            return ChatColor.GOLD + "Please enter the name you would like to rename this region to!";
        }

        @Override
        protected @Nullable Prompt acceptValidatedInput(@NotNull ConversationContext context, @NotNull String input) {
            RegionPlugin.getInstance().getRegionManager().renameRegion(this.regionId, input);
            ((Player)context.getForWhom()).sendMessage(Component.text("You have renamed that region to %s".formatted(input)).color(NamedTextColor.GREEN));
            return null;
        }
    }
}
