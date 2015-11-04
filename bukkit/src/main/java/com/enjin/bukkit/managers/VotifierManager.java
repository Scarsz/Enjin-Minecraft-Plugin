package com.enjin.bukkit.managers;

import com.enjin.bukkit.EnjinMinecraftPlugin;
import com.enjin.bukkit.listeners.VotifierListener;
import com.enjin.bukkit.tasks.VoteSender;
import com.enjin.core.Enjin;
import com.vexsoftware.votifier.Votifier;
import org.bukkit.Bukkit;

public class VotifierManager {
    public static void init(EnjinMinecraftPlugin plugin) {
        if (isVotifierEnabled()) {
            Enjin.getPlugin().debug("Initializing votifier support.");
            Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, new VoteSender(plugin), 20L * 4L, 20L * 4L).getTaskId();
            Bukkit.getPluginManager().registerEvents(new VotifierListener(plugin), plugin);
        }
    }

    public static boolean isVotifierEnabled() {
        return Bukkit.getPluginManager().isPluginEnabled("Votifier");
    }

    public static boolean isVotifierWorking() {
        return !isVotifierEnabled() ? true :  Votifier.getInstance().getVoteReceiver() != null;
    }
}