package me.tewpingz.region.listener;

import lombok.RequiredArgsConstructor;
import me.tewpingz.region.RegionPlugin;
import me.tewpingz.region.profile.RegionProfile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

@RequiredArgsConstructor
public class RegionListener implements Listener {

    private final RegionPlugin regionPlugin;

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (!player.hasPermission("region.wand")) {
            return;
        }

        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        if (itemInHand.getType() == Material.AIR) {
            return;
        }

        if (!this.regionPlugin.getRegionManager().getSelectionWand().equals(itemInHand)) {
            return;
        }

        if (event.getClickedBlock() == null) {
            return;
        }

        event.setCancelled(true);

        RegionProfile profile = this.regionPlugin.getRegionProfileManager().getProfile(player.getUniqueId());
        if (event.getAction().isLeftClick()) {
            profile.setSelectionOne(event.getClickedBlock().getLocation());
            player.sendMessage(Component.text("You have selected the first location.").color(NamedTextColor.GREEN));
        } else if (event.getAction().isRightClick()) {
            profile.setSelectionTwo(event.getClickedBlock().getLocation());
            player.sendMessage(Component.text("You have selected the second location.").color(NamedTextColor.GREEN));
        }

        if (profile.isCuboidSelected()) {
            player.sendMessage(Component.text("Congratulations you have selected a valid cuboid!").color(NamedTextColor.GOLD));
            player.sendMessage(Component.text("You many now continue by using /region create / name OR").color(NamedTextColor.GOLD));
            player.sendMessage(Component.text("Update your a current cuboid through /region <name>").color(NamedTextColor.GOLD));
        }
    }
}
