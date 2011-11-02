package com.gmail.haloinverse.DynamicMarket;

import com.nijikokun.register.payment.Methods;
import com.sk89q.bukkit.migration.PermissionsResolverManager;
import com.sk89q.bukkit.migration.PermissionsResolverServerListener;

import java.awt.List;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Timer;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Server;

public class DynamicMarket extends JavaPlugin {
    public static final Logger log = Logger.getLogger("Minecraft");
    
    public static String name; // = "SimpleMarket";
    public static String codename = "Sentinel";
    public static String version; // = "0.5b";
    
    public iListen playerListener = new iListen(this);
    
    public static Server server = null;
    //public static iConomy economy = null;
    //public static PermissionHandler Permissions = null;
    protected static Object perms; //PermissionsResolverManager
    
    //public static iProperty Settings;
    public static File directory;
    
    //protected static String currency;// = "Coin";
    protected static boolean econLoaded = false;
    
    public static boolean debug = false;
    
    //    protected static boolean wrapperMode = false;
    //protected static boolean wrapperPermissions = false;
    protected static LinkedList<JavaPlugin> wrappers = new LinkedList<JavaPlugin>();
    
    //protected static boolean simplePermissions = false;
    
    public String shop_tag; //= "{BKT}[{}Shop{BKT}]{} ";
    protected int max_per_purchase; // = 64;
    protected int max_per_sale; // = 64;
    public String defaultShopAccount; // = "";
    public boolean defaultShopAccountFree; // = true;
    protected static String database_type; // = "sqlite";
    protected static String sqlite; // = "jdbc:sqlite:" + "plugins/DynamicMarket/shop.db";
    protected static String mysql; // = "jdbc:mysql://localhost:3306/minecraft";
    protected static String mysql_user; // = "root";
    protected static String mysql_pass; // = "pass";
    protected static String mysql_dbEngine; // = "MyISAM";
    protected static Timer timer = null;
    protected static String csvFileName;
    protected static String csvFilePath;
    
    // Configuration keys
    private static final String[] keys =
    {
    	"default-shop-account",
    	"default-shop-account.is-free",
    	"text-color.bracket",
    	"text-color.command",
    	"text-color.error",
    	"text-color.normal",
    	"text-color.param",
    	"database-type",
    	"items-db-path",
    	"mysql.url",
    	"mysql.user",
    	"mysql.pass",
    	"mysql.engine",
    	"import-export.file",
    	"import-export.path",
    	"transaction-log.file",
    	"transaction-log.autoflush",
    	"shop-tag",
    	"items-max-per.purchase",
    	"items-max-per.sale",
    	"debug"
    };
    
    //protected EconType econType = EconType.NONE;
    protected Items items;
    protected String itemsPath = "";
    protected DatabaseMarket db = null;
    
    //protected PermissionInterface permissionWrapper = null;
    protected TransactionLogger transLog = null;
    protected String transLogFile = "transactions.log";
    protected boolean transLogAutoFlush = true;
    private static iPluginListener pluginListener = null;
    
    public void onDisable() {
        //        db.uninitialize();
        log.info(Messaging.bracketize(name) + " version " + Messaging.bracketize(version) + " (" + codename + ") disabled");
    }
    
    
    @Override
    public File getDataFolder()
    {
        directory = super.getDataFolder();
        if (!directory.toString().equals("plugins" + File.separator + "DynamicMarket"))
        {
        	log.log(Level.WARNING, "[" + getDescription().getName() + "] Jar is not named DynamicMarket.jar: beware of running multiple instances.");
        }
        return directory;
    }
    
    
    @Override
    public void onEnable() {
        PluginDescriptionFile desc = getDescription();
        getDataFolder().mkdir();
        
        server = getServer();
        
        name = desc.getName();
        version = desc.getVersion();
        
        sqlite = "jdbc:sqlite:" + directory + File.separator + "shop.db";
        
        PluginManager pm = getServer().getPluginManager();
        
        // Check for Register (dependency)
        if (pm.getPlugin("Register") == null)
        {
        	log.log(Level.SEVERE, "[DynamicMarket] Register not detected; disabling.");
        	pm.disablePlugin(this);
        	return;
        }
        
        // Check if register detected economy yet
    	if (Methods.hasMethod())
    	{
    		econLoaded = true;
    		System.out.println("[DynamicMarket] hooked into Register.");
    	}
        
    	// Check for WorldEdit (dependency)
    	try
    	{
    		perms = new PermissionsResolverManager(this, getDescription().getName(), Logger.getLogger("Minecraft.YourPlugin"));
    	}
    	catch (NoClassDefFoundError e)
    	{
            log.log(Level.SEVERE, "[DynamicMarket] WorldEdit not detected; disabling.");
            pm.disablePlugin(this);
            return;
    	}
        
        // Register events
        new PermissionsResolverServerListener((PermissionsResolverManager)perms, this);
        
        pluginListener = new iPluginListener(this);
        pm.registerEvent(Event.Type.PLUGIN_ENABLE, pluginListener, Priority.Monitor, this);
        pm.registerEvent(Event.Type.PLUGIN_DISABLE, pluginListener, Priority.Monitor, this);
        
        checkLibs();
        if (!setup())
        {
        	return;
        }
        
        log.info(Messaging.bracketize(name) + " version " + Messaging.bracketize(version) + " (" + codename + ") enabled");
    }
    
