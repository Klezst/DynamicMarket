package dynamicmarket.data;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import bukkitutil.Messaging;

/**
 * Handles messaging.
 * 
 * @author Klezst
 */
public enum Message {
    // Alphanumeric order.
    BUY_TOO_MUCH("buy.too_much"), EXPORT_SUCCESS("export.success"), HELP_ADD(
	    "help.add"), HELP_BUY("help.buy"), HELP_EXPORT("help.export"), HELP_IDS(
	    "help.ids"), HELP_IMPORT("help.import"), HELP_INFO("help.info"), HELP_LIST(
	    "help.list"), HELP_RELOAD("help.reload"), HELP_REMOVE("help.remove"), HELP_SELL(
	    "help.sell"), HELP_TAG_BASEPRICE("help.tag.baseprice"), HELP_TAG_BUYABLE(
	    "help.tag.buyable"), HELP_TAG_MAX_PRICE("help.tag.maxstock"), HELP_TAG_MAX_STOCK(
	    "help.tag.maxstock"), HELP_TAG_MIN_PRICE("help.tag.minprice"), HELP_TAG_MIN_STOCK(
	    "help.tag.minstock"), HELP_TAG_SALESTAX("help.tag.salestax"), HELP_TAG_SELLABLE(
	    "help.tag.buyable"), HELP_TAG_STOCK("help.tag.stock"), HELP_TAG_VOLATILITY(
	    "help.tag.volatility"), HELP_UPDATE("help.update"), LOW_STOCK(
	    "buy.low_stock"), NO_SPACE("sell.no_space");

    private static YamlConfiguration config;

    private String message;

    private Message(String key)
    {
	this.message = Messaging.parseColor(getConfig().getString(key, "")); // We cannot use config directly in the constructor because the compiler thinks it's not initialized yet (it get's initialized in getConfig()).
    }

    /**
     * We cannot use config directly in the constructor because the compiler thinks it's not initialized yet. Initializes config.
     * 
     * @return config
     * 
     * @author Klezst
     */
    private static YamlConfiguration getConfig()
    {
	if (config != null) // We already initialized.
	{
	    return config;
	}
	
	config = YamlConfiguration.loadConfiguration(new File(
		"plugins/DynamicMarket/messages.yml"));

	// Load custom chat colors.
	ConfigurationSection section = config.getConfigurationSection("custom_colors");
	if (section == null) // There are no custom colors.
	{
	    return config;
	}
	
	for (Map.Entry<String, Object> entry : section.getValues(false).entrySet())
	{
	    Messaging.addColor(entry.getKey(), (String)entry.getValue());
	}
	
	return config;
    }
    
    /**
     * Returns message.
     * 
     * @return message.
     * 
     * @author Klezst
     */
    public String getMessage() {
	return this.message;
    }

    /**
     * Sends the Message to sender.
     * 
     * @param sender
     *            Who to send the Message to.
     * 
     * @throws NullPointerException
     *             If sender is null.
     * 
     * @author Klezst
     */
    public void send(CommandSender sender) throws NullPointerException {
	send(sender, new HashMap<String, String>());
    }

    /**
     * Sends the Message to sender in the context provided.
     * 
     * @param sender
     *            Who to send the Message to.
     * @param context
     *            A Map of keywords to replace with values.
     * 
     * @throws NullPointerException
     *             If sender is null or context is null.
     * 
     * @author Klezst
     */
    public void send(CommandSender sender, Map<String, String> context)
	    throws NullPointerException {
	String msg = Messaging.replace(this.message, context, "$", "$");

	for (String line : msg.split("\n")) {
	    sender.sendMessage(line);
	}
    }
}
