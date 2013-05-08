package com.github.games647.scoreboardstats.listener;import static com.github.games647.scoreboardstats.ScoreboardStats.getSettings;import com.github.games647.scoreboardstats.pvpstats.Database;import static com.github.games647.scoreboardstats.scoreboard.Score.createScoreboard;import org.bukkit.entity.Player;import org.bukkit.event.EventHandler;public final class PlayerListener implements org.bukkit.event.Listener {    @EventHandler    public void onDeath(final org.bukkit.event.entity.PlayerDeathEvent death) {        final Player killed = death.getEntity();        if (!getSettings().isPvpstats() || getSettings().checkWorld(killed.getWorld().getName()) || Database.getCache(killed.getName()) == null) {            return;        }        Database.getCache(killed.getName()).onDeath();        final Player killer = killed.getKiller();        if (killer != null && killer.isOnline() && Database.getCache(killer.getName()) != null) {            Database.getCache(killer.getName()).onKill();        }    }    @EventHandler(priority = org.bukkit.event.EventPriority.MONITOR) // To set the scoreboard correctly    public void onJoin(final org.bukkit.event.player.PlayerJoinEvent join) {        final Player player = join.getPlayer();        if (getSettings().checkWorld(player.getWorld().getName())) {            return;        }        if (getSettings().isPvpstats()) {            Database.loadAccount(player.getName());        }        createScoreboard(player);    }    @EventHandler(ignoreCancelled = true)    public void onChange(final org.bukkit.event.player.PlayerChangedWorldEvent teleport) {        final Player player = teleport.getPlayer();        if (getSettings().checkWorld(player.getWorld().getName())) {            player.getScoreboard().clearSlot(org.bukkit.scoreboard.DisplaySlot.SIDEBAR);        } else {            createScoreboard(player);        }    }    @EventHandler    public void onKick(final org.bukkit.event.player.PlayerKickEvent kick) {        Database.saveAccount(kick.getPlayer().getName(), true);    }    @EventHandler    public void onQuit(final org.bukkit.event.player.PlayerQuitEvent quit) {        Database.saveAccount(quit.getPlayer().getName(), true);    }}