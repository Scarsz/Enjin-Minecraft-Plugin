package com.enjin.officialplugin.threaded;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.enjin.officialplugin.EnjinErrorReport;
import com.enjin.officialplugin.EnjinMinecraftPlugin;
import com.enjin.officialplugin.packets.Packet10AddPlayerGroup;
import com.enjin.officialplugin.packets.Packet11RemovePlayerGroup;
import com.enjin.officialplugin.packets.Packet12ExecuteCommand;
import com.enjin.officialplugin.packets.Packet13ExecuteCommandAsPlayer;
import com.enjin.officialplugin.packets.Packet14NewerVersion;
import com.enjin.officialplugin.packets.Packet17AddWhitelistPlayers;
import com.enjin.officialplugin.packets.Packet18RemovePlayersFromWhitelist;

/**
 * 
 * @author OverCaste (Enjin LTE PTD).
 * This software is released under an Open Source license.
 * @copyright Enjin 2012.
 * 
 */

public class PeriodicEnjinTask implements Runnable {
	
	EnjinMinecraftPlugin plugin;
	ConcurrentHashMap<String, String> removedplayerperms = new ConcurrentHashMap<String, String>();
	ConcurrentHashMap<String, String> removedplayervotes = new ConcurrentHashMap<String, String>();
	int numoffailedtries = 0;
	
	public PeriodicEnjinTask(EnjinMinecraftPlugin plugin) {
		this.plugin = plugin;
	}
	private URL getUrl() throws Throwable {
		return new URL((EnjinMinecraftPlugin.usingSSL ? "https" : "http") + EnjinMinecraftPlugin.apiurl + "minecraft-sync");
	}
	
