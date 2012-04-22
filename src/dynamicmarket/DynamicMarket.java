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

package dynamicmarket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.gmail.klezst.bukkit.bukkitutil.BukkitUtilJavaPlugin;
import com.gmail.klezst.bukkit.bukkitutil.compatibility.Permission;
import com.gmail.klezst.bukkit.bukkitutil.configuration.Validation;
import com.gmail.klezst.bukkit.bukkitutil.util.Messaging;
import com.gmail.klezst.bukkit.bukkitutil.util.Util;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.LogLevel;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissionsException;
import com.sk89q.minecraft.util.commands.CommandUsageException;
import com.sk89q.minecraft.util.commands.CommandsManager;
import com.sk89q.minecraft.util.commands.MissingNestedCommandException;
import com.sk89q.minecraft.util.commands.WrappedCommandException;

import dynamicmarket.command.Commands;
import dynamicmarket.configuration.Log;
import dynamicmarket.configuration.Message;
import dynamicmarket.configuration.Setting;
import dynamicmarket.util.IO;
import dynamicmarket.util.MyDatabase;

public class DynamicMarket extends BukkitUtilJavaPlugin {
    public static final double DDM_MAXVALUE = 999999999.99;

    private Market market;
    private MyDatabase database;
    private CommandsManager<CommandSender> commandsManager;
    
    // Method template by LennardF1989
    @Override
    public EbeanServer getDatabase() {
	return this.database.getDatabase();
    }

    // Method template by LennardF1989
    private void initializeDatabase() {
	this.database = new MyDatabase(this) {
	    @Override
	    protected java.util.List<Class<?>> getDatabaseClasses() {
		List<Class<?>> list = new ArrayList<Class<?>>();
		list.add(Shop.class);
		list.add(Product.class);
		list.add(Transaction.class);

		return list;
	    }
	};
	this.database.initializeDatabase(
		Setting.DRIVER.getValue(String.class),
		Setting.URL.getValue(String.class),
		Setting.USERNAME.getValue(String.class),
		Setting.PASSWORD.getValue(String.class),
		Setting.ISOLATION.getValue(String.class),
		Setting.LOGGING.getValue(Boolean.class), false // If an update to database structure is done, a function to determine whether or not to rebuild is be written.
		);
	this.database.getDatabase().getAdminLogging().setLogLevel(LogLevel.SQL);
    }

    @Override
    public void onDisable() {
	log(Level.INFO, "Disabled."); // Cannot go though Log, since Log necessarily is not validated yet.
    }

    @Override
    public void onEnable() {
	// Setup commands.
	this.commandsManager = new CommandsManager<CommandSender>() {
	    @Override
	    public boolean hasPermission(CommandSender sender, String permission) {
		return Permission.hasPermission(sender, "dynamicmarket."
			+ permission);
	    }
	};
	this.commandsManager.register(Commands.class);

	// Extract files.
	this.saveResource("logs.yml", false);
	this.saveResource("messages.yml", false);
	this.saveResource("settings.yml", false);
	this.saveResource("shops.csv", false);
	this.saveResource("LICENSE.txt", false);
	    
	// Load & validate logs.
	String errors = Validation.validate(Log.getConfig(), Log.values()); // Migrate paramaters to the Validatable class.
	if (!errors.isEmpty()) {
	    log(Level.SEVERE, "Invalid " + Log.FILEPATH + "; Invalid keys:", errors);
	    this.getServer().getPluginManager().disablePlugin(this);
	    return;
	}
	
	// Load & validate messages.
	errors = Validation.validate(Message.getConfig(), Message.values());
	if (!errors.isEmpty()) {
	    Log.CONFIG_INVALID_MESSAGES.log(Messaging.buildContext("errors", errors, "filepath", Message.FILEPATH));
	    this.getServer().getPluginManager().disablePlugin(this);
	    return;
	}
	
	// Load & validate settings.
	errors = Validation.validate(Setting.getConfig(), Setting.values());
	if (!errors.isEmpty()) {
	    Log.CONFIG_INVALID_SETTINGS.log(Messaging.buildContext("errors", "" + errors, "filepath", Setting.FILEPATH));
	    this.getServer().getPluginManager().disablePlugin(this);
	    return;
	}
	
	// Setup & load database.
	initializeDatabase();
	List<Shop> shops = getDatabase().find(Shop.class).findList();
	if (shops.size() == 0) {
	    log(Level.INFO, "Empty database; loading defaults from shops.csv.");
	    this.market = new Market();
	    if (!importDB()) {
		Log.IMPORT_FAILURE_INITIAL.log();
		this.getServer().getPluginManager().disablePlugin(this);
		return;
	    }
	} else {
	    this.market = new Market(shops);
	}

	log(Level.INFO, "Enabled.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd,
	    String commandLabel, String[] args) {
	if (!(sender instanceof Player) && args.length > 0
		&& !Util.isAny(args[0], "info", "importdb", "exportdb")) // TODO: Make console check an annotation for CommandsManager
	{
	    sender.sendMessage("You must be logged in to issue the "
		    + commandLabel + " command.");
	    return true;
	}

	try {
	    this.commandsManager.execute(cmd.getName(), args, sender, this,
		    sender);
	} catch (CommandPermissionsException e) {
	    sender.sendMessage("You don't have permission"); // TODO: Add to messages.yml and Message.
	} catch (MissingNestedCommandException e) {
	    sender.sendMessage(e.getUsage());
	} catch (CommandUsageException e) {
	    sender.sendMessage(e.getMessage());
	    sender.sendMessage(e.getUsage());
	} catch (WrappedCommandException e) {
	    e.printStackTrace();
	} catch (CommandException e) {
	    sender.sendMessage(e.getMessage());
	}

	return true;
    }

    // Access Methods
    public Market getMarket() {
	return this.market;
    }

    public boolean importDB() {
	List<Shop> shops;
	try {
	    shops = IO.inhaleFromCSV(
		    Setting.IMPORT_EXPORT_PATH.getValue(String.class),
		    "shops.csv"); // throws IOException
	} catch (IOException e) {
	    log(Level.WARNING, e.getMessage());
	    return false;
	}

	// Remove old shops.
	List<Shop> old = this.market.getShops();
	for (Shop shop : old) {
	    getDatabase().delete(shop);
	}

	// Add new shops.
	this.market = new Market(shops);
	for (Shop shop : shops) {
	    getDatabase().save(shop);
	}
	return true;
    }
}