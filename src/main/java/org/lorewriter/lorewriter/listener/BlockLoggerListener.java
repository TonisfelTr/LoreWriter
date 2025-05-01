package org.lorewriter.lorewriter.listener;

import org.lorewriter.lorewriter.log.LogEntry;
import org.lorewriter.lorewriter.log.LogManager;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.explosive.fused.PrimedTNT;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.world.ExplosionEvent;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.world.explosion.Explosion;
import org.spongepowered.api.world.server.ServerLocation;

import java.util.*;

public class BlockLoggerListener {

    private final LogManager logManager;
    private final Map<UUID, String> explosionSources = new HashMap<>();
    private final Set<String> recentBreakLocations = new HashSet<>();

    public BlockLoggerListener(LogManager logManager) {
        this.logManager = logManager;
    }

    @Listener
    public void onBlockChange(ChangeBlockEvent.All event) {
        Optional<ServerPlayer> playerOpt = event.cause().first(ServerPlayer.class);
        String playerName = playerOpt.map(ServerPlayer::name).orElse("Неизвестный источник");

        for (Transaction<BlockSnapshot> transaction : event.transactions()) {
            if (!transaction.isValid()) continue;

            if (transaction.original().state().equals(transaction.finalReplacement().state())) {
                continue;
            }

            Optional<ServerLocation> locationOpt = transaction.finalReplacement().location();
            if (locationOpt.isEmpty()) continue;

            ServerLocation location = locationOpt.get();
            String locationKey = location.world().key().asString() + ":" + location.blockPosition();

            String originalType = transaction.original().state().type().key(RegistryTypes.BLOCK_TYPE).asString();
            String finalType = transaction.finalReplacement().state().type().key(RegistryTypes.BLOCK_TYPE).asString();

            if (!transaction.original().state().type().equals(BlockTypes.AIR.get()) &&
                    transaction.finalReplacement().state().type().equals(BlockTypes.AIR.get())) {

                logAction(playerName, "сломал блок", location, originalType);
                recentBreakLocations.add(locationKey);

                if (transaction.original().state().type().equals(BlockTypes.TNT.get())) {
                    location.world().nearbyEntities(location.position(), 2).stream()
                            .filter(PrimedTNT.class::isInstance)
                            .map(PrimedTNT.class::cast)
                            .findFirst()
                            .ifPresent(tnt -> explosionSources.put(tnt.uniqueId(), playerName));
                }
            } else if (transaction.original().state().type().equals(BlockTypes.AIR.get()) &&
                    !transaction.finalReplacement().state().type().equals(BlockTypes.AIR.get())) {
                logAction(playerName, "поставил блок", location, finalType);
            }
        }
    }

    @Listener
    public void onBurn(ChangeBlockEvent.All event) {
        String source = "Неизвестный источник";

        for (Transaction<BlockSnapshot> transaction : event.transactions()) {
            if (!transaction.isValid()) continue;

            Optional<ServerLocation> locationOpt = transaction.finalReplacement().location();
            if (locationOpt.isEmpty()) continue;

            Optional<ServerPlayer> playerOpt = event.cause().first(ServerPlayer.class);
            if (playerOpt.isPresent()) source = playerOpt.get().name();

            ServerLocation location = locationOpt.get();
            String locationKey = location.world().key().asString() + ":" + location.blockPosition();

            if (recentBreakLocations.contains(locationKey)) {
                recentBreakLocations.remove(locationKey);
                continue;
            }

            if (!transaction.original().state().type().equals(BlockTypes.AIR.get()) &&
                    transaction.finalReplacement().state().type().equals(BlockTypes.FIRE.get())) {

                String blockType = transaction.original().state().type().key(RegistryTypes.BLOCK_TYPE).asString();
                logAction(source, "сжёг блок", location, blockType);
            }
        }
    }

    @Listener
    public void onExplosion(ExplosionEvent.Detonate event) {
        Explosion explosion = event.explosion();
        String source = explosion.sourceExplosive()
                .map(entity -> {
                    if (entity instanceof PrimedTNT tnt) {
                        return explosionSources.getOrDefault(tnt.uniqueId(), "minecraft:tnt");
                    }
                    return entity.type().key(RegistryTypes.ENTITY_TYPE).asString();
                })
                .orElse("Неизвестный источник");

        event.entities().stream()
                .filter(PrimedTNT.class::isInstance)
                .map(PrimedTNT.class::cast)
                .forEach(newTnt -> explosionSources.put(newTnt.uniqueId(), source));

        for (ServerLocation loc : event.affectedLocations()) {
            String blockType = loc.block().type().key(RegistryTypes.BLOCK_TYPE).asString();
            logAction(source, "взорвал блок", loc, blockType);
        }
    }

    private void logAction(String source, String action, ServerLocation location, String blockType) {
        logManager.logAction(new LogEntry(
                source,
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
