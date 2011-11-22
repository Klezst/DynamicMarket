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

import java.util.ArrayList;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Shop
{	
	private boolean infiniteFunding;
	
	private double funds; // TODO: Make this persist
	
	private DynamicMarket plugin;
	
	private int maxPerTransaction;
	
	private String name;
	
	public Shop(boolean infiniteFunding, DynamicMarket plugin, int maxPerTransaction, String name)
	{
		this.infiniteFunding = infiniteFunding;
		this.funds = 0;
		this.plugin = plugin;
		this.maxPerTransaction = maxPerTransaction;
		this.name = name;
	}
	
    public void add(CommandSender sender, String itemString) // TODO: Instead of passing the sender, throw an IllegalArgumentException
    {
        MarketItem newItem = new MarketItem(itemString, plugin.getDatabaseMarket().getDefault(name), plugin.getDatabaseMarket(), name);
        
        if (!newItem.isValid())
        {
            sender.sendMessage(Messaging.parse("{ERR}Unrecognized item name or ID."));
            return;
        }
        
        if (plugin.getDatabaseMarket().hasRecord(newItem))
        {
            sender.sendMessage(Messaging.parse("{ERR}" + newItem.getName() + " is already in the market list."));
            sender.sendMessage(Messaging.parse("{ERR}Use {CMD}/shop update{ERR} instead."));
            return;
        }
        
        if ((newItem.count < 1) || (newItem.count > maxPerTransaction))
        {
            sender.sendMessage(Messaging.parse("{ERR}Invalid amount. (Range: 1.." + maxPerTransaction + ")"));
            return;
        }
        
        if (plugin.getDatabaseMarket().add(newItem))
        {
            sender.sendMessage(Messaging.parse("Item {PRM}" + newItem.getName() + "{} added:"));
            ArrayList<String> thisList = newItem.infoStringFull();
            for (String thisLine : thisList)
            {
                sender.sendMessage(Messaging.parse(thisLine));
            }
        }
        else
        {
            sender.sendMessage(Messaging.parse("{ERR}Item {PRM}" + newItem.getName() + "{ERR} could not be added."));
        }
    }
	
    public void buy(Player player, String itemString)
    {
        // TODO: Check for sufficient inventory space for received items.
        ItemClump requested = new ItemClump(itemString, plugin.getDatabaseMarket(), name, player);
        int transValue;
        int balance;
        
        try
        {
        	balance = Util.getBalance(player.getName());
        }
    	catch (NullPointerException e)
    	{
    		player.sendMessage(Messaging.parse("{ERR}Register hasn't detected an economy plugin!"));
    		return;
    	}
        
        if (!requested.isValid())
        {
            player.sendMessage(Messaging.parse("{ERR}Invalid item."));
            player.sendMessage(Messaging.parse("Use: {CMD}/shop buy {PRM}<item id or name>{BKT}({CMD}:{PRM}<bundles>{BKT})"));
            return;
        }
        
        MarketItem data = plugin.getDatabaseMarket().data(requested, name);
        
        if ((data == null) || !data.isValid())
        {
        	player.sendMessage(Messaging.parse("{ERR}Unrecognized item name, or not in shop."));
            return;
        }
        
        if (data.isDefault())
        {
        	player.sendMessage(Messaging.parse("{ERR}The default item template is not buyable."));
            return;
        }
        
        if (!data.canBuy)
        {
        	player.sendMessage(Messaging.parse("{ERR}" + data.getName() + " currently not purchaseable from shop."));
            return;
        }
        
        if (!data.getCanBuy(requested.count))
        {
        	player.sendMessage(Messaging.parse("{ERR}" + data.getName() + " understocked: only " + data.formatBundleCount(data.leftToBuy()) + " left."));
            return;
        }
        
        if ((requested.count < 1) || (requested.count * data.count > maxPerTransaction))
        {
        	player.sendMessage(Messaging.parse("{ERR}Amount over max items per purchase."));
            return;
        }
        
        transValue = data.getBuyPrice(requested.count);
        
        if (balance < transValue)
        {
        	player.sendMessage(Messaging.parse("{ERR}You do not have enough money to do this."));
        	player.sendMessage(Messaging.parse(data.infoStringBuy(requested.count)));
            return;
        }
        
        Util.deltaBalance(-transValue, player.getName());
        funds += transValue;
        
        player.getInventory().addItem(new ItemStack[] { new ItemStack(data.itemId, requested.count * data.count, (short) 0, (byte) requested.subType) });
        
        plugin.getDatabaseMarket().removeStock(requested, name);
        
        player.sendMessage(Messaging.parse("Purchased {BKT}[{PRM}" + data.formatBundleCount(requested.count) + "{BKT}]{PRM} " + data.getName() + "{} for {PRM}" + Util.format(transValue)));
        player.sendMessage(Messaging.parse("{}Balance: {PRM}" + Util.getFormattedBalance(player.getName())));
        
        if (plugin.getTransactionLogger().isOK)
        {
            plugin.getTransactionLogger().logTransaction(player.getName() + ", Buy, " + (-requested.count) + ", " + data.count + ", " + data.getName() + ", " + data.itemId + ", " + data.subType + ", " + transValue + ", " + name);
        }
    }
    
    public String getName()
    {
    	return name;
    }
    
    public void remove(CommandSender sender, String itemString) // TODO: Instead of passing the sender, throw an IllegalArgumentException
    {
        ItemClump removed = new ItemClump(itemString, plugin.getDatabaseMarket(), name);
        String removedItemName = null;
        
        if (removed.itemId == -1)
        {
            sender.sendMessage(Messaging.parse("{ERR}Unrecognized item name or ID."));
            return;
        }
        
        MarketItem itemToRemove = plugin.getDatabaseMarket().data(removed, name);
        
        if (itemToRemove == null)
        {
            sender.sendMessage(Messaging.parse("{ERR}Item {PRM}" + removed.getName(plugin.getDatabaseMarket(), name) + "{ERR} not found in market."));
            return;
        }
        
        removedItemName = itemToRemove.getName();
        if (removedItemName == null)
        {
            removedItemName = "<Unknown>";
        }
        
        if (plugin.getDatabaseMarket().remove(removed, name))
        {
            sender.sendMessage(Messaging.parse("Item " + removedItemName + " was removed."));
        }
        else
        {
            sender.sendMessage(Messaging.parse("Item " + removedItemName + " {ERR}could not be removed."));
        }
    }
    
    public void sell(Player player, String itemString)
    {
        ItemClump requested = new ItemClump(itemString, plugin.getDatabaseMarket(), name,  player);
        
        int transValue;
        
        if (!requested.isValid())
        {
        	player.sendMessage(Messaging.parse("{ERR}Invalid item."));
        	player.sendMessage(Messaging.parse("Use: {CMD}/shop sell {PRM}<item id or name>{BKT}({CMD}:{PRM}<bundles>{BKT})"));
            return;
        }
        
        MarketItem data = plugin.getDatabaseMarket().data(requested, name);
        
        if ((data == null) || !data.isValid())
        {
        	player.sendMessage(Messaging.parse("{ERR}Unrecognized item name, or not in shop."));
            return;
        }
        
        if (data.isDefault())
        {
            player.sendMessage(Messaging.parse("{ERR}The default template is not sellable."));
            return;
        }
        
        if (data.canSell == false)
        {
            player.sendMessage(Messaging.parse("{ERR}" + data.getName() + " currently not sellable to shop."));
            return;
        }
        
        if ((requested.count < 1) /* || (requested.count * data.count > plugin.max_per_sale) */)
        {
            player.sendMessage(Messaging.parse("{ERR}Amount over max items per sale."));
            return;
        }
        
        if (!data.getCanSell(requested.count))
        {
            player.sendMessage(Messaging.parse("{ERR}" + data.getName() + " overstocked: only " + data.formatBundleCount(data.leftToSell()) + " can be sold."));
            return;
        }
        
        if (!(Items.has(player, data, requested.count)))
        {
            player.sendMessage(Messaging.parse("{ERR}You do not have enough " + data.getName() + " to do this."));
            return;
        }
        
        transValue = data.getSellPrice(requested.count);
        
        if (!infiniteFunding && funds < transValue)
        {
            player.sendMessage(Messaging.parse("{ERR}The shop does not have enough money to pay for " + data.formatBundleCount(requested.count) + " " + data.getName() + "."));
            return;
        }
        
        plugin.removeItem(player, data, requested.count);
        
        try
        {
        	Util.deltaBalance(transValue, player.getName());
        }
    	catch (NullPointerException e)
    	{
    		player.sendMessage(Messaging.parse("{ERR}Register hasn't detected an economy plugin!"));
    		return;
    	}
        funds -= transValue;
        
        plugin.getDatabaseMarket().addStock(requested, name);
        player.sendMessage(Messaging.parse("Sold {BKT}[{PRM}" + data.formatBundleCount(requested.count) + "{BKT}]{PRM} " + data.getName() + "{} for {PRM}" + Util.format(transValue)));
        player.sendMessage(Messaging.parse("{}Balance: {PRM}" + Util.getFormattedBalance(player.getName())));
        
        if (plugin.getTransactionLogger().isOK)
        {
            plugin.getTransactionLogger().logTransaction(player.getName() + ", Sell, " + requested.count + ", " + data.count + ", " + data.getName() + ", " + data.itemId + ", " + data.subType + ", " + (-transValue) + ", " + name);
        }
    }
    
    public void update(CommandSender sender, String itemStringIn) // TODO: Instead of passing the sender, throw an IllegalArgumentException
    {
        // Make a copy of itemStringIn, in case modification is needed.
        String itemString = new String(itemStringIn);
        
        // Check if the item name is "all".
        String firstItem = itemString.split(" ", 2)[0];
        String thisName = firstItem.split(":", 2)[0];
        if (thisName.equalsIgnoreCase("all"))
        {
            // Update-all requested.
            // Check bundle size first.
            try
            {
                if (firstItem.contains(":"))
                {
                    if (Integer.valueOf(firstItem.split(":", 2)[1]) > maxPerTransaction)
                    {
                        sender.sendMessage(Messaging.parse("{ERR}Invalid bundle size [" + firstItem.split(":", 2)[1] + "]. (Range: 1.." + maxPerTransaction + ")"));
                        return;
                    }
                }
            }
            catch (NumberFormatException ex)
            {
                sender.sendMessage(Messaging.parse("{ERR}Invalid bundle size [" + firstItem.split(":", 2)[1] + "]. (Range: 1.." + maxPerTransaction + ")"));
                return;
            }
            
            if (plugin.getDatabaseMarket().updateAllFromTags(itemStringIn, name))
            {
                sender.sendMessage(Messaging.parse("All shop items updated."));
                return;
            }
            else
            {
                sender.sendMessage(Messaging.parse("{ERR}All shop items update failed."));
                return;
            }
        }
        // End of update-all subsection
        
        // Fetch the previous record and use it as the default for parsing these string tags.
        
        ItemClump requested = new ItemClump(itemString, plugin.getDatabaseMarket(), name);
        
        MarketItem prevData = plugin.getDatabaseMarket().data(requested, name);
        
        if (prevData == null)
        {
            sender.sendMessage(Messaging.parse("{ERR}" + itemString.split(" ", 2)[0] + " not found in market."));
            sender.sendMessage(Messaging.parse("{ERR}Use {CMD}/shop add{ERR} instead."));
            return;
        }
        
        // If no :count is input, insert it into itemString.
        if (!(itemString.split(" ")[0].contains(":")))
        {
            String[] itemSubStrings = itemString.split(" ", 2);
            itemSubStrings[0] += ":" + prevData.count;
            if (itemSubStrings.length > 1)
            {
                itemString = itemSubStrings[0] + " " + itemSubStrings[1];
            }
            else
            {
                itemString = itemSubStrings[0];
            }
        }
        
        MarketItem updated = new MarketItem(itemString, prevData, plugin.getDatabaseMarket(), name);
        
        if ((updated.count < 1) || (updated.count > maxPerTransaction))
        {
            sender.sendMessage(Messaging.parse("{ERR}Invalid bundle size. (Range: 1.." + maxPerTransaction + ")"));
            return;
        }
        
        if (plugin.getDatabaseMarket().update(updated))
        {
            sender.sendMessage(Messaging.parse("Item {PRM}" + updated.getName() + "{} updated:"));
            ArrayList<String> thisList = updated.infoStringFull();
            for (String thisLine : thisList)
            {
                sender.sendMessage(Messaging.parse(thisLine));
            }
        }
        else
        {
            sender.sendMessage(Messaging.parse("Item {PRM}" + updated.getName() + "{} update {ERR}failed."));
        }
    }
}
