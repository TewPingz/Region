package me.tewpingz.region;

import lombok.Getter;
import me.tewpingz.region.model.Region;
import me.tewpingz.region.model.RegionCuboid;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RegionManager {

    private final RegionPersistence persistence;
    private final Map<Integer, Region> idToRegion;
    private final Map<String, Integer> nameToId;

    @Getter
    private final ItemStack selectionWand;

    protected RegionManager(RegionPersistence persistence) {
        this.persistence = persistence;
        this.idToRegion = new ConcurrentHashMap<>();
        this.nameToId = new ConcurrentHashMap<>();

        this.selectionWand = new ItemStack(Material.STICK);
        ItemMeta itemMeta = this.selectionWand.getItemMeta();
        itemMeta.setUnbreakable(true);
        itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DESTROYS, ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
        itemMeta.addEnchant(Enchantment.MENDING, 100, true);
        itemMeta.setDestroyableKeys(new ArrayList<>());
        itemMeta.displayName(Component.text("Region selector").color(NamedTextColor.RED));
        this.selectionWand.setItemMeta(itemMeta);

        this.loadRegions();
    }

    /**
     * A function that allows you to create a region
     * @param name the name of region to create
     * @param regionCuboid the region cuboid
     */
    public void createRegion(String name, RegionCuboid regionCuboid) {
        this.persistence.getConnectionAsync().thenApply(connection -> {
            try {
                // Inset region
                PreparedStatement insertRegion = connection.prepareStatement("INSERT INTO REGIONS (REGION_NAME) VALUES(?)");
                insertRegion.setString(1, name);
                insertRegion.executeUpdate();

                // Get last id
                PreparedStatement selectLastId = connection.prepareStatement("SELECT last_insert_id()");
                ResultSet idResultSet = selectLastId.executeQuery();

                if (idResultSet.next()) {
                    int id = idResultSet.getInt(1);
                    Region region = new Region(id, name);
                    region.setRegionCuboid(regionCuboid);

                    // Insert cuboid
                    PreparedStatement insertCuboid = connection.prepareStatement("INSERT INTO REGION_CUBOID (REGION_ID, MIN_X, MIN_Y, MIN_Z, MAX_X, MAX_Y, MAX_Z, WORLD_NAME) VALUES(?,?,?,?,?,?,?,?)");
                    insertCuboid.setInt(1, id);
                    insertCuboid.setInt(2, regionCuboid.getMinX());
                    insertCuboid.setInt(3, regionCuboid.getMinY());
                    insertCuboid.setInt(4, regionCuboid.getMinZ());
                    insertCuboid.setInt(5, regionCuboid.getMaxX());
                    insertCuboid.setInt(6, regionCuboid.getMaxY());
                    insertCuboid.setInt(7, regionCuboid.getMaxZ());
                    insertCuboid.setString(8, regionCuboid.getWorldName());
                    insertCuboid.executeUpdate();

                    return region;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return null;
        }).thenAccept(this::cacheRegion);
    }

    /**
     * A function that allows you to rename a region but keep track of its name in the map
     * @param id the id of the region
     * @param name the new name of the region
     */
    public void renameRegion(int id, String name) {
        Objects.requireNonNull(name);
        Region region = this.getRegionById(id);
        Objects.requireNonNull(region);
        this.nameToId.remove(region.getName().toLowerCase());
        region.updateName(name);
        this.nameToId.put(name.toLowerCase(), region.getId());
    }

    /**
     * A function that allows you to find a region based on its id
     * @param id the id to look for
     * @return the region instance if found otherwise null
     */
    public Region getRegionById(int id) {
        return this.idToRegion.get(id);
    }

    /**
     * A function that allows you to get a region based on its name
     * @param name the name of the region to try and find
     * @return the region if found otherwise null
     */
    public Region getRegionByName(String name) {
        Objects.requireNonNull(name);
        Integer integer = this.nameToId.get(name.toLowerCase());
        if (integer == null) {
            return null;
        }
        return this.getRegionById(integer);
    }

    /**
     * A function that allows you to get a region based on its location
     * As stated below the implementation of this was quick, this should be implemented using
     * a grid system that combines a 5 x 5 chunk radius together and then the user should
     * loop through those instead because they will be able to loop through fewer entries over time
     * which will help the server tremendously
     * @param location the location to check if the region is inside
     * @return the region if it was found otherwise null
     */
    public Region getRegionByLocation(Location location) {
        // In reality this should not be used, this is just a test plugin
        // This should be placed into a chunk grid map so if there are lots of entries
        // The server doesn't waste time processing through each entry one by one
        // But rather loop through a small amount of entries that are near the location to check
        // If the location is in a region.
        for (Region region : this.getRegions()) {
            if (region.contains(location)) {
                return region;
            }
        }
        return null;
    }

    /**
     * A function that allows you to get the list of registered region names
     * @return the set of region names
     */
    public Set<String> getRegionNames() {
        return this.nameToId.keySet();
    }

    /**
     * A function that allows you to get the registered regions
     * @return the collection of registered regions
     */
    public Collection<Region> getRegions() {
        return this.idToRegion.values();
    }

    private void cacheRegion(Region region) {
        if (region == null) {
            return;
        }
        this.nameToId.put(region.getName().toLowerCase(), region.getId());
        this.idToRegion.put(region.getId(), region);
    }

    private void loadRegions() {
        this.persistence.getConnectionAsync().thenAccept(connection -> {
            try {
                // Select all the regions
                PreparedStatement selectStatement = connection.prepareStatement("SELECT * FROM REGIONS");
                ResultSet resultSet = selectStatement.executeQuery();

                // Loop through all the results
                while (resultSet.next()) {
                    int id = resultSet.getInt("REGION_ID");
                    String name = resultSet.getString("REGION_NAME");
                    Region region = new Region(id, name);

                    // Find the region cuboid
                    PreparedStatement cuboidStatement = connection.prepareStatement("SELECT * FROM REGION_CUBOID WHERE REGION_ID=?");
                    cuboidStatement.setInt(1, id);
                    ResultSet cuboidResultSet = cuboidStatement.executeQuery();

                    // If it doesn't have one basically ignore it because that would never occur
                    if (cuboidResultSet.next()) {
                        int minX = cuboidResultSet.getInt("MIN_X");
                        int minY = cuboidResultSet.getInt("MIN_Y");
                        int minZ = cuboidResultSet.getInt("MIN_Z");
                        int maxX = cuboidResultSet.getInt("MAX_X");
                        int maxY = cuboidResultSet.getInt("MAX_Y");
                        int maxZ = cuboidResultSet.getInt("MAX_Z");
                        String worldName = cuboidResultSet.getString("WORLD_NAME");
                        RegionCuboid cuboid = new RegionCuboid(minX, maxX, minY, maxY, minZ, maxZ, worldName);
                        region.setRegionCuboid(cuboid);
                    }

                    // Find the region whitelists
                    PreparedStatement whitelistStatement = connection.prepareStatement("SELECT * FROM REGION_WHITELIST WHERE REGION_ID=?");
                    whitelistStatement.setInt(1, id);
                    ResultSet whitelistResultSet = whitelistStatement.executeQuery();
                    while (whitelistResultSet.next()) {
                        UUID playerId = UUID.fromString(whitelistResultSet.getString("PLAYER_UUID"));
                        region.getWhitelisted().add(playerId);
                    }

                    this.cacheRegion(region);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
