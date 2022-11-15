package me.tewpingz.region;

import org.bukkit.plugin.java.JavaPlugin;

public final class RegionPlugin extends JavaPlugin {

    private static RegionPlugin instance;

    @Override
    public void onEnable() {
        instance = this;

    }

    @Override
    public void onDisable() {

    }
}
