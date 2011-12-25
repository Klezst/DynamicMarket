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

import org.bukkit.command.CommandSender;

/**
 * 
 * @author Klezst
 */
public class Messaging {
    public static String combine(String separator, Object... args)
    {
	String line = "";
	for (Object arg : args) {
	    line += arg + separator;
	}
	return line.substring(0, line.length() - 1); // Remove the extra '\n'.
    }

    public static String repeat(String text, int times) {
	String result = "";
	for (int i = 0; i < times; i++) {
	    result += text;
	}
	return result;
    }   
    
    public static void send(CommandSender recipient, String... messages) {
	for (String message : messages) {
	    String[] lines = message.split("\n");
	    for (String line : lines) {
		recipient.sendMessage(line);
	    }
	}
    }
}
