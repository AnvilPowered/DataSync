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

package org.anvilpowered.datasync.api.serializer.user;

import org.anvilpowered.anvil.api.manager.Manager;
import org.anvilpowered.datasync.api.model.snapshot.Snapshot;
import org.anvilpowered.datasync.api.serializer.user.component.UserSerializerComponent;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface UserSerializerManager<
    TSnapshot extends Snapshot<?>,
    TUser,
    TString>
    extends Manager<UserSerializerComponent<?, TSnapshot, TUser, ?>> {

    @Override
    default String getDefaultIdentifierSingularUpper() {
        return "User serializer";
    }

    @Override
    default String getDefaultIdentifierPluralUpper() {
        return "User serializers";
    }

    @Override
    default String getDefaultIdentifierSingularLower() {
        return "user serializer";
    }

    @Override
    default String getDefaultIdentifierPluralLower() {
        return "user serializers";
    }

    CompletableFuture<TString> serialize(Collection<? extends TUser> users);

    CompletableFuture<TString> serialize(TUser user, String name);

    CompletableFuture<TString> serialize(TUser user);

    CompletableFuture<TString> deserialize(TUser user, String event);

    CompletableFuture<TString> deserialize(TUser user);

    CompletableFuture<TString> restore(UUID userUUID, Optional<String> optionalString);
}
