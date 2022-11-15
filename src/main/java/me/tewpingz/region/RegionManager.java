package me.tewpingz.region;

import lombok.Getter;
import me.tewpingz.region.model.Region;
import me.tewpingz.region.model.RegionCuboid;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RegionManager {

    private final RegionPersistence persistence;
    private final Map<Integer, Region> idToRegion;
    private final Map<String, Integer> nameToId;

    @Getter
    private final ItemStack selectionWand;

    public RegionManager(RegionPersistence persistence) {
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

    public void loadRegions() {
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
                        this.cacheRegion(region);
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void cacheRegion(Region region) {
        if (region == null) {
            return;
        }
        this.nameToId.put(region.getName().toLowerCase(), region.getId());
        this.idToRegion.put(region.getId(), region);
    }
}
