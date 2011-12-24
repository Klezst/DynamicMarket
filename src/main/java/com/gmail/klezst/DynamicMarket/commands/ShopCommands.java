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

package com.gmail.klezst.DynamicMarket.commands;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;

import com.gmail.klezst.DynamicMarket.DynamicMarket;
import com.gmail.klezst.DynamicMarket.Product;
import com.gmail.klezst.DynamicMarket.Setting;
import com.gmail.klezst.DynamicMarket.Shop;
import com.gmail.klezst.DynamicMarket.Transaction;
import com.gmail.klezst.util.Format;
import com.gmail.klezst.util.IO;
import com.gmail.klezst.util.Message;
import com.gmail.klezst.util.Permission;
import com.gmail.klezst.util.Util;
import com.idragonfire.event.DynamicMarketMasterShopAreaListener;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandPermissions;

public class ShopCommands // TODO: All shop modification/creation/deletion commands.
{
    @Command(aliases = { "add", "a" }, desc = "Adds an item to the shop",
    // Currently one cannot issue a command this long.
    usage = "<id>[:<subType>] [bundleSize|basePrice|maxPrice|minPrice|salesTax|volatility|stock|maxStock|minStock:<value>|buyable<true|false>|sellable<true|false>]", min = 1, max = 12)
    @CommandPermissions("items.add")
    public static void add(CommandContext args, DynamicMarket plugin,
	    CommandSender sender) {
	Shop shop = plugin.getMarket().getShop(((Player) sender).getLocation());
	Product product;
	try {
	    // throws IllegalArgumentException, iff args is not a valid Product.
	    product = Product.parseProduct(args);
	    shop.addProduct(product);
	    // Update database if valid
	    plugin.getDatabase().save(product);
	    Message.send(sender, "{}" + args.getString(0)
		    + " is now for sale at " + shop.getName() + ".");
	} catch (IllegalArgumentException e) {
	    Message.send(sender, "{ERR}" + e.getMessage());
	    return;
	}
    }

    @Command(aliases = { "buy", "b" }, desc = "Purchases an item from the store", usage = "<itemID>[:<subType>] [amount]", min = 1, max = 2)
    @CommandPermissions("buy")
    public static void buy(CommandContext args, DynamicMarket plugin,
	    CommandSender sender) {
	sellOrBuyAction(args, plugin, sender, false);
    }

    @Command(aliases = { "exportdb" }, desc = "Saves the database to the shopDB.csv", min = 0, max = 0)
    @CommandPermissions("admin")
    public static void exportDB(
	    CommandContext args,
	    DynamicMarket plugin, CommandSender sender) {
	plugin.log(Level.INFO, sender.getName()
		+ " has issued the exportDB command; exporting.");
	try {
	    IO.dumpToCSV(
		    plugin.getSetting(Setting.IMPORT_EXPORT_PATH, String.class),
		    "shops.csv", plugin.getMarket());
	} catch (IOException e) {
	    plugin.log(Level.WARNING, e.getMessage());
	    Message.send(sender, "{ERR}Export FAILED!");
	    return;
	}
	Message.send(sender, "{}Export successfull.");
    }

