package org.lorewriter.lorewriter.config;

import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class DatabaseConfig {

    private final Path configPath;
    private ConfigurationNode rootNode;

    private String host = "localhost";
    private int port = 3306;
    private String database = "mydatabase";
    private String username = "root";
    private String password = "";

    public DatabaseConfig(Path configPath) {
        this.configPath = configPath;
    }

    public void load() {
        try {
            if (Files.notExists(configPath)) {
                saveDefaultConfig();
            }

            var loader = HoconConfigurationLoader.builder()
                    .path(configPath)
                    .build();

            rootNode = loader.load();

            host = rootNode.node("database", "host").getString("localhost");
            port = rootNode.node("database", "port").getInt(3306);
            database = rootNode.node("database", "name").getString("mydatabase");
            username = rootNode.node("database", "username").getString("root");
            password = rootNode.node("database", "password").getString("");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveDefaultConfig() {
        try {
            var loader = HoconConfigurationLoader.builder()
                    .path(configPath)
                    .build();

            ConfigurationNode node = loader.createNode();

            node.node("database", "host").set("localhost");
            node.node("database", "port").set(3306);
            node.node("database", "name").set("mydatabase");
            node.node("database", "username").set("root");
            node.node("database", "password").set("password");

            loader.save(node);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getDatabase() {
        return database;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
