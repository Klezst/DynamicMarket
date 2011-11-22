package com.gmail.haloinverse.DynamicMarket;

import com.gmail.haloinverse.DynamicMarket.Setting;
import com.gmail.haloinverse.DynamicMarket.commands.Commands;
import com.gmail.haloinverse.DynamicMarket.database.DatabaseMarket;
import com.gmail.haloinverse.DynamicMarket.util.IO;
import com.gmail.haloinverse.DynamicMarket.util.Messaging;
import com.gmail.klezst.util.settings.InvalidSettingsException;
import com.gmail.klezst.util.settings.Settings;
import com.gmail.klezst.util.settings.Validatable;
import com.sk89q.bukkit.migration.PermissionsResolverManager;
import com.sk89q.bukkit.migration.PermissionsResolverServerListener;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissionsException;
import com.sk89q.minecraft.util.commands.CommandUsageException;
import com.sk89q.minecraft.util.commands.CommandsManager;
import com.sk89q.minecraft.util.commands.MissingNestedCommandException;
import com.sk89q.minecraft.util.commands.WrappedCommandException;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class DynamicMarket extends JavaPlugin
{
    private static final Logger log = Logger.getLogger("Minecraft");
    
    private Items items;
    private Shop shop;
    private DatabaseMarket db;
    private Settings settings;
    private Object permissionsManager; // PermissionsResolverManager (Must be Object to prevent NoClassFoundException, iff WorldEdit isn't present).
    private Object commandsManager; // CommandsManager (Must be Object to prevent NoClassFoundException, iff WorldEdit isn't present).
    private TransactionLogger transLog;
    
    @Override
    public void onDisable()
    {
        log(Level.INFO, "Disabled.");
    }
    
    @SuppressWarnings("unchecked")
	@Override
    public void onEnable()
    {
        PluginManager pm = getServer().getPluginManager();
    	
        // Check for Register (dependency).
        if (pm.getPlugin("Register") == null)
        {
        	log(Level.SEVERE, "Register not detected.");
        	pm.disablePlugin(this);
        	return;
        }
        
    	// Check for WorldEdit (dependency).
    	try
    	{
    		// Setup permissions.
    		permissionsManager = new PermissionsResolverManager(this, getDescription().getName(), Logger.getLogger("Minecraft." + getDescription().getName())); // Creates our instance of WorldEdit Permissions Interoperability Framework (WEPIF)
    		new PermissionsResolverServerListener((PermissionsResolverManager)permissionsManager, this); // Tells WEPIF to check for changes in what permissions plugin is used.
    		
    		// Setup commands.
    		final DynamicMarket plugin = this;
    		commandsManager = new CommandsManager<CommandSender>()
    		{
    			@Override
    			public boolean hasPermission(CommandSender sender, String permission)
    			{
    				return plugin.hasPermission(sender, permission);
    			}
   			};
   			((CommandsManager<CommandSender>)commandsManager).register(Commands.class);
    	}
    	catch (NoClassDefFoundError e)
    	{
            log(Level.SEVERE, "WorldEdit not detected");
            pm.disablePlugin(this);
            return;
    	}
    	
		// Set up directory.
    	getDataFolder().mkdirs();
    	
    	// Set up libraries.
        checkLibs();
        
        // Extract files.
        try
        {
        	IO.extract(this, "config.yml", "shopDB.csv", "LICENSE.txt");
        }
        catch (IOException e)
        {
        	log(Level.SEVERE, "Error extracting resources; disabling.");
        	e.printStackTrace();
        	pm.disablePlugin(this);
        	return;
        }
        
        // Load & Validate settings.
    	try
    	{
    		settings = new Settings(getConfig(), Setting.values());
    	}
    	catch (InvalidSettingsException e)
    	{
    		log(Level.SEVERE, "Invalid config.yml:");
    		e.printExceptions(log, "[" + getDescription().getName() + "]\t");
    		pm.disablePlugin(this);
    		return;
    	}
    	
    	// Set messaging settings.
    	Messaging.initialize
    	(
    		getSetting(Setting.NORMAL_COLOR, ChatColor.class),
    		getSetting(Setting.COMMAND_COLOR, ChatColor.class),
        	getSetting(Setting.BRACKET_COLOR, ChatColor.class),
        	getSetting(Setting.PARAM_COLOR, ChatColor.class),
        	getSetting(Setting.ERROR_COLOR, ChatColor.class)
        );
    	
        // Setup database.
        items = new Items(getSetting(Setting.ITEMS_DB_PATH, String.class) + "items.db", this);
        if (getSetting(Setting.DATABASE_TYPE, String.class).equalsIgnoreCase("mysql"))
        {
            try
            {
                Class.forName("com.mysql.jdbc.Driver");
            }
            catch (ClassNotFoundException ex)
            {
                log(Level.SEVERE, "com.mysql.jdbc.Driver class not found.");
                ex.printStackTrace();
                pm.disablePlugin(this);
                return;
            }
            db = new DatabaseMarket(DatabaseMarket.Type.MYSQL, "Market", items, getSetting(Setting.MYSQL_ENGINE, String.class), this);
        }
        else
        {
            try
            {
                Class.forName("org.sqlite.JDBC");
            }
            catch (ClassNotFoundException ex)
            {
                log(Level.SEVERE, "org.sqlite.JDBC class not found.");
                ex.printStackTrace();
                pm.disablePlugin(this);
                return;
            }
            db = new DatabaseMarket(DatabaseMarket.Type.SQLITE, "Market", items, "", this);
        }
        
        // Setup transaction log.
        transLog = new TransactionLogger(this, getDataFolder() + File.separator + getSetting(Setting.TRANSACTION_LOG_FILE, String.class), getSetting(Setting.TRANSACTION_LOG_AUTOFLUSH, Boolean.class));
        
        // Create default shop.
      	shop = new Shop(getSetting(Setting.INFINITE_FUNDING, Boolean.class), this, getSetting(Setting.ITEMS_MAX_PER_TRANSACTION, Integer.class), "");
        
        log(Level.INFO, "Enabled.");
    }
    
    private void checkLibs()
    {
        boolean isok = false;
        
        File a = new File(getDataFolder() + "/sqlitejdbc-v056.jar");
        if (!a.exists())
        {
            isok = IO.fileDownload("http://www.brisner.no/libs/sqlitejdbc-v056.jar", getDataFolder().toString());
            if (isok)
            {
                log(Level.INFO, "Downloaded SQLite Successfully.");
            }
        }
        
        File b = new File(getDataFolder() + "/mysql-connector-java-5.1.15-bin.jar");
        if (!b.exists())
        {
            isok = IO.fileDownload("http://www.brisner.no/libs/mysql-connector-java-5.1.15-bin.jar", getDataFolder().toString());
            if (isok)
            {
                log(Level.INFO, "Downloaded MySQL Successfully.");
            }
        }
        
        File c = new File(getDataFolder() + "/items.db");
        if (!c.exists())
        {
            isok = IO.fileDownload("http://www.brisner.no/DynamicMarket/items.db", getDataFolder().toString());
            if (isok)
            {
                log(Level.INFO, "Downloaded items.db successfully");
            }
        }
    }
    
    @SuppressWarnings("unchecked")
	@Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
    {
        try
        {
            ((CommandsManager<CommandSender>)commandsManager).execute(cmd.getName(), args, sender, this, sender);
        }
        catch (CommandPermissionsException e)
        {
        	sender.sendMessage(Messaging.parseColor("{ERR}" + "You don't have permission"));
        }
        catch (MissingNestedCommandException e)
        {
        	sender.sendMessage(Messaging.parseColor("{ERR}" + e.getUsage()));
        }
        catch (CommandUsageException e)
        {
        	sender.sendMessage(Messaging.parseColor("{ERR}" + e.getMessage()));
        	sender.sendMessage(Messaging.parseColor("{ERR}" + e.getUsage()));
        }
        catch (WrappedCommandException e)
        {
        	e.printStackTrace();
        }
        catch (CommandException e)
        {
        	sender.sendMessage(Messaging.parseColor("{ERR}" + e.getMessage()));
        }
        
        return true;
    }
    
	public void log(Level level, String message)
	{
		log.log(level, "[" + getDescription().getName() + "] " + message);
	}
	
	protected void removeItem(Player player, MarketItem item, int amount)
	{
		items.remove(player, item, amount);
	}
	
	// Access Methods
	public Shop getShop()
	{
		return shop;
	}
	
	public DatabaseMarket getDatabaseMarket()
	{
		return db;
	}
	
	public <T> T getSetting(Validatable setting, Class<T> type)
	{
		return settings.getSetting(setting, type);
	}
	
	protected TransactionLogger getTransactionLogger()
	{
		return transLog;
	}
	
	public boolean hasPermission(CommandSender sender, String permission)
	{
		return ((PermissionsResolverManager)permissionsManager).hasPermission(sender.getName(), getDescription().getName().toLowerCase() + "." + permission);
	}
}