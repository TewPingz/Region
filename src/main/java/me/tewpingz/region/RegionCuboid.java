package me.tewpingz.region;

import lombok.*;
import org.bukkit.Location;
import org.bukkit.block.Block;


@Data
@EqualsAndHashCode
@AllArgsConstructor
@RequiredArgsConstructor
public class RegionCuboid {
    private final int minX, maxX, minY, maxY, minZ, maxZ;
    private final String worldName;

    /**
     * The initial constructor of the cuboid to create the cuboid class
     * @param min the min location of the selection
     * @param max the max location of the selection
     */
    public RegionCuboid(Location min, Location max) {
        if (!min.getWorld().getUID().equals(max.getWorld().getUID())) {
            throw new IllegalArgumentException("The locations are in different worlds");
        }

        this.worldName = min.getWorld().getName();

        // Location x
        this.minX = Math.min(min.getBlockX(), max.getBlockX());
        this.maxX = Math.max(min.getBlockX(), max.getBlockX());

        // Location y
        this.minY = Math.min(min.getBlockY(), max.getBlockY());
        this.maxY = Math.max(min.getBlockY(), max.getBlockY());

        // Location z
        this.minZ = Math.min(min.getBlockZ(), max.getBlockZ());
        this.maxZ = Math.max(min.getBlockZ(), max.getBlockZ());
    }

    /**
     * A function to check if a location is inside the cuboid
     * @param x location x of the location
     * @param y location y of the location
     * @param z location z of the location
     * @return if the location is inside the cuboid or not
     */
    public boolean contains(int x, int y, int z) {
        return this.minX >= x && this.maxX <= x && this.minY >= y && this.maxY <= y && this.minZ >= z && this.maxZ <= z;
    }

    /**
     * A function to check if the location is inside the cuboid
     * @param location the location to check
     * @return if the location is inside the cuboid or not
     */
    public boolean contains(Location location) {
        if (!this.worldName.equals(location.getWorld().getName())) {
            return false;
        }
        return this.contains(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    /**
     * A function to check if the block is inside the cuboid
     * @param block the block to check for
     * @return if the block is inside the cuboid
     */
    public boolean contains(Block block) {
        return contains(block.getX(), block.getY(), block.getZ());
    }
}
