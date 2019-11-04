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

package rocks.milspecsg.msdatasync.commands;

import com.google.inject.Inject;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import rocks.milspecsg.msdatasync.MSDataSyncPluginInfo;
import rocks.milspecsg.msdatasync.api.serializer.user.UserSerializerManager;
import rocks.milspecsg.msdatasync.model.core.snapshot.Snapshot;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

public class UploadStartCommand implements CommandExecutor {

    @Inject
    private UserSerializerManager<Snapshot<?>, User> userSerializer;

    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException {

        SyncLockCommand.assertUnlocked(source);

        // serialize everyone on the server
        Collection<Player> players = Sponge.getServer().getOnlinePlayers();
        ConcurrentLinkedQueue<Player> successful = new ConcurrentLinkedQueue<>();
        ConcurrentLinkedQueue<Player> unsuccessful = new ConcurrentLinkedQueue<>();

        if (players.isEmpty()) {
            throw new CommandException(Text.of(MSDataSyncPluginInfo.pluginPrefix, "There are no players currently online"));
        }

        for (Player player : players) {
            userSerializer.getPrimaryComponent().serialize(player).thenAcceptAsync(optionalSnapshot -> {
                if (optionalSnapshot.isPresent()) {
                    successful.add(player);
                } else {
                    unsuccessful.add(player);
                }
                if (successful.size() + unsuccessful.size() >= players.size()) {
                    if (successful.size() > 0) {
                        String s = successful.stream().map(User::getName).collect(Collectors.joining(", "));
                        source.sendMessage(
                            Text.of(TextColors.YELLOW, "The following players were successfully serialized: \n", TextColors.GREEN, s)
                        );
                    }
                    if (unsuccessful.size() > 0) {
                        String u = unsuccessful.stream().map(User::getName).collect(Collectors.joining(", "));
                        source.sendMessage(
                            Text.of(TextColors.RED, "The following players were unsuccessfully serialized: \n", u)
                        );
                    }
                }
            });
        }

        return CommandResult.success();
    }
}
