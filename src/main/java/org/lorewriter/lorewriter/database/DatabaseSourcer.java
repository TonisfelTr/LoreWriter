package org.lorewriter.lorewriter.database;

import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.DriverManager;

public class DatabaseSourcer {

    private final String jdbcUrl;

    public DatabaseSourcer(String host, int port, String database, String username, String password, Logger logger) {
        // Формируем URL подключения напрямую
        this.jdbcUrl = String.format(
                "jdbc:mysql://%s:%d/%s?user=%s&password=%s&useSSL=false&serverTimezone=UTC",
                host, port, database, username, password
        );

        // Проверка доступности MySQL драйвера при запуске плагина
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            logger.info("[LoreWriter] JDBC драйвер MySQL успешно загружен.");
        } catch (ClassNotFoundException e) {
            logger.error("[LoreWriter] JDBC драйвер MySQL не найден!", e);
            throw new IllegalStateException("MySQL JDBC driver не найден!");
        }

        // Тестовое подключение к БД
        try (Connection connection = getConnection()) {
            logger.info("[LoreWriter] Успешное подключение к базе данных через JDBC!");
        } catch (SQLException e) {
            logger.error("[LoreWriter] Ошибка подключения к базе данных: {}", e.getMessage());
            throw new RuntimeException("Невозможно подключиться к БД.", e);
        }
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(jdbcUrl);
    }
}
