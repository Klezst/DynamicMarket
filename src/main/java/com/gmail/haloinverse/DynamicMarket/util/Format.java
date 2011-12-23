package com.gmail.haloinverse.DynamicMarket.util;

public class Format {
    @SuppressWarnings("boxing")
    public static Boolean parseBoolean(String arg) {
	boolean result = false;
	if (Util.isAny(arg, "yes", "y", "true", "t", "positive", "+",
		"affirmative", "indubitably", "YaY")) {
	    result = true;
	}
	return result;
    }

    @SuppressWarnings("boxing")
    public static Double parseDouble(String arg) throws NumberFormatException {
	Double result;
	if (Util.isAny(arg, "inf", "+inf")) {
	    result = Double.MAX_VALUE;
	} else if (arg.equalsIgnoreCase("-inf")) {
	    result = Double.MIN_VALUE;
	} else {
	    // throws NumberFormatException, if arg isn't a valid Double.
	    result = Double.parseDouble(arg);
	}
	return result;
    }

    @SuppressWarnings("boxing")
    public static Integer parseInteger(String arg) {
	Integer result;
	if (Util.isAny(arg, "inf", "+inf")) {
	    result = Integer.MAX_VALUE;
	} else if (arg.equalsIgnoreCase("-inf")) {
	    result = Integer.MIN_VALUE;
	} else {
	    // throws NumberFormatException, if arg isn't a valid Integer.
	    result = Integer.parseInt(arg);
	}
	return result;
    }

    public static String parseString(boolean arg) {
	String result = "'False'";
	if (arg) {
	    result = "'True'";
	}
	return result;
    }

    public static String parseString(double arg) {
	String result;
	if (arg == Double.MAX_VALUE) {
	    result = "'+INF'";
	} else if (arg == Double.MIN_VALUE) {
	    result = "'-INF'";
	} else {
	    result = "" + arg;
	}
	return result;
    }

    public static String parseString(int arg) {
	String result;
	if (arg == Integer.MAX_VALUE) {
	    result = "'+INF'";
	} else if (arg == Integer.MIN_VALUE) {
	    result = "'-INF'";
	} else {
	    result = "" + arg;
	}
	return result;
    }
}
