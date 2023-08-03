package me.hostadam.trade.framework;

import lombok.Getter;
import me.hostadam.base.api.menu.button.MenuButton;
import me.hostadam.base.api.texture.Texture;
import me.hostadam.base.util.PlayerUtil;
import me.hostadam.base.util.builder.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

@Getter
public class Trade {

    private final TradeHandler tradeHandler;
    private final TradeSide initiator, target;

    public Trade(TradeHandler handler, Player initiator, Player target) {
        this.tradeHandler = handler;
        this.initiator = new TradeSide(initiator);
        this.target = new TradeSide(target);
    }

    public boolean canConfirm() {
        boolean initiator = false,
                target = false;

        for(int slot : this.tradeHandler.getLeftSideSlots()) {
            ItemStack initiatorItem = this.initiator.getInventory().getItem(slot);
            ItemStack targetItem = this.target.getInventory().getItem(slot);

            if(initiatorItem != null && initiatorItem.getType() != Material.AIR && initiatorItem.getAmount() > 0) {
                initiator = true;
                if(target) return true;
            }

            if(targetItem != null && targetItem.getType() != Material.AIR && targetItem.getAmount() > 0) {
                target = true;
                if(initiator) return true;
            }
        }

        return false;
    }

    public boolean canMove(Player player, int slot) {
        if(this.isLocked()) {
            return false;
        }

        return this.tradeHandler.getLeftSideSlots().contains(slot);
    }

    public void open() {
        Inventory inventory = Bukkit.createInventory(null, 54, "§aYour Items            §cTheir Items");
        for(int index : this.tradeHandler.getGlassSlots()) {
            inventory.setItem(index, MenuButton.FILLER_ITEM);
        }

        inventory.setItem(4, new ItemBuilder(Material.BOOK).name("§a§lTrading Instructions").lore(
                " ",
                "§7Insert all your items on the left side.",
                "§7When you have done so, click the Confirm item, but,",
                "§7confirming will not allow you to edit your items.",
                " ",
                "§7If you wish to cancel, click the Cross in the middle.",
                " ").build());
        inventory.setItem(22, new ItemBuilder(Texture.CONFIRM.getItemStack().clone()).name("§a§lConfirm Trade").build());
        inventory.setItem(31, new ItemBuilder(Texture.CANCEL.getItemStack().clone()).name("§c§lCancel Trade").build());

        this.initiator.setupInventory(inventory.getContents());
        this.target.setupInventory(inventory.getContents());
    }

    public void updateInventory(Player player) {
        boolean isInitiator = this.initiator.matches(player);

        for(int slot : this.tradeHandler.getLeftSideSlots()) {
            if(isInitiator) {
                this.target.getInventory().setItem(slot + 5, this.initiator.getInventory().getItem(slot));
            } else {
                this.initiator.getInventory().setItem(slot + 5, this.target.getInventory().getItem(slot));
            }
        }
    }

    public void confirmTrade(Player player) {
        TradeSide side = this.getSideByPlayer(player);
        side.setDone(true);

        if(this.isConfirmed()) {
            PlayerUtil.giveItems(this.target.getPlayer(), this.getInitiatorItems());
            PlayerUtil.giveItems(this.initiator.getPlayer(), this.getTargetItems());

            this.initiator.getPlayer().sendMessage("§aYour Trade with §l" + this.target.getPlayer().getName() + "§a was §a§lSUCCESSFUL!");
            this.target.getPlayer().sendMessage("§aYour Trade with §l" + this.initiator.getPlayer().getName() + "§a was §a§lSUCCESSFUL!");

            tradeHandler.removeTrade(this);

            this.initiator.getPlayer().closeInventory();
            this.target.getPlayer().closeInventory();
        } else if(this.initiator.isDone()) {
            if(this.target.getPlayer().getItemOnCursor() != null) {
                PlayerUtil.giveItem(this.target.getPlayer(), this.target.getPlayer().getItemOnCursor());
                this.target.getPlayer().setItemOnCursor(null);
            }

            this.initiator.getPlayer().sendMessage("§aYou have confirmed and locked the Trade. §6§l" + this.target.getPlayer().getName() + " §emust now §a§lconfirm §eor §c§lcancel §ethis Trade.");
            this.target.getPlayer().sendMessage("§6§l" + this.initiator.getPlayer().getName() + " §ehas §aconfirmed §ethe Trade. You can §cno longer §eedit any items and must either §a§lConfirm §eor §c§lCancel §ethis Trade.");
        } else if(this.target.isDone()) {
            if(this.initiator.getPlayer().getItemOnCursor() != null) {
                PlayerUtil.giveItem(this.initiator.getPlayer(), this.initiator.getPlayer().getItemOnCursor());
                this.initiator.getPlayer().setItemOnCursor(null);
            }

            this.target.getPlayer().sendMessage("§aYou have confirmed and locked the Trade. §6§l" + this.initiator.getPlayer().getName() + " §emust now §a§lconfirm §eor §c§lcancel §ethis Trade.");
            this.initiator.getPlayer().sendMessage("§6§l" + this.target.getPlayer().getName() + " §ehas §aconfirmed §ethe Trade. You can §cno longer §eedit any items and must either §a§lConfirm §eor §c§lCancel §ethis Trade.");
        }
    }

    public void cancelTrade(Player player) {
        this.tradeHandler.removeTrade(this);

        PlayerUtil.giveItems(this.initiator.getPlayer(), this.getInitiatorItems());
        PlayerUtil.giveItems(this.target.getPlayer(), this.getTargetItems());

        this.initiator.getPlayer().sendMessage("§cYour Trade with §l" + this.target.getPlayer().getName() + "§c was §c§lCANCELLED!");
        this.target.getPlayer().sendMessage("§cYour Trade with §l" + this.initiator.getPlayer().getName() + "§c was §c§lCANCELLED!");

        this.initiator.getPlayer().closeInventory();
        this.target.getPlayer().closeInventory();
    }

    public ItemStack[] getInitiatorItems() {
        ItemStack[] array = this.newArray();

        int index = 0;
        for(int slot : this.tradeHandler.getLeftSideSlots()) {
            array[index++] = this.initiator.getInventory().getItem(slot);
        }

        return array;
    }

    public ItemStack[] getTargetItems() {
        ItemStack[] array = this.newArray();

        int index = 0;
        for(int slot : this.tradeHandler.getLeftSideSlots()) {
            array[index++] = this.target.getInventory().getItem(slot);
        }

        return array;
    }

    public ItemStack[] newArray() {
        return new ItemStack[16];
    }

    private boolean isLocked() {
        return this.initiator.isDone() || this.target.isDone();
    }

    public TradeSide getSideByPlayer(Player player) {
        if(this.initiator.matches(player)) {
            return this.initiator;
        }

        return this.target;
    }

    public boolean isConfirmed() {
        return this.initiator.isDone() && this.target.isDone();
    }
}