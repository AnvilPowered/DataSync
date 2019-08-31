package rocks.milspecsg.msdatasync.service.implementation.tasks;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import rocks.milspecsg.msdatasync.MSDataSync;
import rocks.milspecsg.msdatasync.PluginInfo;
import rocks.milspecsg.msdatasync.api.data.PlayerSerializer;
import rocks.milspecsg.msdatasync.model.core.Member;
import rocks.milspecsg.msdatasync.service.tasks.ApiSerializationTaskService;
import rocks.milspecsg.msdatasync.service.config.ConfigKeys;
import rocks.milspecsg.msrepository.api.config.ConfigurationService;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Singleton
public class MSSerializationTaskService extends ApiSerializationTaskService {

    @Inject
    ConfigurationService configurationService;

    @Inject
    PlayerSerializer<Member, Player> playerSerializer;

    @Override
    public void startSerializationTask() {
        Integer interval = configurationService.getConfigInteger(ConfigKeys.SERIALIZATION_TASK_INTERVAL_SECONDS);
        Task.builder().interval(interval, TimeUnit.SECONDS).execute(getSerializationTask()).submit(MSDataSync.plugin);
    }

    @Override
    public Runnable getSerializationTask() {
        return () -> {
            Collection<Player> players = Sponge.getServer().getOnlinePlayers();
            ConcurrentLinkedQueue<Player> successful = new ConcurrentLinkedQueue<>();
            ConcurrentLinkedQueue<Player> unsuccessful = new ConcurrentLinkedQueue<>();

            Text toSend;

            if (players.isEmpty()) {
                toSend = Text.of(PluginInfo.PluginPrefix, TextColors.YELLOW, "Starting sync task... no players online, skipping!");
            } else {
                toSend = Text.of(PluginInfo.PluginPrefix, TextColors.YELLOW, "Starting sync task...");
            }

            Sponge.getServer().getConsole().sendMessage(toSend);

            for (Player player : players) {
                playerSerializer.serialize(player).thenAcceptAsync(success -> {
                    if (success) {
                        successful.add(player);
                    } else {
                        unsuccessful.add(player);
                    }
                    if (successful.size() + unsuccessful.size() >= players.size()) {
                        if (successful.size() > 0) {
                            String s = successful.stream().map(User::getName).collect(Collectors.joining(","));
                            Sponge.getServer().getConsole().sendMessage(
                                Text.of(TextColors.YELLOW, "The following players were successfully serialized: \n", TextColors.GREEN, s)
                            );
                        }
                        if (unsuccessful.size() > 0) {
                            String u = unsuccessful.stream().map(User::getName).collect(Collectors.joining(","));
                            Sponge.getServer().getConsole().sendMessage(
                                Text.of(TextColors.RED, "The following players were unsuccessfully serialized: \n", u)
                            );
                        }
                    }
                });
            }
        };
    }
}