    // Is this help command really necessary? CommandsManager provides a description and usage help. It's already written; so, I'll leave it in here.
    @Command(aliases = { "help", "?" }, desc = "Displays help", usage = "<itemID>", min = 0, max = 2)
    public static void help(CommandContext args, DynamicMarket plugin,
	    CommandSender sender) {
	// Concatenate arguments.
	String topic = "";
	if (args.argsLength() > 0) {
	    topic += args.getString(0);
	    if (args.argsLength() == 2) {
		topic += " " + args.getString(1);
	    }
	}

	// Generate message. TODO: Migrate messages to an Enum or some form of constant.
	if (topic.isEmpty()) {
	    String commands = "";
	    String topics = "";
	    String shortcuts = "";

	    sender.sendMessage(Message.parseColor("{}"
		    + Message.headerify("{CMD} "
			    + plugin.getDescription().getName() + " {BKT}{} ")));
	    sender.sendMessage(Message
		    .parseColor("{} {BKT}[]{} Optional, {BKT}<>{} Required"));
	    sender.sendMessage(Message
		    .parseColor("{CMD} /shop help {PRM}[topic|command]{} - Show help."));
	    sender.sendMessage(Message
		    .parseColor("{CMD} /shop {PRM}<command> <params>{} - Use a shop command."));

	    commands += " list";
	    shortcuts += " -? -l";
	    if (Permission.hasPermission(sender, "buy")) {
		commands += " buy";
		shortcuts += " -b";
	    }
	    if (Permission.hasPermission(sender, "sell")) {
		commands += " sell";
		shortcuts += " -s";
	    }
	    commands += " info";
	    shortcuts += " -i";

	    if (Permission.hasPermission(sender, "items.add")) {
		commands += " add";
		shortcuts += " -a";
	    }
	    if (Permission.hasPermission(sender, "items.update")) {
		commands += " update";
		shortcuts += " -u";
	    }
	    if (Permission.hasPermission(sender, "items.remove")) {
		commands += " remove";
		shortcuts += " -r";
	    }
	    if (Permission.hasPermission(sender, "admin")) {
		commands += " reload";
		commands += " exportdb importdb";
	    }

	    topics += "ids details about";
	    if (Permission.hasPermission(sender, "items.add")
		    || Permission.hasPermission(sender, "items.update")) {
		topics += " tags";
	    }

	    sender.sendMessage(Message.parseColor("{} Commands: {CMD}"
		    + commands));
	    sender.sendMessage(Message.parseColor("{} Shortcuts: {CMD}"
		    + shortcuts));
	    sender.sendMessage(Message.parseColor("{} Other help topics: {PRM}"
		    + topics));

	    return;
	}
	sender.sendMessage(Message.parseColor("{}"
		+ Message.headerify("{} " + plugin.getDescription().getName()
			+ " {BKT}{} : " + topic + "{} ")));

	if (topic.equalsIgnoreCase("buy")) {
	    if (Permission.hasPermission(sender, "buy")) {
		sender.sendMessage(Message
			.parseColor("{CMD} /shop buy {PRM}<id>[{BKT}:{PRM}<subType>] [amount]"));
		sender.sendMessage(Message
			.parseColor("{} Buy {PRM}<amount>{} bundles of an item."));
		sender.sendMessage(Message
			.parseColor("{} If {PRM}<amount>{} is missing, buys 1 bundle."));
		return;
	    }
	}
	if (topic.equalsIgnoreCase("sell")) {
	    if (Permission.hasPermission(sender, "sell")) {
		sender.sendMessage(Message
			.parseColor("{CMD}/shop sell {PRM}<id>[{BKT}:{PRM}<subType>] [amount]"));
		sender.sendMessage(Message
			.parseColor("{} Sell {PRM}<amount>{} bundles of an item."));
		sender.sendMessage(Message
			.parseColor("{} If {PRM}<amount>{} is missing, sells 1 bundle."));
		return;
	    }
	}
	if (topic.equalsIgnoreCase("info")) {
	    sender.sendMessage(Message
		    .parseColor("{CMD}/shop info {PRM}<id>[{BKT}:{PRM}<subType>]"));
	    sender.sendMessage(Message
		    .parseColor("{} Show detailed information about a shop item."));
	    return;
	}
	if (topic.equalsIgnoreCase("add")) {
	    if (Permission.hasPermission(sender, "items.add")) {
		sender.sendMessage(Message
			.parseColor("{CMD}/shop add {PRM}<id>[{BKT}:{PRM}<subType>] <tags>"));
		sender.sendMessage(Message
			.parseColor("{} Adds item {PRM}<id>{BKT}:{PRM}<subType>{} to the shop."));
		sender.sendMessage(Message
			.parseColor("{} See also: {CMD}/shop help tags"));
		return;
	    }
	}
	if (topic.equalsIgnoreCase("update")) {
	    if (Permission.hasPermission(sender, "items.update")) {
		sender.sendMessage(Message
			.parseColor("{CMD}/shop update {PRM}<id>{BKT}:{PRM}[<subType>] <tags>"));
		sender.sendMessage(Message
			.parseColor("{} Changes item {PRM}<id>{BKT}:{PRM}<subType>{}'s shop details."));
		sender.sendMessage(Message
			.parseColor("{} See also: {CMD}/shop help tags"));
		return;
	    }
	}
	if (topic.equalsIgnoreCase("remove")) {
	    if (Permission.hasPermission(sender, "items.remove")) {
		sender.sendMessage(Message
			.parseColor("{CMD} /shop remove {PRM}<id>[{BKT}:{PRM}<subType>]"));
		sender.sendMessage(Message
			.parseColor("{} Removes item {PRM}<id>{BKT}:{PRM}<subType>{} from the shop."));
		return;
	    }
	}
	if (Permission.hasPermission(sender, "admin")) {
	    if (topic.equalsIgnoreCase("reload")) {
		sender.sendMessage(Message.parseColor("{CMD} /shop reload"));
		sender.sendMessage(Message
			.parseColor("{} Restarts the shop plugin."));
		sender.sendMessage(Message
			.parseColor("{} Attempts to reload all relevant config files."));
		return;
	    }
	    if (Util.isAny(topic, "export", "exportdb")) {
		sender.sendMessage(Message.parseColor("{CMD} /shop exportdb"));
		sender.sendMessage(Message
			.parseColor("{} Dumps the shop database to a .csv file."));
		sender.sendMessage(Message
			.parseColor("{} Is saved in the plugins data folder (plugins\\DynamicMarket\\shops.csv)"));
		sender.sendMessage(Message
			.parseColor("{} The file can be edited by most spreadsheet programs."));
		return;
	    }
	    if (Util.isAny(topic, "import", "importdb")) {
		sender.sendMessage(Message.parseColor("{CMD} /shop importdb"));
		sender.sendMessage(Message
			.parseColor("{} Reads a .csv file in to the shop database."));
		sender.sendMessage(Message
			.parseColor("{} File location is set in the main config file."));
		sender.sendMessage(Message
			.parseColor("{} The format MUST be the same as the export format."));
		sender.sendMessage(Message
			.parseColor("{ERR} THIS WILL DROP THE ENTIRE DATABASE! BACKUP YOUR DATABASE FIRST!"));
		return;
	    }
	}
	if (topic.equalsIgnoreCase("ids")) {
	    sender.sendMessage(Message
		    .parseColor("{} Item ID format: {PRM}<id>{BKT}:{PRM}<subtype> <count>)"));
	    sender.sendMessage(Message
		    .parseColor("{PRM} <id>{}: Full name or ID number of the item."));
	    sender.sendMessage(Message
		    .parseColor("{PRM} <subtype>{}: Subtype of the item (default: 0)"));
	    sender.sendMessage(Message
		    .parseColor("{} Subtypes are used for wool/dye colours, log types, etc."));
	    sender.sendMessage(Message
		    .parseColor("{} For transactions, {PRM}<count> {}this sets the number of bundles bought or sold."));
	    return;
	}
	if (topic.equalsIgnoreCase("list")) {
	    sender.sendMessage(Message
		    .parseColor("{CMD}/shop list {PRM}[filter] [page]"));
	    sender.sendMessage(Message
		    .parseColor("{} Displays the items in the shop."));
	    sender.sendMessage(Message
		    .parseColor("{} List format: {PRM}<id> {PRM}<fullName> {}Bundle: {PRM}<bundleSize> {}Buy: {PRM}<buyPrice> {}Sell: {PRM}<sellPrice>"));
	    sender.sendMessage(Message
		    .parseColor("{} Page 1 is displayed by default, if no page number is given."));
	    sender.sendMessage(Message
		    .parseColor("{} If {PRM}<nameFilter>{} is used, displays items containing {PRM}<nameFilter>{}."));
	    return;
	}
	if (topic.equalsIgnoreCase("details")) {
	    sender.sendMessage(Message
		    .parseColor("{CMD} /shop info {PRM}<id>{PRM}[{BKT}:{PRM}<subType>]"));
	    sender.sendMessage(Message
		    .parseColor("{} Displays the current buy/sell price of the selected item."));
	    sender.sendMessage(Message
		    .parseColor("{} Since prices can fluctuate, use {PRM}<count>{} to get batch pricing."));
	    sender.sendMessage(Message
		    .parseColor("{} See {CMD}/shop help ids{} for information on IDs."));
	    return;
	}
	if ((Util.isAny(topic.split(" ")[0], "tags", "tag"))
		&& ((Permission.hasPermission(sender, "items.add") || Permission
			.hasPermission(sender, "items.update")))) {
	    if (topic.indexOf(" ") > -1) {
		// Possible tag listed!
		String thisTag = topic.split(" ")[1].replace(":", "");
		if (Util.isAny(thisTag, "bp", "baseprice")) {
		    sender.sendMessage(Message
			    .parseColor("{CMD}basePrice{BKT}:{PRM}<basePrice>{} - Base purchase price"));
		    sender.sendMessage(Message
			    .parseColor("{} Buy price of the item at stock level 0."));
		    sender.sendMessage(Message
			    .parseColor("{} All other prices are derived from this starting value."));
		    sender.sendMessage(Message
			    .parseColor("{} Referenced by {PRM}SalesTax{}, {PRM}Stock{}, and {PRM}Volatility{}."));
		    sender.sendMessage(Message
			    .parseColor("{} Limited by {PRM}PriceFloor{}/{PRM}PriceCeiling{}."));
		    return;
		}
		if (Util.isAny(thisTag, "s", "stock")) {
		    sender.sendMessage(Message
			    .parseColor("{CMD}stock{BKT}:{PRM}<stock>{} - Current stock level"));
		    sender.sendMessage(Message
			    .parseColor("{} Stock level of this item (in bundles)."));
		    sender.sendMessage(Message
			    .parseColor("{} Increases/decreases when items are sold/bought."));
		    sender.sendMessage(Message
			    .parseColor("{} Affects buy/sell prices, if {PRM}Volatility{} > 0."));
		    sender.sendMessage(Message
			    .parseColor("{} Limited (transactions fail) by {PRM}StockLowest{}/{PRM}StockHighest{}."));
		    return;
		}
		if (Util.isAny(thisTag, "cb", "buyable", "canbuy")) {
		    Message.send(
			    sender, // The recipient.
			    "{CMD}buyable{BKT}:{PRM}{PRM}<buyable>{} - Buyability of item",
			    "{} Use to allow buying an item to the shop.",
			    "{} Set to 'Y' or 'T' to allow buying an item to the shop.",
			    "{} Set to 'N' or 'F' to disallow buying an item to the shop.");
		    return;
		}
		if (Util.isAny(thisTag, "cs", "sellable", "cansell")) {
		    Message.send(
			    sender, // The recipient.
			    "{CMD}sellable{BKT}:{PRM}{PRM}<sellable>{} - Sellability of item",
			    "{} Use to allow selling an item to the shop.",
			    "{} Set to 'Y' or 'T' to allow selling an item to the shop.",
			    "{} Set to 'N' or 'F' to disallow selling an item to the shop.");
		    return;
		}
		if (Util.isAny(thisTag, "v", "vol", "volatility", "float")) {
		    sender.sendMessage(Message
			    .parseColor("{CMD}volatility{BKT}:{PRM}<volatility>{} - Price volatility"));
		    sender.sendMessage(Message
			    .parseColor("{} Percent increase in price per 1 bundle bought from shop, / 100."));
		    sender.sendMessage(Message
			    .parseColor("{PRM} 0 {}prevents the price from changing with stock level."));
		    sender.sendMessage(Message
			    .parseColor("{PRM} 1 {}increases the price 1% per 100 bundles bought."));
		    sender.sendMessage(Message
			    .parseColor("{PRM} 10000 {}increases the price 100% per 1 bundle bought."));
		    sender.sendMessage(Message
			    .parseColor("{} Calculations are compound vs. current stock level."));
		    return;
		}
		if (Util.isAny(thisTag, "st", "salestax")) {
		    sender.sendMessage(Message
			    .parseColor("{CMD}salesTax{BKT}:{PRM}<salesTax>"));
		    sender.sendMessage(Message
			    .parseColor("{} Percent difference between BuyPrice and SellPrice, / 100."));
		    sender.sendMessage(Message
			    .parseColor("{} {PRM}SellPrice{}={PRM}BuyPrice{}*(1-({PRM}SalesTax{}/100))"));
		    sender.sendMessage(Message
			    .parseColor("{} {PRM}SalesTax{} is applied before {PRM}PriceFloor{}/{PRM}PriceCeiling{}."));
		    return;
		}
		if (Util.isAny(thisTag, "sf", "minStock", "stockfloor",
			"stocklowest")) {
		    sender.sendMessage(Message
			    .parseColor("{CMD}minStock{BKT}:{PRM}<minStock>{} - Lowest stock level"));
		    sender.sendMessage(Message
			    .parseColor("{} If the stock floor is reached, customers will no longer be able to buy that product from the shop."));
		    return;
		}
		if (Util.isAny(thisTag, "sc", "maxStock", "stockceiling",
			"stockhighest")) {
		    sender.sendMessage(Message
			    .parseColor("{CMD}maxStock{BKT}:{PRM}<maxStock>{} - Highest stock level"));
		    sender.sendMessage(Message
			    .parseColor("{} If the stock ceiling is reached, customers will no longer be able to sell that product to the shop."));
		    return;
		}
		if (Util.isAny(thisTag, "pf", "minPrice", "pricefloor",
			"priceHighest")) {
		    sender.sendMessage(Message
			    .parseColor("{CMD}minPrice{BKT}:{PRM}<minPrice>{} - Lowest buy price"));
		    sender.sendMessage(Message
			    .parseColor("{} If {PRM}BuyPrice{} falls below {PRM}PriceFloor{}, it will be cropped at {PRM}PriceFloor{}."));
		    sender.sendMessage(Message
			    .parseColor("{} Buy/sell prices will be at a flat rate, until {PRM}BuyPrice{} rises above {PRM}PriceFloor{}."));
		    sender.sendMessage(Message
			    .parseColor("{} {PRM}PriceFloor{} is applied to {PRM}SellPrice{} after {PRM}SalesTax{}."));
		    return;
		}
		if (Util.isAny(thisTag, "pc", "maxPrice", "priceceiling",
			"priceHighest")) {
		    sender.sendMessage(Message
			    .parseColor("{CMD}maxPrice{BKT}:{PRM}<maxPrice>{} - Highest buy price"));
		    sender.sendMessage(Message
			    .parseColor("{} If {PRM}BuyPrice{} rises above {PRM}PriceCeiling{}, it will be cropped at {PRM}PriceCeiling{}."));
		    sender.sendMessage(Message
			    .parseColor("{} Buy/sell prices will be at a flat rate, until {PRM}BuyPrice{} falls below {PRM}PriceCeiling{}."));
		    sender.sendMessage(Message
			    .parseColor("{} {PRM}PriceCeiling{} is applied to {PRM}SellPrice{} after {PRM}SalesTax{}."));
		    return;
		}
		if (Util.isAny(thisTag, "flat", "fixed")) {
		    Message.send(
			    sender, // The recipient.
			    "{}Use {CMD}/shop update {PRM}<itemID> {CMD}volatility{BKT}:{PRM}0",
			    "{} This will make the products price flat (not change based on stock levels).");
		    return;
		}
		if (thisTag.equalsIgnoreCase("finite")) {
		    Message.send(
			    sender, // The recipient.
			    "{}Use {CMD}/shop update {PRM}<itemID> {CMD}minStock{BKT}:{PRM}0",
			    "{} Buying from shop will fail, if it would make {PRM}Stock{} < 0.",
			    "{} Any number of items can be sold to the shop still.");
		    return;
		}
		if (thisTag.equalsIgnoreCase("renorm")) {
		    Message.send(
			    sender, // The recipient.
			    "{CMD}update {PRM}<itemID> {CMD}renorm{} - Renormalize an item's price.",
			    "{} Resets an item's {PRM}Stock{}, while preserving its current price.",
			    "{} Sets an item's {PRM}BasePrice{} to its current {PRM}BuyPrice,",
			    "{} and sets it's {PRM}Stock{} to 0.");
		    return;
		}
		sender.sendMessage(Message.parseColor("{ERR} Unknown tag {PRM}"
			+ thisTag + "{ERR}."));
		sender.sendMessage(Message
			.parseColor("{ERR} Use {CMD}/shop help tags{ERR} to list tags."));
		return;
	    }
	    sender.sendMessage(Message
		    .parseColor("{} Tag format: {PRM}<tagName>{CMD}{BKT}:{PRM}<value> {PRM}<tagName>{CMD}{BKT}:{PRM}<value>..."));
	    sender.sendMessage(Message
		    .parseColor("{} Available tags: {CMD}basePrice, salesTax, canBuy, canSell, volitility,"));
	    sender.sendMessage(Message
		    .parseColor("{CMD} stock, maxStock, minStock, maxPrice, minPrice, buyable, sellable."));
	    sender.sendMessage(Message
		    .parseColor("{} Use {CMD}/shop help tag {PRM}<tagName>{} for tag descriptions."));
	    return;
	}
	if (topic.equalsIgnoreCase("about")) {
	    Message.send(sender, "{} " + plugin.getDescription().getName()
		    + " " + plugin.getDescription().getVersion(),
		    "{}Authors: {PRM}" + plugin.getDescription().getAuthors()
			    + ".");
	    return;
	}
	sender.sendMessage(Message.parseColor("{}Unknown help topic:{CMD} "
		+ topic));
	sender.sendMessage(Message
		.parseColor("{}Use {CMD}/shop help{} to list topics."));
	return;
    }

