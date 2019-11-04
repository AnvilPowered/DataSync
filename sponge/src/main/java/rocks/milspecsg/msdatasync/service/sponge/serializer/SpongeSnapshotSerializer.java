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

package rocks.milspecsg.msdatasync.service.sponge.serializer;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import rocks.milspecsg.msdatasync.events.SerializerInitializationEvent;
import rocks.milspecsg.msdatasync.model.core.snapshot.Snapshot;
import rocks.milspecsg.msdatasync.service.common.serializer.CommonSnapshotSerializer;
import rocks.milspecsg.msrepository.PluginInfo;
import rocks.milspecsg.msrepository.api.config.ConfigurationService;

@Singleton
public class SpongeSnapshotSerializer extends CommonSnapshotSerializer<Snapshot<?>, Key<?>, User, Inventory, ItemStackSnapshot> {

    @Inject
    public SpongeSnapshotSerializer(ConfigurationService configurationService) {
        super(configurationService);
    }

    @Inject
    private PluginInfo<Text> pluginInfo;

    @Override
    protected void postLoadedEvent(Object plugin) {
        Sponge.getPluginManager().fromInstance(plugin).ifPresent(container -> {
            EventContext eventContext = EventContext.builder().add(EventContextKeys.PLUGIN, container).build();
            Sponge.getEventManager().post(new SerializerInitializationEvent<>(this, snapshotManager, Cause.of(eventContext, plugin)));
        });
    }

    @Override
    protected void announceEnabled(String name) {
        Sponge.getServer().getConsole().sendMessage(Text.of(pluginInfo.getPrefix(), TextColors.YELLOW, "Enabling ", name, " serializer"));
    }

    @Override
    protected String getUsername(User user) {
        return user.getName();
    }

}
