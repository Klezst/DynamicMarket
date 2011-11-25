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

package com.gmail.haloinverse.DynamicMarket;

import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.bukkit.Material;
import org.bukkit.material.MaterialData;

import com.avaje.ebean.validation.NotEmpty;
import com.avaje.ebean.validation.NotNull;
import com.gmail.haloinverse.DynamicMarket.util.Message;
import com.gmail.haloinverse.DynamicMarket.util.Util;
import com.gmail.haloinverse.DynamicMarket.util.Format;
import com.sk89q.minecraft.util.commands.CommandContext;

@Entity
@Table(name = "products")
public class Product
{
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
	public Product()
	{
		
	}

	
	public Product
	(
		int type,
		byte data,
		int bundleSize,
		boolean buyable,
		boolean sellable,
		double basePrice,
		double maxPrice,
		double minPrice,
		double markup,
		double volatility,
		int stock,
		int maxStock,
		int minStock
	)
	{
		this.type = type;
		this.data = data;
		this.bundleSize = bundleSize;
		this.buyable = buyable;
		this.sellable = sellable;
		this.basePrice = basePrice;
		this.maxPrice = maxPrice;
		this.minPrice = minPrice;
		this.markup = markup;
		this.volatility = volatility;
		this.stock = stock;
		this.maxStock = maxStock;
		this.minStock = minStock;
	}


	// Gets & sets.
	public int getId()
	{
		return id;
	}
	public void setId(int id)
	{
		this.id = id;
	}
	
	public Shop getShop()
	{
		return shop;
	}
	public void setShop(Shop shop)
	{
		this.shop = shop;
	}
	
	public int getType()
	{
		return type;
	}
	public void setType(int type)
	{
		this.type = type;
	}
	
	public byte getData()
	{
		return data;
	}
	public void setData(byte data)
	{
		this.data = data;
	}
	
	public int getBundleSize()
	{
		return bundleSize;
	}
	public void setBundleSize(int bundleSize)
	{
		this.bundleSize = bundleSize;
	}
	
	public boolean isBuyable()
	{
		return buyable;
	}
	public void setBuyable(boolean buyable)
	{
		this.buyable = buyable;
	}
	
	public boolean isSellable()
	{
		return sellable;
	}
	public void setSellable(boolean sellable)
	{
		this.sellable = sellable;
	}

	public double getBasePrice()
	{
		return basePrice;
	}
	public void setBasePrice(double basePrice)
	{
		this.basePrice = basePrice;
	}
	
	public double getMaxPrice()
	{
		return maxPrice;
	}
	public void setMaxPrice(double maxPrice)
	{
		this.maxPrice = maxPrice;
	}

	public double getMinPrice()
	{
		return minPrice;
	}
	public void setMinPrice(double minPrice)
	{
		this.minPrice = minPrice;
	}

	public double getMarkup()
	{
		return markup;
	}
	public void setMarkup(double markup)
	{
		this.markup = markup;
	}
	
	public double getVolatility()
	{
		return volatility;
	}
	public void setVolatility(double volatility)
	{
		this.volatility = volatility;
	}
	
	public int getStock()
	{
		return stock;
	}
	public void setStock(int stock)
	{
		this.stock = stock;
	}
	
	public int getMaxStock()
	{
		return maxStock;
	}
	public void setMaxStock(int maxStock)
	{
		this.maxStock = maxStock;
	}

	public int getMinStock() {
		return minStock;
	}
	public void setMinStock(int minStock)
	{
		this.minStock = minStock;
	}
	
	// Methods
	public String getName()
	{
			return Material.getMaterial(type).name().replace('_', ' ');
	}
	
