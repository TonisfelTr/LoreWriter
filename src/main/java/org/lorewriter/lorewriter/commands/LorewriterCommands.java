package org.lorewriter.lorewriter.commands;

import net.kyori.adventure.text.Component;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;

public class LorewriterCommands {

    public static Command.Parameterized lwCommand() {
        return Command.builder()
                .executor(context -> {
                    // Проверка: выполняет игрок
                    ServerPlayer player = context.cause().first(ServerPlayer.class)
                            .orElseThrow(() -> new CommandException(Component.text("Эту команду можно использовать только в игре!")));

                    // Создаём блок бедрока
                    ItemStack bedrock = ItemStack.builder()
                            .itemType(ItemTypes.BEDROCK)
                            .quantity(1)
                            .build();

                    // Пытаемся положить в инвентарь
                    player.inventory().offer(bedrock);

                    // Сообщение игроку
                    player.sendMessage(Component.text("Вы получили специальный блок для проверки!"));

                    return CommandResult.success();
                })
                .build();
    }
}
