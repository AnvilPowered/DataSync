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

package rocks.milspecsg.msdatasync.service.common.snapshot.repository;

import com.google.inject.Inject;
import rocks.milspecsg.msdatasync.api.keys.DataKeyService;
import rocks.milspecsg.msdatasync.api.snapshot.repository.SnapshotRepository;
import rocks.milspecsg.msdatasync.model.core.snapshot.Snapshot;
import rocks.milspecsg.msrepository.api.cache.RepositoryCacheService;
import rocks.milspecsg.msrepository.datastore.DataStoreConfig;
import rocks.milspecsg.msrepository.datastore.DataStoreContext;
import rocks.milspecsg.msrepository.service.common.repository.CommonRepository;

import java.util.HashMap;
import java.util.Optional;

public abstract class CommonSnapshotRepository<
    TKey,
    TSnapshot extends Snapshot<TKey>,
    TDataKey,
    TDataStore,
    TDataStoreConfig extends DataStoreConfig>
    extends CommonRepository<TKey, TSnapshot, RepositoryCacheService<TKey, TSnapshot, TDataStore, TDataStoreConfig>, TDataStore, TDataStoreConfig>
    implements SnapshotRepository<TKey, TSnapshot, TDataKey, TDataStore, TDataStoreConfig> {

    @Inject
    DataKeyService<TDataKey> dataKeyService;

    protected CommonSnapshotRepository(DataStoreContext<TKey, TDataStore, TDataStoreConfig> dataStoreContext) {
        super(dataStoreContext);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<TSnapshot> getTClass() {
        return (Class<TSnapshot>) getDataStoreContext().getEntityClassUnsafe("snapshot");
    }

    @Override
    public boolean setSnapshotValue(TSnapshot snapshot, TDataKey key, Optional<?> optionalValue) {
        if (!optionalValue.isPresent()) {
            return false;
        }
        Optional<String> optionalName = dataKeyService.getName(key);
        if (!optionalName.isPresent()) {
            return false;
        }
        if (snapshot.getKeys() == null) {
            snapshot.setKeys(new HashMap<>());
        }
        snapshot.getKeys().put(optionalName.get(), optionalValue.get());
        return true;
    }

    @Override
    public Optional<?> getSnapshotValue(TSnapshot snapshot, TDataKey key) {
        Optional<String> optionalName = dataKeyService.getName(key);
        if (!optionalName.isPresent()) {
            return Optional.empty();
        }
        if (snapshot.getKeys() == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(snapshot.getKeys().get(optionalName.get()));
    }
}