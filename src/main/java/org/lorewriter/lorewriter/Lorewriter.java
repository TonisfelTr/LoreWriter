package org.lorewriter.lorewriter;

import com.google.inject.Inject;
import org.lorewriter.lorewriter.commands.LorewriterCommands;
import org.lorewriter.lorewriter.config.ConfigManager;
import org.lorewriter.lorewriter.database.DatabaseSourcer;
import org.lorewriter.lorewriter.listener.BedrockCheckerListener;
import org.lorewriter.lorewriter.listener.BlockLoggerListener;
import org.lorewriter.lorewriter.listener.ChestLoggerListener;
import org.lorewriter.lorewriter.log.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.*;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

@Plugin("lorewriter")
public class Lorewriter {

    private final PluginContainer container;
    @Inject
    private final Logger logger;

    private ConfigManager configManager;
    private DatabaseSourcer databaseSourcer;
    private LogManager logManager;

    @Inject
    public Lorewriter(final PluginContainer container) {
        this.container = container;
        this.logger = container.logger(); // вот так достаём логгер
    }

    @Listener
    public void onConstructPlugin(final ConstructPluginEvent event) {
        logger.info("[LoreWriter] Плагин загружается...");
    }

    @Listener
    public void onRegisterCommands(final RegisterCommandEvent<Command.Parameterized> event) {
        event.register(this.container, LorewriterCommands.lwCommand(), "lw", "lorewriter");
    }

    @Listener
    public void onStartingServer(final StartingEngineEvent<Server> event) {
        this.configManager = new ConfigManager();
        configManager.load();
        logger.info("[LoreWriter] Конфигурация загружена!");

        try {
            this.databaseSourcer = new DatabaseSourcer(
                    configManager.getDatabaseHost(),
                    configManager.getDatabasePort(),
                    configManager.getDatabaseName(),
                    configManager.getDatabaseUsername(),
                    configManager.getDatabasePassword(),
                    this.logger
            );
        } catch (Exception e) {
            logger.error("[LoreWriter] Не удалось подключиться к базе данных: {}", e.getMessage());
            Sponge.server().shutdown();
        }

        this.logManager = new LogManager(this.databaseSourcer, this.container);

        Sponge.game().eventManager().registerListeners(this.container, new BlockLoggerListener(this.logManager));
        Sponge.game().eventManager().registerListeners(this.container, new BedrockCheckerListener(this.databaseSourcer));
        Sponge.game().eventManager().registerListeners(this.container, new ChestLoggerListener(this.logManager));
    }

    @Listener
    public void onStartedServer(final StartedEngineEvent<Server> event) {
        logger.info("[LoreWriter] Плагин успешно запущен!");
    }

    @Listener
    public void onStoppingServer(final StoppingEngineEvent<Server> event) {
        logger.info("[LoreWriter] LoreWriter останавливается...");
    }
}
