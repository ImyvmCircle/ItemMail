package com.imyvm;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

public class ItemMail extends JavaPlugin{

    private static final Logger log = Logger.getLogger("Minecraft");
    public static Economy econ = null;
    private FileConfiguration config = getConfig();
    private String host, database, username, password, port;
    private static String message_null_mainhand, message_null_inventory, message_enough_slots,
            message_no_permission, message_null_inv, moneyuuid;
    private Boolean useSSL, trustSSL;
    private static int slots;
    private static double price;
    public static ItemMail itemMail;
    static Connection c;
    private static String table;
    private static MySQL MySQL;
    private Command command;

    @Override
    public void onDisable() {
        log.info(String.format("[%s] Disabled Version %s", getDescription().getName(), getDescription().getVersion()));
    }

    @Override
    public void onEnable() {
        log.info(String.format("[%s] Enabled Version %s", getDescription().getName(), getDescription().getVersion()));

        config.addDefault("use_mysql",true);
        config.addDefault("host","localhost");
        config.addDefault("database", "ItemMail");
        config.addDefault("table","date");
        config.addDefault("port","3306");
        config.addDefault("username", "root");
        config.addDefault("password", "password");
        config.addDefault("useSSL",false);
        config.addDefault("trustSSL", false);
        config.addDefault("slots",36);
        config.addDefault("price",2.33);
        config.addDefault("moneyuuid", "a641c611-21ef-4b71-b327-e45ef8fdf647");

        config.addDefault("message_null_mainhand","&b你手上没有物品，无法投递！");
        config.addDefault("message_null_inventory","&b你背包内没有物品，无法投递!");
        config.addDefault("message_enough_slots","&b你没有足够的空间，远程物品箱状态:&e");
        config.addDefault("message_null_inv","&b你的远程物品箱没有物品可提取！");
        config.addDefault("message_no_permission","You don't have the permission!");

        config.options().copyDefaults(true);
        saveConfig();

        host = config.getString("host");
        database = config.getString("database");
        port = config.getString("port");
        username = config.getString("username");
        password = config.getString("password");
        table = config.getString("table");
        useSSL = config.getBoolean("useSSL");
        trustSSL = config.getBoolean("trustSSL");
        slots = config.getInt("slots");
        price = config.getDouble("price");
        moneyuuid = config.getString("moneyuuid");

        message_null_mainhand = config.getString("message_null_mainhand");
        message_null_inventory = config.getString("message_null_inventory");
        message_enough_slots = config.getString("message_enough_slots");
        message_no_permission = config.getString("message_no_permission");
        message_null_inv = config.getString("message_null_inv");

        MySQL = new MySQL(host, port, database, username, password, useSSL, trustSSL);
        try {
            c = MySQL.openConnection();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            getLogger().info("Found an exception connecting to the database. Make sure you've set your db.yml file up.");
        }

        SQLActions.createTable();

        /**
         * @author Holeyness
         * @description 防数据库连接丢失，每天查询一次数据库
         */
        Timer timer = new Timer();
        //每6小时查询一次数据库时间
        timer.schedule(new TimerTask() {
            public void run() {
                if(c != null){
                    String sql = "select sysDate();";
                    try {
                        Statement statement = c.createStatement();
                        ResultSet resultSet = statement.executeQuery(sql);
                        if(resultSet.next()){
                            System.out.println(resultSet.getDate("sysDate()"));
                        }

                        statement.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, 2000, 1000*3600*6);

        command = new Command(this);
        getCommand("itemmail").setExecutor(command);
        Bukkit.getServer().getPluginManager().registerEvents(new OnLogin(table, c), this);
        Bukkit.getServer().getPluginManager().registerEvents(new InventoryListener(this), this);


        RegisteredServiceProvider<Economy> economyP = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyP != null)
            econ = economyP.getProvider();
        else
            Bukkit.getLogger().info( "Unable to initialize Economy Interface with Vault!");
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    public static Economy getEcononomy() {
        return econ;
    }


    public static String getTable() {
        return table;
    }

    public static Integer getSlots(){
        return slots;
    }

    public static double getPrice(){return price;}

    public static String getMessage_null_mainhand() {
        return message_null_mainhand;
    }

    public static String getMessage_null_inventory() {
        return message_null_inventory;
    }

    public static String getMessage_enough_slots() {
        return message_enough_slots;
    }

    public static String getMessage_no_permission() {
        return message_no_permission;
    }

    public static String getMessage_null_inv() {
        return message_null_inv;
    }

    public static String getMoneyuuid() {
        return moneyuuid;
    }

}


