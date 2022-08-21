package de.markusbordihn.lobby.data;

import de.markusbordihn.lobby.Constants;
import de.markusbordihn.lobby.dimension.DimensionManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Date;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber
public class NewDimData extends SavedData {

    public static final Logger log = LogManager.getLogger(Constants.LOG_NAME);

    private static final String FILE_ID = Constants.MOD_ID;
    private static NewDimData data = null;
    private static MinecraftServer server = null;
    private static ServerLevel level = null;

    private Set<UUID> playerTeleportList = ConcurrentHashMap.newKeySet();
    private boolean dimensionLoaded = false;
    private long lastUpdate;

    public NewDimData() {
        this.setDirty();
    }

    @SubscribeEvent
    public static void handleServerAboutToStartEvent(ServerAboutToStartEvent event) {
        // Reset data and server for the integrated server.
        data = null;
        level = null;
        server = null;
    }

    public static NewDimData get() {
        if (NewDimData.data == null || NewDimData.level == null) {
            prepare(ServerLifecycleHooks.getCurrentServer());
        }
        return NewDimData.data;
    }

    public static void prepare(MinecraftServer server) {
        // Make sure we preparing the data only once for the same server!
        if (server == NewDimData.server && NewDimData.data != null && NewDimData.level != null) {
            return;
        }

        NewDimData.server = server;
        NewDimData.level = DimensionManager.getNewDimDimension();
        if (NewDimData.level != null) {
            log.info("{} preparing data for {} and {}", Constants.LOG_NAME, NewDimData.server,
                    NewDimData.level);

            // Using a global approach and storing relevant data in the overworld only!
            NewDimData.data = NewDimData.level.getDataStorage().computeIfAbsent(NewDimData::load,
                    NewDimData::new, NewDimData.getFileId());
        } else {
            log.error("Unable to preparing data for {} and {}", NewDimData.server, NewDimData.level);
        }
    }

    public static String getFileId() {
        return FILE_ID;
    }

    public long getLastUpdate() {
        return this.lastUpdate;
    }

    public void setLastUpdate(long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public boolean getDimensionLoaded() {
        return this.dimensionLoaded;
    }

    public void setDimensionLoaded(boolean loaded) {
        this.dimensionLoaded = loaded;
    }

    public Set<UUID> getPlayerTeleportList() {
        return this.playerTeleportList;
    }

    public void setPlayerTeleportList(Set<UUID> playerTeleportList) {
        this.playerTeleportList = playerTeleportList;
        this.setDirty();
    }

    public static NewDimData load(CompoundTag compoundTag) {
        NewDimData newdimData = new NewDimData();
        log.info("{} loading newDim dimension data ... {}", Constants.LOG_NAME, compoundTag);
        newdimData.dimensionLoaded = compoundTag.getBoolean("DimensionLoaded");
        newdimData.lastUpdate = compoundTag.getLong("LastUpdate");
        return newdimData;
    }

    @Override
    public CompoundTag save(CompoundTag compoundTag) {
        log.info("{} saving newDim dimension data ... {}", Constants.LOG_NAME, this);
        compoundTag.putBoolean("DimensionLoaded", this.dimensionLoaded);
        compoundTag.putLong("LastUpdate", new Date().getTime());
        return compoundTag;
    }

}
