/*
	DynamicMarket
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

package com.gmail.haloinverse.DynamicMarket.util;

import com.nijikokun.register.payment.Methods;

public class Economy
{
	/**
	 * Changes an economy account's balance.
	 * @param amount, Amount to change the balance (can be negative).
	 * @param name, Name of the account.
	 * @throws NullPointerException, iff the economy isn't loaded yet.
	 * @author Klezst
	 */
	public static void deltaBalance(double amount, String name) throws NullPointerException
	{
        Methods.getMethod().getAccount(name).add(amount);
	}
	
	/**
	 * Returns amount in display format.
	 * @param amount, Amount to be formatted.
	 * @return amount in display format.
	 * @throws NullPointerException, iff the economy isn't loaded yet.
	 * @author Klezst
	 */
    public static String format(double amount) throws NullPointerException
    {
    	return Methods.getMethod().format(amount);
    }
	
	/**
	 * Returns an economy account's balance.
	 * @param name, Name of the account.
	 * @return Balance of the account called name.
	 * @throws NullPointerException, iff the economy isn't loaded yet.
	 * @author Klezst
	 */
    public static int getBalance(String name) throws NullPointerException
    {
        return (int)Methods.getMethod().getAccount(name).balance();
    }
    
	/**
	 * Returns an economy account's balance in display format.
	 * @param name, Name of the account.
	 * @return Balance of the account called name in display format.
	 * @throws NullPointerException, iff the economy isn't loaded yet.
	 * @author Klezst
	 */
    public static String getFormattedBalance(String name) throws NullPointerException
    {
    	return Methods.getMethod().format(getBalance(name));
    }
    
    /**
     * Returns whether or not the economy is loaded yet.
     * 
     * @return True, iff the economy is loaded.
     * @author Klezst
     */
    public static boolean isLoaded()
    {
    	return Methods.hasMethod();
    }
}