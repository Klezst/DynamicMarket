package com.gmail.haloinverse.DynamicMarket.util;

public class Util
{
    /**
     * Returns true, iff text equalsIgnoreCase any member of against.
     * 
     * @param text, The string to check.
     * @param against, The strings to compare to.
     * @return True, iff text equalsIgnoreCase any member of against.
     * @author Nijikokun
     * @author Klezst
     */
    public static boolean isAny(String text, String... against)
    {
        for (String thisAgainst : against)
        {
            if (text.equalsIgnoreCase(thisAgainst))
            {
                return true;
            }
        }
        return false;
    }
}