    public static Server getTheServer() {
        return server;
    }
    
    /*
    public static iConomy getiConomy() {
        return economy;
    }
    */
    
    /*
    public static boolean setiConomy(iConomy plugin) {
        if (economy == null) {
            economy = plugin;
            //currency = iConomy.getCurrency();
            econLoaded = true;
            log.info(Messaging.bracketize(name) + " iConomy connected.");
        } else {
            return false;
        }
        return true;
    }
    */
    
    /*
    public static void setupPermissions() {
        Plugin test = getTheServer().getPluginManager().getPlugin("Permissions");
        if (Permissions == null)
            if (test != null)
                DynamicMarket.Permissions = ((Permissions) test).getHandler();
        
    }
    */
    
    private void checkLibs() {
        boolean isok = false;
        File a = new File(getDataFolder() + "/sqlitejdbc-v056.jar");
        if (!a.exists()) {
            isok = FileDownloader.fileDownload("http://www.brisner.no/libs/sqlitejdbc-v056.jar", getDataFolder().toString());
            if (isok)
                System.out.println("[DynamicMarket] Downloaded SQLite Successfully.");
        }
        File b = new File(getDataFolder() + "/mysql-connector-java-5.1.15-bin.jar");
        if (!b.exists()) {
            isok = FileDownloader.fileDownload("http://www.brisner.no/libs/mysql-connector-java-5.1.15-bin.jar", getDataFolder().toString());
            if (isok)
                System.out.println("[DynamicMarket] Downloaded MySQL Successfully.");
        }
        File c = new File(getDataFolder() + "/items.db");
        if (!c.exists()) {
            isok = FileDownloader.fileDownload("http://www.brisner.no/DynamicMarket/items.db", getDataFolder().toString());
            if (isok)
                System.out.println("[DynamicMarket] items.db downloaded successfully");
        }
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd,
            String commandLabel, String[] args) {
        ListIterator<JavaPlugin> itr = DynamicMarket.wrappers.listIterator();
        while (itr.hasNext()) {
            JavaPlugin wrap = itr.next();
            if (wrap.onCommand(sender, cmd, commandLabel, args))
                return true;
        }
        return this.playerListener.parseCommand(sender, cmd.getName(), args, "", defaultShopAccount, defaultShopAccountFree);
    }
    
    public void hookWrapper(JavaPlugin wrap) {
        DynamicMarket.wrappers.add(wrap);
        log.info(Messaging.bracketize(name) + " wrapper mode enabled by " + wrap.getDescription().getName());
    }
    
    public boolean wrapperCommand(CommandSender sender, String cmd,
            String[] args, String shopLabel, String accountName,
            boolean freeAccount) {
        return this.playerListener.parseCommand(sender, cmd, args, (shopLabel == null ? "" : shopLabel), accountName, freeAccount);
    }
    
    public boolean wrapperCommand(CommandSender sender, String cmd,
            String[] args, String shopLabel) {
        return wrapperCommand(sender, cmd, args, (shopLabel == null ? "" : shopLabel), defaultShopAccount, defaultShopAccountFree);
    }
    
    public boolean wrapperCommand(CommandSender sender, String cmd,
            String[] args) {
        return wrapperCommand(sender, cmd, args, "");
    }
    
