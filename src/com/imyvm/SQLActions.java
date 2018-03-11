package com.imyvm;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

import com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

public class SQLActions {

    private static Connection c = ItemMail.c;
    private static int slots = ItemMail.getSlots();
    private static String table = ItemMail.getTable();


    public synchronized static boolean playerDataContainsPlayer(UUID uid) {
        try {
            Statement sql = c.createStatement();
            ResultSet resultSet = sql.executeQuery("SELECT * FROM `" + table + "` WHERE `player_uuid` = '" + uid + "';");
            boolean containsPlayer = resultSet.next();

            sql.close();
            resultSet.close();

            return containsPlayer;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public synchronized static boolean playerDataContainsPlayer(String player) {
        try {
            Statement sql = c.createStatement();
            ResultSet resultSet = sql.executeQuery("SELECT * FROM `" + table + "` WHERE `player` = '" + player + "';");
            boolean containsPlayer = resultSet.next();

            sql.close();
            resultSet.close();

            return containsPlayer;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void createTable() {
        try {
            Statement tableCreate = c.createStatement();

//			tableCreate
//					.execute("CREATE TABLE IF NOT EXISTS `"
//							+ table
//							+ "` (`player_id` int(11) NOT NULL PRIMARY KEY AUTO_INCREMENT, `player` varchar(255) NOT NULL, `player_uuid` varchar(255) NOT NULL, `money` int(20) NOT NULL, `active` int(1) NOT NULL DEFAULT '1') ENGINE=InnoDB DEFAULT CHARSET=latin1;");
            /**
             * @author mononokehimi -> copy for chi3llini
             */
            tableCreate
                    .execute("CREATE TABLE IF NOT EXISTS `"
                            + table
                            + "` (`player_id` int(11) NOT NULL PRIMARY KEY AUTO_INCREMENT, `player` varchar(255) NOT NULL, `player_uuid` varchar(255) NOT NULL, `data` TEXT NOT NULL) ENGINE=InnoDB DEFAULT CHARSET=latin1;");

//			System.out.println("[SQLEconomy] Created/checked the database table");
            tableCreate.close();
        } catch (MySQLSyntaxErrorException e) {
            e.printStackTrace();
            System.out.println("[ItemMail] There was a snag initializing the database. Please send the ENTIRE stack trace above.");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("[ItemMail] There was a snag initializing the database. Make sure you set up the config!");
        }
    }


    public static boolean createAccount(Player player) {
        try {
            PreparedStatement econRegister = c.prepareStatement("INSERT INTO `" + table + "` (player, player_uuid, data) VALUES (?, ?, ?);");
            econRegister.setString(1, player.getName());
            econRegister.setString(2, player.getUniqueId().toString());
            Inventory inv1 = Bukkit.createInventory(null, slots, "ItemMail for imyvm");
            String air = BukkitSerialization.toBase64(inv1);
            econRegister.setString(3, air);
            econRegister.executeUpdate();
            econRegister.close();
            return true;
        } catch (SQLException e) {
            System.out.println("[ItemMail] Error creating user!");
        }

        return false;
    }

    public static Inventory loaddata(Player player){
        try {
            Statement sql = c.createStatement();
            ResultSet resultSet = sql.executeQuery("SELECT * FROM `" + table + "` WHERE `player` = '" + player.getName() + "';");
            if(resultSet.next()) {              // here
                String inv_s = resultSet.getString("data");
                return BukkitSerialization.fromBase64(inv_s);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Bukkit.createInventory(null, InventoryType.CHEST , player.getName());
    }

    public static boolean uploaddata(Player player, Inventory inventory){
        try {
            PreparedStatement econRegister = c.prepareStatement("UPDATE `" + table + "` SET data =? WHERE player_uuid=?;");
            String inv_s = BukkitSerialization.toBase64(inventory);
            econRegister.setString(1, inv_s);
            econRegister.setString(2, player.getUniqueId().toString());
            econRegister.executeUpdate();
            econRegister.close();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("[ItemMail] Error send!");
        }
        return false;
    }

}
