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

package de.markusbordihn.lobby.dimension;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.vehicle.MinecartChest;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.server.ServerLifecycleHooks;

import de.markusbordihn.lobby.Constants;
import de.markusbordihn.lobby.config.CommonConfig;
import de.markusbordihn.lobby.datapack.DataPackHandler;
import de.markusbordihn.lobby.teleporter.TeleporterManager;

@EventBusSubscriber
public class DimensionManager {

  private static final Logger log = LogManager.getLogger(Constants.LOG_NAME);

  private static final CommonConfig.Config COMMON = CommonConfig.COMMON;

  private static String defaultDimension = COMMON.defaultDimension.get();

  private static String lobbyDimension = COMMON.lobbyDimension.get();
  private static List<String> lobbyBuilderList = COMMON.lobbyBuilderList.get();

  private static String miningDimension = COMMON.miningDimension.get();
  private static boolean miningDisableBatSpawning = COMMON.miningDisableBatSpawning.get();
  private static boolean miningDisableMobSpawning = COMMON.miningDisableMobSpawning.get();
  private static boolean miningDisableMinecartChestSpawning =
      COMMON.miningDisableMinecartChestSpawning.get();
  private static boolean miningRemoveSpawner = COMMON.miningRemoveSpawner.get();

  private static Set<ServerPlayer> gameTypeReset = ConcurrentHashMap.newKeySet();

  private static ServerLevel defaultLevel = null;
  private static ServerLevel lobbyLevel = null;
  private static ServerLevel miningLevel = null;

  protected DimensionManager() {}

  @SubscribeEvent
  public static void handleServerAboutToStartEvent(ServerAboutToStartEvent event) {
    // Reset mapping to avoid issues.
    defaultLevel = null;
    lobbyLevel = null;
    miningLevel = null;

    // Make sure we have the current config settings.
    defaultDimension = COMMON.defaultDimension.get();

    lobbyDimension = COMMON.lobbyDimension.get();
    lobbyBuilderList = COMMON.lobbyBuilderList.get();

    miningDimension = COMMON.miningDimension.get();
    miningDisableBatSpawning = COMMON.miningDisableBatSpawning.get();
    miningDisableMobSpawning = COMMON.miningDisableMobSpawning.get();
    miningDisableMinecartChestSpawning = COMMON.miningDisableMinecartChestSpawning.get();
    miningRemoveSpawner = COMMON.miningRemoveSpawner.get();
  }

  @SubscribeEvent
  public static void handleServerStartedEvent(ServerStartedEvent event) {
    // Map dimension and init dimension structure if needed.
    mapServerLevel(event.getServer());
  }

  @SubscribeEvent
  public static void onChangeDimension(PlayerChangedDimensionEvent event) {
    Player player = event.getPlayer();
    String fromLocation = event.getFrom().location().toString();
    String toLocation = event.getTo().location().toString();

    // Ignore known dimension which automatically changing the game type or if the from dimension
    // is not from the lobby.
    if (toLocation.equals(lobbyDimension) || toLocation.equals(miningDimension)
        || (!fromLocation.isEmpty() && !lobbyDimension.equals(fromLocation))) {
      return;
    }

    // Reset game type to survival if user is on the gameTypeReset list or comes from the lobby
    // dimensions.
    if (player instanceof ServerPlayer serverPlayer && (gameTypeReset.contains(serverPlayer)
        || !fromLocation.isEmpty() && lobbyDimension.equals(fromLocation))) {
      changeGameType(serverPlayer, GameType.SURVIVAL);
      gameTypeReset.remove(serverPlayer);
    }
  }

  @SubscribeEvent(priority = EventPriority.HIGHEST)
  public static void handleEntityJoinWorldEvent(EntityJoinWorldEvent event) {

    // Ignore client side and everything which is not the mining dimension.
    Level level = event.getWorld();
    String dimensionLocation = level.dimension().location().toString();
    if (level.isClientSide() || !dimensionLocation.equals(miningDimension)) {
      return;
    }

    // Ignore specific entities and deny spawn of all others.
    Entity entity = event.getEntity();
    if (entity instanceof ItemEntity || entity instanceof ExperienceOrb
        || entity instanceof LightningBolt || entity instanceof FallingBlockEntity
        || entity instanceof Projectile || entity instanceof Player) {
      return;
    }

    // Allow/deny Minecart Chest spawning
    if (miningDisableMinecartChestSpawning && entity instanceof MinecartChest) {
      event.setResult(Event.Result.DENY);
    }

    if (miningDisableMobSpawning) {
      event.setResult(Event.Result.DENY);
    }
  }

  @SubscribeEvent(priority = EventPriority.HIGHEST)
  public static void handleLivingCheckSpawnEvent(LivingSpawnEvent.CheckSpawn event) {
    handleSpawnEvent(event);
  }

  @SubscribeEvent(priority = EventPriority.HIGHEST)
  public static void handleLivingSpecialSpawnEvent(LivingSpawnEvent.SpecialSpawn event) {
    handleSpawnEvent(event);
  }

