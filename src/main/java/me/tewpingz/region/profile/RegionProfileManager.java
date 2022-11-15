package me.tewpingz.region.profile;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RegionProfileManager {

    private final Map<UUID, RegionProfile> profiles = new HashMap<>();

    public RegionProfile getProfile(UUID uuid) {
        return this.profiles.computeIfAbsent(uuid, RegionProfile::new);
    }

    public void untrackProfile(UUID uuid) {
        this.profiles.remove(uuid);
    }
}
