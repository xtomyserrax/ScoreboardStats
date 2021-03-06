package com.github.games647.scoreboardstats.pvp;

import com.github.games647.scoreboardstats.variables.ReplaceManager;

import java.lang.ref.WeakReference;
import java.time.Instant;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;

/**
 * This class is used for loading the player stats.
 */
public class StatsLoader implements Runnable {

    private final Plugin plugin;

    private final WeakReference<Player> weakPlayer;
    private final WeakReference<Database> weakDatabase;

    /**
     * Creates a new loader for a specific player
     *
     * @param plugin        the owning plugin to reschedule
     * @param player        player instance
     * @param statsDatabase the pvp database
     */
    public StatsLoader(Plugin plugin, Player player, Database statsDatabase) {
        this.plugin = plugin;

        //don't prevent the garbage collection of this player if he logs out
        this.weakPlayer = new WeakReference<>(player);
        this.weakDatabase = new WeakReference<>(statsDatabase);
    }

    @Override
    public void run() {
        final Player player = weakPlayer.get();
        Database statsDatabase = weakDatabase.get();
        if (player != null && statsDatabase != null) {
            PlayerStats stats = statsDatabase.loadAccount(player)
                    .orElse(new PlayerStats(player.getUniqueId(), player.getName()));

            //update player name on every load, because it's changeable
            stats.setPlayername(player.getName());
            stats.setLastOnline(Instant.now());

            Bukkit.getScheduler().runTask(plugin, () -> {
                //possible not thread-safe, so reschedule it while setMetadata is thread-safe
                if (player.isOnline()) {
                    //sets it only if the player is only
                    player.setMetadata("player_stats", new FixedMetadataValue(plugin, stats));
                    ReplaceManager.getInstance().forceUpdate(player, "deaths", stats.getDeaths());
                    ReplaceManager.getInstance().forceUpdate(player, "kdr", stats.getKdr());
                    ReplaceManager.getInstance().forceUpdate(player, "kills", stats.getKills());
                    ReplaceManager.getInstance().forceUpdate(player, "killstreak", stats.getKillstreak());
                    ReplaceManager.getInstance().forceUpdate(player, "current_streak", stats.getCurrentStreak());
                    ReplaceManager.getInstance().forceUpdate(player, "mobkills", stats.getMobkills());
                }
            });
        }
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
