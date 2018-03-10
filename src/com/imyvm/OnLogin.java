package com.imyvm;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

public class OnLogin implements Listener {

    private String table;

    private Connection c;

    public OnLogin(String table, Connection c) {
        this.table = table;
        this.c = c;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();

        try {
            if (!SQLActions.playerDataContainsPlayer(player.getUniqueId())) {
                SQLActions.createAccount(player);
                System.out.println("[ItemMail] Added user " + player.getName() + " to the itemmail database.");
            } else {
                // make sure the stored player name is kept current

                Statement statement = c.createStatement();
                statement.executeUpdate("UPDATE `" + table + "` SET player = '" + player.getName()
                        + "' WHERE player_uuid = '" + player.getUniqueId().toString() + "';");
                statement.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
