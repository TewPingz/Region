package me.tewpingz.region;

import com.mysql.cj.jdbc.MysqlDataSource;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

public class RegionPersistence {

    private final RegionPlugin plugin;
    private final String host, database, username, password;
    private final int port;

    private Connection connection;

    /**
     * The constructor for the persistence of the region plugin
     * @param plugin the plugin instance
     */
    protected RegionPersistence(RegionPlugin plugin) {
        plugin.getConfig().options().copyDefaults();
        plugin.saveDefaultConfig();
        FileConfiguration configuration = plugin.getConfig();
        ConfigurationSection section = configuration.getConfigurationSection("mysql");
        this.plugin = plugin;
        this.host = section.getString("host");
        this.port = section.getInt("port");
        this.database = section.getString("database");
        this.username = section.getString("username");
        this.password = section.getString("password");
    }

    /**
     * A function to get the connection on an asynchronous thread to ensure the server is not affected by the connection
     * @return a completable future with the connection
     */
    public CompletableFuture<Connection> getConnectionAsync() {
        return CompletableFuture.supplyAsync(this::attemptToGetConnection);
    }

    /**
     * Function to shut down the MySQL database
     * @throws SQLException when something wrong happens with the MySQL connection
     */
    public void shutdown() throws SQLException {
        this.connection.close();
    }

    private Connection attemptToGetConnection() {
        try {
            if (!this.isConnectionValid()) {
                this.connect();
            }
        } catch (Exception e) {
            this.connect();
            e.printStackTrace();
        }
        return this.connection;
    }

    private boolean isConnectionValid() throws SQLException {
        return this.connection != null && !this.connection.isClosed() && this.connection.isValid(10);
    }

    private void connect() {
        try {
            this.plugin.getLogger().info("Attempting to connect to MySQL database...");

            MysqlDataSource dataSource = new MysqlDataSource();
            dataSource.setUser(this.username);
            dataSource.setPassword(this.password);
            dataSource.setAutoReconnect(true);
            dataSource.setUrl("jdbc:mysql://%s:%s/%s".formatted(this.host, this.port, this.database));
            this.connection = dataSource.getConnection();

            String regionQuery = "CREATE TABLE IF NOT EXISTS REGIONS("
                    + "REGION_ID INTEGER  NOT NULL AUTO_INCREMENT,"
                    + "REGION_NAME VARCHAR(16) NOT NULL,"
                    + "PRIMARY KEY (REGION_ID)"
                    + ")";

            String cuboidQuery = "CREATE TABLE IF NOT EXISTS REGION_CUBOID("
                    + "REGION_ID INTEGER NOT NULL,"
                    + "MIN_X INTEGER NOT NULL,"
                    + "MIN_Y INTEGER NOT NULL,"
                    + "MIN_Z INTEGER NOT NULL,"
                    + "MAX_X INTEGER NOT NULL,"
                    + "MAX_Y INTEGER NOT NULL,"
                    + "MAX_Z INTEGER NOT NULL,"
                    + "WORLD_NAME VARCHAR(16) NOT NULL,"
                    + "PRIMARY KEY (REGION_ID)"
                    + ")";

            String whitelistQuery = "CREATE TABLE IF NOT EXISTS REGION_WHITELIST("
                    + "WHITELIST_ID INTEGER  NOT NULL AUTO_INCREMENT,"
                    + "REGION_ID INTEGER NOT NULL,"
                    + "PLAYER_UUID VARCHAR(36) NOT NULL,"
                    + "PRIMARY KEY (WHITELIST_ID)"
                    + ")";

            this.connection.prepareStatement(regionQuery).executeUpdate();
            this.connection.prepareStatement(cuboidQuery).executeUpdate();
            this.connection.prepareStatement(whitelistQuery).executeUpdate();
            this.plugin.getLogger().info("Connected to MySQL database");
        } catch (SQLException e) {
            this.plugin.getLogger().info("Failed to make a connection to the MySQL database.");
            this.plugin.getServer().getScheduler().runTask(this.plugin, () -> this.plugin.getServer().shutdown());
            e.printStackTrace();
        }
    }
}