	public static Product parseProduct(String... args) throws IllegalArgumentException
	{
		// TODO: Validate args individually.
		MaterialData data = Util.getMaterialData(args[0] + ":" + args[1]); // throws IllegalArgumentException, iff the argument isn't a valid MaterialData.
		try
		{
			return new Product
			(
				data.getItemTypeId(),
				data.getData(),
				Format.parseInteger(args[2]),
				Format.parseBoolean(args[3]),
				Format.parseBoolean(args[4]),
				Format.parseDouble(args[5]),
				Format.parseDouble(args[6]),
				Format.parseDouble(args[7]),
				Format.parseDouble(args[8]),
				Format.parseDouble(args[9]),
				Format.parseInteger(args[10]),
				Format.parseInteger(args[11]),
				Format.parseInteger(args[12])
			);
		}
		catch (NumberFormatException e)
		{
			
		}
		catch (IndexOutOfBoundsException e)
		{
			
		}
		throw new IllegalArgumentException("That is not a valid Product."); // Does not get executed, if the constructor is returned successfully.
	}
	public static Product parseProduct(String arg) throws IllegalArgumentException
	{
		return parseProduct(arg.split(",")); // throws InvalidArgumentException, Iff args.split(",") is not a valid Product.
	}
	public static Product parseProduct(CommandContext args) throws IllegalArgumentException
	{
		Map<String, String> properties = Util.getProperties(args.getSlice(2));
		String[] data = args.getString(0).split(":");
		
		return parseProduct // TODO: These string literals should really be constants & support multiple names for each property.
		(
			data[0],
			data.length > 1 ? data[1] : "0",
			properties.containsKey("bundlesize") ? properties.get("bundlesize") : "1",
			properties.containsKey("buyable") ? properties.get("buyable") : "True",
			properties.containsKey("sellable") ? properties.get("sellable") : "True",
			properties.containsKey("baseprice") ? properties.get("baseprice") : "10",
			properties.containsKey("maxprice") ? properties.get("maxprice") : "+INF",
			properties.containsKey("minprice") ? properties.get("minprice") : "1",
			properties.containsKey("salestax") ? properties.get("salestax") : "0.06",
			properties.containsKey("volatility") ? properties.get("volatility") : "0.05",
			properties.containsKey("stock") ? properties.get("stock") : "0",
			properties.containsKey("maxstock") ? properties.get("maxstock") : "+INF",
			properties.containsKey("minstock") ? properties.get("minstock") : "-INF"
		); // throws IllegalArgumentException, iff arguments do not make a valid Product.
	}
	
	public boolean equals(int type, byte data)
	{
		return this.type == type && this.data == data;
	}
	
	public double getBuyPrice()
	{
		double sellPrice = getSellPrice();
		sellPrice += sellPrice * markup;
		return Math.min(Util.round(sellPrice, 2), maxPrice);
	}
	
	public double getSellPrice()
	{
		double change;
		if (stock >= 0)
		{
			change = 1 - volatility;
		}
		else
		{
			change = 1 + volatility;
		}
		double price = Math.pow(change, Math.abs(stock)) * basePrice;
		return Util.clamp(minPrice, Util.round(price, 2), maxPrice -  price * markup);
	}
	
	public boolean hasStock(int amount)
	{
		int newStock = stock - amount;
		return newStock >= minStock && newStock <= maxStock;
	}
	
	public String toCSV()
	{
		return Message.combine
		(
			",", // This is the separator.
			type,
			data,
			bundleSize,
			buyable,
			sellable,
			basePrice,
			Format.parseString(maxPrice),
			Format.parseString(minPrice),
			markup,
			volatility,
			stock,
			Format.parseString(maxStock),
			Format.parseString(minStock)
		);
	}
	
	public String toString()
	{
		return Message.combine
		(
			"\n", // This is the separator between each line.
			Message.headerify("{PRM}" + getName() + (data == 0 ? "" : "{BKT}:{PRM}" + data)),
			"{}Can buy: {PRM}" + Format.parseString(buyable),
			"{}Can sell: {PRM}" + Format.parseString(sellable),
			"{}Base price: {PRM}" + basePrice,
			"{}Max price: {PRM}" + Format.parseString(maxPrice),
			"{}Min price: {PRM}" + Format.parseString(minPrice),
			"{}Buy price: {PRM}" + getBuyPrice(),
			"{}Sell price: {PRM}" + getSellPrice(),
			"{}Markup: {PRM}" + markup * 100 + "%",
			"{}Volatility {PRM}" + volatility * 100 +"%",
			"{}Bundle size: {PRM}" + bundleSize,
			"{}Stock: {PRM}" + stock,
			"{}Max stock: {PRM}" + Format.parseString(maxStock),
			"{}Min stock: {PRM}" + Format.parseString(minStock)
		);
	}
}