package org.lorewriter.lorewriter.config;

import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.serialize.SerializationException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigManager {

    private static final Path configFolder = Path.of("config", "lorewriter");
    private static final Path configFile = configFolder.resolve("config.conf");
    private ConfigurationNode rootNode;
    private HoconConfigurationLoader loader;

    public void load() {
        try {
            if (!Files.exists(configFolder)) {
                Files.createDirectories(configFolder);
            }
            loader = HoconConfigurationLoader.builder()
                    .path(configFile)
                    .build();

            if (!Files.exists(configFile)) {
                
                ConfigurationNode node = loader.load();
                node.node("logBlockBreaks").set(true);
                node.node("logBlockPlaces").set(true);
                node.node("database", "host").set("localhost");
                node.node("database", "port").set(3306);
                node.node("database", "database").set("lorewriter");
                node.node("database", "username").set("loreuser");
                node.node("database", "password").set("lorepass");
                loader.save(node);
            }

            
            rootNode = loader.load();

        } catch (IOException e) {
            throw new RuntimeException("Ошибка при загрузке конфига LoreWriter", e);
        }
    }

    public boolean shouldLogBlockBreaks() {
        return rootNode.node("logBlockBreaks").getBoolean(true);
    }

    public boolean shouldLogBlockPlaces() {
        return rootNode.node("logBlockPlaces").getBoolean(true);
    }

    public String getDatabaseHost() {
        return rootNode.node("database", "host").getString("localhost");
    }

    public int getDatabasePort() {
        return rootNode.node("database", "port").getInt(3306);
    }

    public String getDatabaseName() {
        return rootNode.node("database", "database").getString("lorewriter");
    }

    public String getDatabaseUsername() {
        return rootNode.node("database", "username").getString("loreuser");
    }

    public String getDatabasePassword() {
        return rootNode.node("database", "password").getString("lorepass");
    }
}
