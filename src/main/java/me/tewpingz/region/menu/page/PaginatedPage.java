package me.tewpingz.region.menu.page;

import fr.mrmicky.fastinv.FastInv;
import fr.mrmicky.fastinv.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;

/**
 * @author TewPingz
 */
public class PaginatedPage extends FastInv {

    private final PaginatedInv paginatedInv;

    public PaginatedPage(PaginatedInv paginatedInv) {
        super(9 * 4, paginatedInv.getTitle());

        this.paginatedInv = paginatedInv;

        this.setItems(this.getBorders(), new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE)
                .name(" ")
                .flags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DYE)
                .build(), event -> event.setCancelled(true));

        this.setItem(31, new ItemBuilder(Material.PAPER)
                .name(ChatColor.GOLD + "%s/%s".formatted(this.paginatedInv.getCurrentPage() + 1, this.paginatedInv.getPageCount()))
                .flags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DYE)
                .build(), event -> event.setCancelled(true));

        this.setItem(30, new ItemBuilder(this.paginatedInv.getCurrentPage() <= 0 ? Material.MAGENTA_DYE : Material.LIME_DYE)
                .name(this.paginatedInv.getCurrentPage() <= 0 ? (ChatColor.RED + "You cannot go back any further") : (ChatColor.GREEN + "Go back!"))
                .flags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DYE)
                .build(), this::handlePreviousPage);

        this.setItem(32, new ItemBuilder(this.paginatedInv.getCurrentPage() >= (this.paginatedInv.getPageCount() - 1) ? Material.MAGENTA_DYE : Material.LIME_DYE)
                .name(this.paginatedInv.getCurrentPage() >= (this.paginatedInv.getPageCount() - 1) ? (ChatColor.RED + "You cannot go any further") : (ChatColor.GREEN + "Next page!"))
                .flags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DYE)
                .build(), this::handleNextPage);

        this.paginatedInv.getPageItems().forEach(paginatedItem -> this.addItem(paginatedItem.getItemStack(), paginatedItem.getConsumer()));
    }

    private void handlePreviousPage(InventoryClickEvent event) {
        if (this.paginatedInv.getCurrentPage() <= 0) {
            return;
        }
        this.paginatedInv.openPreviousPage((Player) event.getWhoClicked());
    }

    private void handleNextPage(InventoryClickEvent event) {
        if (this.paginatedInv.getCurrentPage() >= (this.paginatedInv.getPageCount() - 1)) {
            return;
        }
        this.paginatedInv.openNextPage((Player) event.getWhoClicked());
    }
}