    public boolean setup()
    {
		// Set up directory
    	directory = getDataFolder();
    	if (!directory.exists())
    	{
    		getDataFolder().mkdirs();
    	}
    	if (!extract("config.yml", "shopDB.csv"))
    	{
    		return false;
    	}
    	
        // Verify integrity of configuration
    	FileConfiguration config = getConfig();
    	
        ArrayList<String> invalid = new ArrayList<String>();
        for (String key : keys)
        {
        	if (!config.isSet(key))
        	{
        		invalid.add(key);
        	}
        }
        
        if (invalid.size() > 0)
        {
        	String title = getDescription().getName();
        	log.log(Level.SEVERE, "[" + title + "] Disabling, invalid config.yml; defaults used for:");
	        for (String key : invalid)
	        {
	        	log.log(Level.SEVERE, "[" + title + "]\t" + key);
	        }
	        getServer().getPluginManager().disablePlugin(this);
	        return false;
        }
    	
    	// Load configuration
        defaultShopAccount = config.getString(keys[0], "");
        defaultShopAccountFree = config.getBoolean(keys[1]);
    	
        Messaging.colBracket = "&" + config.getString(keys[2]);
        Messaging.colCmd = "&" + config.getString(keys[3]);
        Messaging.colError = "&" + config.getString(keys[4]);
        Messaging.colNormal = "&" + config.getString(keys[5]);
        Messaging.colParam = "&" + config.getString(keys[6]);
        
        DynamicMarket.database_type = config.getString(keys[7]);
        itemsPath = config.getString(keys[8]);
        
        mysql = config.getString(keys[9]);
        mysql_user = config.getString(keys[10]);
        mysql_pass = config.getString(keys[11]);
        mysql_dbEngine = config.getString(keys[12]);
        
        csvFileName = config.getString(keys[13]);
        csvFilePath = config.getString(keys[14]);
        
        transLogFile = config.getString(keys[15]);
        transLogAutoFlush = config.getBoolean(keys[16]);
        
        shop_tag = config.getString(keys[17]);
        max_per_purchase = config.getInt(keys[18]);
        max_per_sale = config.getInt(keys[19]);
        
        debug = config.getBoolean(keys[20]);
        
        // Setup database
        items = new Items(itemsPath + "items.db", this);
        
        if (DynamicMarket.database_type.equalsIgnoreCase("mysql"))
        {
            try
            {
                Class.forName("com.mysql.jdbc.Driver");
            }
            catch (ClassNotFoundException ex)
            {
                log.log(Level.SEVERE, "[" + getDescription().getName() + "] com.mysql.jdbc.Driver class not found; disabling.");
                ex.printStackTrace();
                getServer().getPluginManager().disablePlugin(this);
                return false;
            }
            db = new DatabaseMarket(DatabaseMarket.Type.MYSQL, "Market", items, mysql_dbEngine, this);
        }
        else
        {
            try
            {
                Class.forName("org.sqlite.JDBC");
            }
            catch (ClassNotFoundException ex)
            {
                log.log(Level.SEVERE, "[" + getDescription().getName() + "] org.sqlite.JDBC class not found; disabling.");
                ex.printStackTrace();
                getServer().getPluginManager().disablePlugin(this);
                return false;
            }
            db = new DatabaseMarket(DatabaseMarket.Type.SQLITE, "Market", items, "", this);
        }
        
        // Setup transaction log
        transLog = new TransactionLogger(this, getDataFolder() + File.separator + transLogFile, transLogAutoFlush);
        
        //System.out.println("------------------\n" + debug + "\n" + itemsPath + "\n" + items + "n" + shop_tag + "n" + max_per_purchase + "\n" + max_per_sale + "\n" + DynamicMarket.database_type + "\n" + mysql + "\n" + mysql_user + "\n" + mysql_pass + "\n" + mysql_dbEngine + "\n" + Messaging.colNormal + "\n" + Messaging.colBracket + "\n" + Messaging.colCmd + "\n" + Messaging.colParam + "\n" + Messaging.colError + "\n" + defaultShopAccount + "\n" + defaultShopAccountFree + "\n" + transLogFile + "\n" + transLogAutoFlush + "\n" + csvFileName + "\n" + csvFilePath);
        return true;
        /*
        //Settings = new iProperty(getDataFolder() + File.separator + name + ".settings");
		
        debug = Settings.getBoolean("debug", false);
        
        // ItemsFile = new iProperty("items.db");
        itemsPath = Settings.getString("items-db-path", getDataFolder() + File.separator);
        items = new Items(itemsPath + "items.db", this);
        
        shop_tag = Settings.getString("shop-tag", shop_tag);
        max_per_purchase = Settings.getInt("max-items-per-purchase", 64);
        max_per_sale = Settings.getInt("max-items-per-sale", 64);
        
        DynamicMarket.database_type = Settings.getString("database-type", "sqlite");
        
        mysql = Settings.getString("mysql-db", mysql);
        mysql_user = Settings.getString("mysql-user", mysql_user);
        mysql_pass = Settings.getString("mysql-pass", mysql_pass);
        mysql_dbEngine = Settings.getString("mysql-dbengine", mysql_dbEngine);
        
        if (DynamicMarket.database_type.equalsIgnoreCase("mysql")) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
            } catch (ClassNotFoundException ex) {
                log.info("com.mysql.jdbc.Driver class not found!");
                ex.printStackTrace();
            }
            db = new DatabaseMarket(DatabaseMarket.Type.MYSQL, "Market", items, mysql_dbEngine, this);
        } else {
            try {
                Class.forName("org.sqlite.JDBC");
            } catch (ClassNotFoundException ex) {
                log.info("org.sqlite.JDBC class not found!");
                ex.printStackTrace();
            }
            db = new DatabaseMarket(DatabaseMarket.Type.SQLITE, "Market", items, "", this);
        }
        
        csvFileName = Settings.getString("csv-file", "shopDB.csv");
        csvFilePath = Settings.getString("csv-file-path", getDataFolder() + File.separator);
        //        wrapperMode = Settings.getBoolean("wrapper-mode", false);
        //simplePermissions = Settings.getBoolean("simple-permissions", false);
        //wrapperPermissions = Settings.getBoolean("wrapper-permissions", false);
        
        Messaging.colNormal = "&" + Settings.getString("text-colour-normal", "e");
        Messaging.colCmd = "&" + Settings.getString("text-colour-command", "f");
        Messaging.colBracket = "&" + Settings.getString("text-colour-bracket", "d");
        Messaging.colParam = "&" + Settings.getString("text-colour-param", "b");
        Messaging.colError = "&" + Settings.getString("text-colour-error", "c");
        
        defaultShopAccount = Settings.getString("default-shop-account", "");
        defaultShopAccountFree = Settings.getBoolean("default-shop-account-free", defaultShopAccountFree);
        
        transLogFile = Settings.getString("transaction-log-file", transLogFile);
        transLogAutoFlush = Settings.getBoolean("transaction-log-autoflush", transLogAutoFlush);
        if ((transLogFile != null) && (!transLogFile.isEmpty())) {
            transLog = new TransactionLogger(this, getDataFolder() + File.separator + transLogFile, transLogAutoFlush);
        } else {
            transLog = new TransactionLogger(this, null, false);
        }
        
        /*
        String econTypeString = Settings.getString("economy-plugin", "iconomy4");
        if (econTypeString.equalsIgnoreCase("iconomy4")) {
            econType = EconType.ICONOMY4;
        } else {
            log.severe(Messaging.bracketize(name) + " Invalid economy setting for 'economy-plugin='.");
            econType = EconType.NONE;
        }
        */
    }
    
