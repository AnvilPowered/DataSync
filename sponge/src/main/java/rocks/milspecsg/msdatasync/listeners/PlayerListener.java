/*
 *     MSDataSync - MilSpecSG
 *     Copyright (C) 2019 Cableguy20
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package rocks.milspecsg.msdatasync.listeners;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import rocks.milspecsg.msdatasync.MSDataSync;
import rocks.milspecsg.msdatasync.MSDataSyncPluginInfo;
import rocks.milspecsg.msdatasync.api.config.ConfigKeys;
import rocks.milspecsg.msdatasync.api.serializer.user.UserSerializerManager;
import rocks.milspecsg.msdatasync.commands.SyncLockCommand;
import rocks.milspecsg.msdatasync.model.core.snapshot.Snapshot;
import rocks.milspecsg.msrepository.api.config.ConfigurationService;

import java.util.concurrent.CompletableFuture;

@Singleton
public class PlayerListener {

    private ConfigurationService configurationService;

    @Inject
    UserSerializerManager<Snapshot<?>, User, Text> userSerializer;

    private boolean enabled = true;

    @Inject
    public PlayerListener(ConfigurationService configurationService) {
        this.configurationService = configurationService;
        this.configurationService.addConfigLoadedListener(this::loadConfig);
    }

    private void loadConfig(Object plugin) {
        enabled = configurationService.getConfigBoolean(ConfigKeys.SERIALIZE_ON_JOIN_LEAVE);
        if (!enabled) {
            Sponge.getServer().getConsole().sendMessage(
                Text.of(MSDataSyncPluginInfo.pluginPrefix, TextColors.RED,
                    "Attention! You have opted to disable join/leave syncing.\n" +
                        "If you would like to enable this, set `serializeOnJoinLeave=true` in the config and restart your server or run /sync reload")
            );
        }
    }


    @Listener
    public void onPlayerJoin(ClientConnectionEvent.Join joinEvent) {
        if (enabled) {
            Player player = joinEvent.getTargetEntity();
            CompletableFuture.runAsync(() -> {
                userSerializer.getPrimaryComponent().deserialize(player, MSDataSync.plugin).thenAcceptAsync(optionalSnapshot -> {
                    if (optionalSnapshot.isPresent()) {
                        Sponge.getServer().getConsole().sendMessage(
                            Text.of(MSDataSyncPluginInfo.pluginPrefix, TextColors.YELLOW, "Successfully deserialized ", player.getName(), " on join!")
                        );
                    } else {
                        Sponge.getServer().getConsole().sendMessage(
                            Text.of(MSDataSyncPluginInfo.pluginPrefix, TextColors.RED, "An error occurred while deserializing ", player.getName(), " on join!")
                        );
                    }
                }).join();
            });

        }
    }

    @Listener
    public void onPlayerDisconnect(ClientConnectionEvent.Disconnect disconnectEvent) {
        SyncLockCommand.lockPlayer(disconnectEvent.getTargetEntity());
        if (enabled) {
            Player player = disconnectEvent.getTargetEntity();
            userSerializer.getPrimaryComponent().serialize(player, "Disconnect").thenAcceptAsync(optionalSnapshot -> {
                if (optionalSnapshot.isPresent()) {
                    Sponge.getServer().getConsole().sendMessage(
                        Text.of(MSDataSyncPluginInfo.pluginPrefix, TextColors.YELLOW, "Successfully serialized ", player.getName(), " on disconnect!")
                    );
                } else {
                    Sponge.getServer().getConsole().sendMessage(
                        Text.of(MSDataSyncPluginInfo.pluginPrefix, TextColors.RED, "An error occurred while serializing ", player.getName(), " on disconnect!")
                    );
                }
            });
        }
    }
}
