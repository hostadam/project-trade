package me.hostadam.trade.framework;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerQuitEvent;
import xyz.geographica.factions.GeoFactions;

public class TradeListener implements Listener {

    private final GeoFactions plugin = GeoFactions.getInstance();

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if(!(event.getWhoClicked() instanceof Player)
                || event.getClickedInventory() == null
                || event.getView() == null
                || !event.getView().getTitle().equals("§aYour Items            §cTheir Items")) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        Trade trade = plugin.getTradeHandler().getTrade(player);
        if(trade == null) {
            return;
        }

        if(event.getAction() == InventoryAction.COLLECT_TO_CURSOR && event.getClick() == ClickType.DOUBLE_CLICK) {
            event.setCancelled(true);
            return;
        }

        //Don't allow shift clicking
        if(event.getClick().isShiftClick()) {
            /*if(event.getClickedInventory().equals(event.getView().getBottomInventory()) && plugin.getTradeHandler().getRightSideSlots().contains(event.getView().getTopInventory().firstEmpty())) {
                event.setCancelled(true);
                return;
            }*/
            event.setCancelled(true);
            player.sendMessage("§cYou cannot shift click items into the trading GUI.");
            return;
        }

        if(event.getRawSlot() == 22) {
            event.setCancelled(true);

            if(!trade.canConfirm()) {
                player.sendMessage("§cThe Trade cannot be confirmed at the moment.");
                return;
            }

            trade.confirmTrade(player);
        } else if(event.getRawSlot() == 31) {
            trade.cancelTrade(player);
            event.setCancelled(true);
        } else if(event.getClickedInventory().equals(event.getView().getTopInventory()) && !trade.canMove(player, event.getRawSlot())) {
            event.setCancelled(true);
        } else {
            Bukkit.getScheduler().runTaskLater(plugin, () -> trade.updateInventory(player), 2);
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        Player player = (Player) event.getWhoClicked();
        Trade trade = plugin.getTradeHandler().getTrade(player);
        if(trade == null) {
            return;
        }

        boolean update = false;

        for(int slot : event.getRawSlots()) {
            if(!trade.canMove(player, slot)) {
                event.setCancelled(true);
                return; //Return to not allow the rest
            }

            update = true;
        }

        if(update) {
            //Add slight delay to make sure event is fully run
            Bukkit.getScheduler().runTaskLater(plugin, () -> trade.updateInventory(player), 2);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        Trade trade = plugin.getTradeHandler().getTrade(player);

        if(trade == null) {
            return;
        }

        trade.cancelTrade(player);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Trade trade = plugin.getTradeHandler().getTrade(player);

        if(trade == null) {
            return;
        }

        trade.cancelTrade(player);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Trade trade = plugin.getTradeHandler().getTrade(player);

        if(trade == null) {
            return;
        }

        trade.cancelTrade(player);
    }
}
