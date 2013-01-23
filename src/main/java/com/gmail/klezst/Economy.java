package com.gmail.klezst;

import org.bukkit.Bukkit;

public class Economy {
    public static net.milkbowl.vault.economy.Economy economy = null;
    static {
	try {
	    economy = Bukkit.getServer().getServicesManager()
		    .getRegistration(net.milkbowl.vault.economy.Economy.class)
		    .getProvider();
	} catch (NullPointerException e) {
	    // Nothing needs to be done, since whatever method was called will throw a NullPointerException.
	}
    }

    /**
     * Changes an economy account's balance.
     * 
     * @param amount
     *            Amount to change the balance (can be negative).
     * @param name
     *            Name of the account.
     * 
     * @throws NullPointerException
     *             If the economy isn't loaded yet.
     * 
     * @author Klezst
     */
    public static void deltaBalance(double amount, String name)
	    throws NullPointerException {
        
        if (amount > 0) {
            economy.depositPlayer(name, amount);
        } else {
            economy.withdrawPlayer(name, -amount);
        }
    }

    /**
     * Returns amount in display format.
     * 
     * @param amount
     *            Amount to be formatted.
     * 
     * @return amount in display format.
     * 
     * @throws NullPointerException
     *             If the economy isn't loaded yet.
     * 
     * @author Klezst
     */
    public static String format(double amount) throws NullPointerException {
	return economy.format(amount);
    }

    /**
     * Returns an economy account's balance.
     * 
     * @param name
     *            Name of the account.
     * 
     * @return Balance of the account called name.
     * 
     * @throws NullPointerException
     *             If the economy isn't loaded yet.
     * 
     * @author Klezst
     */
    public static int getBalance(String name) throws NullPointerException {
	return (int) economy.getBalance(name);
    }

    /**
     * Returns an economy account's balance in display format.
     * 
     * @param name
     *            Name of the account.
     * 
     * @return Balance of the account called name in display format.
     * 
     * @throws NullPointerException
     *             If the economy isn't loaded yet.
     * 
     * @author Klezst
     */
    public static String getFormattedBalance(String name)
	    throws NullPointerException {
	return economy.format(getBalance(name));
    }

    /**
     * Returns whether or not the economy is loaded yet.
     * 
     * @return True, If the economy is loaded.
     * 
     * @author Klezst
     */
    public static boolean isEnabled() {
	if (economy == null) {
	    return false;
	}
	return economy.isEnabled();
    }
    
    /**
     * Returns the singular version of the currency name.
     * 
     * @return the singular version of the currency name.
     * 
     * @throws NullPointerException
     * 		   If the economy is not loaded yet.
     * 
     * @author Klezst
     */
    public static String getCurrencyNameSingular() {
	return economy.currencyNameSingular();
    }
    
    /**
     * Returns the singular version of the currency name.
     * 
     * @return the singular version of the currency name.
     * 
     * @throws NullPointerException
     * 		   If the economy is not loaded yet.
     * 
     * @author Klezst
     */
    public static String getCurrencyNamePlural() {
	return economy.currencyNamePlural();
    }
}
