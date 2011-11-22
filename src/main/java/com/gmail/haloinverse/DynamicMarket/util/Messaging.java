package com.gmail.haloinverse.DynamicMarket.util;

import org.bukkit.ChatColor;

public class Messaging
{
    private static String colNormal; // Normal text color {}.
    private static String colCmd; // Command highlight color {CMD}.
    private static String colBracket; // Highlighting of brackets around params/data {BKT}.
    private static String colParam; // Highlighting of parameters {PRM}.
    private static String colError; // Highlighting for errors {ERR}.
    
    public static void initialize(ChatColor colNormalIn, ChatColor colCmdIn, ChatColor colBracketIn, ChatColor colParamIn, ChatColor colErrorIn)
    {
    	colNormal = colNormalIn.toString();
    	colCmd = colCmdIn.toString();
    	colBracket = colBracketIn.toString();
    	colParam = colParamIn.toString();
    	colError = colErrorIn.toString();
    }
    
    /**
     * Surrounds text with '-'s.
     * 
     * @param innerText, The text to be surrounded by '-'s.
     * @return A string surrounded by '-'s.
     * @author Nijikokun.
     */
    public static String headerify(String innerText)
    {
        // This is capable of crashing the client!
        int extraLength = innerText.length() - stripColor(innerText).length();
        String newString = "--" + innerText + "------------------------------------------------------------";
        return newString.substring(0, 50 + extraLength);
        // This is approximate, due to inability to get string width of the proportional font.
    }
    
    /**
     * Removes color codes from the string.
     * 
     * @param toStrip, The string to remove colors from.
     * @return toStrip without colors.
     */
    public static String stripColor(String toStrip)
    {
        return toStrip.replaceAll("&[a-z0-9]", "").replace("&&", "&").replace("{}", "").replace("{CMD}", "").replace("{BKT}", "").replace("{ERR}", "").replace("{PRM}", "");
    }
    
    public static String parseColor(String original)
    {
        return original.replaceAll("(&([a-z0-9]))", "\u00A7$2").replace("&&", "&").replace("{}", colNormal).replace("{CMD}", colCmd).replace("{BKT}", colBracket).replace("{ERR}", colError).replace("{PRM}", colParam);
    }
    

}
