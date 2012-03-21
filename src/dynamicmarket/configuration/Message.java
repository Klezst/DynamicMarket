package dynamicmarket.configuration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import bukkitutil.configuration.InvalidSettingException;
import bukkitutil.configuration.InvalidSettingsException;
import bukkitutil.configuration.Validatable;
import bukkitutil.util.Messaging;

/**
 * Handles messaging.
 * 
 * @author Klezst
 */
public enum Message implements Validatable {
    // Alphanumeric order.
    ADD_SUCCESS("add.success"),
    BUY_TOO_MUCH("buy.too_much"),
    BUY_LOW_STOCK("buy.low_stock"),
    EXPORT_FAILURE("export.failure"),
    EXPORT_SUCCESS("export.success"),
    HELP_ADD("help.add"),
    HELP_BUY("help.buy"),
    HELP_DEFAULT("help.default"),
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
    HELP_TAG_DEFAULT("help.tag.default"),
    HELP_TAG_INVALID("help.tag.invalid"),
    HELP_TAG_MAX_PRICE("help.tag.maxstock"),
    HELP_TAG_MAX_STOCK("help.tag.maxstock"),
    HELP_TAG_MIN_PRICE("help.tag.minprice"),
    HELP_TAG_MIN_STOCK("help.tag.minstock"),
    HELP_TAG_SALESTAX("help.tag.salestax"),
    HELP_TAG_SELLABLE("help.tag.buyable"),
    HELP_TAG_STOCK("help.tag.stock"),
    HELP_TAG_VOLATILITY("help.tag.volatility"),
    HELP_UPDATE("help.update"),
    IMPORT_FAILURE("import.failure"),
    IMPORT_SUCCESS("import.success"),
    LIST_NO_RESULTS("list.no_results"),
    LIST_PAGE_NON_NUMERIC("list.page.non_numeric"),
    LIST_PAGE_NEGATIVE("list.page.negative"),
    LIST_PAGE_TOO_HIGH("list.page.too_high"),
    RELOAD("reload"),
    REMOVE_SUCCESS("remove.success"),
    SELL_NO_SPACE("sell.no_space"),
    TRANSACTION_AMOUNT_NON_NUMERIC("transaction.amount.non_numeric"),
    UPDATE_SUCCESS("update.success"),
    UPDATE_FLAG_NON_NUMERIC("update.flag.non_numeric");
    
    private static final String FILEPATH = "plugins/DynamicMarket/messages.yml";
    
    private static YamlConfiguration config = null;

    private String key;
    private String message;

    private Message(String key)
    {
	this.key = key;
	this.message = Messaging.parseColor(getConfig().getString(key, "")); // We cannot use config directly in the constructor because the compiler thinks it's not initialized yet (it gets initialized in getConfig()).
    }

    /**
     * We cannot use config directly in the constructor because the compiler thinks it's not initialized yet. Initializes config.
     * 
     * @return config.
     * 
     * @author Klezst
     */
    public static YamlConfiguration getConfig()
    {
	if (config != null) // We already initialized.
	{
	    return config;
	}
	
	config = YamlConfiguration.loadConfiguration(new File(
		FILEPATH));

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
    
    // Validation functions.
    @Override
    public String getKey() {
	return key;
    }

    @Override
    public Class<?> getType() {
	return String.class;
    }

    @Override
    public Object validate(Object value) throws InvalidSettingException,
	    InvalidSettingsException {
	return value;
    }
}
