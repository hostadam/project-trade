package me.hostadam.trade.framework;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

@Getter
@Setter
@RequiredArgsConstructor
public class TradeSide {

    @NonNull
    private Player player;
    private Inventory inventory = null;
    private boolean done = false;

    public void setupInventory(ItemStack[] contents) {
        this.inventory = Bukkit.createInventory(null, 54, "§aYour Items            §cTheir Items");
        this.inventory.setContents(contents);

        //Open inventory
        this.player.openInventory(this.inventory);
    }

    public boolean matches(Player player) {
        return this.player.getUniqueId().equals(player.getUniqueId());
    }
}
