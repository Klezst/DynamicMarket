/*
	DynamicMarket
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

package dynamicmarket.configuration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.gmail.klezst.bukkit.bukkitutil.configuration.Validatable;
import com.gmail.klezst.bukkit.bukkitutil.util.Messaging;

/**
 * Handles logging.
 * 
 * @author Klezst
 */
public enum Log implements Validatable<String> {
    // Alphanumeric order.
    CONFIG_INVALID_MESSAGES("config.invalid.messages"),
    CONFIG_INVALID_SETTINGS("config.invalid.settings"),
    EXPORT("export"),
    IMPORT("import"),
    IMPORT_FAILURE_INITIAL("import.failure.initial"),
    RELOAD("reload");
    
    public static final String FILEPATH = "plugins/DynamicMarket/logs.yml";
    
    private static final Logger LOGGER = Bukkit.getServer().getPluginManager().getPlugin("DynamicMarket").getLogger();
    private static final String LEVEL_KEY = ".level";
    private static final String MESSAGE_KEY = ".message";

    private String key = null;
    private Level level = null;
    private String message = null;

    private Log(String key)
    {
	this.key = key;
    }

    /**
     * Returns config.
     * 
     * @return config.
     * 
     * @author Klezst
     */
    public static FileConfiguration getConfig() {
	return YamlConfiguration.loadConfiguration(new File(FILEPATH));
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
    
    @Override
    public Map<String, Class<?>> getTypes() {
	Map<String, Class<?>> keys = new HashMap<String, Class<?>>();
	keys.put(this.getMessageKey(), String.class);
	keys.put(this.getLevelKey(), String.class);
	return keys;
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
	LOGGER.log(this.level, this.message);
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
	LOGGER.log(this.level, msg);
    }
    
    @Override
    public String set(String key, String value) throws IllegalArgumentException {
	if (key.equals(this.getLevelKey())) {
	    // Parse level.
	    this.level = Level.parse(value);
	    if (this.level == Level.ALL || this.level == Level.OFF) {
		return "May not be \"ALL\" or \"OFF\"";
	    }
	} else {
	    message = value; // TODO: Strip color.
	}
	return null;
    }
}
