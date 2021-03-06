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

package org.anvilpowered.datasync.api.serializer;

import org.anvilpowered.datasync.api.model.snapshot.Snapshot;

public interface Serializer<TUser> {

    /**
     * @return Name of {@link Serializer}.
     * Should follow format "plugin:name"
     * For example "datasync:inventory"
     */
    String getName();

    /**
     * Moves data from {@code player} into {@code member}
     *
     * @param snapshot {@link Snapshot} to add data to
     * @param user     User to get data from
     * @return Whether serialization was successful
     */
    boolean serialize(Snapshot<?> snapshot, TUser user);

    /**
     * Moves data from {@code member} into {@code player}
     *
     * @param snapshot {@link Snapshot} to get data from
     * @param user     User to add data to
     * @return Whether deserialization was successful
     */
    boolean deserialize(Snapshot<?> snapshot, TUser user);
}
