/*
	BukkitUtil
	Copyright (C) 2011 Klezst

	This program is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package dynamicmarket.util;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import bukkitutil.util.Format;

import dynamicmarket.DynamicMarket;
import dynamicmarket.Market;
import dynamicmarket.Product;
import dynamicmarket.Shop;
import dynamicmarket.configuration.Setting;

public class IO {
    final static int size = 1024;

    // Original by HaloInverse
    public static void dumpToCSV(String filePath, String fileName, Market market)
	    throws IOException {
	BufferedWriter writer;
	try {
	    writer = new BufferedWriter(new FileWriter(filePath + fileName,
		    false));
	} catch (IOException ex) {
	    throw new IOException("Error opening " + fileName + "!");
	}

	String line = "'itemID','subType',bundleSize','canBuy','canSell','basePrice','priceCeil','priceFloor','salesTax','volatility','stock','stockCeil','stockFloor'";
	try {
	    writer.write(line);
	} catch (IOException ex) {
	    throw new IOException("Failed to write header to " + fileName + "!");
	}

	List<Shop> shops = market.getShops();
	for (Shop shop : shops) {
	    line = shop.toCSV();
	    try {
		writer.newLine();
		writer.write(line);
	    } catch (IOException e) {
		throw new IOException("Could not write line to " + fileName
			+ ": " + line);
	    }

	    // Write Products.
	    List<Product> products = shop.getProducts();
	    for (Product product : products) {
		// Write a line
		line = product.toCSV();
		try {
		    writer.newLine();
		    writer.write(line);
		} catch (IOException ex) {
		    throw new IOException("Could not write line to " + fileName
			    + ": " + line);
		}
	    }
	}
	try {
	    writer.flush();
	} catch (IOException ex) {
	    throw new IOException("Could not flush output to " + fileName + "!");
	}
	try {
	    writer.close();
	} catch (IOException ex) {
	    throw new IOException("Could not close " + fileName + "!");
	}
    }

    public static boolean FileDownload(String fileAddress,
	    String localFileName, String destinationDir) {
	OutputStream os = null;
	URLConnection URLConn = null;

	// URLConnection class represents a communication link between the
	// application and a URL.

	InputStream is = null;
	try {
	    URL fileUrl;
	    byte[] buf;
	    int ByteRead;
	    fileUrl = new URL(fileAddress);
	    os = new BufferedOutputStream(new FileOutputStream(destinationDir
		    + "/" + localFileName));
	    // The URLConnection object is created by invoking the
	    // openConnection method on a URL.

	    URLConn = fileUrl.openConnection();
	    is = URLConn.getInputStream();
	    buf = new byte[size];
	    while ((ByteRead = is.read(buf)) != -1) {
		os.write(buf, 0, ByteRead);
	    }

	    return true;
	} catch (Exception e) {
	    e.printStackTrace();
	    return false;
	} finally {
	    try {
		is.close();
		os.close();
	    } catch (IOException e) {
		e.printStackTrace();
	    }
	}
    }

    public static boolean fileDownload(String fileAddress, String destinationDir) {
	boolean isok;
	// Find the index of last occurrence of character ‘/’ and ‘.’.

	int lastIndexOfSlash = fileAddress.lastIndexOf('/');
	int lastIndexOfPeriod = fileAddress.lastIndexOf('.');

	// Find the name of file to be downloaded from the address.

	String fileName = fileAddress.substring(lastIndexOfSlash + 1);

	// Check whether path or file name is given correctly.
	if (lastIndexOfPeriod >= 1 && lastIndexOfSlash >= 0
		&& lastIndexOfSlash < fileAddress.length()) {
	    isok = FileDownload(fileAddress, fileName, destinationDir);
	    return isok;
	}
	return false;
    }

    // Original by HaloInverse
    public static List<Shop> inhaleFromCSV(String filePath, String fileName)
	    throws IOException {
	BufferedReader reader;
	try {
	    reader = new BufferedReader(new FileReader(filePath + fileName));
	} catch (FileNotFoundException ex) {
	    throw new IOException("File, " + fileName + " does not exist!");
	}

	String line;
	try {
	    line = reader.readLine(); // Header line.
	    line = reader.readLine();
	} catch (IOException ex) {
	    throw new IOException(fileName + " is not valid!");
	}

	ArrayList<Shop> shops = new ArrayList<Shop>();
	try {
	    while (line != null) {
		line = line.replace("'", "");
		Shop shop;
		try {
		    shop = Shop.parseShop(line);
		} catch (IllegalArgumentException e) {
		    throw new IOException(
			    fileName
				    + " is invalid at line: \n\t"
				    + line
				    + "\n\t\tExpected: '<shopName>',<isInfiniteFunding>,<funds>,<maxTransactionSize>");
		}

		line = reader.readLine();

		// Import Products.
		while (line != null && line.trim().length() != 0) {
		    // Parse a line
		    line = line.replace("'", "");

		    Product product;
		    try {
			// throws DynamicMarketException, iff line is not a valid Product.
			product = Product.parseProduct(line);
		    } catch (IllegalArgumentException e) {
			throw new IOException(fileName
				+ " is invalid at line: \n\t" + line + "\n\t\t"
				+ e.getMessage());
		    }

		    shop.addProduct(product);

		    try {
			line = reader.readLine();
		    } catch (IOException ex) {
			throw new IOException(fileName
				+ " is invalid; unexpected end of " + fileName
				+ "!");
		    }
		}
		shops.add(shop);
	    }
	} catch (IOException e) {
	    // This should never happen. I simply wanted the close statements to run if any exceptions were thrown.
	    e.printStackTrace();
	} finally {
	    try {
		reader.close();
	    } catch (IOException ex) {
		throw new IOException("Error closing " + fileName + "."); // TODO: Not disable plugin when file closing fails.
	    }
	}

	return shops;
    }

    // Original by HaloInverse
    @Deprecated
    public static void importOld(DynamicMarket plugin, String filePath)
	    throws IOException {
	// Inhale from csv.
	BufferedReader reader;
	try {
	    reader = new BufferedReader(new FileReader(filePath));
	} catch (FileNotFoundException ex) {
	    throw new IOException(filePath + " could not be found!");
	}

	// Read first line.
	String line;
	try {
	    line = reader.readLine(); // This is the header.
	    line = reader.readLine();
	} catch (IOException ex) {
	    throw new IOException("ERROR reading " + filePath + "!");
	}

	// Read .csv.
	Shop shop = new Shop("DynamicMarket", true, 0, 64);
	while (line != null) {
	    if (line.trim().length() == 0) {
		continue;
	    }

	    // Convert input.
	    line = line.replace("'", "");
	    String[] chaos = line.split(",");
	    if (chaos.length != 21) {
		throw new IOException("Invalid line: " + line + "!");
	    }

	    String[] order = { chaos[0], chaos[1], chaos[2], chaos[6],
		    chaos[7], chaos[4], chaos[15], chaos[14],
		    Format.parseString(Format.parseDouble(chaos[9]) / 10000),
		    Format.parseString(Format.parseDouble(chaos[8]) / 10000),
		    chaos[5], chaos[11], chaos[10] };

	    // Parse input.
	    Product product;
	    try {
		// throws DynamicMarketException, iff order is not a valid product.
		product = Product.parseProduct(order);
	    } catch (IllegalArgumentException e) {
		throw new IOException(e.getMessage());
	    }
	    shop.addProduct(product);

	    // Read next line.
	    try {
		line = reader.readLine();
	    } catch (IOException ex) {
		try {
		    reader.close();
		} catch (IOException e) {
		    throw new IOException("Unexpected end to " + filePath
			    + "\nAND could not close file!");
		}
	    }
	}

	// Delete old shops.
	List<Shop> shops = plugin.getMarket().getShops();
	for (Shop s : shops) {
	    plugin.getDatabase().delete(s);
	}

	// Save new shop.
	shops = new ArrayList<Shop>();
	shops.add(shop);
	plugin.getMarket().setShops(shops);
	plugin.getDatabase().save(shop);
	plugin.getConfig().set(Setting.VERSION.getKey(), 1);

	// Close file.
	try {
	    reader.close();
	} catch (IOException ex) {
	    throw new IOException("Could not close file: " + filePath + "!");
	}
    }
}
