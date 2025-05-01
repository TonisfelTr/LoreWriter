package org.lorewriter.lorewriter.listener;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.lorewriter.lorewriter.database.DatabaseSourcer;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.world.server.ServerLocation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CompletableFuture;

public class BedrockCheckerListener {

    private final DatabaseSourcer databaseSourcer;

    public BedrockCheckerListener(DatabaseSourcer databaseSourcer) {
        this.databaseSourcer = databaseSourcer;
    }

    @Listener
    public void onBlockPlace(ChangeBlockEvent.All event) {
        ServerPlayer player = event.cause().first(ServerPlayer.class).orElse(null);
        if (player == null) return;

        for (Transaction<BlockSnapshot> transaction : event.transactions()) {
            if (!transaction.isValid()) continue;

            ServerLocation location = transaction.finalReplacement().location().orElse(null);
            if (location == null) continue;

            if (transaction.finalReplacement().state().type().equals(BlockTypes.BEDROCK.get())) {
                transaction.invalidate();
                checkLogsAt(player, location);
            }
        }
    }

    private void checkLogsAt(ServerPlayer player, ServerLocation loc) {
        player.sendMessage(Component.text("Проверка истории блока...").color(NamedTextColor.GOLD));

        CompletableFuture.runAsync(() -> {
            try (Connection conn = databaseSourcer.getConnection()) {
                String sql = "SELECT player, action, block_type, timestamp FROM block_logs WHERE world = ? AND x = ? AND y = ? AND z = ? ORDER BY timestamp ASC";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, loc.world().key().formatted());
                    stmt.setInt(2, loc.blockX());
                    stmt.setInt(3, loc.blockY());
                    stmt.setInt(4, loc.blockZ());

                    ResultSet rs = stmt.executeQuery();
                    boolean found = false;
                    while (rs.next()) {
                        found = true;
                        String p = rs.getString("player");
                        String a = rs.getString("action");
                        String block = rs.getString("block_type");
                        long time = rs.getLong("timestamp");

                        String formattedTime = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(new Date(time));
                        player.sendMessage(Component.text()
                                .append(Component.text("[", NamedTextColor.GOLD))
                                .append(Component.text(formattedTime, NamedTextColor.GOLD))
                                .append(Component.text("] ", NamedTextColor.GOLD))
                                .append(Component.text(p + " " + a + " " + block))
                                .build());
                    }
                    if (!found) {
                        player.sendMessage(Component.text("Нет записей по этим координатам."));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                player.sendMessage(Component.text("Ошибка чтения истории блока."));
            }
        });
    }
}
