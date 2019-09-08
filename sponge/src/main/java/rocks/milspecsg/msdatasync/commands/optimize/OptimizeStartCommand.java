package rocks.milspecsg.msdatasync.commands.optimize;

import com.google.inject.Inject;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import rocks.milspecsg.msdatasync.MSDataSyncPluginInfo;
import rocks.milspecsg.msdatasync.commands.SyncLockCommand;
import rocks.milspecsg.msdatasync.misc.SnapshotOptimizationService;

import java.util.Collection;
import java.util.Optional;

public class OptimizeStartCommand implements CommandExecutor {

    @Inject
    SnapshotOptimizationService snapshotOptimizationService;

    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException {

        SyncLockCommand.assertUnlocked(source);

        Optional<String> optionalMode = context.getOne(Text.of("mode"));
        Collection<User> users = context.getAll(Text.of("user"));

        if (!optionalMode.isPresent()) {
            throw new CommandException(Text.of(MSDataSyncPluginInfo.pluginPrefix, "Mode is required"));
        }

        if (optionalMode.get().equals("all")) {
            if (!snapshotOptimizationService.startOptimizeAll(source)) {
                throw new CommandException(Text.of(MSDataSyncPluginInfo.pluginPrefix, "Optimizer already running! Use /sync optimize info"));
            }
            snapshotOptimizationService.startOptimizeAll(source);
        } else {
            if (users.isEmpty()) {
                throw new CommandException(Text.of(MSDataSyncPluginInfo.pluginPrefix, "No users were affected"));
            }
            snapshotOptimizationService.optimize(users, source, "Manual");
        }

        return CommandResult.success();
    }
}