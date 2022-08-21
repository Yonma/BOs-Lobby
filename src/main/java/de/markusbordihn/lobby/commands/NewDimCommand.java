package de.markusbordihn.lobby.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.markusbordihn.lobby.Constants;
import de.markusbordihn.lobby.config.CommonConfig;
import de.markusbordihn.lobby.dimension.DimensionManager;
import de.markusbordihn.lobby.teleporter.PlayerTeleportManager;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NewDimCommand extends CustomCommand {

    private static final CommonConfig.Config COMMON = CommonConfig.COMMON;
    private static boolean newdimRestrictCommand = COMMON.newdimRestrictCommand.get();
    private static boolean teleportDelayEnabled = COMMON.teleportDelayEnabled.get();
    private static int generalCommandCoolDown = COMMON.generalCommandCoolDown.get();
    private static int teleportDelayCounter = COMMON.teleportDelayCounter.get();


    private static Map<Player, Long> coolDownPlayerMap = new ConcurrentHashMap<>();

    private static final NewDimCommand command = new NewDimCommand();

    @SubscribeEvent
    public static void handleServerAboutToStartEvent(ServerAboutToStartEvent event) {
        newdimRestrictCommand = COMMON.newdimRestrictCommand.get();
        generalCommandCoolDown = COMMON.generalCommandCoolDown.get();
        teleportDelayCounter = COMMON.teleportDelayCounter.get();
        teleportDelayEnabled = COMMON.teleportDelayEnabled.get();
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        if (Boolean.FALSE.equals(COMMON.newdimEnabled.get())) {
            return;
        }
        registerCommand(COMMON.newdimCommandName.get(), COMMON.newdimDimensionName.get(),
                COMMON.newdimCommandPermissionLevel.get());
        dispatcher.register(Commands.literal(COMMON.newdimCommandName.get())
                .requires(cs -> cs.hasPermission(COMMON.newdimCommandPermissionLevel.get()))
                .executes(command));
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();

        // Handle cool-down time of command to avoid command misusage.
        Long coolDownTimer = coolDownPlayerMap.getOrDefault(player, null);
        Long currentTimer = java.time.Instant.now().getEpochSecond();
        if (coolDownTimer != null && coolDownTimer > currentTimer) {
            sendFeedback(context,
                    new TranslatableComponent(Constants.TELEPORT_FAILED_COOLDOWN,
                            COMMON.newdimDimensionName.get(), coolDownTimer - currentTimer)
                            .withStyle(ChatFormatting.RED));
            return 0;
        } else {
            coolDownPlayerMap.put(player, currentTimer + generalCommandCoolDown);
        }

        // Provide feedback to the player for their teleporter request.
        if (DimensionManager.getNewDimDimension() == null) {
            sendFeedback(context,
                    new TranslatableComponent(Constants.UNABLE_TO_TELEPORT_MESSAGE,
                    COMMON.newdimDimensionName.get(), DimensionManager.getNewDimDimensionName()));
        } else if (!newdimRestrictCommand || player.getLevel() != DimensionManager.getNewDimDimension()) {
            if (teleportDelayEnabled && teleportDelayCounter > 0) {
                sendFeedback(context,
                        new TranslatableComponent(Constants.TELEPORT_TO_IN_MESSAGE,
                                COMMON.newdimDimensionName.get(), teleportDelayCounter)
                                .withStyle(ChatFormatting.GREEN));
                PlayerTeleportManager.teleportPlayerToNewDim(player);
            } else {
                sendFeedback(context,
                        new TranslatableComponent(Constants.TELEPORT_TO_MESSAGE,
                                COMMON.newdimDimensionName.get()).withStyle(ChatFormatting.GREEN));

                DimensionManager.teleportToNewDim(player);
            }
        } else {
            sendFeedback(context,
                    new TranslatableComponent(Constants.TELEPORT_FAILED_ALREADY_IN_DIMENSION_MESSAGE,
                            COMMON.newdimDimensionName.get()).withStyle(ChatFormatting.YELLOW));
        }
        return 0;
    }
}
