package com.gmail.klezst.util;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

/**
 * Controls communication with permissions plugins.
 * 
 * @author Klezst
 */
public class Permission {
    public static net.milkbowl.vault.permission.Permission permission = Bukkit
	    .getServer().getServicesManager()
	    .getRegistration(net.milkbowl.vault.permission.Permission.class)
	    .getProvider(); // Will not throw NullPointerException, since Vault is a dependency.

    /**
     * Returns true, iff sender has the permission node.
     * 
     * @param sender
     *            Who to check for permission.
     * @param node
     *            The permission needed.
     * @return, true, iff sender has the permission node.
     * 
     * @author Klezst
     */
    public static boolean hasPermission(CommandSender sender, String node) {
	return permission.has(sender, node);
    }
}
