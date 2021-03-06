package com.imyvm.ItemMail;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class InventoryListener implements Listener {

    ItemMail itemMail;
    private static int slots = ItemMail.getSlots();

    public InventoryListener(ItemMail it){
        it = itemMail;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onInventoryClick(InventoryClickEvent event) {

        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        Player p = (Player) event.getWhoClicked();
        if (event.getView().getTopInventory().getSize() != slots) {
            return;
        }
        if (event.getView().getTitle().equalsIgnoreCase("ItemMail for imyvm")) {
            event.setCancelled(true);
            p.updateInventory();
            if (event.getRawSlot() == 0) {
                p.closeInventory();
                p.openInventory(event.getInventory());
            }
        }
    }
}
