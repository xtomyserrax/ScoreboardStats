package com.github.games647.scoreboardstats.variables;

import java.util.Iterator;
import java.util.Map;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;

public class PluginListener implements Listener {

    private final ReplaceManager replaceManager;

    public PluginListener(ReplaceManager replaceManager) {
        this.replaceManager = replaceManager;
    }

    /**
     * Check for disabled plugin to re add the associated replacer
     *
     * @param enableEvent the enable event
     */
    @EventHandler
    public void onPluginEnable(PluginEnableEvent enableEvent) {
        //Register the listener back again if the plugin is available
        final String enablePluginName = enableEvent.getPlugin().getName();
        for (Map.Entry<Class<? extends VariableReplacer>, String> entry : replaceManager.getDefaults().entrySet()) {
            final String pluginName = entry.getValue();
            if (enablePluginName.equals(entry.getValue())) {
                replaceManager.registerDefault(entry.getKey(), pluginName);
            }
        }
    }

    /**
     * Check for disabled plugin to remove the associated replacer
     *
     * @param disableEvent the disable event
     */
    @EventHandler
    public void onPluginDisable(PluginDisableEvent disableEvent) {
        //Remove the listener if the associated plugin was disabled
        final String disablePluginName = disableEvent.getPlugin().getName();

        final Iterator<Map.Entry<VariableReplacer, String>> iter = replaceManager.getReplacers().entrySet().iterator();
        while (iter.hasNext()) {
            final Map.Entry<VariableReplacer, String> element = iter.next();
            final String pluginName = element.getValue();
            if (disablePluginName.equals(pluginName)) {
                iter.remove();
                final VariableReplacer toRemove = element.getKey();
                final Iterator<VariableReplacer> iterator = replaceManager.getSpecificReplacers().values().iterator();
                while (iterator.hasNext()) {
                    if (iter.next().equals(toRemove)) {
                        iterator.remove();
                    }
                }
            }
        }
    }
}