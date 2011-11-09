package com.gmail.haloinverse.DynamicMarket;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Messaging
{
    
    private Player player = null;
    private CommandSender sender = null;
    private DynamicMarket plugin;
    private static String colNormal; // Normal text color {}
    private static String colCmd; // Command highlight color {CMD}
    private static String colBracket; // Highlighting of brackets around params/data {PBK}
    private static String colParam; // Highlighting of parameters.
    private static String colError; // Highlighting for errors. {ERR}

    public Messaging(CommandSender thisSender, DynamicMarket plugin)
    {
        sender = thisSender;
        if (thisSender instanceof Player)
        {
            player = (Player) thisSender;
        }
        this.plugin = plugin;
    }
    
    public boolean isPlayer()
    {
        if (player == null)
        {
            return false;
        }
        return true;
    }
    
    @Deprecated
    public static String argument(String original, String[] arguments,
            String[] points) {
        for (int i = 0; i < arguments.length; ++i) {
            if (arguments[i].contains(",")) {
                for (String arg : arguments[i].split(",")) {
                    original = original.replace(arg, points[i]);
                }
            } else {
                original = original.replace(arguments[i], points[i]);
            }
        }
        
        return original;
    }
    
    public static String parseHighlights(String original) {
        return original.replace("{}", colNormal).replace("{CMD}", colCmd).replace("{BKT}", colBracket).replace("{ERR}", colError).replace("{PRM}", colParam);
    }
    
    public static String stripHighlights(String original) {
        return original.replace("{}", "").replace("{CMD}", "").replace("{BKT}", "").replace("{ERR}", "").replace("{PRM}", "");
    }
    
    public static String parse(String original) {
        return parseHighlights(original).replaceAll("(&([a-z0-9]))", "\u00A7$2").replace("&&", "&");
    }
    
    /*
    public static String colorize(String original) {
        return original.replace("<black>", "&0").replace("<navy>", "&1").replace("<green>", "&2").replace("<teal>", "&3").replace("<red>", "&4").replace("<purple>", "&5").replace("<gold>", "&6").replace("<silver>", "&7").replace("<gray>", "&8").replace("<blue>", "&9").replace("<lime>", "&a").replace("<aqua>", "&b").replace("<rose>", "&c").replace("<pink>", "&d").replace("<yellow>", "&e").replace("<white>", "&f");
    }
    */
    
    public static String bracketize(String message) {
        return "[" + message + "]";
    }
    
    public void send(String message) {
        if (sender != null) {
            sender.sendMessage(parse(message));
        }
    }
    
    public void broadcast(String message) {
        for (Player p : plugin.getServer().getOnlinePlayers()) {
            p.sendMessage(parse(message));
        }
    }
    
    protected static void initialize(ChatColor colNormalIn, ChatColor colCmdIn, ChatColor colBracketIn, ChatColor colParamIn, ChatColor colErrorIn)
    {
    	colNormal = colNormalIn.toString();
    	colCmd = colCmdIn.toString();
    	colBracket = colBracketIn.toString();
    	colParam = colParamIn.toString();
    	colError = colErrorIn.toString();
    }
}
