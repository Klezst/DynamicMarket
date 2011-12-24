package com.gmail.haloinverse.DynamicMarket;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.avaje.ebean.EbeanServer;
import com.gmail.haloinverse.DynamicMarket.commands.Commands;
import com.gmail.haloinverse.DynamicMarket.util.IO;
import com.gmail.haloinverse.DynamicMarket.util.Message;
import com.gmail.haloinverse.DynamicMarket.util.Util;
import com.gmail.klezst.util.settings.InvalidSettingsException;
import com.gmail.klezst.util.settings.Settings;
import com.gmail.klezst.util.settings.Validatable;
import com.idragonfire.event.DynamicMarketMasterShopAreaListener;
import com.lennardf1989.bukkitex.MyDatabase;
import com.sk89q.bukkit.migration.PermissionsResolverManager;
import com.sk89q.bukkit.migration.PermissionsResolverServerListener;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissionsException;
import com.sk89q.minecraft.util.commands.CommandUsageException;
import com.sk89q.minecraft.util.commands.CommandsManager;
import com.sk89q.minecraft.util.commands.MissingNestedCommandException;
import com.sk89q.minecraft.util.commands.WrappedCommandException;

public class DynamicMarket extends JavaPlugin {
    private static final Logger log = Logger.getLogger("Minecraft");
    public static final double DDM_MAXVALUE = 999999999.99;
    public static DynamicMarket INSTANCE;

    private Market market;
    private MyDatabase database;
    private Settings settings;
    private Object permissionsManager; // PermissionsResolverManager (Must be Object to prevent NoClassFoundException, iff WorldEdit isn't present).
    private Object commandsManager; // CommandsManager (Must be Object to prevent NoClassFoundException, iff WorldEdit isn't present).

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
    }

    @Override
    public void onDisable() {
	log(Level.INFO, "Disabled.");
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onEnable() {
	PluginManager pm = getServer().getPluginManager();
	
	DynamicMarket.INSTANCE = this;
	pm.registerEvent(Event.Type.BLOCK_BREAK, DynamicMarketMasterShopAreaListener.INSTANCE, Priority.Normal, this);
	
	// Check for WorldEdit (dependency).
	try {
	    // Setup permissions.
	    // TODO 9. use vault permissions
	    this.permissionsManager = new PermissionsResolverManager(this,
		    getDescription().getName(), Logger.getLogger("Minecraft."
			    + getDescription().getName())); // Creates our instance of WorldEdit Permissions Interoperability Framework (WEPIF)
	    new PermissionsResolverServerListener(
		    (PermissionsResolverManager) this.permissionsManager, this); // Tells WEPIF to check for changes in what permissions plugin is used.

	    // Setup commands.
	    final DynamicMarket plugin = this;
	    this.commandsManager = new CommandsManager<CommandSender>() {
		@Override
		public boolean hasPermission(CommandSender sender,
			String permission) {
		    return plugin.hasPermission(sender, permission);
		}
	    };
	    ((CommandsManager<CommandSender>) this.commandsManager)
		    .register(Commands.class);
	} catch (NoClassDefFoundError e) {
	    log(Level.SEVERE, "WorldEdit not detected");
	    pm.disablePlugin(this);
	    return;
	}

	// Set up directory.
	getDataFolder().mkdirs();

	// Extract files.
	try {
	    IO.extract(this, "config.yml", "shops.csv", "LICENSE.txt");
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
	    e.printExceptions(log, "[" + getDescription().getName() + "]\t");
	    pm.disablePlugin(this);
	    return;
	}

	// Set messaging settings.
	Message.initialize(getSetting(Setting.NORMAL_COLOR, ChatColor.class),
		getSetting(Setting.COMMAND_COLOR, ChatColor.class),
		getSetting(Setting.BRACKET_COLOR, ChatColor.class),
		getSetting(Setting.PARAM_COLOR, ChatColor.class),
		getSetting(Setting.ERROR_COLOR, ChatColor.class));

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

    @SuppressWarnings("unchecked")
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
	    ((CommandsManager<CommandSender>) this.commandsManager).execute(
		    cmd.getName(), args, sender, this, sender);
	} catch (CommandPermissionsException e) {
	    sender.sendMessage(Message.parseColor("{ERR}"
		    + "You don't have permission"));
	} catch (MissingNestedCommandException e) {
	    sender.sendMessage(Message.parseColor("{ERR}" + e.getUsage()));
	} catch (CommandUsageException e) {
	    sender.sendMessage(Message.parseColor("{ERR}" + e.getMessage()));
	    sender.sendMessage(Message.parseColor("{ERR}" + e.getUsage()));
	} catch (WrappedCommandException e) {
	    e.printStackTrace();
	} catch (CommandException e) {
	    sender.sendMessage(Message.parseColor("{ERR}" + e.getMessage()));
	}

	return true;
    }

    public void log(Level level, String... messages) {
	for (String message : messages) {
	    String[] lines = message.split("\n");
	    for (String line : lines) {
		log.log(level, "[" + getDescription().getName() + "] " + line);
	    }
	}
    }

    // Access Methods
    public Market getMarket() {
	return this.market;
    }

    public <T> T getSetting(Validatable setting, Class<T> type) {
	return this.settings.getSetting(setting, type);
    }

    // TODO 9. use vault permissions
    public boolean hasPermission(CommandSender sender, String permission) {
	return ((PermissionsResolverManager) this.permissionsManager)
		.hasPermission(sender.getName(), getDescription().getName()
			.toLowerCase() + "." + permission);
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