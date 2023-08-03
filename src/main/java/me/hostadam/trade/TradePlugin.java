package me.hostadam.trade;

import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

public class TradePlugin extends JavaPlugin {

    @Getter
    private static TradePlugin instance;

    @Override
    public void onEnable() {
        instance = this;
    }

    @Override
    public void onDisable() {

    }
}
