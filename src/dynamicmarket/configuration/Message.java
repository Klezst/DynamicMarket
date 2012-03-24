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

package dynamicmarket.configuration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import bukkitutil.configuration.Validatable;
import bukkitutil.util.Messaging;

/**
 * Handles messaging.
 * 
 * @author Klezst
 */
public enum Message implements Validatable<String> {
    // Alphanumeric order.
    ADD_SUCCESS("add.success"),
    BUY_TOO_MUCH("buy.too_much"),
    BUY_LOW_STOCK("buy.low_stock"),
    EXPORT_FAILURE("export.failure"),
    EXPORT_SUCCESS("export.success"),
    HELP_ADD("help.add"),
    HELP_BUY("help.buy"),
    HELP_DEFAULT("help.default"),
    HELP_EXPORT("help.export"),
    HELP_IDS("help.ids"),
    HELP_IMPORT("help.import"),
    HELP_INFO("help.info"),
    HELP_LIST("help.list"),
    HELP_RELOAD("help.reload"),
    HELP_REMOVE("help.remove"),
    HELP_SELL("help.sell"),
    HELP_TAG_BASEPRICE("help.tag.baseprice"),
    HELP_TAG_BUYABLE("help.tag.buyable"),
    HELP_TAG_DEFAULT("help.tag.default"),
    HELP_TAG_INVALID("help.tag.invalid"),
    HELP_TAG_MAX_PRICE("help.tag.maxstock"),
    HELP_TAG_MAX_STOCK("help.tag.maxstock"),
    HELP_TAG_MIN_PRICE("help.tag.minprice"),
    HELP_TAG_MIN_STOCK("help.tag.minstock"),
    HELP_TAG_SALESTAX("help.tag.salestax"),
    HELP_TAG_SELLABLE("help.tag.buyable"),
    HELP_TAG_STOCK("help.tag.stock"),
    HELP_TAG_VOLATILITY("help.tag.volatility"),
    HELP_UPDATE("help.update"),
    IMPORT_FAILURE("import.failure"),
    IMPORT_SUCCESS("import.success"),
    LIST_NO_RESULTS("list.no_results"),
    LIST_PAGE_NON_NUMERIC("list.page.non_numeric"),
    LIST_PAGE_NEGATIVE("list.page.negative"),
    LIST_PAGE_TOO_HIGH("list.page.too_high"),
    RELOAD("reload"),
    REMOVE_SUCCESS("remove.success"),
    SELL_NO_SPACE("sell.no_space"),
    TRANSACTION_AMOUNT_NON_NUMERIC("transaction.amount.non_numeric"),
    UPDATE_SUCCESS("update.success"),
    UPDATE_FLAG_NON_NUMERIC("update.flag.non_numeric");
    
    public static final String FILEPATH = "plugins/DynamicMarket/messages.yml";

    private String key;
    private String message;

    private Message(String key) {
	this.key = key;
	this.message = null;
    }

    public static FileConfiguration getConfig() {
	return YamlConfiguration.loadConfiguration(new File(FILEPATH));
    }
    
    public String getKey() {
	return this.key;
    }
    
    public String[] getKeys() {
	String[] keys = {this.key};
	return keys;
    }

    public String getMessage() {
	return this.message;
    }

    public Class<?> getType() {
	return String.class;
    }
    
    /**
     * Sends the Message to sender.
     * 
     * @param sender
     *            Who to send the Message to.
     * 
     * @throws NullPointerException
     *             If sender is null.
     * 
     * @author Klezst
     */
    public void send(CommandSender sender) throws NullPointerException {
	send(sender, new HashMap<String, String>());
    }

    /**
     * Sends the Message to sender in the context provided.
     * 
     * @param sender
     *            Who to send the Message to.
     * @param context
     *            A Map of keywords to replace with values.
     * 
     * @throws NullPointerException
     *             If sender is null or context is null.
     * 
     * @author Klezst
     */
    public void send(CommandSender sender, Map<String, String> context)
	    throws NullPointerException {
	String msg = Messaging.replace(this.message, context, "$", "$");

	for (String line : msg.split("\n")) {
	    sender.sendMessage(line);
	}
    }
    
    public String set(String key, String value) {
	this.message = value.toString();
	return null;
    }
}
