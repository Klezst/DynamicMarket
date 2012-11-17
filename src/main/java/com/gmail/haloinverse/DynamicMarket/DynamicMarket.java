package com.gmail.haloinverse.DynamicMarket;

import java.io.File;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Timer;
import java.util.logging.Logger;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Server;

public class DynamicMarket extends JavaPlugin {
    public static final Logger log = Logger.getLogger("Minecraft");
    
    public static String name; // = "SimpleMarket";
    public static String codename = "Compatibility";
    public static String version; // = "0.4a";
    
    public iListen playerListener = new iListen(this);
    
    public static Server server = null;
    
    public static iProperty Settings;
    public static File directory = null;
    
    //protected static String currency;// = "Coin";
    
    public static boolean debug = false;
    
    //    protected static boolean wrapperMode = false;
    protected static boolean wrapperPermissions = false;
    protected static LinkedList<JavaPlugin> wrappers = new LinkedList<JavaPlugin>();
    
    protected static boolean simplePermissions = false;
    
    public String shop_tag = "{BKT}[{}Shop{BKT}]{} ";
    protected int max_per_purchase = 64;
    protected int max_per_sale = 64;
    public String defaultShopAccount = "";
    public boolean defaultShopAccountFree = true;
    protected static String database_type = "sqlite";
    protected static String sqlite = "jdbc:sqlite:" + "plugins/DynamicMarket/shop.db";
    protected static String mysql = "jdbc:mysql://localhost:3306/minecraft";
    protected static String mysql_user = "root";
    protected static String mysql_pass = "pass";
    protected static String mysql_dbEngine = "MyISAM";
    protected static Timer timer = null;
    protected static String csvFileName;
    protected static String csvFilePath;
    
    protected Items items;
    protected String itemsPath = "";
    protected DatabaseMarket db = null;
    
    protected PermissionInterface permissionWrapper = null;
    protected TransactionLogger transLog = null;
    protected String transLogFile = "transactions.log";
    protected boolean transLogAutoFlush = true;
    
    public void onDisable() {
        //        db.uninitialize();
        log.info(Messaging.bracketize(name) + " version " + Messaging.bracketize(version) + " (" + codename + ") disabled");
    }
    
    public File getFolder() {
        if (directory == null) {
            String pluginDirString = "plugins" + File.separator + "DynamicMarket";
            if (!super.getDataFolder().toString().equals(pluginDirString)) {
                log.warning("Jar is not named DynamicMarket.jar!  Beware of multiple DynamicMarket instances being loaded!");
                directory = new File(pluginDirString);
            } else {
                directory = super.getDataFolder();
            }
        }
        
        return directory;
    }
    
    @Override
    public void onEnable() {
        PluginDescriptionFile desc = getDescription();
        getFolder().mkdir();
        
        server = getServer();
        
        name = desc.getName();
        version = desc.getVersion();
        
        sqlite = "jdbc:sqlite:" + directory + File.separator + "shop.db";
        
        checkLibs();
        setup();
        
        log.info(Messaging.bracketize(name) + " version " + Messaging.bracketize(version) + " (" + codename + ") enabled");
    }
    
    public static Server getTheServer() {
        return server;
    }
    
    private void checkLibs() {
        this.saveResource("sqlitejdbc-v056.jar", false);
        this.saveResource("mysql-connector-java-5.1.15-bin.jar", false);
        this.saveResource("items.db", false);
        this.saveResource("shopDB.csv", false);
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
    
    public void setup() {
        Settings = new iProperty(getFolder() + File.separator + name + ".settings");
        
        debug = Settings.getBoolean("debug", false);
        
        // ItemsFile = new iProperty("items.db");
        itemsPath = Settings.getString("items-db-path", getFolder() + File.separator);
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
        csvFilePath = Settings.getString("csv-file-path", getFolder() + File.separator);
        //        wrapperMode = Settings.getBoolean("wrapper-mode", false);
        simplePermissions = Settings.getBoolean("simple-permissions", false);
        wrapperPermissions = Settings.getBoolean("wrapper-permissions", false);
        
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
            transLog = new TransactionLogger(this, getFolder() + File.separator + transLogFile, transLogAutoFlush);
        } else {
            transLog = new TransactionLogger(this, null, false);
        }
    }
}