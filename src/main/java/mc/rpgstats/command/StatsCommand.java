package mc.rpgstats.command;

import com.mojang.brigadier.CommandDispatcher;
import mc.rpgstats.component.internal.PlayerPreferencesComponent;
import mc.rpgstats.main.CustomComponents;
import mc.rpgstats.main.RPGStats;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class StatsCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("rpgstats")
                .executes(
                    (commandContext) -> execute(
                        commandContext.getSource(), (ServerPlayerEntity)commandContext.getSource().getEntity()
                    )
                ).then(
                CommandManager.literal("GUI").executes(
                    (commandContext) -> {
                        ServerCommandSource source = commandContext.getSource();
                        ServerPlayerEntity player = source.getPlayerOrThrow();
                        if (ServerPlayNetworking.canSend(player, RPGStats.OPEN_GUI)) {
                            ServerPlayNetworking.send(player, RPGStats.OPEN_GUI, PacketByteBufs.empty());
                            return 1;
                        } else {
                            source.sendError(Text.translatable("rpgstats.error.not_on_client"));
                            return 0;
                        }
                    }
                )
            ).then(CommandManager.literal("for")
                .then(
                    CommandManager.argument("targets", EntityArgumentType.player())
                        .executes(
                            (commandContext) -> execute(
                                commandContext.getSource(), EntityArgumentType.getPlayer(commandContext, "targets")
                            )
                        )
                )
            ).then(CommandManager.literal("toggleSetting")
                .then(CommandManager.literal("spamSneak")
                    .executes(
                        context -> {
                            PlayerPreferencesComponent component = CustomComponents.PREFERENCES.get(context.getSource().getPlayerOrThrow());
                            component.isOptedOutOfButtonSpam = !component.isOptedOutOfButtonSpam;
                            context.getSource().sendFeedback(Text.translatable("rpgstats.feedback.toggle_sneak", component.isOptedOutOfButtonSpam), false);
                            return 1;
                        }
                    )
                )
            )
        );
    }
    
    private static int execute(ServerCommandSource source, ServerPlayerEntity target) {
        if (source.getEntity() instanceof ServerPlayerEntity spe && target != null) {
    
            spe.sendMessage(Text.translatable("RPGStats > ")
                .formatted(Formatting.GREEN)
                .append(Text.translatable("rpgstats.stats_for", target.getEntityName()).formatted(Formatting.WHITE)), false);
            
            CustomComponents.components.keySet().forEach(identifier ->
                spe.sendMessage(RPGStats.getFormattedLevelData(identifier, target), false)
            );
        } else if (target != null) {
            if (source.getEntity() == null) {
                source.sendFeedback(Text.translatable("rpgstats.stats_for", target.getEntityName()), false);
    
                CustomComponents.components.keySet().forEach(identifier ->
                    source.sendFeedback(RPGStats.getNotFormattedLevelData(identifier, target), false)
                );
            } else {
                ServerPlayerEntity spe = (ServerPlayerEntity)source.getEntity();
                ServerPlayerEntity targeted = (ServerPlayerEntity)source.getEntity();
                
                spe.sendMessage(Text.literal("RPGStats > ")
                    .formatted(Formatting.GREEN)
                    .append(Text.translatable("rpgstats.stats_for", target.getEntityName()).formatted(Formatting.WHITE)), false);
    
                CustomComponents.components.keySet().forEach(identifier ->
                    spe.sendMessage(RPGStats.getFormattedLevelData(identifier, targeted), false)
                );
            }
        } else {
            source.sendError(Text.translatable("rpgstats.error.console_player_required"));
        }
        return 1;
    }
}