    @Command(aliases = { "importdb" }, desc = "Loads the database from the shopDB.csv", min = 0, max = 0)
    @CommandPermissions("admin")
    public static void importDB(
	    CommandContext args,
	    DynamicMarket plugin, CommandSender sender) {
	if (plugin.importDB()) {
	    sender.sendMessage(Message.parseColor("{}Import successful."));
	} else {
	    sender.sendMessage(Message.parseColor("{ERR}Import FAILED!"));
	}
    }

    @Command(aliases = { "info", "i", "about" }, desc = "Displays information about an item", usage = "[itemID]", min = 0, max = 1)
    public static void info(CommandContext args, DynamicMarket plugin,
	    CommandSender sender) {
	if (args.argsLength() == 0) // Show plugin info.
	{
	    sender.sendMessage(Message.parseColor("{}"
		    + plugin.getDescription().getFullName()
		    + " Copyright (C) 2011 Klezst"));
	    sender.sendMessage(Message.parseColor("{}" + "Authors: {PRM}"
		    + plugin.getDescription().getAuthors()));
	} else // Show item info.
	{
	    if (sender instanceof Player) {
		Product product;
		try {
		    Shop shop = plugin.getMarket().getShop(
			    ((Player) sender).getLocation()); // throws IllegalArgumentException, Iff no shop at the player's location.
		    MaterialData data = Util.getMaterialData(args.getString(0)); // throws IllegalArgumentException, If id is not a valid MaterialData.
		    product = shop.getProduct(data); // throws IllegalArgumentException, Iff shop doesn't sell data.
		} catch (IllegalArgumentException e) {
		    sender.sendMessage(Message.parseColor("{ERR}"
			    + e.getMessage()));
		    return;
		}
		Message.send(sender, product.toString());
	    } else {
		sender.sendMessage("You must be logged in to issue this command");
	    }
	}
    }

