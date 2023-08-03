package me.hostadam.trade.framework;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import lombok.Getter;
import me.hostadam.base.BasePlugin;
import me.hostadam.base.api.handler.BaseHandler;
import org.bukkit.entity.Player;

import java.util.*;

public class TradeHandler extends BaseHandler {

    private Map<UUID, Trade> tradeMap = new HashMap<>();
    private Table<UUID, UUID, Long> tradeRequestMap = HashBasedTable.create();

    @Getter
    private List<Integer> glassSlots = Arrays.asList(0, 1, 2, 3, 5, 6, 7, 8, 13, 40, 45, 46, 47, 48, 49, 50, 51, 52, 53),
            leftSideSlots = Arrays.asList(9, 10, 11, 12, 18, 19, 20, 21, 27, 28, 29, 30, 36, 37, 38, 39),
            rightSideSlots = Arrays.asList(14, 15, 16, 17, 23, 24, 25, 26, 32, 33, 34, 35, 41, 42, 43, 44);

    public TradeHandler(BasePlugin plugin) {
        super(plugin);
    }

    public void initiateTrade(Player player, Player target) {
        Trade trade = new Trade(this, player, target);

        this.tradeRequestMap.remove(player.getUniqueId(), target.getUniqueId());
        this.tradeRequestMap.remove(target.getUniqueId(), player.getUniqueId());

        this.tradeMap.put(player.getUniqueId(), trade);
        this.tradeMap.put(target.getUniqueId(), trade);

        trade.open();
    }

    public void removeTrade(Trade trade) {
        this.tradeMap.remove(trade.getInitiator().getPlayer().getUniqueId());
        this.tradeMap.remove(trade.getTarget().getPlayer().getUniqueId());
    }

    public Trade getTrade(Player player) {
        return this.tradeMap.get(player.getUniqueId());
    }

    public boolean hasSentTradeRequest(Player player, Player target) {
        return this.tradeRequestMap.contains(player.getUniqueId(), target.getUniqueId());
    }

    public void removeTradeRequest(Player player, Player target) {
        this.tradeRequestMap.remove(player.getUniqueId(), target.getUniqueId());
    }

    public void sendTradeRequest(Player player, Player target) {
        this.tradeRequestMap.put(player.getUniqueId(), target.getUniqueId(), System.currentTimeMillis());
    }

    public long getTimeSinceTradeRequest(Player player, Player target) {
        return System.currentTimeMillis() - this.tradeRequestMap.get(player.getUniqueId(), target.getUniqueId());
    }

    @Override
    public void onEnable() { }

    @Override
    public void onDisable() { }
}