/*
	BukkitUtil
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

package bukkitutil;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.material.MaterialData;

import dynamicmarket.DynamicMarketException;

/**
 * Provides miscellaneous convenience functions.
 * 
 * @author Klezst
 */
public class Util {
    /**
     * Returns the MaterialData corresponding to arg.
     * 
     * @param arg
     *            , The alias of the MaterialData or <type>[:<data>].
     * @return The MaterialData corresponding to arg.
     * @throws DynamicMarketException
     *             , If arg is not a valid MaterialData.
     * @author Klezst
     */
    // TODO: Add support for custom aliases via a utility plugin?
    public static MaterialData getMaterialData(String arg)
	    throws DynamicMarketException {
	try {
	    String[] id = arg.split(":");
	    byte data = 0;
	    Material material = null;

	    try {
		int type = Integer.parseInt(id[0]); // May throw IndexOutOfBoundsException
		material = Material.getMaterial(type);
	    } catch (NumberFormatException e)

	    {
		material = Material.matchMaterial(id[0]);
	    }

	    if (id.length == 2) {
		int temp = Integer.parseInt(id[1]); // May throw NumberFormatException.
		data = (byte) temp; // May throw ClassCastException.
	    }
	    return material.getNewData(data); // May throw NullPointerException.
	} catch (Exception e) {
	    throw new DynamicMarketException(arg + " is not a valid item!");
	}

    }

    /**
     * Returns true, iff text equalsIgnoreCase any member of against.
     * 
     * @param text
     *            , The string to check.
     * @param against
     *            , The strings to compare to.
     * @return True, iff text equalsIgnoreCase any member of against.
     * @author Nijikokun
     * @author Klezst
     */
    public static boolean isAny(String text, String... against) {
	for (String thisAgainst : against) {
	    if (text.equalsIgnoreCase(thisAgainst)) {
		return true;
	    }
	}
	return false;
    }

    public static double round(double num, int precision) {
	double modifier = Math.pow(10, precision);

	double result = Math.round(num * modifier);
	return result / modifier;
    }

    public static double clamp(double min, double arg, double max) {
	return Math.min(max, Math.max(min, arg));
    }

    public static Map<String, String> getProperties(String... args)
	    throws DynamicMarketException {
	Map<String, String> properties = new HashMap<String, String>();
	for (String arg : args) {
	    String[] entry = arg.split(":");
	    if (entry.length != 2) {
		throw new DynamicMarketException(arg
			+ " isn't a valid property!");
	    }
	    properties.put(entry[0].toLowerCase(), entry[1]);
	}
	return properties;
    }
}