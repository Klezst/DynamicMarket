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

package com.gmail.haloinverse.DynamicMarket.commands;

import java.util.ArrayList;
import java.util.logging.Level;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.gmail.haloinverse.DynamicMarket.DynamicMarket;
import com.gmail.haloinverse.DynamicMarket.ItemClump;
import com.gmail.haloinverse.DynamicMarket.MarketItem;
import com.gmail.haloinverse.DynamicMarket.Setting;
import com.gmail.haloinverse.DynamicMarket.util.Messaging;
import com.gmail.haloinverse.DynamicMarket.util.Util;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandPermissions;

public class ShopCommands
{
	@Command
	(
		aliases = {"add", "a"},
		desc = "Adds an item to the shop",
		usage = "<id>[:<bundleSize>] [buyPrice] [sellPrice] [tagList]",
		min = 1,
		max = -1
	)
	@CommandPermissions("items.add")
	public static void add(CommandContext args, DynamicMarket plugin, CommandSender sender)
	{
		plugin.getShop().add(sender, args.getJoinedStrings(0));
	}
	
	@Command
	(
		aliases = {"buy", "b"},
		desc = "Purchases an item from the store",
		usage = "<itemID>[:<count>]", // TODO: Make usage <itemID>[:[itemData]] [amount]
		min = 1,
		max = 1
	)
	@CommandPermissions("buy")
	public static void buy(CommandContext args, DynamicMarket plugin, CommandSender sender)
	{
        if (sender instanceof Player)
        {
        	plugin.getShop().buy((Player)sender, args.getString(0));
        }
        else
        {
        	sender.sendMessage(Messaging.parseColor("{ERR}Cannot purchase items without being logged in."));
        }
	}
	
	@Command
	(
		aliases = {"exportdb"},
		desc = "Saves the database to the shopDB.csv",
		min = 0,
		max = 0
	)
	@CommandPermissions("admin")
	public static void exportDB(CommandContext args, DynamicMarket plugin, CommandSender sender)
	{
		plugin.log(Level.INFO, sender.getName() + " has issued the exportDB command; exporting.");
        if (plugin.getDatabaseMarket().dumpToCSV(plugin.getSetting(Setting.IMPORT_EXPORT_PATH, String.class) + plugin.getShop().getName() + plugin.getSetting(Setting.IMPORT_EXPORT_FILE, String.class), plugin.getShop().getName()))
        {
            sender.sendMessage(Messaging.parseColor("{}Export successful."));
        }
        else
        {
        	sender.sendMessage(Messaging.parseColor("{ERR}Export FAILED!"));
        }
	}
	
