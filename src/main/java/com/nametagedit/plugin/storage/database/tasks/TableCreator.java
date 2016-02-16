package com.nametagedit.plugin.storage.database.tasks;

import com.zaxxer.hikari.HikariDataSource;
import lombok.AllArgsConstructor;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class TableCreator extends BukkitRunnable {

    private HikariDataSource hikari;

    private final List<String> QUERIES = new ArrayList<String>() {{
        add("CREATE TABLE IF NOT EXISTS `nte_players` (`uuid` varchar(64) NOT NULL, `name` varchar(16) NOT NULL, `prefix` varchar(16) NOT NULL, `suffix` varchar(16) NOT NULL, PRIMARY KEY (`uuid`))");
        add("CREATE TABLE IF NOT EXISTS `nte_groups` (`name` varchar(64) NOT NULL, `permission` varchar(16) NOT NULL, `prefix` varchar(16) NOT NULL, `suffix` varchar(16) NOT NULL, PRIMARY KEY (`name`))");
    }};

    @Override
    public void run() {
        try (Connection connection = hikari.getConnection()) {
            for (String query : QUERIES) {
                PreparedStatement insert = connection.prepareStatement(query);
                insert.execute();
                insert.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}