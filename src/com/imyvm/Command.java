package com.imyvm;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import static com.imyvm.ItemMail.econ;

import java.text.DecimalFormat;
import java.util.*;

public class Command implements CommandExecutor {

//    ItemMail itemMail;
//    public Command(ItemMail it){
//        it = itemMail;
//    }
    private static int slots = ItemMail.getSlots();
    private static double price = ItemMail.getPrice();
    private String null_mainhand = ItemMail.getMessage_null_mainhand();
    private String null_inventory = ItemMail.getMessage_null_inventory();
    private String full_inv = ItemMail.getMessage_full();
    private String no_slots = ItemMail.getMessage_enough_slots();
    private String no_permission = ItemMail.getMessage_no_permission();
    private String null_inv = ItemMail.getMessage_null_inv();

    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command cmdObj, String label, String[] args) {
        Player player = (Player) sender;
        if (args.length==0){
            player.sendMessage("-----ItemMail Help-------");
        }else if (args.length==1){
            String cmd = args[0];
            if (cmd.equalsIgnoreCase("send")){
                if (sender.hasPermission("ItemMail.send.self")) {
                    sendsingle(player, player);
                }else {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&',this.null_mainhand));
                    return false;
                }
            }else if (cmd.equalsIgnoreCase("open") && player.hasPermission("ItemMail.open")) {
                Inventory inv_s = SQLActions.loaddata(player);
                player.openInventory(inv_s);
            }else if (cmd.equalsIgnoreCase("create") && player.hasPermission("ItemMail.create")){
                if (!SQLActions.playerDataContainsPlayer(player.getUniqueId())){
                    SQLActions.createAccount(player);
                    player.sendMessage("success");
                }else {
                    player.sendMessage("exist!");
                }
            }else if (cmd.equalsIgnoreCase("get")){
                if (player.hasPermission("ItemMail.get")){
                    Inventory playerinv = player.getInventory();
                    Inventory inventory = SQLActions.loaddata(player);
                    int inv_amount = inventory.getSize();
                    if (empty(inventory)){
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&',this.null_inv));
                        return false;
                    }
                    ItemStack[] mid = playerinv.getStorageContents();

                    Inventory midv = Bukkit.createInventory(null, slots, player.getName());
                    midv.setStorageContents(mid);

                    ItemStack[] items = inventory.getStorageContents();

                    if (realAdd(midv, items)){
                        playerinv.addItem(items);
                        player.updateInventory();
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&',"&b提取成功"));
                        Inventory inv1 = Bukkit.createInventory(null, slots, player.getName());
                        SQLActions.uploaddata(player, inv1);
                    }else {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                this.no_slots+getAmount(inventory)+"/"+inv_amount));
                        return false;
                    }
                }else {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&',this.no_permission));
                    return false;
                }
            }else if (cmd.equalsIgnoreCase("sendtotal")){
                if (player.hasPermission("ItemMail.send.total")){
                    return sendtoal(player);
                }else {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&',this.no_permission));
                    return false;
                }
            }else {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&',"&cinvalid &ccommand"));
                return false;
            }
        }else if (args.length==2){
            if (args[0].equalsIgnoreCase("send")){
                if (player.hasPermission("ItemMail.send.others")){
                    Player player1 = Bukkit.getPlayerExact(args[1]);
                    if (player1 == null){
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&',"&e该玩家不存在或不在线！"));
                        return false;
                    }else {
                        return sendsingle(player1, player);
                    }
                }else {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&',this.no_permission));
                    return false;
                }
            }else if (args[0].equalsIgnoreCase("create")){
                if (player.hasPermission("ItemMail.create")){
                    Player player1 = Bukkit.getPlayerExact(args[1]);
                    if (player1 == null){
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&',"&e该玩家不存在或不在线！"));
                        return false;
                    }else {
                        if (!SQLActions.playerDataContainsPlayer(player1.getUniqueId())){
                            SQLActions.createAccount(player1);
                            player.sendMessage("success");
                        }else {
                            player.sendMessage("exist!");
                            return false;
                        }
                    }
                }else {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&',this.no_permission));
                    return false;
                }
            }else if (args[0].equalsIgnoreCase("open")){
                if (player.hasPermission("ItemMail.open")){
                    Player player1 = Bukkit.getPlayerExact(args[1]);
                    if (player1 == null){
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&',"&e该玩家不存在或不在线！"));
                    }else {
                        Inventory inv_s = SQLActions.loaddata(player1);
                        player.openInventory(inv_s);
                    }
                }else {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&',this.no_permission));
                    return false;
                }
            }
            else {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&',"&cinvalid &ccommand"));
                return false;
            }
        }else {
            player.sendMessage("-----ItemMail Help-------");
            return false;
        }
        return true;
    }

    private boolean fullinventory(Inventory inventory){
        return inventory.firstEmpty()==-1;
    }

    private static int getAmount(Inventory inventory) {
        int amount = 0;
        for (int i = 0; i < inventory.getStorageContents().length; i++) {
            ItemStack slot = inventory.getItem(i);
            if (slot != null){
                amount += 1;
            }
        }
        return amount;
    }

    private static int getamount(Inventory inventory) {
        int amount = 0;
        for (int i = 0; i < inventory.getStorageContents().length; i++) {
            ItemStack slot = inventory.getItem(i);
            if (slot != null){
                amount += slot.getAmount();
            }
        }
        return amount;
    }

    private boolean empty(Inventory inventory){
        for(ItemStack it : inventory.getContents())
        {
            if(it != null) return false;
        }
        return true;
    }

    private boolean empty(ItemStack[] itemStacks){
        for (ItemStack it:itemStacks){
            if(it != null) return false;
        }
        return true;
    }

    private boolean sendtoal(Player player) {
        DecimalFormat df = new DecimalFormat("0.00 ");
        if (SQLActions.playerDataContainsPlayer(player.getUniqueId())) {
            Inventory inv_s = SQLActions.loaddata(player);
            ItemStack[] itemStack = player.getInventory().getStorageContents();

            Inventory midv = Bukkit.createInventory(null, slots, player.getName());
            midv.setStorageContents(inv_s.getStorageContents());

            if (empty(itemStack)) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', this.null_inventory));
                return false;
            }
            Double totalprice = price * getamount(player.getInventory());
            if (econ.has(player, totalprice)) {
                if (realAdd(midv, itemStack)) {
                    inv_s.addItem(itemStack);
                    ItemStack[] stacks1 = new ItemStack[itemStack.length];
                    SQLActions.uploaddata(player, inv_s);
                    player.getInventory().setStorageContents(stacks1);
                    player.updateInventory();
                    econ.withdrawPlayer(player, totalprice);
                    //等待给某op
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            "&b投递成功，花费 &6" + df.format(totalprice) + "&6D"));
                } else {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            this.no_slots+getAmount(inv_s)+"/"+inv_s.getSize()));
                    return false;
                }
            } else {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&b你没有足够的钱！"));
                return false;
            }
        } else {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&4No exist!"));
            return false;
        }
        return true;
    }

    private boolean sendsingle(Player player, Player self) {
        DecimalFormat df = new DecimalFormat("0.00 ");
        if (SQLActions.playerDataContainsPlayer(player.getUniqueId())) {
            Inventory inv_s = SQLActions.loaddata(player);
            ItemStack itemStack = self.getInventory().getItemInMainHand();

            Inventory midv = Bukkit.createInventory(null, slots, player.getName());
            midv.setStorageContents(inv_s.getStorageContents());

            if (itemStack != null && !itemStack.getType().equals(Material.AIR)) {
                ItemStack midit = new ItemStack(itemStack);
                if (econ.has(self, price * itemStack.getAmount())) {
                    if (realadd(midv, midit)) {
                        inv_s.addItem(itemStack);
                        SQLActions.uploaddata(player, inv_s);
                        self.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
                        self.updateInventory();
                        econ.withdrawPlayer(self, price * itemStack.getAmount());
                        self.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                "&b投递成功，花费 &6" +
                                        df.format(price * itemStack.getAmount()) + "&6D"));
                    } else {
                        self.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                this.no_slots+getAmount(inv_s)+"/"+inv_s.getSize()));
                    }
                } else {
                    self.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            "&4你没有足够的钱！"));
                }
            } else {
                self.sendMessage(ChatColor.translateAlternateColorCodes('&', this.null_mainhand));
                return false;
            }
        } else {
            self.sendMessage(ChatColor.translateAlternateColorCodes('&', "&4No exist this player!"));
            return false;
        }
        return true;
    }


    private boolean realAdd(Inventory inventory, ItemStack[] itemStacks){

        for (int i = 0; i < itemStacks.length; i++) {
            ItemStack it = itemStacks[i];
            if (it == null){
                itemStacks[i] = new ItemStack(Material.AIR);
            }
        }

        Map<Integer, ItemStack> leftover = inventory.addItem(itemStacks);
        return leftover.isEmpty();
    }


    private boolean realadd(Inventory inventory, ItemStack itemStack){

        Map<Integer, ItemStack> leftover = inventory.addItem(itemStack);

        return leftover.isEmpty();
    }

}
