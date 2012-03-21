package dynamicmarket.configuration;

import java.io.File;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import bukkitutil.util.Logging;
import bukkitutil.util.Messaging;

/**
 * Handles logging.
 * NOTE: You should always initialize this class by using validate(). If validate() does not return null, do not use this class!
 * 
 * @author Klezst
 */
public enum Log {
    // Alphanumeric order.
    EXPORT("export"),
    IMPORT("import");
    
    private static final String FILEPATH = "plugins/DynamicMarket/logs.yml";
    private static final String LEVEL_KEY = ".level";
    private static final String MESSAGE_KEY = ".message";
    private static final String PREFIX = "[DynamicMarket] ";
    
    private static YamlConfiguration config = null;

    private String key = null;
    private Level level = null;
    private String message = null;

    private Log(String key)
    {
	YamlConfiguration file = getConfig(); // We cannot use config directly in the constructor because the compiler thinks it's not initialized yet (it gets initialized in getConfig()).
	this.key = key;
	this.message = file.getString(this.getMessageKey(), ""); // TODO: Strip color.
	
	// Parse level.
	try {
	    this.level = Level.parse(file.getString(this.getLevelKey(), ""));
	} catch (IllegalArgumentException e) {
	    this.level = null;
	}
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
	
	config = YamlConfiguration.loadConfiguration(new File(FILEPATH));

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
     * Validates config.
     * 
     * @return If config is valid, null; otherwise, a String that represents the errors.
     * 
     * @author Klezst
     */
    public static String validate() {
	String errors = "";
	
	// Validate.
	for (Log log : Log.values()) {
	    if (log.getLevel() == null || log.getLevel() == Level.ALL || log.getLevel() == Level.OFF) {
		errors += "\t\t" + log.getLevelKey() + "\n";
	    }
	    if (log.getMessage().isEmpty()) {
		errors += "\t\t" + log.getMessageKey() + "\n";
	    }
	}
	
	if (!errors.isEmpty()) {
	    errors = "Invalid " + FILEPATH + ":\n\tInvalid keys:\n" + errors;
	    errors.substring(0, errors.length() - 1); // Remove trailing newline.
	    return errors;
	}
	return null;
    }

    /**
     * Returns key.
     * 
     * @return key.
     * 
     * @author Klezst
     */
    public String getKey() {
	return this.key;
    }
    
    /**
     * Returns level.
     * 
     * @return level.
     * 
     * @author Klezst
     */
    public Level getLevel() {
	return this.level;
    }
    
    /**
     * Returns key += LEVEL_KEY.
     * 
     * @return key += LEVEL_KEY.
     * 
     * @author Klezst
     */
    public String getLevelKey() {
	return this.getKey() + Log.LEVEL_KEY;
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
     * Returns key += MESSAGE_KEY.
     * 
     * @return key += MESSAGE_KEY.
     * 
     * @author Klezst
     */
    public String getMessageKey() {
	return this.getKey() + Log.MESSAGE_KEY;
    }
    
    /**
     * Logs messages. Each line will have PREFIX added before it.
     * 
     * @throws NullPointerException
     * 		   If level or message is null.
     * 
     * @author Klezst
     */
    public void log() {
	Logging.prefixLog(this.level, PREFIX, this.message);
    }
    
    /**
     * Logs messages. Each line will have PREFIX added before it.
     * 
     * @throws NullPointerException
     * 		   If context, level, or message is null.
     * 
     * @author Klezst
     */
    public void log(Map<String, String> context) throws NullPointerException {
	String msg = Messaging.replace(this.message, context, "$", "$"); // TODO: Migrate "$" and "$" to constants.
	Logging.prefixLog(this.level, PREFIX, msg);
    }
}
