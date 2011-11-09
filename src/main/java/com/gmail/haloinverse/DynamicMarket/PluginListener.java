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

package com.gmail.haloinverse.DynamicMarket;

import java.util.logging.Level;

import com.nijikokun.register.payment.Methods;

import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.ServerListener;

public class PluginListener extends ServerListener
{
	
	private DynamicMarket plugin;
	
    public PluginListener(DynamicMarket plugin)
    {
    	this.plugin = plugin;
    }
    
    @Override
    public void onPluginDisable(PluginDisableEvent event)
    {
        if (DynamicMarket.isEconLoaded())
        {
        	// Disable, if Register disables (dependency).
            if (event.getPlugin().getDescription().getName().equals("Register"))
            {
            	plugin.log(Level.SEVERE, "Register disabled!");
            	plugin.getServer().getPluginManager().disablePlugin(plugin);
            }
            
            // Wait, if the economy plugin is disabled.
            else if (!Methods.hasMethod())
            {
            	DynamicMarket.setEconLoaded(false);
                plugin.log(Level.WARNING, "Un-hooked from Register.");
            }
        }
    }
    
    @Override
    public void onPluginEnable(PluginEnableEvent event)
    {
    	// Begin, if economy plugin is loaded
        if (!DynamicMarket.isEconLoaded() && Methods.hasMethod())
        {
        	DynamicMarket.setEconLoaded(true);
            plugin.log(Level.INFO, "Hooked into Register.");
        }
    }
}