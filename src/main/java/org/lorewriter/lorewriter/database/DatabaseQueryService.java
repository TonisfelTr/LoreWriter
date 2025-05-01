package org.lorewriter.lorewriter.database;

import org.spongepowered.api.Sponge;
import org.spongepowered.plugin.PluginContainer;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.concurrent.CompletableFuture;

public class DatabaseQueryService {

    private final String jdbcUrl;
    private final String username;
    private final String password;
    private final PluginContainer plugin;
    private final Logger logger;

    public DatabaseQueryService(String host, int port, String database, String username, String password, PluginContainer plugin, Logger logger) {
        this.jdbcUrl = String.format("jdbc:mysql:
        this.username = username;
        this.password = password;
        this.plugin = plugin;
        this.logger = logger;

        try (Connection connection = getConnection()) {
            logger.info("[LoreWriter] Успешное подключение к базе данных через JDBC!");
        } catch (SQLException e) {
            logger.error("[LoreWriter] Ошибка подключения к базе данных: {}", e.getMessage());
            throw new RuntimeException("Cannot initialize database connection.", e);
        }
    }

    public CompletableFuture<Void> printAllUsersAsync() {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users;");
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    logger.info("User: {}", rs.getString("name"));
                }
            } catch (SQLException e) {
                logger.error("[LoreWriter] Ошибка выполнения запроса:", e);
            }
        }, Sponge.asyncScheduler().executor(plugin));
    }

    public CompletableFuture<String> findUserNameByIdAsync(int userId) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement("SELECT name FROM users WHERE id = ?")) {
                stmt.setInt(1, userId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString("name");
                    } else {
                        return null;
                    }
                }
            } catch (SQLException e) {
                logger.error("[LoreWriter] Ошибка выполнения запроса:", e);
                return null;
            }
        }, Sponge.asyncScheduler().executor(plugin));
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(jdbcUrl, username, password);
    }
}