	// Is this help command really necessary? CommandsManager provides a description and usage help. It's already written; so, I'll leave it in here.
	@Command
	(
		aliases = {"help", "?"},
		desc = "Displays help",
		usage = "<itemID>",
		min = 0,
		max = 2
	)
	public static void help(CommandContext args, DynamicMarket plugin, CommandSender sender)
	{
		// Concatenate arguments.
		String topic = "";
        if (args.argsLength() > 0)
        {
            topic += args.getString(0);
            if (args.argsLength() == 2)
            {
            	topic += " " + args.getString(1);
            }
        }
        
        // Generate message. TODO: Migrate messages to an Enum.
        if (topic.isEmpty())
        {
            String commands = "";
            String topics = "";
            String shortcuts = "";
            
            sender.sendMessage(Messaging.parseColor("{}" + Messaging.headerify("{CMD} " + plugin.getDescription().getName() + " {BKT}{} ")));
            sender.sendMessage(Messaging.parseColor("{} {BKT}(){} Optional, {PRM}<>{} Parameter"));
            sender.sendMessage(Messaging.parseColor("{CMD} /shop help {BKT}({PRM}<topic/command>{BKT}){} - Show help."));
            sender.sendMessage(Messaging.parseColor("{CMD} /shop {PRM}<command> <params>{} - Use a shop command."));
            
            commands += " list";
            shortcuts += " -? -l";
            if (plugin.hasPermission(sender, "buy"))
            {
                commands += " buy";
                shortcuts += " -b";
            }
            if (plugin.hasPermission(sender, "sell"))
            {
                commands += " sell";
                shortcuts += " -s";
            }
            commands += " info";
            shortcuts += " -i";
            
            if (plugin.hasPermission(sender, "items.add"))
            {
                commands += " add";
                shortcuts += " -a";
            }
            if (plugin.hasPermission(sender, "items.update"))
            {
                commands += " update";
                shortcuts += " -u";
            }
            if (plugin.hasPermission(sender, "items.remove"))
            {
                commands += " remove";
                shortcuts += " -r";
            }
            if (plugin.hasPermission(sender, "admin"))
            {
                commands += " reload";
                commands += " reset";
                commands += " exportdb importdb";
            }
            
            topics += "ids details about";
            if (plugin.hasPermission(sender, "items.add") || plugin.hasPermission(sender, "items.update"))
            {
                topics += " tags";
            }
            
            sender.sendMessage(Messaging.parseColor("{} Commands: {CMD}" + commands));
            sender.sendMessage(Messaging.parseColor("{} Shortcuts: {CMD}" + shortcuts));
            sender.sendMessage(Messaging.parseColor("{} Other help topics: {PRM}" + topics));
            
            return;
        }
        sender.sendMessage(Messaging.parseColor("{}" + Messaging.headerify("{} " + plugin.getDescription().getName() + " {BKT}{} : " + topic + "{} ")));
        
        if (topic.equalsIgnoreCase("buy"))
        {
            if (plugin.hasPermission(sender, "buy"))
            {
                sender.sendMessage(Messaging.parseColor("{CMD} /shop buy {PRM}<id>{BKT}({CMD}:{PRM}<count>{CMD})"));
                sender.sendMessage(Messaging.parseColor("{} Buy {PRM}<count>{} bundles of an item."));
                sender.sendMessage(Messaging.parseColor("{} If {PRM}<count>{} is missing, buys 1 bundle."));
                return;
            }
        }
        if (topic.equalsIgnoreCase("sell"))
        {
            if (plugin.hasPermission(sender, "sell"))
            {
                sender.sendMessage(Messaging.parseColor("{CMD} /shop sell {PRM}<id>{BKT}({CMD}:{PRM}<count>{CMD})"));
                sender.sendMessage(Messaging.parseColor("{} Sell {PRM}<count>{} bundles of an item."));
                sender.sendMessage(Messaging.parseColor("{} If {PRM}<count>{} is missing, sells 1 bundle."));
                return;
            }
        }
        if (topic.equalsIgnoreCase("info"))
        {
            // if (plugin.hasPermission(player,"sell"))
            // {
            sender.sendMessage(Messaging.parseColor("{CMD} /shop info {PRM}<id>"));
            sender.sendMessage(Messaging.parseColor("{} Show detailed information about a shop item."));
            return;
            // }
        }
        if (topic.equalsIgnoreCase("add"))
        {
            if (plugin.hasPermission(sender, "items.add"))
            {
                sender.sendMessage(Messaging.parseColor("{CMD} /shop add {PRM}<id>{BKT}({CMD}:{PRM}<bundle>{BKT}) ({PRM}<buyPrice>{BKT} ({PRM}<sellPrice>{BKT})) {PRM}<tags>"));
                sender.sendMessage(Messaging.parseColor("{} Adds item {PRM}<id>{} to the shop."));
                sender.sendMessage(Messaging.parseColor("{} Transactions will be in {PRM}<bundle>{} units (default 1)."));
                sender.sendMessage(Messaging.parseColor("{PRM} <buyPrice>{} and {PRM}<sellPrice>{} will be converted, if used."));
                sender.sendMessage(Messaging.parseColor("{} Prices are per-bundle."));
                sender.sendMessage(Messaging.parseColor("{} See also: {CMD}/shop help tags"));
                return;
            }
        }
        if (topic.equalsIgnoreCase("update"))
        {
            if (plugin.hasPermission(sender, "items.update"))
            {
                sender.sendMessage(Messaging.parseColor("{CMD} /shop update {PRM}<id>{BKT}({CMD}:{PRM}<bundle>{BKT}) ({PRM}<buyPrice>{BKT} ({PRM}<sellPrice>{BKT})) {PRM}<tags>"));
                sender.sendMessage(Messaging.parseColor("{} Changes item {PRM}<id>{}'s shop details."));
                sender.sendMessage(Messaging.parseColor("{PRM} <bundle>{}, {PRM}<buyPrice>{}, {PRM}<sellPrice>{}, and {PRM}<tags>{} will be changed."));
                sender.sendMessage(Messaging.parseColor("{} Transactions will be in {PRM}<bundle>{} units (default 1)."));
                sender.sendMessage(Messaging.parseColor("{} Prices are per-bundle."));
                sender.sendMessage(Messaging.parseColor("{} See also: {CMD}/shop help tags"));
                return;
            }
        }
        if (topic.equalsIgnoreCase("remove"))
        {
            if (plugin.hasPermission(sender, "items.remove"))
            {
                sender.sendMessage(Messaging.parseColor("{CMD} /shop remove {PRM}<id>"));
                sender.sendMessage(Messaging.parseColor("{} Removes item {PRM}<id>{} from the shop."));
                return;
            }
        }
        if (plugin.hasPermission(sender, "admin"))
        {
            if (topic.equalsIgnoreCase("reload"))
            {
                sender.sendMessage(Messaging.parseColor("{CMD} /shop reload"));
                sender.sendMessage(Messaging.parseColor("{} Restarts the shop plugin."));
                sender.sendMessage(Messaging.parseColor("{} Attempts to reload all relevant config files."));
                return;
            }
            if (topic.equalsIgnoreCase("reset"))
            {
                sender.sendMessage(Messaging.parseColor("{CMD} /shop reset"));
                sender.sendMessage(Messaging.parseColor("{} Completely resets the shop database."));
                sender.sendMessage(Messaging.parseColor("{} This will remove all items from the shop, and"));
                sender.sendMessage(Messaging.parseColor("{} create a new empty shop database."));
                return;
            }
            if (topic.equalsIgnoreCase("exportdb"))
            {
                sender.sendMessage(Messaging.parseColor("{CMD} /shop exportdb"));
                sender.sendMessage(Messaging.parseColor("{} Dumps the shop database to a .csv file."));
                sender.sendMessage(Messaging.parseColor("{} Name and location are set in the main config file."));
                sender.sendMessage(Messaging.parseColor("{} The file can be edited by most spreadsheet programs."));
                return;
            }
            if (topic.equalsIgnoreCase("importdb"))
            {
                sender.sendMessage(Messaging.parseColor("{CMD} /shop importdb"));
                sender.sendMessage(Messaging.parseColor("{} Reads a .csv file in to the shop database."));
                sender.sendMessage(Messaging.parseColor("{} Name and location are set in the main config file."));
                sender.sendMessage(Messaging.parseColor("{} The format MUST be the same as the export format."));
                sender.sendMessage(Messaging.parseColor("{} Records matching id/subtype will be updated."));
                return;
            }
        }
        if (topic.equalsIgnoreCase("ids"))
        {
            sender.sendMessage(Messaging.parseColor("{} Item ID format: {PRM}<id>{BKT}({CMD},{PRM}<subtype>{BKT})({CMD}:{PRM}<count>{BKT})"));
            sender.sendMessage(Messaging.parseColor("{PRM} <id>{}: Full name or ID number of the item."));
            sender.sendMessage(Messaging.parseColor("{PRM} <subtype>{}: Subtype of the item (default: 0)"));
            sender.sendMessage(Messaging.parseColor("{} Subtypes are used for wool/dye colours, log types, etc."));
            sender.sendMessage(Messaging.parseColor("{PRM} <count>{}: For shop items, this specifies bundle size."));
            sender.sendMessage(Messaging.parseColor("{} For transactions, this sets the number of bundles bought or sold."));
            return;
        }
        if (topic.equalsIgnoreCase("list"))
        {
            sender.sendMessage(Messaging.parseColor("{CMD} /shop list {BKT}({PRM}<nameFilter>{BKT}) ({PRM}<page>{BKT})"));
            sender.sendMessage(Messaging.parseColor("{} Displays the items in the shop."));
            sender.sendMessage(Messaging.parseColor("{} List format: {BKT}[{PRM}<id#>{BKT}]{PRM} <fullName> {BKT}[{PRM}<bundleSize>{BKT}]{} Buy {BKT}[{PRM}<buyPrice>{BKT}]{} Sell {BKT}[{PRM}<sellPrice>{BKT}]"));
            sender.sendMessage(Messaging.parseColor("{} Page 1 is displayed by default, if no page number is given."));
            sender.sendMessage(Messaging.parseColor("{} If {PRM}<nameFilter>{} is used, displays items containing {PRM}<nameFilter>{}."));
            return;
        }
        if (topic.equalsIgnoreCase("details"))
        {
            sender.sendMessage(Messaging.parseColor("{CMD} /shop {PRM}<id>{BKT}({CMD}:{PRM}<count>{BKT})"));
            sender.sendMessage(Messaging.parseColor("{} Displays the current buy/sell price of the selected item."));
            sender.sendMessage(Messaging.parseColor("{} Since prices can fluctuate, use {PRM}<count>{} to get batch pricing."));
            sender.sendMessage(Messaging.parseColor("{} See {CMD}/shop help ids{} for information on IDs."));
            return;
        }
        if ((Util.isAny(topic.split(" ")[0], "tags", "tag")) && ((plugin.hasPermission(sender, "items.add") || plugin.hasPermission(sender, "items.update"))))
        {
            if (topic.indexOf(" ") > -1)
            {
                // Possible tag listed!
                String thisTag = topic.split(" ")[1].replace(":", "");
                if (Util.isAny(thisTag, "n", "name"))
                {
                    sender.sendMessage(Messaging.parseColor("{CMD} n:{BKT}|{CMD}name:{} - Name/rename item"));
                    sender.sendMessage(Messaging.parseColor("{} Sets the item's name in the shop DB."));
                    sender.sendMessage(Messaging.parseColor("{} New name will persist until the item is removed."));
                    sender.sendMessage(Messaging.parseColor("{} If name is blank, will try to reload the name from items.db."));
                    return;
                }
                if (Util.isAny(thisTag, "bp", "baseprice"))
                {
                    sender.sendMessage(Messaging.parseColor("{CMD} bp:{BKT}|{CMD}BasePrice:{} - Base purchase price"));
                    sender.sendMessage(Messaging.parseColor("{} Buy price of the item at stock level 0."));
                    sender.sendMessage(Messaging.parseColor("{} All other prices are derived from this starting value."));
                    sender.sendMessage(Messaging.parseColor("{} Referenced by {PRM}SalesTax{}, {PRM}Stock{}, and {PRM}Volatility{}."));
                    sender.sendMessage(Messaging.parseColor("{} Soft-limited by {PRM}PriceFloor{}/{PRM}PriceCeiling{}."));
                    return;
                }
                if (Util.isAny(thisTag, "s", "stock"))
                {
                    sender.sendMessage(Messaging.parseColor("{CMD} s:{BKT}|{CMD}Stock:{} - Current stock level"));
                    sender.sendMessage(Messaging.parseColor("{} Stock level of this item (in bundles)."));
                    sender.sendMessage(Messaging.parseColor("{} Increases/decreases when items are sold/bought."));
                    sender.sendMessage(Messaging.parseColor("{} Affects buy/sell prices, if {PRM}Volatility{} > 0."));
                    sender.sendMessage(Messaging.parseColor("{} Soft-limited by {PRM}StockFloor{}/{PRM}StockCeiling{}."));
                    sender.sendMessage(Messaging.parseColor("{} Hard-limited (transactions fail) by {PRM}StockLowest{}/{PRM}StockHighest{}."));
                    return;
                }
                if (Util.isAny(thisTag, "cb", "canbuy"))
                {
                    sender.sendMessage(Messaging.parseColor("{CMD} cb:{BKT}|{CMD}CanBuy:{} - Buyability of item"));
                    sender.sendMessage(Messaging.parseColor("{} Set to 'Y', 'T', or blank to allow buying from shop."));
                    sender.sendMessage(Messaging.parseColor("{} Set to 'N' or 'F' to disallow buying from shop."));
                    return;
                }
                if (Util.isAny(thisTag, "cs", "cansell"))
                {
                    sender.sendMessage(Messaging.parseColor("{CMD} cs:{BKT}|{CMD}CanSell:{} - Sellability of item"));
                    sender.sendMessage(Messaging.parseColor("{} Set to 'Y', 'T', or blank to allow selling to shop."));
                    sender.sendMessage(Messaging.parseColor("{} Set to 'N' or 'F' to disallow selling to shop."));
                    return;
                }
                if (Util.isAny(thisTag, new String[] { "v", "vol", "volatility" }))
                {
                    sender.sendMessage(Messaging.parseColor("{CMD} v:{BKT}|{CMD}Vol:{}{BKT}|{CMD}Volatility:{} - Price volatility"));
                    sender.sendMessage(Messaging.parseColor("{} Percent increase in price per 1 bundle bought from shop, * 10000."));
                    sender.sendMessage(Messaging.parseColor("{} v=0 prevents the price from changing with stock level."));
                    sender.sendMessage(Messaging.parseColor("{} v=1 increases the price 1% per 100 bundles bought."));
                    sender.sendMessage(Messaging.parseColor("{} v=10000 increases the price 100% per 1 bundle bought."));
                    sender.sendMessage(Messaging.parseColor("{} Calculations are compound vs. current stock level."));
                    return;
                }
                if (Util.isAny(thisTag, new String[] { "iv", "ivol", "invvolatility" }))
                {
                    sender.sendMessage(Messaging.parseColor("{CMD} iv:{BKT}|{CMD}IVol:{}{BKT}|{CMD}InvVolatility:{} - Inverse Volatility"));
                    sender.sendMessage(Messaging.parseColor("{} Number of bundles bought in order to double the price."));
                    sender.sendMessage(Messaging.parseColor("{} Converted to volatility when entered."));
                    sender.sendMessage(Messaging.parseColor("{} iv=+INF prevents the price from changing with stock level."));
                    sender.sendMessage(Messaging.parseColor("{} iv=6400 doubles the price for each 6400 items bought."));
                    sender.sendMessage(Messaging.parseColor("{} iv=1 doubles the price for each item bought."));
                    sender.sendMessage(Messaging.parseColor("{} Calculations are compound vs. current stock level."));
                    return;
                }
                if (Util.isAny(thisTag, "st", "salestax"))
                {
                    sender.sendMessage(Messaging.parseColor("{CMD} st:{BKT}|{CMD}SalesTax:{} - Sales Tax"));
                    sender.sendMessage(Messaging.parseColor("{} Percent difference between BuyPrice and SellPrice, * 100."));
                    sender.sendMessage(Messaging.parseColor("{} {PRM}SellPrice{}={PRM}BuyPrice{}*(1-({PRM}SalesTax{}/100))"));
                    sender.sendMessage(Messaging.parseColor("{} If {PRM}SellPrice{} is entered as an untagged value, it is used to calculate {PRM}SalesTax{}."));
                    sender.sendMessage(Messaging.parseColor("{} {PRM}SalesTax{} is applied after {PRM}PriceFloor{}/{PRM}PriceCeiling{}."));
                    return;
                }
                if (Util.isAny(thisTag, "sl", "stocklowest"))
                {
                    sender.sendMessage(Messaging.parseColor("{CMD} sl:{BKT}|{CMD}StockLowest:{} - Lowest stock level (hard limit)"));
                    sender.sendMessage(Messaging.parseColor("{} Buying from shop will fail if it would put stock below {PRM}StockLowest{}."));
                    sender.sendMessage(Messaging.parseColor("{} Set to 0 to to make stock 'finite'."));
                    sender.sendMessage(Messaging.parseColor("{} Set to -INF or a negative value to use stock level as a 'relative offset'."));
                    return;
                }
                if (Util.isAny(thisTag, "sh", "stockhighest"))
                {
                    sender.sendMessage(Messaging.parseColor("{CMD} sh:{BKT}|{CMD}StockHighest:{} - Highest stock level (hard limit)"));
                    sender.sendMessage(Messaging.parseColor("{} Selling to shop will fail if it would put stock above {PRM}StockHighest{}."));
                    sender.sendMessage(Messaging.parseColor("{} Set to +INF to let maximum stock be unlimited."));
                    return;
                }
                if (Util.isAny(thisTag, "sf", "stockfloor"))
                {
                    sender.sendMessage(Messaging.parseColor("{CMD} sf:{BKT}|{CMD}StockFloor:{} - Lowest stock level (soft limit)"));
                    sender.sendMessage(Messaging.parseColor("{} If {PRM}Stock{} falls below {PRM}StockFloor{}, it will be reset to {PRM}StockFloor{}."));
                    sender.sendMessage(Messaging.parseColor("{} Further purchases will be at a flat rate, until {PRM}Stock{} rises."));
                    return;
                }
                if (Util.isAny(thisTag, "sc", "stockceiling"))
                {
                    sender.sendMessage(Messaging.parseColor("{CMD} sc:{BKT}|{CMD}StockCeiling:{} - Highest stock level (soft limit)"));
                    sender.sendMessage(Messaging.parseColor("{} If {PRM}Stock{} rises above {PRM}StockCeiling{}, it will be reset to {PRM}StockCeiling{}."));
                    sender.sendMessage(Messaging.parseColor("{} Further sales will be at a flat rate, until {PRM}Stock{} falls."));
                    return;
                }
                if (Util.isAny(thisTag, "pf", "pricefloor"))
                {
                    sender.sendMessage(Messaging.parseColor("{CMD} pf:{BKT}|{CMD}PriceFloor:{} - Lowest buy price (soft limit)"));
                    sender.sendMessage(Messaging.parseColor("{} If {PRM}BuyPrice{} falls below {PRM}PriceFloor{}, it will be cropped at {PRM}PriceFloor{}."));
                    sender.sendMessage(Messaging.parseColor("{} Buy/sell prices will be at a flat rate, until {PRM}BuyPrice{} rises above {PRM}PriceFloor{}."));
                    sender.sendMessage(Messaging.parseColor("{} {PRM}PriceFloor{} is applied to {PRM}SellPrice{} before {PRM}SalesTax{}."));
                    return;
                }
                if (Util.isAny(thisTag, "pc", "priceceiling"))
                {
                    sender.sendMessage(Messaging.parseColor("{CMD} pc:{BKT}|{CMD}PriceCeiling:{} - Highest buy price (soft limit)"));
                    sender.sendMessage(Messaging.parseColor("{} If {PRM}BuyPrice{} rises above {PRM}PriceCeiling{}, it will be cropped at {PRM}PriceCeiling{}."));
                    sender.sendMessage(Messaging.parseColor("{} Buy/sell prices will be at a flat rate, until {PRM}BuyPrice{} falls below {PRM}PriceCeiling{}."));
                    sender.sendMessage(Messaging.parseColor("{} {PRM}PriceCeiling{} is applied to {PRM}SellPrice{} before {PRM}SalesTax{}."));
                    return;
                }
                if (thisTag.equalsIgnoreCase("flat"))
                {
                    sender.sendMessage(Messaging.parseColor("{CMD} flat{} - Set item with flat pricing."));
                    sender.sendMessage(Messaging.parseColor("{} Buy/sell prices for this item will not change with stock level."));
                    sender.sendMessage(Messaging.parseColor("{} Stock level WILL be tracked, and can float freely."));
                    sender.sendMessage(Messaging.parseColor("{} Equivalent to: {CMD}s:0 sl:-INF sh:+INF sf:-INF sc:+INF v:0 pf:0 pc:+INF"));
                    return;
                }
                if (thisTag.equalsIgnoreCase("fixed"))
                {
                    sender.sendMessage(Messaging.parseColor("{CMD} fixed{} - Set item with fixed pricing."));
                    sender.sendMessage(Messaging.parseColor("{} Buy/sell prices for this item will not change with transactions."));
                    sender.sendMessage(Messaging.parseColor("{} Stock level WILL NOT be tracked, and {PRM}Stock{} will remain at 0."));
                    sender.sendMessage(Messaging.parseColor("{} Equivalent to: {CMD}s:0 sl:-INF sh:+INF sf:0 sc:0 v:0 pf:0 pc:+INF"));
                    return;
                }
                if (thisTag.equalsIgnoreCase("float"))
                {
                    sender.sendMessage(Messaging.parseColor("{CMD} float{} - Set item with floating pricing."));
                    sender.sendMessage(Messaging.parseColor("{} Buy/sell prices for this item will vary by stock level."));
                    sender.sendMessage(Messaging.parseColor("{} If {PRM}Vol{}=0, {PRM}Vol{} will be set to a default of 100."));
                    sender.sendMessage(Messaging.parseColor("{} (For finer control, set {PRM}Volatility{} to an appropriate value.)"));
                    sender.sendMessage(Messaging.parseColor("{} Stock level can float freely above and below 0 with transactions."));
                    sender.sendMessage(Messaging.parseColor("{} Equivalent to: {CMD}sl:-INF sh:+INF sf:-INF sc:+INF {BKT}({CMD}v:100{BKT}){CMD} pf:0 pc:+INF"));
                    return;
                }
                if (thisTag.equalsIgnoreCase("finite"))
                {
                    sender.sendMessage(Messaging.parseColor("{CMD} finite{} - Set item with finite stock."));
                    sender.sendMessage(Messaging.parseColor("{} Buying from shop will fail if it would make {PRM}Stock{} < 0."));
                    sender.sendMessage(Messaging.parseColor("{} Any number of items can be sold to the shop."));
                    sender.sendMessage(Messaging.parseColor("{} Equivalent to: {CMD}sl:0 sh:+INF sf:-INF sc:+INF"));
                    return;
                }
                if (thisTag.equalsIgnoreCase("renorm"))
                {
                    sender.sendMessage(Messaging.parseColor("{CMD} renorm{BKT}({CMD}:{PRM}<stock>{BKT}){} - Renormalize an item's price."));
                    sender.sendMessage(Messaging.parseColor("{} Resets an item's {PRM}Stock{}, while preserving its current price."));
                    sender.sendMessage(Messaging.parseColor("{} Sets an item's {PRM}BasePrice{} to its current {PRM}BuyPrice{},"));
                    sender.sendMessage(Messaging.parseColor("{} then sets {PRM}Stock{} to {PRM}<stock>{} (0 if blank or missing)."));
                    return;
                }
                sender.sendMessage(Messaging.parseColor("{ERR} Unknown tag {PRM}" + thisTag + "{ERR}."));
                sender.sendMessage(Messaging.parseColor("{ERR} Use {CMD}/shop help tags{ERR} to list tags."));
                return;
            }
            else
            {
                sender.sendMessage(Messaging.parseColor("{} Tag format: {PRM}<tagName>{BKT}({CMD}:{PRM}<value>{BKT}) ({PRM}<tagName>{BKT}({CMD}:{PRM}<value>{BKT}))..."));
                sender.sendMessage(Messaging.parseColor("{} Available tags: {CMD} Name: BasePrice: SalesTax: Stock: CanBuy: CanSell: Vol: IVol:"));
                sender.sendMessage(Messaging.parseColor("{CMD} StockLowest: StockHighest: StockFloor: StockCeiling: PriceFloor: PriceCeiling:"));
                sender.sendMessage(Messaging.parseColor("{} Available preset tags: {CMD}Fixed Flat Float Finite Renorm:"));
                sender.sendMessage(Messaging.parseColor("{} Use {CMD}/shop help tag {PRM}<tagName>{} for tag descriptions."));
                return;
            }
        }
        if (topic.equalsIgnoreCase("about"))
        {
            sender.sendMessage(Messaging.parseColor("{} " + plugin.getDescription().getName() + " " + plugin.getDescription().getVersion() + " written by " + plugin.getDescription().getAuthors() + "."));
            return;
        }
        sender.sendMessage(Messaging.parseColor("{}Unknown help topic:{CMD} " + topic));
        sender.sendMessage(Messaging.parseColor("{}Use {CMD}/shop help{} to list topics."));
        return;
	}
	
