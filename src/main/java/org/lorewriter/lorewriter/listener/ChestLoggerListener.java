package org.lorewriter.lorewriter.listener;

import org.lorewriter.lorewriter.log.LogEntry;
import org.lorewriter.lorewriter.log.LogManager;
import org.spongepowered.api.block.entity.BlockEntity;
import org.spongepowered.api.block.entity.carrier.CarrierBlockEntity;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.item.inventory.container.ClickContainerEvent;
import org.spongepowered.api.event.item.inventory.container.InteractContainerEvent;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.world.server.ServerLocation;

import java.util.Optional;

public class ChestLoggerListener {

    private final LogManager logManager;

    public ChestLoggerListener(LogManager logManager) {
        this.logManager = logManager;
    }

    // Открытие контейнера
    @Listener
    public void onContainerOpen(InteractContainerEvent.Open event) {
        Optional<ServerPlayer> playerOpt = event.cause().first(ServerPlayer.class);
        if (!playerOpt.isPresent()) return;

        ServerPlayer player = playerOpt.get();

        // Получение местоположения контейнера
        Optional<ServerLocation> locationOpt = event.cause().first(ServerLocation.class);

        if (!locationOpt.isPresent()) return;

        ServerLocation location = locationOpt.get();
        String blockType = location.block().type().key(RegistryTypes.BLOCK_TYPE).asString();

        logAction(player.name(), "открыл контейнер", location, blockType);
    }

    // Закрытие контейнера
    @Listener
    public void onContainerClose(InteractContainerEvent.Close event) {
        Optional<ServerPlayer> playerOpt = event.cause().first(ServerPlayer.class);
        if (!playerOpt.isPresent()) return;

        ServerPlayer player = playerOpt.get();

        Optional<ServerLocation> locationOpt = event.cause().first(ServerLocation.class);

        if (!locationOpt.isPresent()) return;

        ServerLocation location = locationOpt.get();
        String blockType = location.block().type().key(RegistryTypes.BLOCK_TYPE).asString();

        logAction(player.name(), "закрыл контейнер", location, blockType);
    }

    // Извлечение предметов из контейнера
    @Listener
    public void onItemTaken(ClickContainerEvent event) {
        Optional<ServerPlayer> playerOpt = event.cause().first(ServerPlayer.class);
        if (!playerOpt.isPresent()) return;
        ServerPlayer player = playerOpt.get();

        Optional<ServerLocation> locationOpt = event.cause().first(ServerLocation.class);
        if (!locationOpt.isPresent()) return;
        ServerLocation location = locationOpt.get();

        event.transactions().forEach(transaction -> {
            if (transaction.original().isEmpty()) return;

            ItemStack itemStack = transaction.original().asMutableCopy();
            String itemType = itemStack.type().key(RegistryTypes.ITEM_TYPE).asString();
            int quantity = itemStack.quantity();

            logAction(player.name(),
                    "извлёк",
                    location,
                    itemType,
                    quantity);
        });
    }

    private void logAction(String player, String action, ServerLocation location, String blockType) {
        logManager.logAction(new LogEntry(
                player,
                action,
                location.world().key().asString(),
                location.blockX(),
                location.blockY(),
                location.blockZ(),
                blockType,
                System.currentTimeMillis()
        ));
    }

    private void logAction(String player, String action, ServerLocation location, String blockType, int quantity) {
        logManager.logAction(new LogEntry(
                player,
                action,
                location.world().key().asString(),
                location.blockX(),
                location.blockY(),
                location.blockZ(),
                blockType,
                System.currentTimeMillis()
        ));
    }
}
