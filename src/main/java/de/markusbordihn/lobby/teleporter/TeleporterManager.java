/**
 * Copyright 2022 Markus Bordihn
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package de.markusbordihn.lobby.teleporter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

import dev.ftb.mods.ftbessentials.util.FTBEPlayerData;

import de.markusbordihn.lobby.Constants;
import de.markusbordihn.lobby.config.CommonConfig;
import de.markusbordihn.lobby.dimension.DimensionManager;

@EventBusSubscriber
public class TeleporterManager {

  private static final Logger log = LogManager.getLogger(Constants.LOG_NAME);

  private static final CommonConfig.Config COMMON = CommonConfig.COMMON;

  // Custom spawn definitions
  private static boolean defaultUseCustomSpawnPoint = COMMON.defaultUseCustomSpawnPoint.get();
  private static int defaultSpawnPointX = COMMON.defaultSpawnPointX.get();
  private static int defaultSpawnPointY = COMMON.defaultSpawnPointY.get();
  private static int defaultSpawnPointZ = COMMON.defaultSpawnPointZ.get();

  private static boolean fishingUseCustomSpawnPoint = COMMON.fishingUseCustomSpawnPoint.get();
  private static int fishingSpawnPointX = COMMON.fishingSpawnPointX.get();
  private static int fishingSpawnPointY = COMMON.fishingSpawnPointY.get();
  private static int fishingSpawnPointZ = COMMON.fishingSpawnPointZ.get();

  private static boolean newdimUseCustomSpawnPoint = COMMON.newdimUseCustomSpawnPoint.get();
  private static int newdimSpawnPointX = COMMON.newdimSpawnPointX.get();
  private static int newdimSpawnPointY = COMMON.newdimSpawnPointY.get();
  private static int newdimSpawnPointZ = COMMON.newdimSpawnPointZ.get();

  private static boolean gamingUseCustomSpawnPoint = COMMON.gamingUseCustomSpawnPoint.get();
  private static int gamingSpawnPointX = COMMON.gamingSpawnPointX.get();
  private static int gamingSpawnPointY = COMMON.gamingSpawnPointY.get();
  private static int gamingSpawnPointZ = COMMON.gamingSpawnPointZ.get();

  private static boolean lobbyUseCustomSpawnPoint = COMMON.lobbyUseCustomSpawnPoint.get();
  private static int lobbySpawnPointX = COMMON.lobbySpawnPointX.get();
  private static int lobbySpawnPointY = COMMON.lobbySpawnPointY.get();
  private static int lobbySpawnPointZ = COMMON.lobbySpawnPointZ.get();

  private static boolean miningUseCustomSpawnPoint = COMMON.miningUseCustomSpawnPoint.get();
  private static int miningSpawnPointX = COMMON.miningSpawnPointX.get();
  private static int miningSpawnPointY = COMMON.miningSpawnPointY.get();
  private static int miningSpawnPointZ = COMMON.miningSpawnPointZ.get();

  private static boolean voidUseCustomSpawnPoint = COMMON.voidUseCustomSpawnPoint.get();
  private static int voidSpawnPointX = COMMON.voidSpawnPointX.get();
  private static int voidSpawnPointY = COMMON.voidSpawnPointY.get();
  private static int voidSpawnPointZ = COMMON.voidSpawnPointZ.get();

  // Default spawn points for the default structures
  private static BlockPos defaultFishingSpawnPoint = new BlockPos(42, 51, 12);
  private static BlockPos defaultNewDimSpawnPoint = new BlockPos(42, 51, 12);
  private static BlockPos defaultGamingSpawnPoint = new BlockPos(0, 4, 0);
  private static BlockPos defaultLobbySpawnPoint = new BlockPos(9, 11, 9);
  private static BlockPos defaultMiningSpawnPoint = new BlockPos(203, 9, 560);
  private static BlockPos defaultVoidSpawnPoint = new BlockPos(0, 4, 0);

  // Clickable commands
  private static Component fishingCommand;
  private static Component newdimCommand;
  private static Component gamingCommand;
  private static Component lobbyCommand;
  private static Component miningCommand;
  private static Component spawnCommand;
  private static Component voidCommand;

  protected TeleporterManager() {}

  @SubscribeEvent
  public static void handleServerAboutToStartEvent(ServerAboutToStartEvent event) {
    // Make sure we have the current config settings.
    defaultSpawnPointX = COMMON.defaultSpawnPointX.get();
    defaultSpawnPointY = COMMON.defaultSpawnPointY.get();
    defaultSpawnPointZ = COMMON.defaultSpawnPointZ.get();

    fishingUseCustomSpawnPoint = COMMON.fishingUseCustomSpawnPoint.get();
    fishingSpawnPointX = COMMON.fishingSpawnPointX.get();
    fishingSpawnPointY = COMMON.fishingSpawnPointY.get();
    fishingSpawnPointZ = COMMON.fishingSpawnPointZ.get();

    newdimUseCustomSpawnPoint = COMMON.newdimUseCustomSpawnPoint.get();
    newdimSpawnPointX = COMMON.newdimSpawnPointX.get();
    newdimSpawnPointY = COMMON.newdimSpawnPointY.get();
    newdimSpawnPointZ = COMMON.newdimSpawnPointZ.get();

    gamingUseCustomSpawnPoint = COMMON.gamingUseCustomSpawnPoint.get();
    gamingSpawnPointX = COMMON.gamingSpawnPointX.get();
    gamingSpawnPointY = COMMON.gamingSpawnPointY.get();
    gamingSpawnPointZ = COMMON.gamingSpawnPointZ.get();

    lobbyUseCustomSpawnPoint = COMMON.lobbyUseCustomSpawnPoint.get();
    lobbySpawnPointX = COMMON.lobbySpawnPointX.get();
    lobbySpawnPointY = COMMON.lobbySpawnPointY.get();
    lobbySpawnPointZ = COMMON.lobbySpawnPointZ.get();

    miningUseCustomSpawnPoint = COMMON.miningUseCustomSpawnPoint.get();
    miningSpawnPointX = COMMON.miningSpawnPointX.get();
    miningSpawnPointY = COMMON.miningSpawnPointY.get();
    miningSpawnPointZ = COMMON.miningSpawnPointZ.get();

    voidUseCustomSpawnPoint = COMMON.voidUseCustomSpawnPoint.get();
    voidSpawnPointX = COMMON.voidSpawnPointX.get();
    voidSpawnPointY = COMMON.voidSpawnPointY.get();
    voidSpawnPointZ = COMMON.voidSpawnPointZ.get();

    // Construct Clickable commands
    fishingCommand = new TextComponent("/" + COMMON.fishingCommandName.get())
        .setStyle(Style.EMPTY.withColor(ChatFormatting.GREEN).withClickEvent(new ClickEvent(
            ClickEvent.Action.SUGGEST_COMMAND, "/" + COMMON.fishingCommandName.get())));
    newdimCommand = new TextComponent("/" + COMMON.newdimCommandName.get())
            .setStyle(Style.EMPTY.withColor(ChatFormatting.GREEN).withClickEvent(new ClickEvent(
                    ClickEvent.Action.SUGGEST_COMMAND, "/" + COMMON.newdimCommandName.get())));
    gamingCommand = new TextComponent("/" + COMMON.gamingCommandName.get())
        .setStyle(Style.EMPTY.withColor(ChatFormatting.GREEN).withClickEvent(new ClickEvent(
            ClickEvent.Action.SUGGEST_COMMAND, "/" + COMMON.gamingCommandName.get())));
    lobbyCommand = new TextComponent("/" + COMMON.lobbyCommandName.get())
        .setStyle(Style.EMPTY.withColor(ChatFormatting.GREEN).withClickEvent(new ClickEvent(
            ClickEvent.Action.SUGGEST_COMMAND, "/" + COMMON.lobbyCommandName.get())));
    miningCommand = new TextComponent("/" + COMMON.miningCommandName.get())
        .setStyle(Style.EMPTY.withColor(ChatFormatting.GREEN).withClickEvent(new ClickEvent(
            ClickEvent.Action.SUGGEST_COMMAND, "/" + COMMON.miningCommandName.get())));
    spawnCommand = new TextComponent("/" + COMMON.defaultCommandName.get())
        .setStyle(Style.EMPTY.withColor(ChatFormatting.GREEN).withClickEvent(new ClickEvent(
            ClickEvent.Action.SUGGEST_COMMAND, "/" + COMMON.defaultCommandName.get())));
    voidCommand = new TextComponent("/" + COMMON.voidCommandName.get())
        .setStyle(Style.EMPTY.withColor(ChatFormatting.GREEN).withClickEvent(
            new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/" + COMMON.voidCommandName.get())));

    if (defaultUseCustomSpawnPoint) {
      log.info("{} Using custom spawn point {} {} {} for default dimension",
          Constants.LOG_TELEPORT_MANAGER_PREFIX, defaultSpawnPointX, defaultSpawnPointY,
          defaultSpawnPointZ);
    }
    if (fishingUseCustomSpawnPoint) {
      log.info("{} Using custom spawn point {} {} {} for fishing dimension",
          Constants.LOG_TELEPORT_MANAGER_PREFIX, fishingSpawnPointX, fishingSpawnPointY,
          fishingSpawnPointZ);
    }
    if (newdimUseCustomSpawnPoint) {
      log.info("{} Using custom spawn point {} {} {} for newdim dimension",
              Constants.LOG_TELEPORT_MANAGER_PREFIX, newdimSpawnPointX, newdimSpawnPointY,
              newdimSpawnPointZ);
    }
    if (gamingUseCustomSpawnPoint) {
      log.info("{} Using custom spawn point {} {} {} for gaming dimension",
          Constants.LOG_TELEPORT_MANAGER_PREFIX, gamingSpawnPointX, gamingSpawnPointY,
          gamingSpawnPointZ);
    }
    if (lobbyUseCustomSpawnPoint) {
      log.info("{} Using custom spawn point {} {} {} for lobby dimension",
          Constants.LOG_TELEPORT_MANAGER_PREFIX, lobbySpawnPointX, lobbySpawnPointY,
          lobbySpawnPointZ);
    }
    if (miningUseCustomSpawnPoint) {
      log.info("{} Using custom spawn point {} {} {} for mining dimension",
          Constants.LOG_TELEPORT_MANAGER_PREFIX, miningSpawnPointX, miningSpawnPointY,
          miningSpawnPointZ);
    }
    if (voidUseCustomSpawnPoint) {
      log.info("{} Using custom spawn point {} {} {} for void dimension",
          Constants.LOG_TELEPORT_MANAGER_PREFIX, voidSpawnPointX, voidSpawnPointY, voidSpawnPointZ);
    }
    if (ModList.get().isLoaded("ftbessentials")) {
      log.info("Enable FTB Essentials integration.");
    }
  }

  public static boolean teleportToDefaultDimension(ServerPlayer player) {
    ServerLevel defaultDimension = DimensionManager.getDefaultDimension();
    boolean isSameDimension = player.level == defaultDimension;
    boolean successfullyTeleported = false;
    if (defaultUseCustomSpawnPoint) {
      successfullyTeleported = teleportPlayer(player, defaultDimension, defaultSpawnPointX,
          defaultSpawnPointY, defaultSpawnPointZ);
    } else {
      successfullyTeleported = teleportPlayer(player, defaultDimension);
    }
    if (successfullyTeleported && !isSameDimension) {
      player.sendMessage(
          new TranslatableComponent(Constants.TEXT_PREFIX + "welcome_to_default", fishingCommand,
              gamingCommand, lobbyCommand, miningCommand, spawnCommand, voidCommand),
          Util.NIL_UUID);
    }
    return successfullyTeleported;
  }

  public static boolean teleportToFishingDimension(ServerPlayer player) {
    ServerLevel fishingDimension = DimensionManager.getFishingDimension();
    boolean isSameDimension = player.level == fishingDimension;
    boolean successfullyTeleported = false;
    if (fishingUseCustomSpawnPoint) {
      successfullyTeleported = teleportPlayer(player, fishingDimension, fishingSpawnPointX,
          fishingSpawnPointY, fishingSpawnPointZ);
    } else {
      successfullyTeleported =
          teleportPlayer(player, fishingDimension, defaultFishingSpawnPoint.getX(),
              defaultFishingSpawnPoint.getY(), defaultFishingSpawnPoint.getZ());
    }
    if (successfullyTeleported && !isSameDimension) {
      player.sendMessage(
          new TranslatableComponent(Constants.TEXT_PREFIX + "welcome_to_fishing", fishingCommand,
              gamingCommand, lobbyCommand, miningCommand, spawnCommand, voidCommand),
          Util.NIL_UUID);
    }
    return successfullyTeleported;
  }

  public static boolean teleportToNewDimDimension(ServerPlayer player) {
    ServerLevel newdimDimension = DimensionManager.getNewDimDimension();
    boolean isSameDimension = player.level == newdimDimension;
    boolean successfullyTeleported = false;
    if (newdimUseCustomSpawnPoint) {
      successfullyTeleported = teleportPlayer(player, newdimDimension, newdimSpawnPointX,
              newdimSpawnPointY, newdimSpawnPointZ);
    } else {
      successfullyTeleported =
              teleportPlayer(player, newdimDimension, defaultNewDimSpawnPoint.getX(),
                      defaultNewDimSpawnPoint.getY(), defaultNewDimSpawnPoint.getZ());
    }
    if (successfullyTeleported && !isSameDimension) {
      player.sendMessage(
              new TranslatableComponent(Constants.TEXT_PREFIX + "welcome_to_newdim", fishingCommand,
                      gamingCommand, lobbyCommand, miningCommand, spawnCommand, voidCommand, newdimCommand),
              Util.NIL_UUID);
    }
    return successfullyTeleported;
  }

  public static boolean teleportToGamingDimension(ServerPlayer player) {
    ServerLevel gamingDimension = DimensionManager.getGamingDimension();
    boolean isSameDimension = player.level == gamingDimension;
    boolean successfullyTeleported = false;
    if (gamingUseCustomSpawnPoint) {
      successfullyTeleported = teleportPlayer(player, gamingDimension, gamingSpawnPointX,
          gamingSpawnPointY, gamingSpawnPointZ);
    } else {
      successfullyTeleported =
          teleportPlayer(player, gamingDimension, defaultGamingSpawnPoint.getX(),
              defaultGamingSpawnPoint.getY(), defaultGamingSpawnPoint.getZ());
    }
    if (successfullyTeleported && !isSameDimension) {
      player.sendMessage(
          new TranslatableComponent(Constants.TEXT_PREFIX + "welcome_to_gaming", fishingCommand,
              gamingCommand, lobbyCommand, miningCommand, spawnCommand, voidCommand),
          Util.NIL_UUID);
    }
    return successfullyTeleported;
  }

  public static boolean teleportToLobbyDimension(ServerPlayer player) {
    ServerLevel lobbyDimension = DimensionManager.getLobbyDimension();
    boolean isSameDimension = player.level == lobbyDimension;
    boolean successfullyTeleported = false;
    if (lobbyUseCustomSpawnPoint) {
      successfullyTeleported = teleportPlayer(player, lobbyDimension, lobbySpawnPointX,
          lobbySpawnPointY, lobbySpawnPointZ);
    } else {
      successfullyTeleported = teleportPlayer(player, lobbyDimension, defaultLobbySpawnPoint.getX(),
          defaultLobbySpawnPoint.getY(), defaultLobbySpawnPoint.getZ());
    }
    if (successfullyTeleported && !isSameDimension) {
      player.sendMessage(
          new TranslatableComponent(Constants.TEXT_PREFIX + "welcome_to_lobby", fishingCommand,
              gamingCommand, lobbyCommand, miningCommand, spawnCommand, voidCommand),
          Util.NIL_UUID);
    }
    return successfullyTeleported;
  }

  public static boolean teleportToMiningDimension(ServerPlayer player) {
    ServerLevel miningDimension = DimensionManager.getMiningDimension();
    boolean isSameDimension = player.level == miningDimension;
    boolean successfullyTeleported = false;
    if (miningUseCustomSpawnPoint) {
      successfullyTeleported = teleportPlayer(player, miningDimension, miningSpawnPointX,
          miningSpawnPointY, miningSpawnPointZ);
    } else {
      successfullyTeleported =
          teleportPlayer(player, miningDimension, defaultMiningSpawnPoint.getX(),
              defaultMiningSpawnPoint.getY(), defaultMiningSpawnPoint.getZ());
    }
    if (successfullyTeleported && !isSameDimension) {
      player.sendMessage(
          new TranslatableComponent(Constants.TEXT_PREFIX + "welcome_to_mining", fishingCommand,
              gamingCommand, lobbyCommand, miningCommand, spawnCommand, voidCommand),
          Util.NIL_UUID);
    }
    return successfullyTeleported;
  }

  public static boolean teleportToVoidDimension(ServerPlayer player) {
    ServerLevel voidDimension = DimensionManager.getVoidDimension();
    boolean isSameDimension = player.level == voidDimension;
    boolean successfullyTeleported = false;
    if (voidUseCustomSpawnPoint) {
      successfullyTeleported =
          teleportPlayer(player, voidDimension, voidSpawnPointX, voidSpawnPointY, voidSpawnPointZ);
    } else {
      successfullyTeleported = teleportPlayer(player, voidDimension, defaultVoidSpawnPoint.getX(),
          defaultVoidSpawnPoint.getY(), defaultVoidSpawnPoint.getZ());
    }
    if (successfullyTeleported && !isSameDimension) {
      player.sendMessage(
          new TranslatableComponent(Constants.TEXT_PREFIX + "welcome_to_void", fishingCommand,
              gamingCommand, lobbyCommand, miningCommand, spawnCommand, voidCommand),
          Util.NIL_UUID);
    }
    return successfullyTeleported;
  }

  private static boolean teleportPlayer(ServerPlayer player, ServerLevel dimension) {
    // Ignore client side levels and if dimension was not found.
    if (player.getLevel().isClientSide() || dimension == null) {
      return false;
    }
    BlockPos sharedSpawnPos = dimension.getSharedSpawnPos();
    return teleportPlayer(player, dimension, sharedSpawnPos.getX(), sharedSpawnPos.getY(),
        sharedSpawnPos.getZ());
  }

  private static boolean teleportPlayer(ServerPlayer player, ServerLevel dimension, int x, int y,
      int z) {
    // Ignore client side levels and if dimension was not found.
    if (player.getLevel().isClientSide() || dimension == null) {
      return false;
    }

    // If we are already in the same dimension use a simple teleport instead.
    if (player.level == dimension) {
      addTeleportHistory(player);
      player.teleportTo(x, y, z);
      return true;
    }

    // Use dimensional teleporter for the player.
    addTeleportHistory(player);
    player.teleportTo(dimension, x, y, z, player.getYRot(), player.getXRot());
    return true;
  }

  private static void addTeleportHistory(ServerPlayer player) {
    ServerLevel level = player.getLevel();
    ResourceKey<Level> dimension = level.dimension();
    BlockPos blockPos = player.blockPosition();

    log.debug("Add teleport history for player {} in {} with {}", player, dimension, blockPos);
    if (ModList.get().isLoaded("ftbessentials")) {
      FTBEPlayerData.addTeleportHistory(player, dimension, blockPos);
    }
  }
}
