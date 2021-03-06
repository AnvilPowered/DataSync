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

package org.anvilpowered.datasync.api.serializer.user;

import org.anvilpowered.anvil.api.datastore.Manager;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface UserSerializerManager<
    TUser,
    TString>
    extends Manager<UserSerializerComponent<?, TUser, ?>> {

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

    CompletableFuture<TString> serializeSafe(TUser user, String name);

    CompletableFuture<TString> deserialize(TUser user, String event, CompletableFuture<Boolean> waitFuture);

    CompletableFuture<TString> deserialize(TUser user, String event);

    CompletableFuture<TString> deserialize(TUser user);

    CompletableFuture<TString> deserializeJoin(TUser user);

    CompletableFuture<TString> restore(UUID userUUID, @Nullable String snapshot);
}
