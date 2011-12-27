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

package dynamicmarket.event;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandPermissions;

import dynamicmarket.core.DynamicMarket;
import dynamicmarket.core.Product;
import dynamicmarket.core.Shop;
import dynamicmarket.core.Transaction;
import dynamicmarket.data.IO;
import dynamicmarket.data.Message;
import dynamicmarket.data.Messaging;
import dynamicmarket.data.Setting;
import dynamicmarket.util.Format;
import dynamicmarket.util.Permission;
import dynamicmarket.util.Util;

public class ShopCommands // TODO: All shop modification/creation/deletion commands.
{
    @Command(aliases = { "add", "a" }, desc = "Adds an item to the shop",
    // Currently one cannot issue a command this long.
    usage = "<id>[:<subType>] [bundleSize|basePrice|maxPrice|minPrice|salesTax|volatility|stock|maxStock|minStock:<value>|buyable<true|false>|sellable<true|false>]", min = 1, max = 12)
    @CommandPermissions("items.add")
    public static void add(CommandContext args, DynamicMarket plugin,
	    CommandSender sender) {
	// TODO: over id
	try {
	    // throws DynamicMarketExceotion, iff args is not a valid Product.
	    Shop shop = plugin.getMarket().getShop(
		    ((Player) sender).getLocation());
	    // throws DynamicMarketExceotion, iff args is not a valid Product.
	    Product product = Product.parseProduct(args);
	    shop.addProduct(product);
	    // Update database if valid
	    plugin.getDatabase().save(product);
	    Messaging.send(sender, "{}" + args.getString(0)
		    + " is now for sale at " + shop.getName() + ".");
	} catch (DynamicMarketException e) {
	    Messaging.send(sender, "{ERR}" + e.getMessage());
	    return;
	}
    }

    @Command(aliases = { "buy", "b" }, desc = "Purchases an item from the store", usage = "<itemID>[:<subType>] [amount]", min = 1, max = 2)
    @CommandPermissions("buy")
    public static void buy(CommandContext args, DynamicMarket plugin,
	    CommandSender sender) {
	sellOrBuyAction(args, plugin, sender, false);
    }

    @Command(aliases = { "export", "exportdb" }, desc = "Saves the database to the shopDB.csv", min = 0, max = 0)
    @CommandPermissions("admin")
    public static void exportDB(CommandContext args, DynamicMarket plugin,
	    CommandSender sender) {
	plugin.log(Level.INFO, sender.getName()
		+ " has issued the exportDB command; exporting.");
	try {
	    IO.dumpToCSV(
		    plugin.getSetting(Setting.IMPORT_EXPORT_PATH, String.class),
		    "shops.csv", plugin.getMarket());
	} catch (IOException e) {
	    plugin.log(Level.WARNING, e.getMessage());
	    Messaging.send(sender, "{ERR}Export FAILED!");
	    return;
	}
	Messaging.send(sender, "{}Export successfull.");
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

	    sender.sendMessage("/shop help [topic]");
	    sender.sendMessage("Displays information about the topic.");
	    // TODO: Add generic help message to Message and message.yml.

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
		commands += " export import";
	    }

	    topics += "ids details about";
	    if (Permission.hasPermission(sender, "items.add")
		    || Permission.hasPermission(sender, "items.update")) {
		topics += " tags";
	    }

	    sender.sendMessage("Commands: " + commands); // TODO: Add to Message and messages.yml under HELP?.
	    sender.sendMessage("Shortcuts: " + shortcuts);
	    sender.sendMessage("Other help topics: " + topics);

