package me.tewpingz.region;

import lombok.Getter;
import me.tewpingz.region.command.RegionCommand;
import me.tewpingz.region.listener.RegionListener;
import me.tewpingz.region.listener.RegionProfileListener;
import me.tewpingz.region.profile.RegionProfileManager;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public final class RegionPlugin extends JavaPlugin {

    @Getter
    private static RegionPlugin instance;

    private RegionPersistence regionPersistence;
    private RegionProfileManager regionProfileManager;
    private RegionManager regionManager;

    @Override
    public void onEnable() {
        instance = this;
        this.regionPersistence = new RegionPersistence(this);
        this.regionProfileManager = new RegionProfileManager();
        this.regionManager = new RegionManager(this.regionPersistence);

        this.getCommand("region").setExecutor(new RegionCommand(this));
        this.getServer().getPluginManager().registerEvents(new RegionProfileListener(this.regionProfileManager), this);
        this.getServer().getPluginManager().registerEvents(new RegionListener(this), this);
    }

    @Override
    public void onDisable() {

    }
}
