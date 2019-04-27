package com.imyvm.ItemMail;

import org.apache.commons.collections4.map.PassiveExpiringMap;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.ShulkerBox;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

import java.text.DecimalFormat;
import java.util.*;

import static com.imyvm.ItemMail.ItemMail.econ;

public class Command implements CommandExecutor {

    ItemMail itemMail;
    public Command(ItemMail it){
        it = itemMail;
    }
    private static int slots = ItemMail.getSlots();
    private static double price = ItemMail.getPrice();
    private String null_mainhand = ItemMail.getMessage_null_mainhand();
    private String null_inventory = ItemMail.getMessage_null_inventory();
    private String no_slots = ItemMail.getMessage_enough_slots();
    private String no_permission = ItemMail.getMessage_no_permission();
    private String null_inv = ItemMail.getMessage_null_inv();
    private String moneyuuid = ItemMail.getMoneyuuid();
    private String message_received = ItemMail.getMessage_received();
    private PassiveExpiringMap<Player, List> map = new PassiveExpiringMap<>(30000);

    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command cmdObj, String label, String[] args) {
        if (!(sender instanceof Player)){
            return false;
        }
        Player player = (Player) sender;
        if (args.length==0){
            commandmessage(player);
            return false;
        }else if (args.length==1){
            String cmd = args[0];
            if (cmd.equalsIgnoreCase("send")){
                if (sender.hasPermission("ItemMail.send.self") || sender.hasPermission("ItemMail.player.*")) {
                    sendsingle(player, player);
                }else {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&',this.null_mainhand));
                    return false;
                }
            }else if (cmd.equalsIgnoreCase("open")) {
                if (player.hasPermission("ItemMail.open") || player.hasPermission("ItemMail.player.*")){
                    Inventory inv_s = SQLActions.loaddata(player);
                    player.openInventory(inv_s);
                }else {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&',this.no_permission));
                    return false;
                }
            }else if (cmd.equalsIgnoreCase("create")){
                if ( player.hasPermission("ItemMail.create") || player.hasPermission("ItemMail.admin.*")){
                    if (!SQLActions.playerDataContainsPlayer(player.getUniqueId())){
                        SQLActions.createAccount(player);
                        player.sendMessage("success");
                    }else {
                        player.sendMessage("exist!");
                    }
                }else {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&',this.no_permission));
                    return false;
                }
            }else if (cmd.equalsIgnoreCase("get")){
                if (player.hasPermission("ItemMail.get") || sender.hasPermission("ItemMail.player.*")){
                    Inventory playerinv = player.getInventory();
                    Inventory inventory = SQLActions.loaddata(player);
                    int inv_amount = inventory.getSize();
                    if (empty(inventory)){
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&',this.null_inv));
                        return false;
                    }
                    ItemStack[] mid = playerinv.getStorageContents();

                    Inventory midv = Bukkit.createInventory(null, slots, "ItemMail for imyvm");
                    midv.setStorageContents(mid);

                    ItemStack[] items = inventory.getStorageContents();

                    if (realAdd(midv, items) || ((mid.length-getAmount(playerinv))>=getAmount(inventory))){
                        playerinv.addItem(items);
                        player.updateInventory();
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&',"&b提取成功"));
                        Inventory inv1 = Bukkit.createInventory(null, slots, "ItemMail for imyvm");
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
                if (player.hasPermission("ItemMail.send.total") || sender.hasPermission("ItemMail.player.*")){
                    return sendtoal(player);
                }else {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&',this.no_permission));
                    return false;
                }
            } else if (cmd.equalsIgnoreCase("confirm")) {
                if (!player.hasPermission("ItemMail.send.confirm")) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', this.no_permission));
                    return false;
                }
                if (!map.containsKey(player)) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            "&c你没有待确认的投递单！"));
                    return false;
                }
                List list = map.get(player);
                Player receiver = (Player) list.get(0);
                Double price = (Double) list.get(2);
                if (list.get(1) instanceof ItemStack) {
                    ItemStack itemStack = (ItemStack) list.get(1);
                    return process(itemStack, player, receiver, price);
                } else {
                    ItemStack[] itemStack = (ItemStack[]) list.get(1);
                    return process(itemStack, player, receiver, price);
                }

            } else {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&',"&cinvalid &ccommand"));
                return false;
            }
        }else if (args.length==2){
            if (args[0].equalsIgnoreCase("send")){
                if (player.hasPermission("ItemMail.send.others") || sender.hasPermission("ItemMail.player.*")){
                    Player player1 = Bukkit.getPlayerExact(args[1]);
                    if (player1 == null){
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&',"&e该玩家不存在或不在线！"));
                        return false;
                    }else {
                        if (sendsingle(player1, player)) {
                            player1.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                    message_received.replace("{player}", player.getDisplayName())));
                            return true;
                        } else {
                            return false;
                        }
                    }
                }else {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&',this.no_permission));
                    return false;
                }
            }else if (args[0].equalsIgnoreCase("create")){
                if (player.hasPermission("ItemMail.create") || player.hasPermission("ItemMail.admin.*")){
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
                if (player.hasPermission("ItemMail.open.others") || player.hasPermission("ItemMail.admin.*")){
                    /*
                    Player player1 = Bukkit.getPlayerExact(args[1]);
                    if (player1 == null){
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&',"&e该玩家不存在或不在线！"));
                    }else {
                        Inventory inv_s = SQLActions.loaddata(player1);
                        player.openInventory(inv_s);
                    }
                    */
                    if (!SQLActions.playerDataContainsPlayer(args[1])){
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                "&4该玩家不存在"));
                    }else {
                        Inventory inv_s = SQLActions.loaddata(args[1]);
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
            commandmessage(player);
            return false;
        }
        return true;
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

    private static int getrealamount(ItemStack itemStack) {
        int amount_Box = 0;
        if (itemStack.getItemMeta() instanceof BlockStateMeta) {
            BlockStateMeta im = (BlockStateMeta) itemStack.getItemMeta();
            if (im.getBlockState() instanceof ShulkerBox) {
                ShulkerBox shulkerBox = (ShulkerBox) im.getBlockState();
                amount_Box = getamount(shulkerBox.getInventory());
            }
        }
        return amount_Box;
    }

    private static int getrealamount(ItemStack[] itemStacks) {
        int total_amount;
        total_amount = Arrays.stream(itemStacks).filter(Objects::nonNull).mapToInt(Command::getrealamount).sum();
        return total_amount;
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

            // Items: itemStack
            ItemStack[] itemStack = player.getInventory().getStorageContents();

            if (empty(itemStack)) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', this.null_inventory));
                return false;
            }

            // Price: totalprice
            double totalprice = getTotalPrice(getamount(player.getInventory()) + getrealamount(itemStack));

            List<Object> list = new ArrayList<>();
            list.add(player);
            list.add(itemStack);
            list.add(totalprice);
            map.put(player, list);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    "&c本次投递需花费" + df.format(totalprice) + "&bD&c,请在30秒内输入/imail confirm 确认投递"));

        } else {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&4No exist!"));
            return false;
        }
        return true;
    }

    private boolean sendsingle(Player player, Player self) {
        DecimalFormat df = new DecimalFormat("0.00 ");
        if (SQLActions.playerDataContainsPlayer(player.getUniqueId())) {

            // Items
            ItemStack itemStack = self.getInventory().getItemInMainHand();
            if (itemStack.getType().equals(Material.AIR)) {
                self.sendMessage(ChatColor.translateAlternateColorCodes('&', this.null_mainhand));
                return false;
            }

            // Price
            int amount_Box = getrealamount(itemStack);
            ItemStack midit = new ItemStack(itemStack);
            double tprice = getTotalPrice(itemStack.getAmount() + amount_Box);

            List<Object> list = new ArrayList<>();
            list.add(player);
            list.add(itemStack);
            list.add(tprice);
            map.put(self, list);
            self.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    "&c本次投递需花费" + df.format(tprice) + "&bD&c,请在30秒内输入/imail confirm 确认投递"));
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

    private void commandmessage(Player player){
        player.sendMessage(ChatColor.translateAlternateColorCodes('&',"&b-----ItemMail Help-------"));
        player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                "&e/itemmail send     - 传送手上的物品至远程物品箱"));
        player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                "&e/itemmail sendtotal     - 传送背包内的物品至远程物品箱"));
        player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                "&e/itemmail send [player]     - 传送物品至其他人"));
        player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                "&e/itemmail get     - 提取物品"));
        player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                "&e/itemmail open     - 查看远程物品箱"));
    }

    private boolean process(ItemStack[] itemStack, Player self, Player player, Double price) {

        DecimalFormat df = new DecimalFormat("0.00 ");

        // Remote Inventory: midv
        Inventory inv_s = SQLActions.loaddata(player);
        Inventory midv = Bukkit.createInventory(null, slots, "ItemMail for imyvm");
        midv.setStorageContents(inv_s.getStorageContents());

        // Process
        if (econ.has(self, price)) {
            if (realAdd(midv, itemStack) || (getAmount(self.getInventory()) <= (inv_s.getSize() - getAmount(inv_s)))) {
                inv_s.addItem(itemStack);
                SQLActions.uploaddata(player, inv_s);
                ItemStack[] stacks1 = new ItemStack[itemStack.length];
                self.getInventory().setStorageContents(stacks1);
                self.updateInventory();
                econ.withdrawPlayer(self, price);
                econ.depositPlayer(Bukkit.getOfflinePlayer(UUID.fromString(moneyuuid)), price);
                self.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        "&b投递成功，花费 &6" + df.format(price) + "&6D"));
            } else {
                self.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        this.no_slots + getAmount(inv_s) + "/" + inv_s.getSize()));
                return false;
            }
        } else {
            self.sendMessage(ChatColor.translateAlternateColorCodes('&', "&b你没有足够的钱！"));
            return false;
        }
        return true;
    }

    private boolean process(ItemStack itemStack, Player self, Player player, Double price) {

        DecimalFormat df = new DecimalFormat("0.00 ");

        // Remote Inventory: midv
        Inventory inv_s = SQLActions.loaddata(player);
        Inventory midv = Bukkit.createInventory(null, slots, "ItemMail for imyvm");
        midv.setStorageContents(inv_s.getStorageContents());

        // Process
        if (econ.has(self, price)) {
            if (realadd(midv, itemStack) || (getAmount(player.getInventory()) <= (inv_s.getSize() - getAmount(inv_s)))) {
                inv_s.addItem(itemStack);
                SQLActions.uploaddata(player, inv_s);
                self.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
                self.updateInventory();
                econ.withdrawPlayer(self, price);
                econ.depositPlayer(Bukkit.getOfflinePlayer(UUID.fromString(moneyuuid)), price);
                self.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        "&b投递成功，花费 &6" +
                                df.format(price) + "&6D"));
            } else {
                self.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        this.no_slots + getAmount(inv_s) + "/" + inv_s.getSize()));
                return false;
            }
        } else {
            self.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    "&4你没有足够的钱！"));
            return false;
        }
        return true;
    }

    private double getTotalPrice(int num) {
        if (num <= 648) {
            return price * num;
        } else if (num <= 2304) {
            return price * 648 + price * 0.5 * (num - 648);
        } else {
            return price * 648 + price * 0.5 * 1656 + price * 0.05 * (num - 2304);
        }
    }

}
