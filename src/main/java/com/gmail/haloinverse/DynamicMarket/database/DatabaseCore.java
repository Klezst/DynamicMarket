package com.gmail.haloinverse.DynamicMarket.database;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.util.ArrayList;
import java.util.logging.Level;

import com.gmail.haloinverse.DynamicMarket.DynamicMarket;
import com.gmail.haloinverse.DynamicMarket.ItemClump;
import com.gmail.haloinverse.DynamicMarket.Setting;

public abstract class DatabaseCore
{
    
    public Type databaseType = null;
    public String tableName; // default: SimpleMarket
    private DynamicMarket plugin = null;
    public String engine = "MyISAM";
    private static Connection conn = null;
    
    public DatabaseCore(Type databaseType, String tableAccessed,
            String thisEngine, DynamicMarket thisPlugin)
    {
        this.databaseType = databaseType;
        this.tableName = tableAccessed;
        if (thisEngine != null)
        {
            engine = thisEngine;
        }
        this.plugin = thisPlugin;
        initialize();
    }
    
    protected boolean initialize() {
        return initialize("");
    }
    
    protected boolean initialize(String tableSuffix) {
        if (!(checkTable(tableSuffix)))
        {
            plugin.log(Level.INFO, "Creating database.");
            if (createTable(tableSuffix))
            {
                plugin.log(Level.INFO, "Database Created.");
                return true;
            }
            else
            {
                plugin.log(Level.SEVERE, "Database creation failed.");
                return false;
            }
        }
        return false;
    }
    
    protected boolean deleteDatabase() {
        return deleteDatabase("");
    }
    
    protected boolean deleteDatabase(String tableSuffix) {
        SQLHandler myQuery = new SQLHandler(this);
        myQuery.executeStatement("DROP TABLE " + tableName + tableSuffix + ";");
        myQuery.close();
        
        if (myQuery.isOK) {
            plugin.log(Level.INFO, "Database table successfully deleted.");
        } else {
            plugin.log(Level.SEVERE, "Database table could not be deleted.");
        }
        
        return myQuery.isOK;
    }
    
    public boolean resetDatabase() {
        return resetDatabase("");
    }
    
    public boolean resetDatabase(String tableSuffix) {
        deleteDatabase(tableSuffix);
        return initialize(tableSuffix);
    }
    
    protected Connection connection() throws ClassNotFoundException, SQLException
    {
        //    	DynamicMarket.log.info("connection: " +
        //    			               "null? " + ((DatabaseCore.conn == null)?"true":"false") + 
        //    			               " isClosed? " + (((DatabaseCore.conn != null) && DatabaseCore.conn.isClosed())?"true":"false"));
        //    	new Throwable().printStackTrace();
        
    	boolean debug = plugin.getSetting(Setting.DEBUG, Boolean.class);
    	
        if (debug)
            plugin.log(Level.INFO, "DatabaseCore:connection() called");
        
        if ((DatabaseCore.conn != null) && (!DatabaseCore.conn.isClosed())) {
            boolean bad = false;
            SQLWarning sw = conn.getWarnings();
            while (sw != null) {
                bad = true;
                plugin.log(Level.INFO, "leftover warning: " + sw.getMessage());
                sw = sw.getNextWarning();
            }
            if (bad) {
                if (debug)
                    plugin.log(Level.INFO, "DatabaseCore:connection() not re-using connection");
                conn = null;
            } else {
                if (debug)
                    plugin.log(Level.INFO, "DatabaseCore:connection() returning last connection");
                return DatabaseCore.conn;
            }
        }
        
        if (debug)
            plugin.log(Level.INFO, "DatabaseCore:connection() new connection");
        
        // CHANGED: Sets connections to auto-commit, rather than emergency
        // commit-on-close behavior.
        Connection newConn;
        
        if (this.databaseType.equals(Type.SQLITE))
        {
            Class.forName("org.sqlite.JDBC");
            newConn = DriverManager.getConnection("jdbc:sqlite:" + plugin.getDataFolder() + File.separator + "shop.db");
            newConn.setAutoCommit(true);
            DatabaseCore.conn = newConn;
            return DatabaseCore.conn;
        }
        
        Class.forName("com.mysql.jdbc.Driver");
        newConn = DriverManager.getConnection(plugin.getSetting(Setting.MYSQL_URL, String.class), plugin.getSetting(Setting.MYSQL_USER, String.class),plugin.getSetting(Setting.MYSQL_PASS, String.class));
        newConn.setAutoCommit(false);
        DatabaseCore.conn = newConn;
        return DatabaseCore.conn;
    }
    
    protected String dbTypeString() {
        return ((this.databaseType.equals(Type.SQLITE)) ? "sqlite" : "mysql");
    }
    
    protected void logSevereException(String exDesc, Exception exDetail) {
        plugin.log(Level.SEVERE, exDesc + ": " + exDetail);
    }
    
    protected void logSevereException(String exDesc) {
        plugin.log(Level.SEVERE, exDesc);
    }
    
    protected boolean checkTable(String tableSuffix) {
        boolean bool;
        SQLHandler myQuery = new SQLHandler(this);
        
        bool = myQuery.checkTable(tableName + tableSuffix);
        myQuery.close();
        return bool;
    }
    
    protected boolean checkTable() {
        return checkTable("");
    }
    
    protected DynamicMarket getPlugin()
    {
    	return plugin;
    }
    
    protected abstract boolean createTable(String tableSuffix);
    
    protected boolean createTable() {
        return createTable("");
    }
    
    public abstract boolean add(Object newObject);
    
    public abstract boolean update(Object updateRef);
    
    public abstract boolean remove(ItemClump removed);
    
    public abstract ArrayList<?> list(int pageNum);
    
    public abstract Object data(ItemClump thisItem);
    
    public static enum Type
    {
        
        SQLITE,
        MYSQL,
        FLATFILE;
    }
}
