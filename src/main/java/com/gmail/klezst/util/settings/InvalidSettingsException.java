package com.gmail.klezst.util.settings;

import java.util.List;
import java.util.logging.Logger;

public class InvalidSettingsException extends RuntimeException
{
	private static final long serialVersionUID = -4808725946544992759L;
	
	private List<InvalidSettingException> exceptions;
	
	public InvalidSettingsException(List<InvalidSettingException> errors)
	{
		super("Invalid config.yml");
		this.exceptions = errors;
	}
	
	public List<InvalidSettingException> getExceptions()
	{
		return exceptions;
	}
	
	/**
	 * Prints the exception to log. NOTE: The output is indented.
	 * @param log, The Logger to print to.
	 * @param prefix, A String that precedes each line printed.
	 */
	public void printExceptions(Logger log, String prefix)
	{
		for (InvalidSettingException exception : exceptions)
		{
			exception.printException(log, prefix);
		}
	}
}
