package me.tewpingz.region;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
public class Region {

    private final String name;

    @Getter(AccessLevel.NONE)
    // should not be accessed externally because people mess with lists!
    private final List<UUID> whitelisted;

    // This cant be final as in the requirements they want the ability to refined location
    private RegionCuboid regionCuboid;

    public Region(String name, RegionCuboid cuboid) {
        this.name = name;
        this.regionCuboid = cuboid;
        this.whitelisted = new ArrayList<>();
    }

    /**
     * Function to allow you to check if a user uuid is whitelisted
     * @param uuid the uuid to check for in the list
     * @return if the user is whitelisted to edit the region
     */
    public boolean isWhitelisted(UUID uuid) {
        return this.whitelisted.contains(uuid);
    }

    /**
     * Function to allow you to add a user to the whitelist to edit and build
     * @param uuid the uuid of the user
     */
    public void addToWhitelist(UUID uuid) {
        this.whitelisted.add(uuid);
    }

    /**
     * Function to allow you to remove the user from the whitelist to build
     * @param uuid the uuid to remove
     */
    public void removeFromWhitelist(UUID uuid) {
        this.whitelisted.remove(uuid);
    }

    /**
     * A function to check if a location is inside the cuboid
     * @param x location x of the location
     * @param y location y of the location
     * @param z location z of the location
     * @return if the location is inside the cuboid or not
     */
    public boolean contains(int x, int y, int z) {
        return this.regionCuboid.contains(x, y, z);
    }

    /**
     * A function to check if the location is inside the cuboid
     * @param location the location to check
     * @return if the location is inside the cuboid or not
     */
    public boolean contains(Location location) {
        return this.regionCuboid.contains(location);
    }

    /**
     * A function to check if the block is inside the cuboid
     * @param block the block to check for
     * @return if the block is inside the cuboid
     */
    public boolean contains(Block block) {
        return this.regionCuboid.contains(block);
    }
}
