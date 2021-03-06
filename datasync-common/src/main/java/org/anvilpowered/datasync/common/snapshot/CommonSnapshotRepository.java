/*
 *   DataSync - AnvilPowered
 *   Copyright (C) 2020 Cableguy20
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.anvilpowered.datasync.common.snapshot;

import com.google.inject.Inject;
import org.anvilpowered.anvil.base.datastore.BaseRepository;
import org.anvilpowered.datasync.api.key.DataKeyService;
import org.anvilpowered.datasync.api.model.snapshot.Snapshot;
import org.anvilpowered.datasync.api.snapshot.SnapshotRepository;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public abstract class CommonSnapshotRepository<
    TKey,
    TDataKey,
    TDataStore>
    extends BaseRepository<TKey, Snapshot<TKey>, TDataStore>
    implements SnapshotRepository<TKey, TDataKey, TDataStore> {

    @Inject
    DataKeyService<TDataKey> dataKeyService;

    @Override
    @SuppressWarnings("unchecked")
    public Class<Snapshot<TKey>> getTClass() {
        return (Class<Snapshot<TKey>>) getDataStoreContext().getEntityClassUnsafe("snapshot");
    }

    @Override
    public boolean setSnapshotValue(Snapshot<?> snapshot, TDataKey key, Optional<?> optionalValue) {
        if (!optionalValue.isPresent()) {
            return false;
        }
        Optional<String> optionalName = dataKeyService.getName(key);
        if (!optionalName.isPresent()) {
            return false;
        }
        snapshot.getKeys().put(optionalName.get(), optionalValue.get());
        return true;
    }

    @Override
    public Optional<?> getSnapshotValue(Snapshot<?> snapshot, TDataKey key) {
        Optional<String> optionalName = dataKeyService.getName(key);
        return optionalName.map(s -> snapshot.getKeys().get(s));
    }

    @Override
    public CompletableFuture<Boolean> parseAndSetInventory(
        Object id, byte[] inventory) {
        return parse(id).map(i -> setInventory(i, inventory))
            .orElse(CompletableFuture.completedFuture(false));
    }
}
