package dynamicmarket.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.PluginManager;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.LogLevel;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissionsException;
import com.sk89q.minecraft.util.commands.CommandUsageException;
import com.sk89q.minecraft.util.commands.CommandsManager;
import com.sk89q.minecraft.util.commands.MissingNestedCommandException;
import com.sk89q.minecraft.util.commands.WrappedCommandException;

import dynamicmarket.data.IO;
import dynamicmarket.data.MyDatabase;
import dynamicmarket.data.Setting;
import dynamicmarket.data.Settings;
import dynamicmarket.data.Validatable;
import dynamicmarket.event.Commands;
import dynamicmarket.event.DynamicMarketMasterShopAreaListener;
import dynamicmarket.event.InvalidSettingsException;
import dynamicmarket.util.BukkitUtilJavaPlugin;
import dynamicmarket.util.Permission;
import dynamicmarket.util.Util;

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

    // Method template by LennardF1989
    @Override
    public EbeanServer getDatabase() {
	return this.database.getDatabase();
    }

    // Method template by LennardF1989
    @SuppressWarnings("boxing")
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
	log(Level.INFO, "Disabled.");
    }

    @Override
    public void onEnable() {
	PluginManager pm = getServer().getPluginManager();

	DynamicMarket.INSTANCE = this;
	pm.registerEvent(Event.Type.BLOCK_BREAK,
		DynamicMarketMasterShopAreaListener.INSTANCE, Priority.Normal,
		this);

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
	    IO.extract(this, "config.yml", "messages.yml", "shops.csv",
		    "LICENSE.txt");
	} catch (IOException e) {
	    log(Level.SEVERE, "Error extracting resources; disabling.");
	    e.printStackTrace();
	    pm.disablePlugin(this);
	    return;
	}

	// Load & Validate settings.
	try {
	    this.settings = new Settings(getConfig(), Setting.values());
	} catch (InvalidSettingsException e) {
	    log(Level.SEVERE, "Invalid config.yml:");
	    e.printExceptions(BukkitUtilJavaPlugin.logger, "["
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
		log(Level.SEVERE, "Database import failed on first run!");
		log(Level.INFO,
			"\tTry deleting plugins/DynamicMarket/shops.csv.");
		pm.disablePlugin(this);
		return;
	    }
	} else {
	    this.market = new Market(shops);
	}

	log(Level.INFO, "Enabled.");
    }

    @Deprecated
    private void checkLibs() {
	boolean isok = false;

	File a = new File(getDataFolder() + "/sqlitejdbc-v056.jar");
	if (!a.exists()) {
	    isok = IO.fileDownload(
		    "http://www.brisner.no/libs/sqlitejdbc-v056.jar",
		    getDataFolder().toString());
	    if (isok) {
		log(Level.INFO, "Downloaded SQLite Successfully.");
	    }
	}

	File b = new File(getDataFolder()
		+ "/mysql-connector-java-5.1.15-bin.jar");
	if (!b.exists()) {
	    isok = IO
		    .fileDownload(
			    "http://www.brisner.no/libs/mysql-connector-java-5.1.15-bin.jar",
			    getDataFolder().toString());
	    if (isok) {
		log(Level.INFO, "Downloaded MySQL Successfully.");
	    }
	}

	File c = new File(getDataFolder() + "/items.db");
	if (!c.exists()) {
	    isok = IO.fileDownload(
		    "http://www.brisner.no/DynamicMarket/items.db",
		    getDataFolder().toString());
	    if (isok) {
		log(Level.INFO, "Downloaded items.db successfully");
	    }
	}
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

	log(Level.INFO, "Import successful.");
	return true;
    }
}