    @SuppressWarnings("boxing")
    @Command(aliases = { "list", "l" }, desc = "Lists items for sale", usage = "[filter] [page]", min = 0, max = 2)
    public static void list(CommandContext args, DynamicMarket plugin,
	    CommandSender sender) {
	Shop shop = plugin.getMarket().getShop(((Player) sender).getLocation());
	String[] lines = shop.toString().split("\n");

	int page = 1;
	if (args.argsLength() > 0) {
	    try {
		page = Format.parseInteger(args.getString(0));
	    } catch (NumberFormatException e) {
		if (args.argsLength() > 1) {
		    try {
			page = Format.parseInteger(args.getString(1));
		    } catch (NumberFormatException ex) {
			Message.send(sender, "{ERR}" + args.getString(1)
				+ " is not a page number!");
			return;
		    }
		}

		// Filter
		String filter = args.getString(0).toLowerCase();
		List<String> temp = new ArrayList<String>();
		for (String product : lines) {
		    if (product.toLowerCase().contains(filter)) {
			temp.add(product);
		    }
		}

		lines = temp.toArray(new String[temp.size()]);
	    }
	}

	if (page < 1) {
	    Message.send(sender,
		    "{ERR}You must specify a positive page number!");
	    return;
	}

	int bound = Math.min(page * 8, lines.length);
	int startIndex = (page - 1) * 8;

	if (startIndex > bound) {
	    Message.send(sender, "{ERR}There aren't that many pages!");
	    return;
	}

	lines = Arrays.copyOfRange(lines, startIndex, bound);

	if (lines.length == 0) {
	    Message.send(sender, "{ERR}No such products found.");
	    return;
	}

	Message.send(sender, lines);
    }

