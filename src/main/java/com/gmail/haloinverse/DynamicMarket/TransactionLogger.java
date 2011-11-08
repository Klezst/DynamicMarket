package com.gmail.haloinverse.DynamicMarket;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;

public class TransactionLogger {
    
    private BufferedWriter logWriter = null;
    private String logFileName;
    private DynamicMarket plugin;
    
    public boolean autoFlush;
    public boolean isOK;
    
    public TransactionLogger(DynamicMarket thisPlugin, String fileName, boolean setAutoFlush)
    {
    	this.plugin = thisPlugin;
        this.isOK = true;
        this.logFileName = fileName;
        this.autoFlush = setAutoFlush;
        if ((fileName == null) || (fileName.isEmpty())) {
            isOK = false;
            return;
        }
        try {
            logWriter = new BufferedWriter(new FileWriter(logFileName, true));
        } catch (IOException ex) {
            logSevereException("Error opening log file for writing: " + logFileName, ex);
            isOK = false;
            return;
        }
    }
    
    public void logTransaction(String thisLine) {
        if (logWriter == null) {
            return;
        }
        try {
            logWriter.newLine();
            logWriter.write(thisLine);
            if (this.autoFlush) {
                logWriter.flush();
            }
        } catch (IOException ex) {
            logSevereException("Error writing output line to log file: " + logFileName, ex);
            isOK = false;
        }
    }
    
    protected void finalize() {
        if (logWriter == null) {
            return;
        }
        try {
            logWriter.flush();
        } catch (IOException ex) {
            logSevereException("Error flushing output while closing logfile:" + logFileName, ex);
        }
        try {
            logWriter.close();
        } catch (IOException ex) {
            logSevereException("Error closing logfile:" + logFileName, ex);
        }
    }
    
    private void logSevereException(String exDesc, Exception exDetail)
    {
        plugin.log(Level.SEVERE, exDesc + ": " + exDetail);
    }
}
