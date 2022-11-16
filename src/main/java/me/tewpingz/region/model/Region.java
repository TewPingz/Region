package me.tewpingz.region.model;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import me.tewpingz.region.RegionPlugin;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

@Data
public class Region {

    private final int id;

    // should not be accessed externally because people mess with lists!
    private final Set<UUID> whitelisted;

    // This cant be final as in the requirements they want the ability to rename the cuboid
    private String name;

    // This cant be final as in the requirements they want the ability to refined location
    private RegionCuboid regionCuboid;

    public Region(int id, String name) {
        this.id = id;
        this.name = name;
        this.whitelisted = new HashSet<>();
    }

    public void updateName(String name) {
        this.name = name;
        RegionPlugin.getInstance().getRegionPersistence().getConnectionAsync().thenAccept(connection -> {
            try {
                PreparedStatement updateStatement = connection.prepareStatement("UPDATE REGIONS SET REGION_NAME=? WHERE REGION_ID=?");
                updateStatement.setString(1, name);
                updateStatement.setInt(2, this.id);
                updateStatement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * A function that allows you to set the region cuboid
     * Update the region cuboid without having to make a manual connection to the database.
     * @param regionCuboid the region cuboid to update to
     */
    public void updateRegionCuboid(RegionCuboid regionCuboid) {
        this.regionCuboid = regionCuboid;
        RegionPlugin.getInstance().getRegionPersistence().getConnectionAsync().thenAccept(connection -> {
            try {
                PreparedStatement updateStatement = connection.prepareStatement("UPDATE REGION_CUBOID SET MIN_X=?, MIN_Y=?, MIN_Z=?, MAX_X=?, MAX_Y=?, MAX_Z=?, WORLD_NAME=? WHERE REGION_ID=?");
                updateStatement.setInt(1, regionCuboid.getMinX());
                updateStatement.setInt(2, regionCuboid.getMinY());
                updateStatement.setInt(3, regionCuboid.getMinZ());
                updateStatement.setInt(4, regionCuboid.getMaxX());
                updateStatement.setInt(5, regionCuboid.getMaxY());
                updateStatement.setInt(6, regionCuboid.getMaxZ());
                updateStatement.setString(7, regionCuboid.getWorldName());
                updateStatement.setInt(8, this.id);
                updateStatement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
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
        RegionPlugin.getInstance().getRegionPersistence().getConnectionAsync().thenAccept(connection -> {
            try {
                PreparedStatement insertWhitelist = connection.prepareStatement("INSERT INTO REGION_WHITELIST (REGION_ID, PLAYER_UUID) VALUES(?,?)");
                insertWhitelist.setInt(1, this.id);
                insertWhitelist.setString(2, uuid.toString());
                insertWhitelist.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Function to allow you to remove the user from the whitelist to build
     * @param uuid the uuid to remove
     */
    public void removeFromWhitelist(UUID uuid) {
        this.whitelisted.remove(uuid);
        RegionPlugin.getInstance().getRegionPersistence().getConnectionAsync().thenAccept(connection -> {
            try {
                PreparedStatement deleteWhitelist = connection.prepareStatement("DELETE FROM REGION_WHITELIST WHERE REGION_ID=? AND PLAYER_UUID=?");
                deleteWhitelist.setInt(1, this.id);
                deleteWhitelist.setString(2, uuid.toString());
                deleteWhitelist.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
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