	@Command
	(
		aliases = {"importdb"},
		desc = "Loads the database from the shopDB.csv",
		min = 0,
		max = 0
	)
	@CommandPermissions("admin")
	public static void importDB(CommandContext args, DynamicMarket plugin, CommandSender sender)
	{
		plugin.log(Level.INFO, sender.getName() + " has issued the importDB command; importing.");
        if (plugin.getDatabaseMarket().inhaleFromCSV(plugin.getSetting(Setting.IMPORT_EXPORT_PATH, String.class) + plugin.getShop().getName() + plugin.getSetting(Setting.IMPORT_EXPORT_FILE, String.class), plugin.getShop().getName()))
        {
            sender.sendMessage(Messaging.parseColor("{}Import successful."));
        }
        else
        {
        	sender.sendMessage(Messaging.parseColor("{ERR}Import FAILED!"));
        }
	}
	
	@Command
	(
		aliases = {"info", "i"},
		desc = "Displays information about an item",
		usage = "[itemID]",
		min = 0,
		max = 1
	)
	public static void info(CommandContext args, DynamicMarket plugin, CommandSender sender)
	{
		if (args.argsLength() == 0) // Show plugin info.
		{
			sender.sendMessage(Messaging.parseColor("{}" + plugin.getDescription().getFullName() + " Copyright (C) 2011 Klezst"));
			sender.sendMessage(Messaging.parseColor("{}" + "Authors: " + plugin.getDescription().getAuthors()));
		}
		else // Show item info.
		{
			String shopLabel = "";
			
	        ItemClump requested = new ItemClump(args.getString(0), plugin.getDatabaseMarket(), shopLabel);
	        if (requested.isValid())
	        {
		        MarketItem data = plugin.getDatabaseMarket().data(requested, shopLabel);
		        if (data != null)
		        {
			        sender.sendMessage(Messaging.parseColor("{}Item {PRM}" + data.getName() + "{BKT}[{PRM}" + data.idString() + "{BKT}]{} info:"));
		            ArrayList<String> thisList = data.infoStringFull();
		            for (String thisLine : thisList)
		            {
		                sender.sendMessage(Messaging.parseColor(thisLine));
		            }
		        }
		        else
		        {
		        	sender.sendMessage(Messaging.parseColor("{ERR}" + args.getString(0) + " is not currently traded in shop."));
		        }
	        }
	        else
	        {
	            sender.sendMessage(Messaging.parseColor("{ERR}" + args.getString(0) + " is not a valid item."));
	        }
		}
	}
	
