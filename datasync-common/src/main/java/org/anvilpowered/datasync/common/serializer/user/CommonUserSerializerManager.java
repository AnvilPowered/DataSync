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

package org.anvilpowered.datasync.common.serializer.user;

import com.google.inject.Inject;
import org.anvilpowered.anvil.api.entity.RestrictionCriteria;
import org.anvilpowered.anvil.api.entity.RestrictionService;
import org.anvilpowered.anvil.api.plugin.PluginInfo;
import org.anvilpowered.anvil.api.registry.Registry;
import org.anvilpowered.anvil.api.util.TextService;
import org.anvilpowered.anvil.api.util.TimeFormatService;
import org.anvilpowered.anvil.api.util.UserService;
import org.anvilpowered.anvil.base.datastore.BaseManager;
import org.anvilpowered.datasync.api.member.MemberManager;
import org.anvilpowered.datasync.api.model.snapshot.Snapshot;
import org.anvilpowered.datasync.api.registry.DataSyncKeys;
import org.anvilpowered.datasync.api.serializer.user.UserSerializerComponent;
import org.anvilpowered.datasync.api.serializer.user.UserSerializerManager;
import org.anvilpowered.datasync.api.serializer.user.UserTransitCache;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

public class CommonUserSerializerManager<
    TUser,
    TPlayer,
    TString,
    TCommandSource>
    extends BaseManager<UserSerializerComponent<?, TUser, ?>>
    implements UserSerializerManager<TUser, TString> {

    @Inject
    protected MemberManager<TString> memberManager;

    @Inject
    protected TextService<TString, TCommandSource> textService;

    @Inject
    protected PluginInfo<TString> pluginInfo;

    @Inject
    protected UserService<TUser, TPlayer> userService;

    @Inject
    private UserTransitCache userTransitCache;

    @Inject
    private RestrictionService restrictionService;

    @Inject
    protected TimeFormatService timeFormatService;

    @Inject
    public CommonUserSerializerManager(Registry registry) {
        super(registry);
    }

    private String getCreatedString(Snapshot<?> snapshot) {
        return timeFormatService.format(snapshot.getCreatedUtc()).toString()
           + " (" + snapshot.getName() + ")";
    }

    @Override
    public CompletableFuture<TString> serialize(Collection<? extends TUser> users) {
        if (users.isEmpty()) {
            return CompletableFuture.completedFuture(
                textService.builder()
                    .append(pluginInfo.getPrefix())
                    .red().append("There are no players currently online")
                    .build()
            );
        }

        ConcurrentLinkedQueue<TUser> successful = new ConcurrentLinkedQueue<>();
        ConcurrentLinkedQueue<TUser> unsuccessful = new ConcurrentLinkedQueue<>();
        ConcurrentLinkedQueue<TUser> joining = new ConcurrentLinkedQueue<>();
        CompletableFuture<TString> result = new CompletableFuture<>();

        for (TUser user : users) {
            if (userTransitCache.isJoining(userService.getUUID(user))) {
                joining.add(user);
                continue;
            }
            getPrimaryComponent().serialize(user, "Manual").thenAcceptAsync(os -> {
                if (os.isPresent()) {
                    successful.add(user);
                } else {
                    unsuccessful.add(user);
                }
                if (successful.size() + unsuccessful.size() >= users.size()) {
                    TextService.Builder<TString, TCommandSource> builder = textService.builder();
                    if (!successful.isEmpty()) {
                        String s = successful.stream().map(userService::getUserName)
                            .collect(Collectors.joining(", "));
                        builder.yellow()
                            .append("The following players were successfully serialized:\n")
                            .green().append(s);
                    }
                    if (!unsuccessful.isEmpty()) {
                        String s = unsuccessful.stream().map(userService::getUserName)
                            .collect(Collectors.joining(", "));
                        builder.red()
                            .append("The following players were unsuccessfully serialized:\n")
                            .green().append(s);
                    }
                    if (!joining.isEmpty()) {
                        String s = joining.stream().map(userService::getUserName)
                            .collect(Collectors.joining(", "));
                        builder.red()
                            .append("The following players were joining and not serialized to prevent data loss:\n")
                            .green().append(s);
                    }
                    result.complete(builder.build());
                }
            });
        }
        return result;
    }

    @Override
    public CompletableFuture<TString> serialize(TUser user, String name) {
        return getPrimaryComponent().serialize(user, name).thenApplyAsync(optionalSnapshot -> {
            if (optionalSnapshot.isPresent()) {
                return textService.builder()
                    .append(pluginInfo.getPrefix())
                    .yellow().append("Successfully uploaded snapshot ")
                    .gold().append(getCreatedString(optionalSnapshot.get()))
                    .yellow().append(" for ", userService.getUserName(user), "!")
                    .build();
            }
            return textService.builder()
                .append(pluginInfo.getPrefix())
                .red().append(
                    "An error occurred while serializing ", name, " for ",
                    userService.getUserName(user), "!"
                )
                .build();
        });
    }

    @Override
    public CompletableFuture<TString> serialize(TUser user) {
        return serialize(user, "Manual");
    }

    @Override
    public CompletableFuture<TString> serializeSafe(TUser user, String name) {
        return CompletableFuture.supplyAsync(() -> {
            UUID userUUID = userService.getUUID(user);
            if (userTransitCache.isJoining(userUUID)) {
                userTransitCache.joinEnd(userUUID);
                return textService.builder()
                    .append(pluginInfo.getPrefix())
                    .red().append("Prevented ")
                    .gold().append(name)
                    .red().append(" serialization for ")
                    .gold().append(userService.getUserName(user))
                    .red().append(". Skipping upload to prevent data loss!")
                    .build();
            }
            return serialize(user, name).join();
        });
    }

    @Override
    public CompletableFuture<TString> deserialize(TUser user, String event, CompletableFuture<Boolean> waitFuture) {
        return getPrimaryComponent().deserialize(user, waitFuture).thenApplyAsync(optionalSnapshot -> {
            if (optionalSnapshot.isPresent()) {
                return textService.builder()
                    .append(pluginInfo.getPrefix())
                    .yellow().append("Successfully downloaded snapshot ")
                    .gold().append(getCreatedString(optionalSnapshot.get()))
                    .yellow().append(" for ", userService.getUserName(user), " on ", event, "!")
                    .build();
            }
            return textService.builder()
                .append(pluginInfo.getPrefix())
                .red().append(
                    "An error occurred while deserializing ",
                    userService.getUserName(user), " on ", event, "!"
                )
                .build();
        });
    }

    @Override
    public CompletableFuture<TString> deserialize(TUser user, String event) {
        return deserialize(user, event, CompletableFuture.completedFuture(true));
    }

    @Override
    public CompletableFuture<TString> deserialize(TUser user) {
        return deserialize(user, "N/A");
    }

    @Override
    public CompletableFuture<TString> deserializeJoin(TUser user) {
        final int delay = registry.getOrDefault(DataSyncKeys.DESERIALIZE_ON_JOIN_DELAY_MILLIS);
        if (delay <= 0) {
            return deserialize(user, "Join");
        }
        final Optional<TPlayer> optionalPlayer = userService.getPlayer(user);
        if (!optionalPlayer.isPresent()) {
            return CompletableFuture.completedFuture(textService.builder()
                .append(pluginInfo.getPrefix())
                .red().append("User ")
                .gold().append(userService.getUserName(user))
                .red().append(" is not online!")
                .build());
        }
        return CompletableFuture.supplyAsync(() -> {
            final UUID userUUID = userService.getUUID(user);
            final CompletableFuture<Boolean> waitFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    Thread.sleep(delay);
                    return true;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return false;
                }
            });
            userTransitCache.joinStart(userUUID, waitFuture);
            final DecimalFormat df = new DecimalFormat("#.#");
            df.setRoundingMode(RoundingMode.CEILING);
            final String formattedDelay = df.format((double) delay / 1000d);
            final TPlayer player = optionalPlayer.get();
            restrictionService.put(
                user,
                RestrictionCriteria.all()
            );
            textService.builder()
                .append(pluginInfo.getPrefix())
                .yellow().append("You have been frozen. Your data will be downloaded in ")
                .gold().append(formattedDelay)
                .yellow().append(" seconds!")
                .sendTo((TCommandSource) player);
            final TString result = deserialize(user, "Join", waitFuture).join();
            restrictionService.remove(player);
            textService.builder()
                .append(pluginInfo.getPrefix())
                .green().append("You have been unfrozen!")
                .sendTo((TCommandSource) player);
            userTransitCache.joinEnd(userUUID);
            return result;
        });
    }

    @Override
    public CompletableFuture<TString> restore(UUID userUUID, @Nullable String snapshot) {
        return memberManager.getPrimaryComponent()
            .getSnapshotForUser(userUUID, snapshot)
            .exceptionally(e -> {
                e.printStackTrace();
                return Optional.empty();
            })
            .thenApplyAsync(optionalSnapshot -> {
                Optional<TUser> optionalUser = userService.get(userUUID);
                if (!optionalUser.isPresent()) {
                    return textService.builder()
                        .append(pluginInfo.getPrefix())
                        .red().append("Could not find ", userUUID)
                        .build();
                }
                String userName = userService.getUserName(optionalUser.get());
                if (!optionalSnapshot.isPresent()) {
                    return textService.builder()
                        .append(pluginInfo.getPrefix())
                        .red().append("Could not find snapshot for ", userName)
                        .build();
                }
                getPrimaryComponent().deserialize(optionalSnapshot.get(), optionalUser.get());
                return textService.builder()
                    .append(pluginInfo.getPrefix())
                    .yellow().append("Restored snapshot ")
                    .gold().append(getCreatedString(optionalSnapshot.get()))
                    .yellow().append(" for ", userName)
                    .build();
            });
    }
}
