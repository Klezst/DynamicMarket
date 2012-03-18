package com.gmail.klezst;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

public class Permission {
    public static net.milkbowl.vault.permission.Permission permission = null;
    static {
	try {
	    permission = Bukkit
		    .getServer()
		    .getServicesManager()
		    .getRegistration(
			    net.milkbowl.vault.permission.Permission.class)
		    .getProvider();
	} catch (NullPointerException e) {
	    // We must catch this exception, because Java doesn't throw the NullPointerException to the method caller.
	    // Instead a NullPointerException will be thrown by the method they called.
	}

    }
    
    /**
     * Returns true, if sender has the permission node.
     * NOTE: There is a difference between has(CommandSender, String) and hasPermission(CommandSender, String).
     * 
     * @param sender
     *            Who to check for permission.
     * @param node
     *            The permission needed.
     * 
     * @return True, if sender has the permission node.
     * 
     * @throws NullPointerException
     *             If no permission is loaded.
     * 
     * @author Klezst
     */
    public static boolean hasPermission(CommandSender sender, String node)
	    throws NullPointerException {
	return permission.has(sender, node);
    }

    /**
     * Returns true, if Vault has loaded a permissions plugin.
     * 
     * @return true, if Vault has loaded a permissions plugin.
     * 
     * @author Klezst
     */
    public static boolean isEnabled() {
	if (permission == null) {
	    return false;
	}
	return permission.isEnabled();
    }
}
