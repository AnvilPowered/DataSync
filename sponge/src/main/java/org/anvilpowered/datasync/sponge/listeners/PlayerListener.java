/*
 *   DataSync - AnvilPowered
 *   Copyright (C) 2020 Cableguy20
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

package org.anvilpowered.datasync.sponge.listeners;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.anvilpowered.anvil.api.data.registry.Registry;
import org.anvilpowered.anvil.api.plugin.PluginInfo;
import org.anvilpowered.datasync.api.serializer.user.UserSerializerManager;
import org.anvilpowered.datasync.common.data.key.DataSyncKeys;
import org.anvilpowered.datasync.sponge.command.SyncLockCommand;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

@Singleton
public class PlayerListener {

    private Registry registry;

    @Inject
    private PluginInfo<Text> pluginInfo;

    @Inject
    private UserSerializerManager<User, Text> userSerializerManager;

    private boolean joinSerializationEnabled;
    private boolean disconnectSerializationEnabled;
    private boolean deathSerializationEnabled;

    @Inject
    public PlayerListener(Registry registry) {
        this.registry = registry;
        registry.whenLoaded(this::registryLoaded);
    }

    private void registryLoaded() {
        joinSerializationEnabled = registry.getOrDefault(DataSyncKeys.DESERIALIZE_ON_JOIN);
        if (!joinSerializationEnabled) {
            sendWarning("serialize.deserializeOnJoin");
        }
        disconnectSerializationEnabled = registry.getOrDefault(DataSyncKeys.SERIALIZE_ON_DISCONNECT);
        if (!disconnectSerializationEnabled) {
            sendWarning("serialize.serializeOnDisconnect");
        }
        deathSerializationEnabled = registry.getOrDefault(DataSyncKeys.SERIALIZE_ON_DEATH);
        if (!deathSerializationEnabled) {
            sendWarning("serialize.serializeOnDeath");
        }
    }

    private void sendWarning(String name) {
        Sponge.getServer().getConsole().sendMessage(
            Text.of(pluginInfo.getPrefix(), TextColors.RED,
                "Attention! You have opted to disable ", name, ".\n" +
                    "If you would like to enable this, set `", name, "=true` in the config and restart your server or run /sync reload")
        );
    }

    @Listener
    public void onPlayerJoin(ClientConnectionEvent.Join joinEvent, @Root Player player) {
        if (joinSerializationEnabled) {
            userSerializerManager.deserialize(player, "Join")
                .thenAcceptAsync(Sponge.getServer().getConsole()::sendMessage);
        }
    }

    @Listener
    public void onPlayerDisconnect(ClientConnectionEvent.Disconnect disconnectEvent, @Root Player player) {
        SyncLockCommand.lockPlayer(player);
        if (disconnectSerializationEnabled) {
            userSerializerManager.serialize(player, "Disconnect")
                .thenAcceptAsync(Sponge.getServer().getConsole()::sendMessage);
        }
    }

    @Listener
    public void onPlayerDeath(DestructEntityEvent.Death deathEvent, @Getter("getTargetEntity") Player player) {
        if (deathSerializationEnabled) {
            userSerializerManager.serialize(player, "Death")
                .thenAcceptAsync(Sponge.getServer().getConsole()::sendMessage);
        }
    }
}
