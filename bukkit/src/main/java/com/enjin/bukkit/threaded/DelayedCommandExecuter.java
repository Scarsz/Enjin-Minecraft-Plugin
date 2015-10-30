package com.enjin.bukkit.threaded;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.PriorityBlockingQueue;

import com.enjin.bukkit.CommandWrapper;
import com.enjin.core.Enjin;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import com.enjin.bukkit.EnjinMinecraftPlugin;

public class DelayedCommandExecuter implements Runnable {

    PriorityBlockingQueue<CommandWrapper> commandqueue = new PriorityBlockingQueue<CommandWrapper>();
    long nexttime = -1;
    long lastsavetime = 0;
    boolean dirty = false;

    EnjinMinecraftPlugin plugin;

    public DelayedCommandExecuter(EnjinMinecraftPlugin plugin) {
        lastsavetime = System.currentTimeMillis();
        this.plugin = plugin;
    }

    public synchronized void addCommand(CommandWrapper command) {
        commandqueue.add(command);
        CommandWrapper comm;
        if ((comm = commandqueue.peek()) != null) {
            nexttime = comm.getDelay();
        } else {
            nexttime = -1;
        }
        dirty = true;
    }

    public void saveCommands() {
        //If the data isn't dirty, don't write it out.
        if (!dirty) {
            return;
        }
        BufferedWriter buffWriter = null;
        try {
            File commandstoexecute = new File(plugin.getDataFolder(), "commandqueue.txt");
            FileWriter fileWriter = new FileWriter(commandstoexecute, false);
            buffWriter = new BufferedWriter(fileWriter);
            //set the dirty variable here, just in case we get more stuff in afterwards.
            dirty = false;
            Iterator<CommandWrapper> thequeue = commandqueue.iterator();
            while (thequeue.hasNext()) {
                CommandWrapper thecommand = thequeue.next();
                buffWriter.write(thecommand.toString());
                buffWriter.newLine();
            }
        } catch (Exception e) {
            //Something happened on write! Also, we need to re-set the dirty flag.
            dirty = true;
        } finally {
            if (buffWriter != null) {
                try {
                    buffWriter.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        //reset the time, so that we aren't trying to write to a non-existent file every half second.
        lastsavetime = System.currentTimeMillis();
    }

    public void loadCommands(CommandSender sender) {
        commandqueue.clear();
        try {
            File commandstoexecute = new File(plugin.getDataFolder(), "commandqueue.txt");
            if (!commandstoexecute.exists()) {
                return;
            }
            FileReader fileReader = new FileReader(commandstoexecute.getPath());
            BufferedReader buffReader = new BufferedReader(fileReader);
            String currentLine = "";
            while (((currentLine = buffReader.readLine()) != null)) {
                String[] commandsplit = currentLine.split("\0");
                if (commandsplit.length > 2) {
                    addCommand(new CommandWrapper(sender, commandsplit[0], Long.getLong(commandsplit[1]), commandsplit[2]));
                } else if (commandsplit.length > 1) {
                    addCommand(new CommandWrapper(sender, commandsplit[0], Long.getLong(commandsplit[1]), ""));
                } else {
                    //Well, we couldn't split it, so execute the value immediately
                    addCommand(new CommandWrapper(sender, commandsplit[0], 0L, ""));
                }
            }
            buffReader.close();
            return;
        } catch (Exception e) {
            return;
        }
    }

    @Override
    public synchronized void run() {
        if (nexttime > -1 && nexttime <= System.currentTimeMillis()) {
            CommandWrapper comm;
            try {
                while ((comm = commandqueue.peek()) != null && comm.getDelay() <= System.currentTimeMillis()) {
                    comm = commandqueue.poll();
                    Enjin.getPlugin().debug("Executing delayed command: " + comm.getCommand());
                    Bukkit.getServer().dispatchCommand(comm.getSender(), comm.getCommand());
                    comm.setResult(plugin.getLastLogLine());
                    Enjin.getPlugin().debug("Result: " + comm.getResult());
                }
            } catch (Exception e) {
                //Concurrent modification or null exception anyone?
            }
            if ((comm = commandqueue.peek()) != null) {
                nexttime = comm.getDelay();
            } else {
                nexttime = -1;
            }
            dirty = true;
        }
        //Save the file every 60 seconds if changes have been made.
        if (dirty && lastsavetime + 1000 * 60 > System.currentTimeMillis()) {
            saveCommands();
        }
    }
}