	@Override
	public void run() {
		boolean successful = false;
		StringBuilder builder = new StringBuilder();
		try {
			plugin.debug("Connecting to Enjin...");
			URL enjinurl = getUrl();
			HttpURLConnection con;
			// Mineshafter creates a socks proxy, so we can safely bypass it
	        // It does not reroute POST requests so we need to go around it
	        if (isMineshafterPresent()) {
	            con = (HttpURLConnection) enjinurl.openConnection(Proxy.NO_PROXY);
	        } else {
	            con = (HttpURLConnection) enjinurl.openConnection();
	        }
			con.setRequestMethod("POST");
			con.setReadTimeout(15000);
			con.setConnectTimeout(15000);
			con.setDoInput(true);
			con.setDoOutput(true);
			con.setRequestProperty("User-Agent", "Mozilla/4.0");
			con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			//StringBuilder builder = new StringBuilder();
			builder.append("authkey=" + encode(EnjinMinecraftPlugin.hash));
			builder.append("&maxplayers=" + encode(String.valueOf(Bukkit.getServer().getMaxPlayers()))); //max players
			builder.append("&players=" + encode(String.valueOf(Bukkit.getServer().getOnlinePlayers().length))); //current players
			builder.append("&hasranks=" + encode(((EnjinMinecraftPlugin.permission == null || EnjinMinecraftPlugin.permission.getName().equalsIgnoreCase("SuperPerms")) ? "FALSE" : "TRUE")));
			builder.append("&pluginversion=" + encode(plugin.getDescription().getVersion()));
			builder.append("&plugins=" + encode(getPlugins()));
			builder.append("&playerlist=" + encode(getPlayers()));
			builder.append("&worlds=" + encode(getWorlds()));
			builder.append("&time=" + encode(getTimes()));
			//Don't add the votifier tag if no one has voted.
			if(plugin.playervotes.size() > 0) {
				builder.append("&votifier=" + encode(getVotes()));
			}
			
			//We don't want to throw any errors if they are using superperms
			//which doesn't support groups. Therefore we can't support it.
			if(EnjinMinecraftPlugin.permission != null && !EnjinMinecraftPlugin.permission.getName().equalsIgnoreCase("SuperPerms")) {
				builder.append("&groups=" + encode(getGroups()));
				if(plugin.playerperms.size() > 0) {
					builder.append("&playergroups=" + encode(getPlayerGroups()));
				}
			}
			con.setRequestProperty("Content-Length", String.valueOf(builder.length()));
			plugin.debug("Sending content: \n" + builder.toString());
			con.getOutputStream().write(builder.toString().getBytes());
			//System.out.println("Getting input stream...");
			InputStream in = con.getInputStream();
			//System.out.println("Handling input stream...");
			handleInput(in);
			successful = true;
		} catch (SocketTimeoutException e) {
			//We don't need to spam the console every minute if the synch didn't complete correctly.
			if(numoffailedtries++ > 5) {
				Bukkit.getLogger().warning("[Enjin Minecraft Plugin] Timeout, the enjin server didn't respond within the required time. Please be patient and report this bug to enjin.");
				numoffailedtries = 0;
			}
			plugin.lasterror = new EnjinErrorReport(e, "Regular synch. Information sent:\n" + builder.toString());
		} catch (Throwable t) {
			//We don't need to spam the console every minute if the synch didn't complete correctly.
			if(numoffailedtries++ > 5) {
				Bukkit.getLogger().warning("[Enjin Minecraft Plugin] Oops, we didn't get a proper response, we may be doing some maintenance. Please be patient and report this bug to enjin if it persists.");
				numoffailedtries = 0;
			}
			if(plugin.debug) {
				t.printStackTrace();
			}
			plugin.lasterror = new EnjinErrorReport(t, "Regular synch. Information sent:\n" + builder.toString());
		}
		if(!successful) {
			plugin.debug("Synch unsuccessful.");
			Set<Entry<String, String>> es = removedplayerperms.entrySet();
			for(Entry<String, String> entry : es) {
				//If the plugin has put a new set of player perms in for this player,
				//let's not overwrite it.
				if(!plugin.playerperms.containsKey(entry.getKey())) {
					plugin.playerperms.put(entry.getKey(), entry.getValue());
				}
				removedplayerperms.remove(entry.getKey());
			}
			
			Set<Entry<String, String>> voteset = removedplayervotes.entrySet();
			for(Entry<String, String> entry : voteset) {
				//If the plugin has put new votes in, let's not overwrite them.
				if(plugin.playervotes.containsKey(entry.getKey())) {
					//combine the lists.
					String lists = plugin.playervotes.get(entry.getKey()) + "," + entry.getValue();
					plugin.playervotes.put(entry.getKey(), lists);
				}else {
					plugin.playervotes.put(entry.getKey(), entry.getValue());
				}
				removedplayervotes.remove(entry.getKey());
			}
		}else {
			plugin.debug("Synch successful.");
		}
	}
	