	@Command
	(
		aliases = {"list", "l"},
		desc = "Lists items for sale",
		usage = "[filter] [page]",
		min = 0,
		max = 2
	)
	public static void list(CommandContext args, DynamicMarket plugin, CommandSender sender)
	{
        // Possible inputs:
        // none (default first page, unfiltered)
        // pageNum
        // nameFilter
        // nameFilter pageNum
        // TODO: Break into another method.
        int pageSelect = 1;
        String nameFilter = null;
        if (args.argsLength() == 2)
        {
            try
            {
                pageSelect = Integer.valueOf(args.getString(0)).intValue();
            }
            catch (NumberFormatException ex)
            {
                nameFilter = args.getString(0);
            }
        }
        if (args.argsLength() == 3)
        {
            nameFilter = args.getString(0);
            try
            {
                pageSelect = Integer.valueOf(args.getString(1)).intValue();
            }
            catch (NumberFormatException ex)
            {
                pageSelect = 1;
            }
        }
        ArrayList<MarketItem> list = plugin.getDatabaseMarket().list(pageSelect, nameFilter, plugin.getShop().getName());
        ArrayList<MarketItem> listToCount = plugin.getDatabaseMarket().list(0, nameFilter, plugin.getShop().getName());
        int numPages = (listToCount.size() / 8 + (listToCount.size() % 8 > 0 ? 1 : 0));
        if (listToCount.isEmpty())
        {
            sender.sendMessage(Messaging.parseColor("{ERR}No items are set up in the shop yet..."));
            return;
        }
        if (pageSelect > numPages)
        {
            sender.sendMessage(Messaging.parseColor("{ERR}The shop only has " + numPages + " pages of items."));
            return;
        }
        if (list.isEmpty())
        {
            sender.sendMessage(Messaging.parseColor("{ERR}Horrors! The page calculation made a mistake!"));
            return;
        }
        else
        {
            sender.sendMessage(Messaging.parseColor("{}Shop Items: Page {BKT}[{PRM}" + pageSelect + "{BKT}]{} of {BKT}[{PRM}" + numPages + "{BKT}]"));
            for (MarketItem data : list)
            {
                sender.sendMessage(Messaging.parseColor(data.infoStringShort()));
            }
        }
	}
	
