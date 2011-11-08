package com.gmail.haloinverse.DynamicMarket;

import java.util.logging.Level;

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
	
	private DynamicMarket plugin;
	
    public iPluginListener(DynamicMarket plugin)
    {
    	this.plugin = plugin;
    }
    
    @Override
    public void onPluginDisable(PluginDisableEvent event)
    {
        if (DynamicMarket.isEconLoaded())
        {
            if (event.getPlugin().getDescription().getName().equals("Register"))
            {
            	plugin.log(Level.SEVERE, "Register disabled!");
            	plugin.getServer().getPluginManager().disablePlugin(plugin);
            }
            else if (!Methods.hasMethod())
            {
            	DynamicMarket.setEconLoaded(false);
                plugin.log(Level.WARNING, "Un-hooked from Register.");
            }
        }
        
        if (event.getPlugin().getDescription().getName().equals("WorldEdit"))
        {
        	plugin.log(Level.SEVERE, "WorldEdit disabled!");
        	plugin.getServer().getPluginManager().disablePlugin(plugin);
        }
    }
    
    @Override
    public void onPluginEnable(PluginEnableEvent event)
    {   
        if (!DynamicMarket.isEconLoaded() && Methods.hasMethod())
        {
        	DynamicMarket.setEconLoaded(true);
            plugin.log(Level.INFO, "Hooked into Register.");
        }
    }
}