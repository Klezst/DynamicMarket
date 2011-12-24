package com.gmail.klezst.util;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class Message {
    private static String colNormal; // Normal text color {}.
    private static String colCmd; // Command highlight color {CMD}.
    private static String colBracket; // Highlighting of brackets around params/data {BKT}.
    private static String colParam; // Highlighting of parameters {PRM}.
    private static String colError; // Highlighting for errors {ERR}.

    public static void initialize(ChatColor colNormalIn, ChatColor colCmdIn,
	    ChatColor colBracketIn, ChatColor colParamIn, ChatColor colErrorIn) {
	colNormal = colNormalIn.toString();
	colCmd = colCmdIn.toString();
	colBracket = colBracketIn.toString();
	colParam = colParamIn.toString();
	colError = colErrorIn.toString();
    }

    public static String combine(String separator, Object... args) {
	String line = "";
	for (Object arg : args) {
	    line += arg + separator;
	}
	line = line.substring(0, line.length() - 1); // Remove the extra '\n'.
	return parseColor(line);
    }

    // TODO: Update all sender.sendMessage(Messaging.parseColor((String)) to this method instead.
    public static void send(CommandSender recipient, String... messages) {
	for (String message : messages) {
	    String[] lines = message.split("\n");
	    for (String line : lines) {
		recipient.sendMessage(parseColor(line));
	    }
	}
    }

    /**
     * Surrounds text with '-'s.
     * 
     * @param innerText
     *            , The text to be surrounded by '-'s.
     * @return A string surrounded by '-'s.
     * @author Nijikokun
     * @author Klezst
     */
    public static String headerify(String innerText) {
	// This is capable of crashing the client!
	int dashes = (50 - stripColor(innerText).length()) / 2;
	return "{}" + repeat("-", dashes) + parseColor(innerText) + "{}"
		+ repeat("-", dashes); // innerText defaults to {} color, if no color is specified.
    }

    public static String repeat(String text, int times) {
	String result = "";
	for (int i = 0; i < times; i++) {
	    result += text;
	}
	return result;
    }

    /**
     * Removes color codes from the string.
     * 
     * @param toStrip
     *            , The string to remove colors from.
     * @return toStrip without colors.
     */
    public static String stripColor(String toStrip) {
	return toStrip.replaceAll("&[a-z0-9]", "").replace("&&", "&")
		.replace("{}", "").replace("{CMD}", "").replace("{BKT}", "")
		.replace("{ERR}", "").replace("{PRM}", "");
    }

    public static String parseColor(String original) {
	return original.replaceAll("(&([a-z0-9]))", "\u00A7$2")
		.replace("&&", "&").replace("{}", colNormal)
		.replace("{CMD}", colCmd).replace("{BKT}", colBracket)
		.replace("{ERR}", colError).replace("{PRM}", colParam);
    }

}