    @Command(aliases = { "reload" }, desc = "Restarts the plugin", min = 0, max = 0)
    @CommandPermissions("admin")
    public static void reload(CommandContext args,
	    DynamicMarket plugin, CommandSender sender) {
	plugin.log(Level.INFO, sender.getName()
		+ " has issued the reload command; reloading.");
	plugin.onDisable();
	plugin.onEnable();
	sender.sendMessage(Message.parseColor("{}"
		+ plugin.getDescription().getName() + " Reloaded."));
    }

    @Command(aliases = { "remove", "r" }, desc = "Removes an item from the shop", usage = "<id>[:<subType>]", min = 1, max = 1)
    @CommandPermissions("items.remove")
    public static void remove(CommandContext args, DynamicMarket plugin,
	    CommandSender sender) {
	Shop shop;
	Product product;
	try {
	    shop = plugin.getMarket().getShop(((Player) sender).getLocation()); // throws IllegalArgumentException, Iff no shop at the player's location.
	    MaterialData data = Util.getMaterialData(args.getString(0)); // throws IllegalArgumentException, If id is not a valid MaterialData.
	    product = shop.getProduct(data); // throws IllegalArgumentException, Iff shop doesn't sell data.
	} catch (IllegalArgumentException e) {
	    sender.sendMessage(Message.parseColor("{ERR}" + e.getMessage()));
	    return;
	}

	shop.remove(product);
	plugin.getDatabase().delete(product);

	Message.send(sender, "{}" + args.getString(0)
		+ " is no longer sold at " + shop.getName() + ".");
    }

