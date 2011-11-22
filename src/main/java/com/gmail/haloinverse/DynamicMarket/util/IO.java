package com.gmail.haloinverse.DynamicMarket.util;

import java.io.*;
import java.net.*;

import org.bukkit.plugin.java.JavaPlugin;

public class IO
{
    final static int size = 1024;
    
    /**
    * Copy files from the .jar.
    * 
    * @param names, Names of the files to be copied.
    * @throws IOException, if an IOException occurs.
    * @author sk89q
    * @author Klezst
    */
    public static void extract(JavaPlugin plugin, String... names) throws IOException
    {
	   for (String name : names)
	   {
		   // Check, if file already exists.
	       File actual = new File(plugin.getDataFolder(), name);
	       if (!actual.exists())
	       {
	    	   // Get input.
	    	   InputStream input = plugin.getResource("resources/" + name); // Will throw IllegalArgumentException, iff name == null.
	           if (input == null)
	           {
	        	   throw new IOException("Unable to get InputStream for INTERNAL file " + name + ".");
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
	           }
               // Close files.
               finally
               {
                   if (input != null)
                   {
                       input.close();
                   }
                   if (output != null)
                   {
                       output.close();
                   }
               }
	       }
	   }
    }
    
    public static boolean FileDownload(String fileAddress, String localFileName, String destinationDir)
    {
        OutputStream os = null;
        URLConnection URLConn = null;
        
        // URLConnection class represents a communication link between the
        // application and a URL.
        
        InputStream is = null;
        try
        {
            URL fileUrl;
            byte[] buf;
            int ByteRead;
            fileUrl = new URL(fileAddress);
            os = new BufferedOutputStream(new FileOutputStream(destinationDir + "/" + localFileName));
            //The URLConnection object is created by invoking the	
            // openConnection method on a URL.
            
            URLConn = fileUrl.openConnection();
            is = URLConn.getInputStream();
            buf = new byte[size];
            while ((ByteRead = is.read(buf)) != -1)
            {
                os.write(buf, 0, ByteRead);
            }
            
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
        finally
        {
            try
            {
                is.close();
                os.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
    
    public static boolean fileDownload(String fileAddress, String destinationDir)
    {
        boolean isok;
        // Find the index of last occurrence of character ‘/’ and ‘.’.
        
        int lastIndexOfSlash = fileAddress.lastIndexOf('/');
        int lastIndexOfPeriod = fileAddress.lastIndexOf('.');
        
        // Find the name of file to be downloaded from the address.
        
        String fileName = fileAddress.substring(lastIndexOfSlash + 1);
        
        // Check whether path or file name is given correctly.
        if (lastIndexOfPeriod >= 1 && lastIndexOfSlash >= 0 && lastIndexOfSlash < fileAddress.length())
        {
            isok = FileDownload(fileAddress, fileName, destinationDir);
            return isok;
        }
        else
        {
            return false;
        }
    }
}
