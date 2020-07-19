package org.anvilpowered.datasync.sponge.command;

import com.google.inject.Inject;
import org.anvilpowered.anvil.api.plugin.PluginInfo;
import org.anvilpowered.datasync.api.serializer.user.UserSerializerManager;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class SpongeSyncTestCommand implements CommandExecutor {

    @Inject
    private UserSerializerManager<User, Text> userSerializerManager;

    @Inject
    private PluginInfo<Text> pluginInfo;

    @Override
    public CommandResult execute(CommandSource source, CommandContext context) {
        if (!(source instanceof Player)) {
            source.sendMessage(Text.of(pluginInfo.getPrefix(), TextColors.RED, "Run as player"));
            return CommandResult.empty();
        }
        Player player = (Player) source;
        userSerializerManager.serialize(player)
            .exceptionally(e -> {
                e.printStackTrace();
                source.sendMessage(Text.of((Object[]) e.getStackTrace()));
                return null;
            })
            .thenAcceptAsync(text -> {
                if (text == null) {
                    return;
                }
                source.sendMessage(text);
                source.sendMessage(Text.of(pluginInfo.getPrefix(),
                    TextColors.GREEN, "Deserializing in 5 seconds"));
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                userSerializerManager.restore(player.getUniqueId(), null)
                    .thenAcceptAsync(source::sendMessage);
            });
        return CommandResult.success();
    }
}