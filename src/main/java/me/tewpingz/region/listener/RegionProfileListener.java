package me.tewpingz.region.listener;

import lombok.RequiredArgsConstructor;
import me.tewpingz.region.profile.RegionProfileManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

@RequiredArgsConstructor
public class RegionProfileListener implements Listener {

    private final RegionProfileManager regionProfileManager;

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        this.regionProfileManager.untrackProfile(event.getPlayer().getUniqueId());
    }
}