  private static void mapServerLevel(MinecraftServer server) {
    // Skip search if we already found all dimensions.
    if (defaultLevel != null && lobbyLevel != null && miningLevel != null) {
      return;
    }

    // Mapping names to server level for easier access.
    for (ServerLevel serverLevel : server.getAllLevels()) {
      String dimensionLocation = serverLevel.dimension().location().toString();
      if (defaultLevel == null && dimensionLocation.equals(defaultDimension)) {
        log.info("Found default dimension with name {}: {}", defaultDimension, serverLevel);
        defaultLevel = serverLevel;
      } else if (lobbyLevel == null && dimensionLocation.equals(lobbyDimension)) {
        log.info("Found lobby dimension with name {}: {}", lobbyDimension, serverLevel);
        lobbyLevel = serverLevel;
        DataPackHandler.prepareDataPackOnce(lobbyLevel);
      } else if (miningLevel == null && dimensionLocation.equals(miningDimension)) {
        log.info("Found mining dimension with name {}: {}", miningDimension, serverLevel);
        miningLevel = serverLevel;
        DataPackHandler.prepareDataPackOnce(miningLevel);
      }
    }

    // Give error messages, if we are unable to match any dimension.
    if (defaultLevel == null) {
      log.error("Unable to found default dimension named {}!", defaultDimension);
    }
    if (lobbyLevel == null) {
      log.error("Unable to found lobby dimension named {}!", lobbyDimension);
    }
    if (miningLevel == null) {
      log.error("Unable to found mining dimension named {}!", miningDimension);
    }
  }

  public static ServerLevel getLobbyDimension() {
    if (lobbyLevel == null) {
      mapServerLevel(ServerLifecycleHooks.getCurrentServer());
    }
    return lobbyLevel;
  }

  public static ServerLevel getMiningDimension() {
    if (miningLevel == null) {
      mapServerLevel(ServerLifecycleHooks.getCurrentServer());
    }
    return miningLevel;
  }

  public static ServerLevel getDefaultDimension() {
    if (defaultLevel == null) {
      mapServerLevel(ServerLifecycleHooks.getCurrentServer());
    }
    return defaultLevel;
  }

  public static void teleportToDefault(ServerPlayer player) {
    if (TeleporterManager.teleportToDefaultDimension(player)) {
      changeGameType(player, GameType.SURVIVAL);
    }
  }

  public static void teleportToLobby(ServerPlayer player) {
    if (TeleporterManager.teleportToLobbyDimension(player)) {
      if (!lobbyBuilderList.isEmpty() && lobbyBuilderList.contains(player.getName().getString())) {
        log.info("Give builder {} creative mode for lobby.", player.getName().getString());
        changeGameType(player, GameType.CREATIVE);
      } else {
        changeGameType(player, GameType.ADVENTURE);
      }
    }
  }

  public static void teleportToMining(ServerPlayer player) {
    if (TeleporterManager.teleportToMiningDimension(player)) {
      changeGameType(player, GameType.SURVIVAL);
    }
  }

  public static void changeGameType(Player player, GameType gameType) {
    if (player instanceof ServerPlayer serverPlayer) {
      changeGameType(serverPlayer, gameType);
    }
  }

  public static void changeGameType(ServerPlayer serverPlayer, GameType gameType) {
    GameType currentGameType = serverPlayer.gameMode.getGameModeForPlayer();
    if (currentGameType != gameType) {
      // Add player to reset list of the game mode if game type is not survival to avoid cheating.
      if (gameType != GameType.SURVIVAL) {
        gameTypeReset.add(serverPlayer);
      }
      log.debug("Changing players {} game type from {} to {}", serverPlayer, currentGameType,
          gameType);
      serverPlayer.setGameMode(gameType);
    }
  }

  private static void handleSpawnEvent(LivingSpawnEvent event) {

    // Ignore client side.
    LevelAccessor level = event.getWorld();
    if (level.isClientSide()) {
      return;
    }

    // Ignore null entities and specific entities.
    Entity entity = event.getEntity();
    if (entity == null || entity instanceof Projectile) {
      return;
    }

    // Restrict spawn control to mining dimension.
    String dimensionLocation = entity.getLevel().dimension().location().toString();
    if (!miningDimension.equals(dimensionLocation)) {
      return;
    }

    // Removing spawners as soon they try to spawn something.
    if (miningRemoveSpawner && event instanceof LivingSpawnEvent.CheckSpawn checkSpawn
        && checkSpawn.getSpawner() != null) {
      BaseSpawner spawner = checkSpawn.getSpawner();
      BlockPos blockPos = spawner.getSpawnerBlockEntity().getBlockPos();
      if (blockPos != null) {
        log.debug("Removing spawner {} at {}", spawner, blockPos);
        level.removeBlock(blockPos, true);
      }
    }

    // Allow/deny bat spawning for better cave experience
    if (!miningDisableBatSpawning && entity instanceof Bat) {
      return;
    }

    // Allow/deny Minecart Chest spawning
    if (!miningDisableMinecartChestSpawning && entity instanceof MinecartChest) {
      return;
    }

    if (miningDisableMobSpawning) {
      event.setResult(Event.Result.DENY);
    }
  }

}
