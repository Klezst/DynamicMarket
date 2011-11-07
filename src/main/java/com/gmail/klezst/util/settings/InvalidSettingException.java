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

import java.util.logging.Level;
import java.util.logging.Logger;

public class InvalidSettingException extends RuntimeException
{
	private static final long serialVersionUID = 4084554841030860252L;
	
	private String exception;
	private String key;
	
	public InvalidSettingException(String exception, String key)
	{
		super("Invalid config.yml @ " + key);
		this.exception = exception;
		this.key = key;
	}
	
	public String getException()
	{
		return exception;
	}
	
	public String getKey()
	{
		return key;
	}
	
	/**
	 * Prints the exception to log. NOTE: The output is indented.
	 * @param log, The Logger to print to.
	 * @param prefix, A String that precedes each line printed.
	 */
	public void printException(Logger log, String prefix)
	{
		log.log(Level.SEVERE, prefix + "\t" + exception + ":");
		log.log(Level.SEVERE, prefix + "\t\t" + key + ".");
	}
}