	@Command
	(
		aliases = {"reload"},
		desc = "Restarts the plugin",
		min = 0,
		max = 0
	)
	@CommandPermissions("admin")
	public static void reload(CommandContext args, DynamicMarket plugin, CommandSender sender)
	{
		plugin.log(Level.INFO, sender.getName() + " has issued the reload command; reloading.");
		plugin.onDisable();
		plugin.onEnable();
		sender.sendMessage(Messaging.parseColor("{}" + plugin.getDescription().getName() + " Reloaded."));
	}
	
	@Command
	(
		aliases = {"reset"},
		desc = "Resets the database",
		min = 0,
		max = 0
	)
	@CommandPermissions("admin")
	public static void reset(CommandContext args, DynamicMarket plugin, CommandSender sender)
	{
		plugin.log(Level.INFO, sender.getName() + " has issued the reset command; resetting the database.");
        if (plugin.getDatabaseMarket().resetDatabase(plugin.getShop().getName()))
        {
        	sender.sendMessage(Messaging.parseColor("{}Database reset successful."));
        }
        else
        {
        	sender.sendMessage(Messaging.parseColor("{}Database reset {ERR}FAILED!"));
        }
	}
	
	@Command
	(
		aliases = {"remove", "r"},
		desc = "Removes an item from the shop",
		usage = "<id>",
		min = 1,
		max = 1
	)
	@CommandPermissions("items.remove")
	public static void remove(CommandContext args, DynamicMarket plugin, CommandSender sender)
	{
		plugin.getShop().remove(sender, args.getString(0));
	}
	
	@Command
	(
		aliases = {"sell", "s"},
		desc = "Sells an item to the store",
		usage = "<itemID>[:<count>]", // TODO: Make usage <itemID>[:[itemData]] [amount]
		min = 1,
		max = 1
	)
	@CommandPermissions("sell")
	public static void sell(CommandContext args, DynamicMarket plugin, CommandSender sender)
	{
        if (sender instanceof Player)
        {
        	plugin.getShop().sell((Player)sender, args.getString(0));
        }
        else
        {
        	sender.sendMessage(Messaging.parseColor("{ERR}Cannot purchase items without being logged in."));
        }
	}
	
	@Command
	(
		aliases = {"update", "u"},
		desc = "Updates an item at the shop",
		usage = "<id>[:<bundleSize>] [buyPrice] [sellPrice] [tagList]",
		min = 1,
		max = -1
	)
	@CommandPermissions("items.update")
	public static void update(CommandContext args, DynamicMarket plugin, CommandSender sender)
	{
		plugin.getShop().update(sender, args.getJoinedStrings(0));
	}
}
