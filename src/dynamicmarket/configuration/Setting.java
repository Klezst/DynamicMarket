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

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import bukkitutil.configuration.Validatable;


public enum Setting implements Validatable<Object> {
    // Do not use primitive data types or null.
    VERSION("version", Double.class), // Double.class is the class of the type of data you expect to get from the config.yml.
    DRIVER("database.driver", String.class),
    URL("database.url", String.class),
    USERNAME("database.username", String.class),
    PASSWORD("database.password", String.class),
    ISOLATION("database.isolation", String.class),
    LOGGING("database.logging", Boolean.class),
    IMPORT_EXPORT_PATH("import_export_path", String.class),
    TRANSACTION_LOGGING("transaction_logging", Boolean.class);
    
    public static final String FILEPATH = "plugins/DynamicMarket/settings.yml";
    
    private String key = null;
    private Class<?> type = null;
    private Object value = null;

    private Setting(String key, Class<?> type) {
	this.key = key;
	this.type = type;
    }
    
    public static FileConfiguration getConfig() {
	return YamlConfiguration.loadConfiguration(new File(FILEPATH));
    }

    public String getKey() {
	return this.key;
    }
    
    @Override
    public Map<String, Class<?>> getTypes() {
	Map<String, Class<?>> keys = new HashMap<String, Class<?>>();
	keys.put(this.key, this.type);
	return keys;
    }

    /**
     * Returns a setting's value.
     * 
     * @param type
     * 		The Class of the setting
     * 
     * @return The value of the setting.
     * 
     * @throws NullPointerException
     * 		If you did not properly validate this.
     * @throws IllegalArgumentException
     * 		If the setting is not of Class type.
     * 
     * @author Klezst
     */
    @SuppressWarnings("unchecked")
    public <T> T getValue(Class<T> type) throws NullPointerException, IllegalArgumentException {
	if (value.getClass().equals(type)) {
	    return (T)value;
	}
	throw new IllegalArgumentException("Programmer error:\n\tThe setting " + this.name()
		+ " is not a " + type.getSimpleName() + ".");
    }
    
    @Override
    public String set(String key, Object value) {
	this.value = value;
	return null;
    }
}
