package me.tewpingz.region.menu;

import me.tewpingz.region.RegionPlugin;
import me.tewpingz.region.menu.page.PaginatedInv;
import me.tewpingz.region.model.RegionCuboid;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class RegionsMenu extends PaginatedInv {
    public RegionsMenu() {
        super("Regions Menu");

        RegionPlugin.getInstance().getRegionManager().getRegions().forEach(region -> {
            ItemStack itemStack = new ItemStack(Material.NETHER_STAR);
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.addEnchant(Enchantment.MENDING, 1, true);
            itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);

            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());

            RegionCuboid cuboid = region.getRegionCuboid();
            lore.add(Component.text()
                    .append(Component.text("World:").color(NamedTextColor.YELLOW))
                    .append(Component.space())
                    .append(Component.text(cuboid.getWorldName()).color(NamedTextColor.WHITE))
                    .build());
            lore.add(Component.text()
                    .append(Component.text("Min:").color(NamedTextColor.YELLOW))
                    .append(Component.text("%s, %s, %s".formatted(cuboid.getMinX(), cuboid.getMinY(), cuboid.getMinZ())).color(NamedTextColor.WHITE))
                    .build());
            lore.add(Component.text()
                    .append(Component.text("Max:").color(NamedTextColor.YELLOW))
                    .append(Component.text("%s, %s, %s".formatted(cuboid.getMaxX(), cuboid.getMaxY(), cuboid.getMaxZ())).color(NamedTextColor.WHITE))
                    .build());

            lore.add(Component.empty());
            itemMeta.lore(lore);
            itemMeta.displayName(Component.text(region.getName()).color(NamedTextColor.GOLD));
            itemStack.setItemMeta(itemMeta);

            this.addItem(itemStack, event -> new RegionMenu(region).open((Player) event.getWhoClicked()));
        });
    }
}
