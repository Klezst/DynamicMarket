package com.gmail.klezst;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.configuration.file.FileConfiguration;

public class Settings
{
	public static enum Type
	{
		// Do not use primitive data types
    	ACCOUNT_NAME(String.class, "default-shop-account.name"),
    	ACCOUNT_FREE(Boolean.class, "default-shop-account.is-free"),
    	BRACKET_COLOR(String.class, "text-color.bracket"),
    	COMMAND_COLOR(String.class, "text-color.command"),
    	ERROR_COLOR(String.class, "text-color.error"),
    	NORMAL_COLOR(String.class, "text-color.normal"),
    	PARAM_COLOR(String.class, "text-color.param"),
    	DATABASE_TYPE(String.class, "database-type"),
    	ITEMS_DB_PATH(String.class, "items-db-path"),
    	MYSQL_URL(String.class, "mysql.url"),
    	MYSQL_USER(String.class, "mysql.user"),
    	MYSQL_PASS(String.class, "mysql.pass"),
    	MYSQL_ENGINE(String.class, "mysql.engine"),
    	IMPORT_EXPORT_FILE(String.class, "import-export.file"),
    	IMPORT_EXPORT_PATH(String.class, "import-export.path"),
    	TRANSACTION_LOG_FILE(String.class, "transaction-log.file"),
    	TRANSACTION_LOG_AUTOFLUSH(Boolean.class, "transaction-log.autoflush"),
    	SHOP_TAG(String.class, "shop-tag"),
    	ITEMS_MAX_PER_PURCHASE(Integer.class, "items-max-per.purchase"),
    	ITEMS_MAX_PER_SALE(Integer.class, "items-max-per.sale"),
    	DEBUG(Boolean.class, "debug");
    	
    	private static final Map<String,Type> types = new HashMap<String,Type>();

    	static
    	{
    		for(Type s : EnumSet.allOf(Type.class))
    		{
    			types.put(s.getKey(), s);
    		}
    	}
    	
    	private String key;
    	private Class<? extends Object> dataType;
    	
    	private Type(Class<? extends Object> dataType, String key)
    	{
    		this.dataType = dataType;
    		this.key = key;
    	}
    	
    	public Class<? extends Object> getDataType()
    	{
    		return dataType;
    	}
    	
    	public String getKey()
    	{
    		return key;
    	}
    	
    	public static Set<String> getKeys()
    	{
    		return types.keySet();
    	}
    	
    	public static Type getType(String key)
    	{
    		return types.get(key);
    	}
	}
	
	private Map<Type, Object> settings = new HashMap<Type, Object>();
	
	public Settings(FileConfiguration settings)
	{
		Set<String> keys = Type.getKeys();
		Set<String> invalid = new HashSet<String>(keys);
		
		// Validate
		for (String key : keys)
		{
			Object value = settings.get(key);
			Type type = Type.getType(key);
			if (value != null && value.getClass().equals(type.getDataType()))
			{
				this.settings.put(type, value);
				invalid.remove(key);
			}
		}
		
		if (invalid.size() > 0)
		{
			Logger log = Logger.getLogger("Minecraft");
			log.log(Level.SEVERE, "[DynamicMarket] Disabling, invalid config.yml; errors on:");
			for (String key : invalid)
			{
				log.log(Level.SEVERE, "[DynamicMarket]\t" + key);
			}
			
			throw new IllegalArgumentException();
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getSetting(Type type, Class<T> dataType)
	{
		Object value = settings.get(type);
		if (value.getClass().equals(dataType))
		{
			return (T)value;
		}
		return null;
	}
}
