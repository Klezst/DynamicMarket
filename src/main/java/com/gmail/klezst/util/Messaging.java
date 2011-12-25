/*
	BukkitUtil
	Copyright (C) 2011 Klezst

	This program is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.gmail.klezst.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

/**
 * Provides convenience functions for messages.
 * 
 * @author Klezst
 */
public class Messaging {
    private static Set<Map.Entry<String, String>> chatColors;
    static {
	// Get the names of the ChatColors.
	Map<String, String> map = new HashMap<String, String>();
	for (ChatColor chatColor : ChatColor.values()) {
	    map.put("{" + chatColor.name() + "}", chatColor.toString());
	}
	chatColors = map.entrySet();
    }

    /**
     * Returns a String with all elements of args separated by separator.
     * 
     * @param separator
     *            The separator to be used between elements of args.
     * @param args
     *            The objects to be separated by separator.
     * @return, a String with all elements of args separated by separator.
     */
    public static String combine(String separator, Object... args) {
	String line = "";
	for (Object arg : args) {
	    line += arg + separator;
	}
	return line.substring(0, line.length() - 1); // Remove the extra '\n'.
    }

    /**
     * Returns a String with text occurring times times.
     * 
     * @param text
     *            The string to repeat.
     * @param times
     *            The number of times to repeat text.
     * @return, a String with text occurring times times.
     */
    public static String repeat(String text, int times) {
	String result = "";
	for (int i = 0; i < times; i++) {
	    result += text;
	}
	return result;
    }

    /**
     * Sends the messages to recipient. This allows '\n' and places a '\n' between each element of messages.
     * 
     * @param recipient
     *            The CommandSender to receive the messages.
     * @param messages
     *            The messages to send.
     */
    public static void send(CommandSender recipient, String... messages) {
	for (String message : messages) {
	    String[] lines = message.split("\n");
	    for (String line : lines) {
		recipient.sendMessage(line);
	    }
	}
    }

    /**
     * Replaces color tags with color codes.
     * 
     * @param message
     *            The message to add color to.
     * @return a String with color tags replaced with color codes.
     */
    public static String parseColor(final String message) {
	String colored = message;
	for (Entry<String, String> entry : chatColors) {
	    colored = colored.replace(entry.getKey(), entry.getValue());
	}
	return colored;
    }
}
