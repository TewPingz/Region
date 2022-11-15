package me.tewpingz.region.profile;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import me.tewpingz.region.model.RegionCuboid;
import org.bukkit.Location;

import java.util.UUID;

@Data
@RequiredArgsConstructor
public class RegionProfile {

    private final UUID playerId;
    private Location selectionOne, selectionTwo;

    public boolean isCuboidSelected() {
        return this.selectionOne != null && this.selectionTwo != null && this.selectionOne.getWorld().getUID().equals(this.selectionTwo.getWorld().getUID());
    }

    public RegionCuboid toRegionCuboid() {
        return new RegionCuboid(this.selectionOne, this.selectionTwo);
    }

}
