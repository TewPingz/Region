package me.tewpingz.region.command;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RegionCommandTabComplete implements TabCompleter {

    private final Set<RegionCommandEntry> entries;

    public RegionCommandTabComplete() {
        this.entries = new HashSet<>();
        this.entries.add(new RegionCommandEntry("create", "region.create"));
        this.entries.add(new RegionCommandEntry("wand", "region.create"));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length <= 1) {
            this.entries.forEach(regionCommandEntry -> {
                if (sender.isOp() || sender.hasPermission(regionCommandEntry.getPermission())) {
                    completions.add(regionCommandEntry.getLabel());
                }
            });
        }

        return completions;
    }

    @Getter
    @RequiredArgsConstructor
    private static class RegionCommandEntry {
        private final String label, permission;
    }
}
