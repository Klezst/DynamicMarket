package com.gmail.haloinverse.DynamicMarket;

import com.gmail.haloinverse.DynamicMarket.Setting;
import com.gmail.klezst.util.settings.InvalidSettingsException;
import com.gmail.klezst.util.settings.Settings;
import com.gmail.klezst.util.settings.Validatable;
import com.nijikokun.register.payment.Methods;
import com.sk89q.bukkit.migration.PermissionsResolverManager;
import com.sk89q.bukkit.migration.PermissionsResolverServerListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class DynamicMarket extends JavaPlugin
{
	// Statics
    private static final Logger log = Logger.getLogger("Minecraft");
    private static boolean econLoaded = false;
    
    // Listeners
    private iListen playerListener;
    private PluginListener pluginListener;
    
    // Settings
    private Settings settings;
    private String defaultShopAccount;
    private boolean defaultShopAccountFree;
    
    // Vital Utilities
    private Items items;
    private DatabaseMarket db;
    private Object permissionsManager; // PermissionsResolverManager (Must be Object to prevent NoClassFoundException, iff WorldEdit isn't present).
    
    // Utilities
    private TransactionLogger transLog;
    private File directory;
    private LinkedList<JavaPlugin> wrappers = new LinkedList<JavaPlugin>();
    
    @Override
    public void onDisable()
    {
        log(Level.INFO, "Disabled.");
    }
    
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
    		permissionsManager = new PermissionsResolverManager(this, getDescription().getName(), Logger.getLogger("Minecraft." + getDescription().getName())); // Creates our instance of WorldEdit Permissions Interoperability Framework (WEPIF)
    	}
    	catch (NoClassDefFoundError e)
    	{
            log(Level.SEVERE, "WorldEdit not detected");
            pm.disablePlugin(this);
            return;
    	}
    	
		// Set up directory.
    	directory = getDataFolder();
    	directory.mkdirs();
    	
    	// Set up libraries.
        checkLibs();
        
        // Extract files.
        if (!extract("config.yml", "shopDB.csv", "LICENSE.txt"))
        {
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
    	
    	// Set settings.
    	defaultShopAccount = getSetting(Setting.ACCOUNT_NAME, String.class);
    	defaultShopAccountFree = getSetting(Setting.ACCOUNT_FREE, Boolean.class);
    	
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
        transLog = new TransactionLogger(this, directory + File.separator + getSetting(Setting.TRANSACTION_LOG_FILE, String.class), getSetting(Setting.TRANSACTION_LOG_AUTOFLUSH, Boolean.class));
        
        // Check, if Register detected an economy yet.
    	if (Methods.hasMethod())
    	{
    		econLoaded = true;
    		System.out.println("[DynamicMarket] hooked into Register.");
    	}
        
        // Register events.
        new PermissionsResolverServerListener((PermissionsResolverManager)permissionsManager, this); // Tells WEPIF to check for changes in what permissions plugin is used.
        
      	playerListener = new iListen(this); // Runs on this.onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args).
      	
        pluginListener = new PluginListener(this); // Checks for changes in Register.
        pm.registerEvent(Event.Type.PLUGIN_ENABLE, pluginListener, Priority.Monitor, this);
        pm.registerEvent(Event.Type.PLUGIN_DISABLE, pluginListener, Priority.Monitor, this);
        
        log(Level.INFO, "Enabled.");
    }
    
    private void checkLibs()
    {
        boolean isok = false;
        
        File a = new File(directory + "/sqlitejdbc-v056.jar");
        if (!a.exists())
        {
            isok = FileDownloader.fileDownload("http://www.brisner.no/libs/sqlitejdbc-v056.jar", directory.toString());
            if (isok)
            {
                log(Level.INFO, "Downloaded SQLite Successfully.");
            }
        }
        
        File b = new File(directory + "/mysql-connector-java-5.1.15-bin.jar");
        if (!b.exists())
        {
            isok = FileDownloader.fileDownload("http://www.brisner.no/libs/mysql-connector-java-5.1.15-bin.jar", directory.toString());
            if (isok)
            {
                log(Level.INFO, "Downloaded MySQL Successfully.");
            }
        }
        
        File c = new File(directory + "/items.db");
        if (!c.exists())
        {
            isok = FileDownloader.fileDownload("http://www.brisner.no/DynamicMarket/items.db", directory.toString());
            if (isok)
            {
                log(Level.INFO, "Downloaded items.db successfully");
            }
        }
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
    {
    	// Runs wrappers' onCommand(CommandSender, Command, String, String[]).
        ListIterator<JavaPlugin> itr = wrappers.listIterator();
        while (itr.hasNext())
        {
            JavaPlugin wrap = itr.next();
            if (wrap.onCommand(sender, cmd, commandLabel, args))
            {
                return true;
            }
        }
        
        // Runs iListen, iff no wrapper returned true.
        return this.playerListener.parseCommand(sender, cmd.getName(), args, "", defaultShopAccount, defaultShopAccountFree);
    }
    
    public void hookWrapper(JavaPlugin wrap)
    {
        wrappers.add(wrap);
        log(Level.WARNING, "Wrapper mode enabled by " + wrap.getDescription().getName());
    }
    
    public boolean wrapperCommand(CommandSender sender, String cmd, String[] args, String shopLabel, String accountName, boolean freeAccount)
    {
        return this.playerListener.parseCommand(sender, cmd, args, (shopLabel == null ? "" : shopLabel), accountName, freeAccount);
    }
    
    public boolean wrapperCommand(CommandSender sender, String cmd, String[] args, String shopLabel)
    {
        return wrapperCommand(sender, cmd, args, (shopLabel == null ? "" : shopLabel), defaultShopAccount, defaultShopAccountFree);
    }
    
    public boolean wrapperCommand(CommandSender sender, String cmd, String[] args)
    {
        return wrapperCommand(sender, cmd, args, "");
    }
    
    /**
    * Copy files from the .jar.
    * 
    * @param names, Names of the files to be copied
    * @author sk89q, Klezst
    */
    private boolean extract(String... names)
    {
	   for (String name : names)
	   {
		   // Check, if file already exists.
	       File actual = new File(directory, name);
	       if (!actual.exists())
	       {
	    	   // Get input.
	    	   InputStream input;
		       	try
		    	{
		    		JarFile file = new JarFile(getFile());
		    		ZipEntry copy = file.getEntry("resources/" + name);
		    		if (copy == null)
		    		{
		    			log(Level.SEVERE, "Unable to find INTERNAL file " + name + ".");
		    			return false;
		    		}
		    		input = file.getInputStream(copy);
		    	}
		    	catch (IOException e)
		    	{
		    		log(Level.SEVERE, "Unable to read INTERNAL file " + name + ".");
		    		return false;
		    	}
		       	
	           if (input == null)
	           {
	        	   log(Level.SEVERE, "Unable to get InputStream for INTERNAL file " + name + ".");
	        	   return false;
	           }
	           
	           // Get & write to output
               FileOutputStream output = null;
               try
               {
                   output = new FileOutputStream(actual);
                   byte[] buf = new byte[8192];
                   int length = 0;
                   while ((length = input.read(buf)) > 0)
                   {
                       output.write(buf, 0, length);
                   }
                   
                   log(Level.INFO, "Resource " + name + " successfully extracted.");
               }
               catch (IOException e)
               {
                   log(Level.SEVERE, "Unable to write file " + name + ".");
                   e.printStackTrace();
                   return false; // Finally will still try to close the files
               }
               
               // Close files.
               finally
               {
                   try
                   {
                       if (input != null)
                       {
                           input.close();
                       }
                   }
                   catch (IOException e)
                   {
                	   log(Level.WARNING, "Unable to close INTERNAL file " + name + ".");
                   }

                   try
                   {
                       if (output != null)
                       {
                           output.close();
                       }
                   }
                   catch (IOException e)
                   {
                	   log(Level.WARNING, "Unable to close file " + name + ".");
                   }
               }
	       }
	   }
	   return true;
    }
    
	protected void log(Level level, String message)
	{
		log.log(level, "[" + getDescription().getName() + "] " + message);
	}
    
	public static boolean isEconLoaded()
	{
		return econLoaded;
	}
	
	protected static void setEconLoaded(boolean state)
	{
		econLoaded = state;
	}
	
	protected void removeItem(Player player, MarketItem item, int amount)
	{
		items.remove(player, item, amount);
	}
	
	// Access Methods
	protected DatabaseMarket getDatabaseMarket()
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