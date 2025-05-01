package org.lorewriter.lorewriter.log;

import org.lorewriter.lorewriter.database.DatabaseSourcer;
import org.spongepowered.api.Sponge;
import org.spongepowered.plugin.PluginContainer;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.*;
import java.util.concurrent.Executor;

public class LogManager {

    private final DatabaseSourcer databaseSourcer;
    private final List<LogEntry> buffer = new ArrayList<>();
    private final Object lock = new Object();
    private final int batchSize = 500;
    private final Logger logger;
    private final PluginContainer plugin;
    private final Executor executor;

    public LogManager(DatabaseSourcer databaseSourcer, PluginContainer plugin) {
        this.databaseSourcer = databaseSourcer;
        this.plugin = plugin;
        this.logger = plugin.logger();
        this.executor = Sponge.asyncScheduler().executor(plugin);

        checkOrCreateTable();
        startAutoFlush();
    }

    public void logAction(LogEntry entry) {
        synchronized (lock) {
            buffer.add(entry);
            if (buffer.size() >= batchSize) {
                flushAsync();
            }
        }
    }

    private void startAutoFlush() {
        executor.execute(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    Thread.sleep(5000);
                    flushAsync();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.warn("[LoreWriter] Поток автосброса логов остановлен.");
            }
        });
    }

    private void checkOrCreateTable() {
        executor.execute(() -> {
            try (Connection conn = databaseSourcer.getConnection()) {
                DatabaseMetaData meta = conn.getMetaData();
                try (ResultSet tables = meta.getTables(null, null, "block_logs", null)) {
                    if (!tables.next()) {
                        try (Statement stmt = conn.createStatement()) {
                            stmt.executeUpdate("""
                                CREATE TABLE block_logs (
                                    id INT AUTO_INCREMENT PRIMARY KEY,
                                    player VARCHAR(255),
                                    action VARCHAR(255),
                                    world VARCHAR(255),
                                    x INT,
                                    y INT,
                                    z INT,
                                    block_type VARCHAR(255),
                                    timestamp BIGINT,
                                    quantity INT DEFAULT 0
                                )
                                """);
                            logger.info("[LoreWriter] Таблица 'block_logs' успешно создана.");
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("[LoreWriter] Ошибка проверки/создания таблицы block_logs:", e);
            }
        });
    }

    private void flushAsync() {
        List<LogEntry> entriesToInsert;
        synchronized (lock) {
            if (buffer.isEmpty()) {
                return;
            }
            entriesToInsert = new ArrayList<>(buffer);
            buffer.clear();
        }

        executor.execute(() -> {
            try (Connection conn = databaseSourcer.getConnection()) {
                String sql = "INSERT INTO block_logs (player, action, world, x, y, z, block_type, timestamp, quantity) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    for (LogEntry entry : entriesToInsert) {
                        if ("minecraft:bedrock".equals(entry.getBlockType())) {
                            continue;
                        }

                        stmt.setString(1, entry.getPlayer());
                        stmt.setString(2, entry.getAction());
                        stmt.setString(3, entry.getWorld());
                        stmt.setInt(4, entry.getX());
                        stmt.setInt(5, entry.getY());
                        stmt.setInt(6, entry.getZ());
                        stmt.setString(7, entry.getBlockType());
                        stmt.setLong(8, entry.getTimestamp());
                        stmt.setInt(9, entry.getQuantity());
                        stmt.addBatch();
                    }
                    stmt.executeBatch();
                }
            } catch (Exception e) {
                logger.error("[LoreWriter] Ошибка при сохранении логов блоков:", e);
            }
        });
    }
}
