package com.gmail.klezst.util;

import java.util.ArrayList;
import java.util.List;

//This is a template intended for use with any program.
public enum Message {
    TEMP("RAWR");

    private List<String> lines;

    private Message(String... messages) {
	this.lines = new ArrayList<String>();

	for (String message : messages) {
	    for (String line : message.split("\n")) {
		this.lines.add(line);
	    }
	}
    }
}
