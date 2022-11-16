package me.tewpingz.region;

import fr.mrmicky.fastinv.FastInv;
import fr.mrmicky.fastinv.FastInvManager;
import lombok.Getter;
import me.tewpingz.region.command.RegionCommand;
import me.tewpingz.region.command.RegionCommandTabComplete;
import me.tewpingz.region.listener.RegionListener;
import me.tewpingz.region.listener.RegionProfileListener;
import me.tewpingz.region.profile.RegionProfileManager;
import org.bukkit.command.PluginCommand;
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

        FastInvManager.register(this);
        this.regionPersistence = new RegionPersistence(this);
        this.regionProfileManager = new RegionProfileManager();
        this.regionManager = new RegionManager(this.regionPersistence);

        PluginCommand pluginCommand = this.getCommand("region");
        pluginCommand.setExecutor(new RegionCommand(this));
        pluginCommand.setTabCompleter(new RegionCommandTabComplete());

        this.getServer().getPluginManager().registerEvents(new RegionProfileListener(this.regionProfileManager), this);
        this.getServer().getPluginManager().registerEvents(new RegionListener(this), this);
    }

    @Override
    public void onDisable() {

    }
}
