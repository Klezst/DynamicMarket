package dynamicmarket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;

import bukkitutil.BukkitUtilJavaPlugin;
import bukkitutil.compatibility.Permission;
import bukkitutil.configuration.InvalidSettingsException;
import bukkitutil.configuration.Settings;
import bukkitutil.configuration.Validatable;
import bukkitutil.util.Logging;
import bukkitutil.util.Util;

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
    public static DynamicMarket INSTANCE;

    private Market market;
    private MyDatabase database;
    private Settings settings;
    private CommandsManager<CommandSender> commandsManager;

    public DynamicMarket() {
	super("[DynamicMarket]");
    }

    /**
     * Disables the plugin.
     * NOTE: This will still return to the calling method and finish execution!
     * 
     * @param errors
     * 		Messages to log.
     */
    private void disable(String errors) {
	this.log(Level.SEVERE, errors);
	this.getServer().getPluginManager().disablePlugin(this);
    }
    
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
		getSetting(Setting.DRIVER, String.class),
		getSetting(Setting.URL, String.class),
		getSetting(Setting.USERNAME, String.class),
		getSetting(Setting.PASSWORD, String.class),
		getSetting(Setting.ISOLATION, String.class),
		getSetting(Setting.LOGGING, Boolean.class), false // If an update to database structure is done, a function to determine whether or not to rebuild is be written.
		);
	this.database.getDatabase().getAdminLogging().setLogLevel(LogLevel.SQL);
    }

    @Override
    public void onDisable() {
	log(Level.INFO, "Disabled."); // This cannot be migrated to logs.yml because, it still executes, if Log fails to validate.
    }

    @Override
    public void onEnable() {
	PluginManager pm = getServer().getPluginManager();

	DynamicMarket.INSTANCE = this; // TODO: Remove this variable.

	// Setup commands.
	this.commandsManager = new CommandsManager<CommandSender>() {
	    @Override
	    public boolean hasPermission(CommandSender sender, String permission) {
		return Permission.hasPermission(sender, "dynamicmarket."
			+ permission);
	    }
	};
	this.commandsManager.register(Commands.class);

	// Set up directory.
	getDataFolder().mkdirs();

	// Extract files.
	try {
	    bukkitutil.util.IO.extract(this, "config.yml", "logs.yml", "messages.yml",
		    "shops.csv", "LICENSE.txt");
	} catch (IOException e) {
	    this.disable("Error extracting resources; disabling.");
	    e.printStackTrace();
	    return;
	}

	// Load & validate log configuration.
	String errors = Log.validate();
	if (errors != null) {
	    disable(errors);
	    return;
	}
	
	// Load & validate messages.
	try {
	    new Settings(Message.getConfig(), Message.values());
	} catch (InvalidSettingsException e) {
	    log(Level.SEVERE, "Invalid messages.yml:");
	    e.printExceptions(Logging.getLogger(), "["
		    + getDescription().getName() + "]\t");
	    pm.disablePlugin(this);
	    return;
	}
	
	// Load & validate settings.
	try {
	    this.settings = new Settings(getConfig(), Setting.values());
	} catch (InvalidSettingsException e) {
	    log(Level.SEVERE, "Invalid config.yml:");
	    e.printExceptions(Logging.getLogger(), "["
		    + getDescription().getName() + "]\t");
	    pm.disablePlugin(this);
	    return;
	}
	
	// Setup & load database.
	initializeDatabase();
	List<Shop> shops = getDatabase().find(Shop.class).findList();
	if (shops.size() == 0) {
	    log(Level.INFO, "Empty database; loading defaults from shops.csv.");
	    this.market = new Market();
	    if (!importDB()) {
		this.disable("Database import failed on first run!\n\tTry deleting plugins/DynamicMarket/shops.csv.");
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

    public <T> T getSetting(Validatable setting, Class<T> type) {
	return this.settings.getSetting(setting, type);
    }

    public boolean importDB() {
	List<Shop> shops;
	try {
	    shops = IO.inhaleFromCSV(
		    getSetting(Setting.IMPORT_EXPORT_PATH, String.class),
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