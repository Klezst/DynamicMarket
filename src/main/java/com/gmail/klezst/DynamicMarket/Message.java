package com.gmail.klezst.DynamicMarket;

import java.io.File;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import com.gmail.klezst.util.Messaging;

/**
 * Handles messaging.
 * 
 * @author Klezst
 */
public enum Message
{
	// Alphanumeric order.
	BUY_TOO_MUCH("buy.toomuch"),
	HELP_ADD("help.add"),
	HELP_BUY("help.buy"),
	HELP_EXPORT("help.export"),
	HELP_IDS("help.ids"),
	HELP_IMPORT("help.import"),
	HELP_INFO("help.info"),
	HELP_LIST("help.list"),
	HELP_RELOAD("help.reload"),
	HELP_REMOVE("help.remove"),
	HELP_SELL("help.sell"),
	HELP_TAG_BASEPRICE("help.tag.baseprice"),
	HELP_TAG_BUYABLE("help.tag.buyable"),
	HELP_TAG_MAX_PRICE("help.tag.maxstock"),
	HELP_TAG_MAX_STOCK("help.tag.maxstock"),
	HELP_TAG_MIN_PRICE("help.tag.minprice"),
	HELP_TAG_MIN_STOCK("help.tag.minstock"),
	HELP_TAG_SALESTAX("help.tag.salestax"),
	HELP_TAG_SELLABLE("help.tag.buyable"),
	HELP_TAG_STOCK("help.tag.stock"),
	HELP_TAG_VOLATILITY("help.tag.volatility"),
	HELP_UPDATE("help.update"),
	NO_SPACE("buy.nospace");
	
	private static YamlConfiguration config;
	private static YamlConfiguration getConfig() // Java initializes enums before enum static fields, so we need this function to use the static field during the constructor.
	{
		if (config == null)
		{
			config = YamlConfiguration.loadConfiguration(new File("plugins/DynamicMarket/messages.yml"));
		}
		return config;
	}
	
	private String message;
	
	private Message(String key)
	{
		message = Messaging.parseColor(getConfig().getString(key, ""));
	}
	
	public String getMessage()
	{
		return message;
	}
	
	public void send(CommandSender sender)
	{
		for (String line : message.split("\n"))
		{
			sender.sendMessage(line); // TODO: insert items like $shop$!?!?
		}
	}
}
