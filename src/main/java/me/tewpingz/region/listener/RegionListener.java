package me.tewpingz.region.listener;

import io.papermc.paper.event.block.BlockBreakBlockEvent;
import lombok.RequiredArgsConstructor;
import me.tewpingz.region.RegionPlugin;
import me.tewpingz.region.model.Region;
import me.tewpingz.region.profile.RegionProfile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

@RequiredArgsConstructor
public class RegionListener implements Listener {

    private final RegionPlugin regionPlugin;

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerInteractInRegion(PlayerInteractEvent event) {
        if (event.getPlayer().hasPermission("region.bypass")) {
            return;
        }

        if (!(event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_BLOCK)) {
            return;
        }

        Region region = this.regionPlugin.getRegionManager().getRegionByLocation(event.getInteractionPoint());

        if (region == null) {
            return;
        }

        if (region.isWhitelisted(event.getPlayer().getUniqueId())) {
            return;
        }

        event.setCancelled(true);
        event.getPlayer().sendMessage(Component.text("You cannot interact with in this region!").color(NamedTextColor.RED));
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.getPlayer().hasPermission("region.bypass")) {
            return;
        }

        Region region = this.regionPlugin.getRegionManager().getRegionByLocation(event.getBlock().getLocation());

        if (region == null) {
            return;
        }

        if (region.isWhitelisted(event.getPlayer().getUniqueId())) {
            return;
        }

        event.setCancelled(true);
        event.getPlayer().sendMessage(Component.text("You cannot interact with in this region!").color(NamedTextColor.RED));
    }


    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.getPlayer().hasPermission("region.bypass")) {
            return;
        }

        Region region = this.regionPlugin.getRegionManager().getRegionByLocation(event.getBlock().getLocation());

        if (region == null) {
            return;
        }

        if (region.isWhitelisted(event.getPlayer().getUniqueId())) {
            return;
        }

        event.setCancelled(true);
        event.getPlayer().sendMessage(Component.text("You cannot interact with in this region!").color(NamedTextColor.RED));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerInteractWithWand(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        if (!player.hasPermission("region.create")) {
            return;
        }

        ItemStack itemInHand = event.getItem();
        if (itemInHand == null || itemInHand.getType() == Material.AIR) {
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

        switch (event.getAction()) {
            case RIGHT_CLICK_BLOCK -> {
                profile.setSelectionOne(event.getClickedBlock().getLocation());
                player.sendMessage(Component.text("You have selected the first location.").color(NamedTextColor.GREEN));
                this.attemptToApply(profile, player);
            }
            case LEFT_CLICK_BLOCK -> {
                profile.setSelectionTwo(event.getClickedBlock().getLocation());
                player.sendMessage(Component.text("You have selected the second location.").color(NamedTextColor.GREEN));
                this.attemptToApply(profile, player);
            }
        }
    }

    private void attemptToApply(RegionProfile profile, Player player) {
        if (profile.isCuboidSelected()) {
            if (profile.getSelectingFor() != -1) {
                Region region = RegionPlugin.getInstance().getRegionManager().getRegionById(profile.getSelectingFor());
                region.updateRegionCuboid(profile.toRegionCuboid());
                profile.setSelectingFor(-1);
                profile.setSelectionOne(null);
                profile.setSelectionTwo(null);
                player.sendMessage(Component.text("Congratulations you have updated the cuboid for %s!".formatted(region.getName())).color(NamedTextColor.GOLD));
            } else {
                player.sendMessage(Component.text("Congratulations you have selected a valid cuboid!").color(NamedTextColor.GOLD));
                player.sendMessage(Component.text("You many now continue by using /region create").color(NamedTextColor.GOLD));
            }
        }
    }
}
