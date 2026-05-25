package com.compassmod;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.network.chat.Component;

public class CompassCommand {

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(
                ClientCommandManager.literal("compasstrack")
                    .then(ClientCommandManager.literal("add")
                        .then(ClientCommandManager.argument("player", StringArgumentType.word())
                            .executes(ctx -> {
                                String name = StringArgumentType.getString(ctx, "player");
                                CompassHud.addTrackedPlayer(name);
                                ctx.getSource().sendFeedback(
                                    Component.literal("§6[Compass] §fNow tracking: §e" + name)
                                );
                                return 1;
                            })
                        )
                    )
                    .then(ClientCommandManager.literal("remove")
                        .then(ClientCommandManager.argument("player", StringArgumentType.word())
                            .executes(ctx -> {
                                String name = StringArgumentType.getString(ctx, "player");
                                CompassHud.removeTrackedPlayer(name);
                                ctx.getSource().sendFeedback(
                                    Component.literal("§6[Compass] §fStopped tracking: §e" + name)
                                );
                                return 1;
                            })
                        )
                    )
                    .then(ClientCommandManager.literal("list")
                        .executes(ctx -> {
                            var list = CompassHud.getTrackedPlayers();
                            if (list.isEmpty()) {
                                ctx.getSource().sendFeedback(
                                    Component.literal("§6[Compass] §fTracking all players in range")
                                );
                            } else {
                                ctx.getSource().sendFeedback(
                                    Component.literal("§6[Compass] §fTracked: §e" + String.join(", ", list))
                                );
                            }
                            return 1;
                        })
                    )
                    .then(ClientCommandManager.literal("clear")
                        .executes(ctx -> {
                            CompassHud.getTrackedPlayers().clear();
                            ctx.getSource().sendFeedback(
                                Component.literal("§6[Compass] §fCleared tracked players list")
                            );
                            return 1;
                        })
                    )
            );
        });
    }
}
