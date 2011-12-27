/*
	DynamicMarket
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

package dynamicmarket.core;

import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.bukkit.Material;
import org.bukkit.material.MaterialData;

import com.avaje.ebean.validation.NotEmpty;
import com.avaje.ebean.validation.NotNull;
import com.sk89q.minecraft.util.commands.CommandContext;

import dynamicmarket.data.Messaging;
import dynamicmarket.util.Format;
import dynamicmarket.util.Util;

@Entity
@Table(name = "dm_products")
public class Product {
    // Fields.
    @Id
    private int id;

    @ManyToOne
    private Shop shop;

    @NotNull
    private byte data;

    @NotNull
    private boolean buyable;
    @NotNull
    private boolean sellable;

    @NotNull
    private double basePrice;
    @NotNull
    private double maxPrice;
    @NotNull
    private double minPrice;
    @NotEmpty
    private double markup;
    @NotNull
    private double volatility;

    @NotNull
    private int bundleSize;
    @NotNull
    private int stock;
    @NotNull
    private int maxStock;
    @NotNull
    private int minStock;
    @NotNull
    private int type;

    // Constructors.
    public Product() {
	// TODO: ? - IDragonfire
    }

    public Product(int type, byte data, int bundleSize, boolean buyable,
	    boolean sellable, double basePrice, double maxPrice,
	    double minPrice, double markup, double volatility, int stock,
	    int maxStock, int minStock) {
	this.type = type;
	this.data = data;
	this.bundleSize = bundleSize;
	this.buyable = buyable;
	this.sellable = sellable;
	this.basePrice = basePrice;
	setMaxPrice(maxPrice);
	this.minPrice = minPrice;
	this.markup = markup;
	this.volatility = volatility;
	this.stock = stock;
	this.maxStock = maxStock;
	this.minStock = minStock;
    }

    // Gets & sets.
    public int getId() {
	return this.id;
    }

    public void setId(int id) {
	this.id = id;
    }

    public Shop getShop() {
	return this.shop;
    }

    public void setShop(Shop shop) {
	this.shop = shop;
    }

    public int getType() {
	return this.type;
    }

    public void setType(int type) {
	this.type = type;
    }

    public byte getData() {
	return this.data;
    }

    public void setData(byte data) {
	this.data = data;
    }

    public int getBundleSize() {
	return this.bundleSize;
    }

    public void setBundleSize(int bundleSize) {
	this.bundleSize = bundleSize;
    }

    public boolean isBuyable() {
	return this.buyable;
    }

    public void setBuyable(boolean buyable) {
	this.buyable = buyable;
    }

    public boolean isSellable() {
	return this.sellable;
    }

    public void setSellable(boolean sellable) {
	this.sellable = sellable;
    }

    public double getBasePrice() {
	return this.basePrice;
    }

    public void setBasePrice(double basePrice) {
	this.basePrice = basePrice;
    }

    public double getMaxPrice() {
	return this.maxPrice;
    }

    public void setMaxPrice(double maxPrice) {
	if (maxPrice >= Double.MAX_VALUE) {
	    this.maxPrice = DynamicMarket.DDM_MAXVALUE;
	} else {
	    this.maxPrice = maxPrice;
	}
    }

    public double getMinPrice() {
	return this.minPrice;
    }

    public void setMinPrice(double minPrice) {
	this.minPrice = minPrice;
    }

    public double getMarkup() {
	return this.markup;
    }

    public void setMarkup(double markup) {
	this.markup = markup;
    }

    public double getVolatility() {
	return this.volatility;
    }

    public void setVolatility(double volatility) {
	this.volatility = volatility;
    }

    public int getStock() {
	return this.stock;
    }

    public void setStock(int stock) {
	this.stock = stock;
    }

    public int getMaxStock() {
	return this.maxStock;
    }

    public void setMaxStock(int maxStock) {
	this.maxStock = maxStock;
    }

    public int getMinStock() {
	return this.minStock;
    }

    public void setMinStock(int minStock) {
	this.minStock = minStock;
    }

    // Methods
    public String getName() {
	return Material.getMaterial(this.type).name().replace('_', ' ');
    }

    @SuppressWarnings("boxing")
    public static Product parseProduct(String... args)
	    throws IllegalArgumentException {
	// TODO: Validate args individually.
	// throws IllegalArgumentException, if the argument isn't a valid MaterialData.
	MaterialData data = Util.getMaterialData(args[0] + ":" + args[1]);
	try {
	    return new Product(data.getItemTypeId(), data.getData(),
		    Format.parseInteger(args[2]), Format.parseBoolean(args[3]),
		    Format.parseBoolean(args[4]), Format.parseDouble(args[5]),
		    Format.parseDouble(args[6]), Format.parseDouble(args[7]),
		    Format.parseDouble(args[8]), Format.parseDouble(args[9]),
		    Format.parseInteger(args[10]),
		    Format.parseInteger(args[11]),
		    Format.parseInteger(args[12]));
	} catch (NumberFormatException e) {
	    // TODO: catch exceptions
	    e.printStackTrace();
	} catch (IndexOutOfBoundsException e) {
	    // TODO: catch exceptions
	    e.printStackTrace();
	}
	throw new IllegalArgumentException("That is not a valid Product.");
	// Does not get executed, if the constructoris returned successfully.
    }

    public static Product parseProduct(String arg)
	    throws IllegalArgumentException {
	// throws InvalidArgumentException, if args.split(",") is not a valid Product.
	return parseProduct(arg.split(","));
    }

    public static Product parseProduct(CommandContext args)
	    throws IllegalArgumentException {
	Map<String, String> properties = Util.getProperties(args.getSlice(2));
	String[] data = args.getString(0).split(":");
	// TODO: These string literals should really be constants
	// & support multiple names for each property.
	// throws IllegalArgumentException, ifarguments do not make a valid Product.
	return parseProduct(
		data[0],
		data.length > 1 ? data[1] : "0",
		properties.containsKey("bundlesize") ? properties
			.get("bundlesize") : "1",
		properties.containsKey("buyable") ? properties.get("buyable")
			: "True",
		properties.containsKey("sellable") ? properties.get("sellable")
			: "True",
		properties.containsKey("baseprice") ? properties
			.get("baseprice") : "10",
		properties.containsKey("maxprice") ? properties.get("maxprice")
			: "+INF",
		properties.containsKey("minprice") ? properties.get("minprice")
			: "1",
		properties.containsKey("salestax") ? properties.get("salestax")
			: "0.06",
		properties.containsKey("volatility") ? properties
			.get("volatility") : "0.05",
		properties.containsKey("stock") ? properties.get("stock") : "0",
		properties.containsKey("maxstock") ? properties.get("maxstock")
			: "+INF",
		properties.containsKey("minstock") ? properties.get("minstock")
			: "-INF");
    }

    public boolean equals(int type2, byte data2) {
	return this.type == type2 && this.data == data2;
    }

    public double getBuyPrice() {
	double sellPrice = getSellPrice();
	sellPrice += sellPrice * this.markup;
	return Math.min(Util.round(sellPrice, 2), this.maxPrice);
    }

    public double getSellPrice() {
	double change;
	if (this.stock >= 0) {
	    change = 1 - this.volatility;
	} else {
	    change = 1 + this.volatility;
	}
	double price = Math.pow(change, Math.abs(this.stock)) * this.basePrice;
	return Util.clamp(this.minPrice, Util.round(price, 2), this.maxPrice
		- price * this.markup);
    }

    public boolean hasStock(int amount) {
	int newStock = this.stock - amount;
	return newStock >= this.minStock && newStock <= this.maxStock;
    }

    @SuppressWarnings("boxing")
    public String toCSV() {
	return Messaging.combine(
		",", // This is the separator.
		this.type, this.data, this.bundleSize, this.buyable,
		this.sellable, this.basePrice,
		Format.parseString(this.maxPrice),
		Format.parseString(this.minPrice), this.markup,
		this.volatility, this.stock, Format.parseString(this.maxStock),
		Format.parseString(this.minStock));
    }

    @Override
    public String toString() {
	return Messaging.combine(
		"\n", // This is the separator between each line.
		"{PRM}" + getName()
			+ (this.data == 0 ? "" : "{BKT}:{PRM}" + this.data),
		"{}Can buy: {PRM}" + Format.parseString(this.buyable),
		"{}Can sell: {PRM}" + Format.parseString(this.sellable),
		"{}Base price: {PRM}" + this.basePrice, "{}Max price: {PRM}"
			+ Format.parseString(this.maxPrice),
		"{}Min price: {PRM}" + Format.parseString(this.minPrice),
		"{}Buy price: {PRM}" + getBuyPrice(), "{}Sell price: {PRM}"
			+ getSellPrice(), "{}Markup: {PRM}" + this.markup * 100
			+ "%", "{}Volatility {PRM}" + this.volatility * 100
			+ "%", "{}Bundle size: {PRM}" + this.bundleSize,
		"{}Stock: {PRM}" + this.stock,
		"{}Max stock: {PRM}" + Format.parseString(this.maxStock),
		"{}Min stock: {PRM}" + Format.parseString(this.minStock));
    }
}