    @Command(aliases = { "sell", "s" }, desc = "Sells an item to the store", usage = "<itemID>[:<subType>] [amount]", min = 1, max = 2)
    @CommandPermissions("sell")
    public static void sell(CommandContext args, DynamicMarket plugin,
	    CommandSender sender) {
	sellOrBuyAction(args, plugin, sender, true);
    }

    private static void sellOrBuyAction(CommandContext args,
	    DynamicMarket plugin, CommandSender sender, boolean sell) {
	// TODO: 1.add better exception handling
	try {
	    // throws NumberFormatException, iff args.getString(1) is not a valid Integer.
	    int amount = (args.argsLength() == 2 ? args.getInteger(1) : 1);
	    if (sell) {
		amount *= -1;
	    }
	    new Transaction(plugin, amount, (Player) sender, args.getString(0));
	} catch (NumberFormatException e) {
	    sender.sendMessage(Message.parseColor("{ERR}" + args.getString(1)
		    + " is not a valid amount!"));
	}
    }

    @SuppressWarnings("boxing")
    @Command(aliases = { "update", "u" }, desc = "Updates an item at the shop", usage = "<id>[:<bundleSize>] [buyPrice] [sellPrice] [tagList]", min = 1, max = 12)
    @CommandPermissions("items.update")
    public static void update(CommandContext args, DynamicMarket plugin,
	    CommandSender sender) {
	Product product;
	Map<String, String> properties;
	try {
	    Shop shop = plugin.getMarket().getShop(
		    ((Player) sender).getLocation()); // throws IllegalArgumentException, Iff no shop at the player's location.
	    MaterialData data = Util.getMaterialData(args.getString(0)); // throws IllegalArgumentException, If id is not a valid MaterialData.
	    product = shop.getProduct(data); // throws IllegalArgumentException, Iff shop doesn't sell data.
	    properties = Util.getProperties(args.getSlice(2)); // throws IllegalArgumentException, Iff an argument is not a valid property.
	} catch (IllegalArgumentException e) {
	    sender.sendMessage(Message.parseColor("{ERR}" + e.getMessage()));
	    return;
	}

	try {
	    if (properties.containsKey("bundlesize")) {
		product.setBundleSize(Format.parseInteger(properties
			.get("bundlesize")));
	    }
	    if (properties.containsKey("buyable")) {
		product.setBuyable(Format.parseBoolean(properties
			.get("buyable")));
	    }
	    if (properties.containsKey("sellable")) {
		product.setSellable(Format.parseBoolean(properties
			.get("sellable")));
	    }
	    if (properties.containsKey("baseprice")) {
		product.setBasePrice(Format.parseDouble(properties
			.get("baseprice")));
	    }
	    if (properties.containsKey("maxprice")) {
		product.setMaxPrice(Format.parseDouble(properties
			.get("maxprice")));
	    }
	    if (properties.containsKey("minprice")) {
		product.setMinPrice(Format.parseDouble(properties
			.get("minprice")));
	    }
	    if (properties.containsKey("salestax")) {
		product.setMarkup(Format.parseDouble(properties.get("salestax")));
	    }
	    if (properties.containsKey("volatility")) {
		product.setVolatility(Format.parseDouble(properties
			.get("volatility")));
	    }
	    if (properties.containsKey("stock")) {
		product.setStock(Format.parseInteger(properties.get("stock")));
	    }
	    if (properties.containsKey("maxstock")) {
		product.setMaxStock(Format.parseInteger(properties
			.get("maxstock")));
	    }
	    if (properties.containsKey("minstock")) {
		product.setMinStock(Format.parseInteger(properties
			.get("minstock")));
	    }
	} catch (NumberFormatException e) {
	    Message.send(sender,
		    "{ERR}Invalid flags; some of the updates may have been successful!");
	    // Finally will still try to save any changes.
	    return;
	} finally {
	    plugin.getDatabase().update(product); // We must still save the product, in case earlier flags were successful.
	}

	Message.send(sender, "{}" + args.getString(0) + " updated.");
    }

    @Command(aliases = { "area", "a" }, desc = "Go into Shop area mode", usage = "<shopid>", min = 1, max = 12)
    @CommandPermissions("items.update")
    public static void area(CommandContext args, DynamicMarket plugin,
	    CommandSender sender) {
	((Player) sender).sendMessage("update");
	// TODO: dont create new listener if exists
	DynamicMarketMasterShopAreaListener.INSTANCE
		.addListener((Player) sender);
    }

    @Deprecated
    @Command(aliases = { "importold" }, desc = "Imports a .csv in the original format.", min = 0, max = 0)
    @CommandPermissions("admin")
    public static void importOldDB(
	    CommandContext args,
	    DynamicMarket plugin, CommandSender sender) {
	plugin.log(Level.INFO, sender.getName()
		+ " has issued the importOld command; importing.");
	try {
	    IO.importOld(plugin,
		    plugin.getSetting(Setting.IMPORT_EXPORT_PATH, String.class)
			    + "shopDB.csv");
	} catch (IOException e) {
	    plugin.log(Level.WARNING, e.getMessage());
	    return;
	}
	plugin.log(Level.INFO, "Import successful.");
	sender.sendMessage("Import successful.");
    }
}