	private String getVotes() {
		removedplayervotes.clear();
		StringBuilder votes = new StringBuilder();
		Set<Entry<String,String>> voteset = plugin.playervotes.entrySet();
		for(Entry<String, String> entry : voteset) {
			String player = entry.getKey();
			String lists = entry.getValue();
			if(votes.length() != 0) {
				votes.append(";");
			}
			votes.append(player + ":" + lists);
			removedplayervotes.put(player, lists);
			plugin.playervotes.remove(player);
		}
		return votes.toString();
	}
	private String getPlayerGroups() {
		removedplayerperms.clear();
		HashMap<String, String> theperms = new HashMap<String, String>();
		Iterator<Entry<String, String>> es = plugin.playerperms.entrySet().iterator();
		
		//With the push command, we need to limit how many ranks we send
		//with each synch.
		for(int k = 0; es.hasNext() && k < 3000; k++) {
			Entry<String, String> entry = es.next();
			StringBuilder perms = new StringBuilder();
			//Let's get global groups
			LinkedList<String> globalperms = new LinkedList<String>();
			//We don't want to get global groups with plugins that don't support it.
			if(plugin.supportsglobalgroups) {
				String[] tempperms = EnjinMinecraftPlugin.permission.getPlayerGroups((World)null, entry.getKey());
				if(perms.length() > 0 && tempperms.length > 0) {
					perms.append("|");
				}

				if(tempperms != null && tempperms.length > 0) {
					perms.append('*' + ":");
					for(int i = 0, j = 0; i < tempperms.length; i++) {
						if(j > 0) {
							perms.append(",");
						}
						globalperms.add(tempperms[i]);
						perms.append(tempperms[i]);
						j++;
					}
				}
			}
			//Now let's get groups per world.
			for(World w: Bukkit.getWorlds()) {
				String[] tempperms = EnjinMinecraftPlugin.permission.getPlayerGroups(w, entry.getKey());
				if(tempperms != null && tempperms.length > 0) {
					
					//The below variable is only used for GroupManager since it
					//likes transmitting all the groups
					LinkedList<String> skipgroups = new LinkedList<String>();
					
					LinkedList<String> worldperms = new LinkedList<String>();
					for(int i = 0; i < tempperms.length; i++) {
						//GroupManager has a bug where all lower groups in a hierarchy get
						//transmitted along with the main group, so we have to catch and
						//process it differently.
						if(plugin.groupmanager != null) {
							List<String> subgroups = plugin.groupmanager.getWorldsHolder().getWorldData(w.getName()).getGroup(tempperms[i]).getInherits();
							for(String group : subgroups) {
								//Groups are case insensitive in GM
								skipgroups.add(group.toLowerCase());
							}
						}
						if(globalperms.contains(tempperms[i]) || (EnjinMinecraftPlugin.usingGroupManager && (tempperms[i].startsWith("g:") || skipgroups.contains(tempperms[i].toLowerCase())))) {
							continue;
						}
						worldperms.add(tempperms[i]);
					}
					if(perms.length() > 0 && worldperms.size() > 0) {
						perms.append("|");
					}
					if(worldperms.size() > 0) {
						perms.append(w.getName() + ":");
						for(int i = 0; i < worldperms.size(); i++) {
							if(i > 0) {
								perms.append(",");
							}
							perms.append(worldperms.get(i));
						}
					}
				}
			}
			theperms.put(entry.getKey(), perms.toString());
			//remove that player from the list.
			plugin.playerperms.remove(entry.getKey());
			//If the synch fails we need to put the values back...
			removedplayerperms.put(entry.getKey(), entry.getValue());
		}
		StringBuilder allperms = new StringBuilder();
		Set<Entry<String, String>> ns = theperms.entrySet();
		for(Entry<String, String> entry : ns) {
			if(allperms.length() > 0) {
				allperms.append("\n");
			}
			allperms.append(entry.getKey() + ";" + entry.getValue());
		}
		return allperms.toString();
	}
	private String encode(String in) throws UnsupportedEncodingException {
		return URLEncoder.encode(in, "UTF-8");
		//return in;
	}
	
