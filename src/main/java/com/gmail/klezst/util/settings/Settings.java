/*
	SettingsValidation
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

package com.gmail.klezst.util.settings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.file.FileConfiguration;

public class Settings
{	
	private Map<Validatable, Object> settings = new HashMap<Validatable, Object>();
	
	/**
	 * Validates and stores configuration settings, invalid settings will not be stored.
	 * @param enums, the list of settings to validate and store.
	 * @param config, the FileConfiguration that contains the settings.
	 * @throws InvalidSettingsException, Thrown, iff any settings fail validation.
	 */
	public Settings(FileConfiguration config, Validatable[] enums)
	{
		ArrayList<InvalidSettingException> exceptions = new ArrayList<InvalidSettingException>();
		
		for (Validatable setting : enums)
		{
			String key = setting.getKey();
			Object value = config.get(key);
			
			// Validate
			if (value == null)
			{
				exceptions.add(new InvalidSettingException("Must specify a value", key));
			}
			else
			{
				Class<?> type = setting.getType();
				if (value.getClass().equals(type))
				{
					this.settings.put(setting, value);
				}
				else
				{
					exceptions.add(new InvalidSettingException("Must be a " + type.getSimpleName(), key));
				}
			}
		}
		
		// Print invalid keys
		if (exceptions.size() > 0)
		{
			throw new InvalidSettingsException(exceptions);
		}
	}
	
	/**
	 * Returns a value corresponding to setting.
	 * @param setting, The setting to get the value of.
	 * @param type, The class of the value to be returned.
	 * @return The value corresponding to setting, iff the setting exists and is of class type; otherwise null.
	 */
	@SuppressWarnings("unchecked")
	public <T> T getSetting(Validatable setting, Class<T> type)
	{
		Object value = settings.get(setting);
		if (value != null && value.getClass().equals(type))
		{
			return (T)value;
		}
		return null;
	}
}