	    return;
	}
	sender.sendMessage("----------" + plugin.getDescription().getName()
		+ "----------");

	if (topic.equalsIgnoreCase("buy")) {
	    Message.HELP_BUY.send(sender);
	    return;
	}
	if (topic.equalsIgnoreCase("sell")) {
	    Message.HELP_SELL.send(sender);
	    return;
	}
	if (topic.equalsIgnoreCase("info")) {
	    Message.HELP_INFO.send(sender);
	    return;
	}
	if (topic.equalsIgnoreCase("add")) {
	    Message.HELP_ADD.send(sender);
	    return;
	}
	if (topic.equalsIgnoreCase("update")) {
	    Message.HELP_UPDATE.send(sender);
	    return;
	}
	if (topic.equalsIgnoreCase("remove")) {
	    Message.HELP_REMOVE.send(sender);
	    return;
	}
	if (topic.equalsIgnoreCase("reload")) {
	    Message.HELP_RELOAD.send(sender);
	    return;
	}
	if (Util.isAny(topic, "export", "exportdb")) {
	    Message.HELP_EXPORT.send(sender);
	    return;
	}
	if (Util.isAny(topic, "import", "importdb")) {
	    Message.HELP_IMPORT.send(sender);
	    return;
	}
	if (topic.equalsIgnoreCase("ids")) {
	    Message.HELP_IDS.send(sender);
	    return;
	}
	if (topic.equalsIgnoreCase("list")) {
	    Message.HELP_LIST.send(sender);
	    return;
	}
	if ((Util.isAny(topic.split(" ")[0], "tag", "tags"))
		&& ((Permission.hasPermission(sender, "items.add") || Permission
			.hasPermission(sender, "items.update")))) {
	    if (topic.indexOf(" ") > -1) {
		// Possible tag listed!
		String thisTag = topic.split(" ")[1].replace(":", "");
		if (Util.isAny(thisTag, "bp", "baseprice")) {
		    Message.HELP_TAG_BASEPRICE.send(sender);
		    return;
		}
		if (Util.isAny(thisTag, "s", "stock")) {
		    Message.HELP_TAG_STOCK.send(sender);
		    return;
		}
		if (Util.isAny(thisTag, "cb", "buyable", "canbuy")) {
		    Message.HELP_TAG_BUYABLE.send(sender);
		    return;
		}
		if (Util.isAny(thisTag, "cs", "sellable", "cansell")) {
		    Message.HELP_TAG_SELLABLE.send(sender);
		    return;
		}
		if (Util.isAny(thisTag, "v", "vol", "volatility", "float")) {
		    Message.HELP_TAG_VOLATILITY.send(sender);
		    return;
		}
		if (Util.isAny(thisTag, "st", "salestax")) {
		    Message.HELP_TAG_SALESTAX.send(sender);
		    return;
		}
		if (Util.isAny(thisTag, "sf", "minStock", "stockfloor",
			"stocklowest")) {
		    Message.HELP_TAG_MIN_STOCK.send(sender);
		    return;
		}
		if (Util.isAny(thisTag, "sc", "maxStock", "stockceiling",
			"stockhighest")) {
		    Message.HELP_TAG_MAX_STOCK.send(sender);
		    return;
		}
		if (Util.isAny(thisTag, "pf", "minPrice", "pricefloor",
			"priceHighest")) {
		    Message.HELP_TAG_MIN_PRICE.send(sender);
		    return;
		}
		if (Util.isAny(thisTag, "pc", "maxPrice", "priceceiling",
			"priceHighest")) {
		    Message.HELP_TAG_MAX_PRICE.send(sender);
		    return;
		}

		// TODO: Implement these flags. Don't forget to add the messages to Message and messages.yml.
		/* if (Util.isAny(thisTag, "flat", "fixed")) { Messaging .send(sender, // The recipient. "{}Use {CMD}/shop update {PRM}<itemID> {CMD}volatility{BKT}:{PRM}0",
		 * "{} This will make the products price flat (not change based on stock levels)."); return; } if (thisTag.equalsIgnoreCase("finite")) { Messaging .send(sender, // The recipient.
		 * "{}Use {CMD}/shop update {PRM}<itemID> {CMD}minStock{BKT}:{PRM}0", "{} Buying from shop will fail, if it would make {PRM}Stock{} < 0.",
		 * "{} Any number of items can be sold to the shop still."); return; } if (thisTag.equalsIgnoreCase("renorm")) { Messaging .send(sender, // The recipient.
		 * "{CMD}update {PRM}<itemID> {CMD}renorm{} - Renormalize an item's price.", "{} Resets an item's {PRM}Stock{}, while preserving its current price.",
		 * "{} Sets an item's {PRM}BasePrice{} to its current {PRM}BuyPrice,", "{} and sets it's {PRM}Stock{} to 0."); return; } */

		sender.sendMessage("Unknown tag" + thisTag + "."); // TODO: Add to messages.yml and Message.
		sender.sendMessage("Use /shop help tags to list tags.");
		return;
	    }
	    sender.sendMessage("Tag format: <tagName>:<value> <tagName>:<value>..."); // TODO: Add to messages.yml and Message.
	    sender.sendMessage("Available tags: basePrice, salesTax, canBuy, canSell, volitility,");
	    sender.sendMessage("stock, maxStock, minStock, maxPrice, minPrice, buyable, sellable.");
	    sender.sendMessage("Use /shop help tag <tagName> for tag descriptions.");
	    return;
	}
	if (topic.equalsIgnoreCase("about")) {
	    Messaging.send(sender, "{} " + plugin.getDescription().getName()
		    + " " + plugin.getDescription().getVersion(),
		    "{}Authors: {PRM}" + plugin.getDescription().getAuthors()
			    + ".");
	    return;
	}
	sender.sendMessage("Unknown help topic: " + topic); // TODO: Add to messages.yml and Message.
	sender.sendMessage("Use /shop help to list topics.");
	return;
    }

    @Command(aliases = { "import", "importdb" }, desc = "Loads the database from the shopDB.csv", min = 0, max = 0)
    @CommandPermissions("admin")
    public static void importDB(CommandContext args, DynamicMarket plugin,
	    CommandSender sender) {
	if (plugin.importDB()) {
	    sender.sendMessage("Import successful."); // TODO: Add to messages.yml and Message.
	} else {
	    sender.sendMessage("Import FAILED!");
	}
    }

    @Command(aliases = { "info", "i", "about" }, desc = "Displays information about an item", usage = "[itemID]", min = 0, max = 1)
    public static void info(CommandContext args, DynamicMarket plugin,
	    CommandSender sender) {
	if (args.argsLength() == 0) // Show plugin info.
	{
	    sender.sendMessage(plugin.getDescription().getFullName()
		    + " Copyright (C) 2011 Klezst");
	    sender.sendMessage("Authors: "
		    + plugin.getDescription().getAuthors());
	} else // Show item info.
	{
	    if (sender instanceof Player) {
		Product product;
		try {
		    // throws DynamicMarketException, Iff no shop at the player's location.
		    Shop shop = plugin.getMarket().getShop(
			    ((Player) sender).getLocation());
		    // throws DynamicMarketException, If id is not a valid MaterialData.
		    MaterialData data = Util.getMaterialData(args.getString(0));
		    // throws DynamicMarketException, Iff shop doesn't sell data.
		    product = shop.getProduct(data);
		} catch (DynamicMarketException e) {
		    sender.sendMessage(e.getMessage());
		    return;
		}
		Messaging.send(sender, product.toString());
	    } else {
		sender.sendMessage("You must be logged in to issue this command");
	    }
	}
    }

    @SuppressWarnings("boxing")
    @Command(aliases = { "list", "l" }, desc = "Lists items for sale", usage = "[filter] [page]", min = 0, max = 2)
    public static void list(CommandContext args, DynamicMarket plugin,
	    CommandSender sender) {
	try {
	    Shop shop = plugin.getMarket().getShop(
		    ((Player) sender).getLocation());
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
			    Messaging.send(sender, "{ERR}" + args.getString(1)
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
		Messaging.send(sender,
			"{ERR}You must specify a positive page number!");
		return;
	    }

	    int bound = Math.min(page * 8, lines.length);
	    int startIndex = (page - 1) * 8;

	    if (startIndex > bound) {
		Messaging.send(sender, "{ERR}There aren't that many pages!");
		return;
	    }

	    lines = Arrays.copyOfRange(lines, startIndex, bound);

	    if (lines.length == 0) {
		Messaging.send(sender, "{ERR}No such products found.");
		return;
	    }

	    Messaging.send(sender, lines);
	} catch (DynamicMarketException e) {
	    e.printStackTrace();
	}
    }

    @Command(aliases = { "reload" }, desc = "Restarts the plugin", min = 0, max = 0)
    @CommandPermissions("admin")
    public static void reload(CommandContext args, DynamicMarket plugin,
	    CommandSender sender) {
	plugin.log(Level.INFO, sender.getName()
		+ " has issued the reload command; reloading.");
	plugin.onDisable();
	plugin.onEnable();
	sender.sendMessage(plugin.getDescription().getName() + " Reloaded."); // TODO: Add to messages.yml and Message.
    }

    @Command(aliases = { "remove", "r" }, desc = "Removes an item from the shop", usage = "<id>[:<subType>]", min = 1, max = 1)
    @CommandPermissions("items.remove")
    public static void remove(CommandContext args, DynamicMarket plugin,
	    CommandSender sender) {
	try {
	    // throws DynamicMarketException, Iff no shop at the player's location.
	    Shop shop = plugin.getMarket().getShop(
		    ((Player) sender).getLocation());
	    // throws DynamicMarketException, If id is not a valid MaterialData.
	    MaterialData data = Util.getMaterialData(args.getString(0));
	    // throws DynamicMarketException, Iff shop doesn't sell data.
	    Product product = shop.getProduct(data);
	    shop.remove(product);
	    plugin.getDatabase().delete(product);

	    Messaging.send(sender, args.getString(0) + " is no longer sold at "
		    + shop.getName() + "."); // TODO: Add to messages.yml and Message.
	} catch (DynamicMarketException e) {
	    sender.sendMessage(e.getMessage());
	    return;
	}
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
	    sender.sendMessage(args.getString(1) + " is not a valid amount!"); // TODO: Add to messages.yml and Message.
	}
    }

    @SuppressWarnings("boxing")
    @Command(aliases = { "update", "u" }, desc = "Updates an item at the shop", usage = "<id>[:<bundleSize>] [buyPrice] [sellPrice] [tagList]", min = 1, max = 12)
    @CommandPermissions("items.update")
    public static void update(CommandContext args, DynamicMarket plugin,
	    CommandSender sender) {
	try {
	    // throws DynamicMarketException, Iff no shop at the player's location.
	    Shop shop = plugin.getMarket().getShop(
		    ((Player) sender).getLocation());
	    // throws DynamicMarketException, If id is not a valid MaterialData.
	    MaterialData data = Util.getMaterialData(args.getString(0));
	    // throws DynamicMarketException, Iff shop doesn't sell data.
	    Product product = shop.getProduct(data);
	    // throws DynamicMarketException, Iff an argument is not a valid property.
	    Map<String, String> properties = Util.getProperties(args
		    .getSlice(2));

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
	    Messaging.send(sender, "{}" + args.getString(0) + " updated.");
	} catch (DynamicMarketException e) {
	    sender.sendMessage(e.getMessage());
	} catch (NumberFormatException e) {
	    Messaging
		    .send(sender,
			    "{ERR}Invalid flags; some of the updates may have been successful!");
	} finally {
	    // TODO: check if we WANT to update, make no sense for me - IDragonfire
	    // Finally will still try to save any changes.
	    // plugin.getDatabase().update(product);
	    // We must still save the product, in case earlier flags were successful.
	}
    }

    @Command(aliases = { "area", "a" }, desc = "Go into Shop area mode", usage = "<shopid>", min = 1, max = 12)
    @CommandPermissions("items.update")
    public static void area(CommandContext args, DynamicMarket plugin,
	    CommandSender sender) {
	if (args.argsLength() == 1) {
	    try {
		Shop shop = plugin.getMarket().getShopById(args.getInteger(0));
		if (shop != null) {
		    DynamicMarketMasterShopAreaListener.INSTANCE.addListener(
			    (Player) sender, shop);
		} else {
		    // TODO 0.Message System
		    sender.sendMessage("shop doesn't exists");
		}
	    } catch (Exception e) {
		// TODO 0.Message System
		e.printStackTrace();
		sender.sendMessage("no valid shop id");
	    }
	} else {
	    // TODO 0.Message System
	    sender.sendMessage("enter only one shopid");
	}
    }

    @Deprecated
    @Command(aliases = { "importold" }, desc = "Imports a .csv in the original format.", min = 0, max = 0)
    @CommandPermissions("admin")
    public static void importOldDB(CommandContext args, DynamicMarket plugin,
	    CommandSender sender) {
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
