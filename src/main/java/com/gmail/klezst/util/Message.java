package com.gmail.klezst.util;

import java.util.ArrayList;
import java.util.List;

public enum Message // This is a template intended for use with any program.
{
	TEMP("RAWR");
	
	
	private List<String> lines;
	
	private Message(String... messages)
	{
		lines = new ArrayList<String>();
		
		for (String message : messages)
		{
			for (String line : message.split("\n"))
			{
				lines.add(line);
			}
		}
	}
}
