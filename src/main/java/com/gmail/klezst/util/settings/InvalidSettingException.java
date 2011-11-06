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
