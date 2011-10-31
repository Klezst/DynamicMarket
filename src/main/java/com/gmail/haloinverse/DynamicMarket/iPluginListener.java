package com.gmail.haloinverse.DynamicMarket;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.nijikokun.register.payment.Methods;

import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.ServerListener;

/**
 * iPluginListener Allows us to hook into permissions even if it is loaded later on.
 * 
 * Checks for Plugins on the event that they are enabled, checks the name given with the usual name of the plugin to verify the existence. If the name matches we pass the plugin along to iConomy to utilize in various ways.
 * 
 * @author Nijikokun
 */
public class iPluginListener extends ServerListener {
	
	private DynamicMarket dynamicMarket;
	
    public iPluginListener(DynamicMarket dynamicMarket)
    {
    	this.dynamicMarket = dynamicMarket;
    }
    
    @Override
    public void onPluginDisable(PluginDisableEvent event) {
        if (DynamicMarket.econLoaded) {
            if (event.getPlugin().getDescription().getName().equals("Register"))
            {
            	Logger.getLogger("Minecraft").log(Level.SEVERE, "[DynamicMarket] Register disabled! Disabling.");
            	dynamicMarket.getServer().getPluginManager().disablePlugin(dynamicMarket);
            }
            else if (!Methods.hasMethod())
            {
            	DynamicMarket.econLoaded = false;
                System.out.println("[DynamicMarket] un-hooked from Register.");
            }
        }
        
        if (event.getPlugin().getDescription().getName().equals("WorldEdit"))
        {
        	Logger.getLogger("Minecraft").log(Level.SEVERE, "[DynamicMarket] WorldEdit disabled! Disabling.");
        	dynamicMarket.getServer().getPluginManager().disablePlugin(dynamicMarket);
        }
    }
    
    @Override
    public void onPluginEnable(PluginEnableEvent event) {   
        if (!DynamicMarket.econLoaded && Methods.hasMethod()) {
        	DynamicMarket.econLoaded = true;
            System.out.println("[DynamicMarket] hooked into Register.");
        }
        
        /*
        if (event.getPlugin().getDescription().getName().equals("Permissions")) {
            if (DynamicMarket.Permissions == null) {
                Plugin Permissions = DynamicMarket.getTheServer().getPluginManager().getPlugin("Permissions");
                if (Permissions != null) {
                    DynamicMarket.setupPermissions();
                    System.out.println("[DynamicMarket] Successfully linked with Permissions.");
                }
            }
        }
        */
    }
}