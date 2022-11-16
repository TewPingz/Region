package me.tewpingz.region.profile;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RegionProfileManager {

    private final Map<UUID, RegionProfile> profiles = new HashMap<>();

    /**
     * A function that allows you to get the profile or create it if it doesn't exist
     * @param uuid the uuid of the profile
     * @return the profile instance
     */
    public RegionProfile getProfile(UUID uuid) {
        return this.profiles.computeIfAbsent(uuid, RegionProfile::new);
    }

    /**
     * A function that allows you to void a profile from the map
     * @param uuid the uuid to remove
     */
    public void untrackProfile(UUID uuid) {
        this.profiles.remove(uuid);
    }
}
