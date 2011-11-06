package com.gmail.klezst.util.settings;

public interface Validatable
{
	/**
	 * Expected to return a settings key.
	 * @return Key of a setting.
	 */
	public String getKey();
	
	/**
	 * Expected to return the required class of a settings value.
	 * @return Required class of a settings value, should not be a primitive data type.
	 */
	public Class<?> getType();
}