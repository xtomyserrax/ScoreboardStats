package com.github.games647.scoreboardstats.scoreboard;

import static com.github.games647.scoreboardstats.ScoreboardStats.getInstance;
import static com.github.games647.scoreboardstats.ScoreboardStats.getSettings;
import com.github.games647.scoreboardstats.pvpstats.Database;
import com.github.games647.variables.Other;
import com.github.games647.variables.Permissions;
import java.util.Map;
import org.bukkit.Bukkit;
import static org.bukkit.ChatColor.translateAlternateColorCodes;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

public final class SbManager {

    public static void createScoreboard(final Player player) {
        if (!player.hasPermission(Permissions.USE_PERMISSION)) {
            return;
        }

        if (player.getScoreboard().getObjective(DisplaySlot.SIDEBAR) != null
                && !player.getScoreboard().getObjective(DisplaySlot.SIDEBAR).getName().equals(Other.TOPLIST)) {
            return;
        }

        final Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        final Objective objective = scoreboard.registerNewObjective(Other.PLUGIN_NAME, Other.EMPTY_CRITERA);
        objective.setDisplayName(translateAlternateColorCodes(Other.CHATCOLOR_CHAR, getSettings().getTitle()));

        if (player.isOnline()) {
            player.setScoreboard(scoreboard);
            objective.setDisplaySlot(DisplaySlot.SIDEBAR);
            getSettings().sendUpdate(player, true);

            if (getSettings().isTempScoreboard()) {
                Bukkit.getScheduler().runTaskLater(getInstance(), new com.github.games647.scoreboardstats.pvpstats.TempScoreShow(player), getSettings().getTempShow() * Other.TICKS_PER_SECOND);
            }
        }
    }

    public static void createTopListScoreboard(final Player player) {
        final Scoreboard scoreboard = player.getScoreboard();

        if (!player.hasPermission(Permissions.USE_PERMISSION)
                || scoreboard.getObjective(DisplaySlot.SIDEBAR) == null
                || !scoreboard.getObjective(DisplaySlot.SIDEBAR).getName().startsWith(Other.PLUGIN_NAME)) {
            return;
        }

        if (scoreboard.getObjective(Other.TOPLIST) != null) {
            scoreboard.getObjective(Other.TOPLIST).unregister();  //to remove the old scores
        }

        final Objective objective = scoreboard.registerNewObjective(Other.TOPLIST, Other.EMPTY_CRITERA);
        objective.setDisplayName(translateAlternateColorCodes(Other.CHATCOLOR_CHAR, getSettings().getTempTitle()));

        if (player.isOnline()) {
            player.setScoreboard(scoreboard);
            final java.util.Map<String, Integer> top = Database.getTop();
            final String color = getSettings().getTempColor();
            Bukkit.getScheduler().runTaskLater(getInstance(), new com.github.games647.scoreboardstats.pvpstats.TempScoreDisapper(player), getSettings().getTempDisapper() * Other.TICKS_PER_SECOND);
            objective.setDisplaySlot(DisplaySlot.SIDEBAR);

            for (final Map.Entry<String, Integer> entry : top.entrySet()) {
                sendScore(objective, String.format("%s%s", color, checkLength(entry.getKey())), entry.getValue(), true);
            }
        }
    }

    public static void sendScore(final Objective objective, final String title, final int value, final boolean complete) {
        final org.bukkit.scoreboard.Score score = objective.getScore(Bukkit.getOfflinePlayer(translateAlternateColorCodes('&', title)));

        if (complete && value == 0) { //Have to use this because the score wouldn't send otherwise
            score.setScore(-1);
        }

        score.setScore(value);
    }

    private static String checkLength(final String check) {

        return (check.length() > Other.MINECRAFT_LIMIT - 2) ? check.substring(0, Other.MINECRAFT_LIMIT - 2) : check; //Because adding the color
    }

    public static void regAll() {
        final boolean ispvpstats = getSettings().isPvpStats();

        for (final Player player : Bukkit.getOnlinePlayers()) {
            if (!player.isOnline()) {
                continue;
            }

            if (ispvpstats) {
                Database.loadAccount(player.getName());
            }

            createScoreboard(player);
        }
    }

    public static void unregisterAll() {
        for (final Player player : Bukkit.getOnlinePlayers()) {
            if (!player.isOnline()) {
                continue;
            }

            player.getScoreboard().clearSlot(DisplaySlot.SIDEBAR);
        }
    }
}
