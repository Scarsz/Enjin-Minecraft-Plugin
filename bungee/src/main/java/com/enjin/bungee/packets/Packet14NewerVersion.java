package com.enjin.bungee.packets;

import java.io.BufferedInputStream;
import java.io.File;

import com.enjin.bungee.EnjinPlugin;
import com.enjin.bungee.DownloadPluginThread;
import net.md_5.bungee.api.ProxyServer;

public class Packet14NewerVersion {

    public static void handle(BufferedInputStream in, EnjinPlugin plugin) {
        try {
            String newversion = PacketUtilities.readString(in);
            if (plugin.autoupdate && !plugin.hasupdate) {
                if (plugin.updatefailed) {
                    return;
                }
                //plugin.newversion = newversion;
                plugin.hasupdate = true;
                DownloadPluginThread downloader = new DownloadPluginThread(plugin.getDataFolder().getParent(), newversion, new File(plugin.getDataFolder().getParent() + File.separator + "EnjinMinecraftPlugin.jar"), plugin);
                ProxyServer.getInstance().getScheduler().runAsync(plugin, downloader);
                EnjinPlugin.debug("Updating to new version " + newversion);
            }
        } catch (Throwable t) {
            plugin.getLogger().warning("Failed to dispatch command via 0x14, " + t.getMessage());
            t.printStackTrace();
        }
    }
}