	private void handleInput(InputStream in) throws IOException {
		BufferedInputStream bin = new BufferedInputStream(in);
		bin.mark(Integer.MAX_VALUE);
		//TODO: A for loop??? Maybe a while(code = in.read() != -1) {}
		for(;;) {
			int code = bin.read();
			switch(code) {
			case -1:
				plugin.debug("No more packets. End of stream. Update ended.");
				if(plugin.debug) {
					bin.reset();
					StringBuilder input = new StringBuilder();
					while((code = bin.read()) != -1) {
						input.append((char)code);
					}
					plugin.debug("Raw data received:\n" + input.toString());
				}
				return; //end of stream reached
			case 0x10:
				plugin.debug("Packet [0x10](Add Player Group) received.");
				Packet10AddPlayerGroup.handle(bin, plugin);
				break;
			case 0x11:
				plugin.debug("Packet [0x11](Remove Player Group) received.");
				Packet11RemovePlayerGroup.handle(bin, plugin);
				break;
			case 0x12:
				plugin.debug("Packet [0x12](Execute Command) received.");
				Packet12ExecuteCommand.handle(bin, plugin);
				break;
			case 0x13:
				plugin.debug("Packet [0x13](Execute command as Player) received.");
				Packet13ExecuteCommandAsPlayer.handle(bin, plugin);
				break;
			case 0x0A:
				plugin.debug("Packet [0x0A](New Line) received, ignoring...");
				break;
			case 0x0D:
				plugin.debug("Packet [0x0D](Carriage Return) received, ignoring...");
				break;
			case 0x14:
				plugin.debug("Packet [0x14](Newer Version) received.");
				Packet14NewerVersion.handle(bin, plugin);
				break;
			case 0x17:
				plugin.debug("Packet [0x17](Add Whitelist Players) received.");
				Packet17AddWhitelistPlayers.handle(bin, plugin);
				break;
			case 0x18:
				plugin.debug("Packet [0x18](Remove Players From Whitelist) received.");
				Packet18RemovePlayersFromWhitelist.handle(bin, plugin);
				break;
			default :
				Bukkit.getLogger().warning("[Enjin] Received an invalid opcode: " + code);
			}
		}
	}

	private String getPlugins() {
		StringBuilder builder = new StringBuilder();
		for(Plugin p : Bukkit.getPluginManager().getPlugins()) {
			builder.append(',');
			builder.append(p.getName());
		}
		if(builder.length() > 2) {
			builder.deleteCharAt(0);
		}
		return builder.toString();
	}
	
	private String getPlayers() {
		StringBuilder builder = new StringBuilder();
		for(Player p : Bukkit.getOnlinePlayers()) {
			builder.append(',');
			builder.append(p.getName());
		}
		if(builder.length() > 2) {
			builder.deleteCharAt(0);
		}
		return builder.toString();
	}
	
	private String getGroups() {
		StringBuilder builder = new StringBuilder();
		if(EnjinMinecraftPlugin.usingGroupManager) {
			for(String group : EnjinMinecraftPlugin.permission.getGroups()) {
				builder.append(',');
				builder.append(group);
			}
		} else {
			for(String group : EnjinMinecraftPlugin.permission.getGroups()) {
				if(group.startsWith("g:")) {
					continue;
				}
				builder.append(',');
				builder.append(group);
			}
		}
		if(builder.length() > 2) {
			builder.deleteCharAt(0);
		}
		return builder.toString();
	}
	
	private String getWorlds() {
		StringBuilder builder = new StringBuilder();
		for(World w : Bukkit.getWorlds()) {
			builder.append(',');
			builder.append(w.getName());
		}
		if(builder.length() > 2) {
			builder.deleteCharAt(0);
		}
		return builder.toString();
	}
	
	//Get world times strings
	private String getTimes() {
		StringBuilder builder = new StringBuilder();
		for(World w : Bukkit.getWorlds()) {
			//Make sure it's a normal envrionment, as the end and the
			//nether don't have any weather.
			if(w.getEnvironment() == Environment.NORMAL) {
				if(builder.length() > 0) {
					builder.append(";");
				}
				builder.append(w.getName() + ":" + Long.toString(w.getTime()) + ",");
				int moonphase = (int) ((w.getFullTime()/24000)%8);
				builder.append(Integer.toString(moonphase) + ",");
				if(w.hasStorm()) {
					if(w.isThundering()) {
						builder.append("2");
					}else {
						builder.append("1");
					}
				}else {
					builder.append("0");
				}
			}
		}
		return builder.toString();
	}
	/**
	 * Check if mineshafter is present. If it is, we need to bypass it to send POST requests
	 *
	 * @return
	 */
	private boolean isMineshafterPresent() {
	    try {
	        Class.forName("mineshafter.MineServer");
	        return true;
	    } catch (Exception e) {
	        return false;
	    }
	}
}