    public void InitializeEconomy() {
        econLoaded = true;
        log.info(Messaging.bracketize(name) + " successfully hooked into iConomy.");
    }
    
    /*
    public static enum EconType {
        
        NONE,
        ICONOMY4;
    }
    */
    private InputStream getInputStream(String name)
    {
    	InputStream input = null;
    	try
    	{
    		JarFile file = new JarFile(getFile());
    		ZipEntry copy = file.getEntry("resources/" + name);
    		if (copy == null)
    		{
    			log.severe("[" + getDescription().getName() + "] Unable to find INTERNAL file " + name + "; disabling.");
    			getServer().getPluginManager().disablePlugin(this);
    			return null;
    		}
    		input = file.getInputStream(copy);
    	}
    	catch (IOException e)
    	{
    		log.severe("[" + getDescription().getName() + "] Unable to read INTERNAL file " + name + "; disabling.");
    		getServer().getPluginManager().disablePlugin(this);
    		return null;
    	}
    	return input;
    }
    
    
	 /**
	 * Original by sk89q
    * Copy files from the .jar.
    * 
    * @param names, names of the files to be copied
    */
   protected boolean extract(String... names)
   {
	   for (String name : names)
	   {
		   // Get input
	       File actual = new File(getDataFolder(), name);
	       if (!actual.exists())
	       {
	           InputStream input = getInputStream(name);
	           if (input == null)
	           {
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
                   
                   log.info("[" + getDescription().getName() + "] Default file " + name + " successfully extracted.");
               }
               catch (IOException e)
               {
                   log.severe("[" + getDescription().getName() + "] Unable to write file " + name + "; disabling.");
                   e.printStackTrace();
                   getServer().getPluginManager().disablePlugin(this);
               }
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
                	   log.warning("[" + getDescription().getName() + "] Unable to close INTERNAL file " + name + ".");
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
                	   log.warning("[" + getDescription().getName() + "] Unable to close file " + name + "; disabling.");
                   }
               }
	       }
	   }
	   return true;
   }
}