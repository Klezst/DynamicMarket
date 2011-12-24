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

package com.gmail.klezst.DynamicMarket;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.bukkit.material.MaterialData;

import com.avaje.ebean.validation.NotEmpty;
import com.avaje.ebean.validation.NotNull;
import com.gmail.klezst.DynamicMarket.util.Format;
import com.gmail.klezst.DynamicMarket.util.Message;
import com.sk89q.minecraft.util.commands.CommandContext;

@Entity
@Table(name = "shops")
public class Shop // TODO: Add location support.
{
    // Fields.
    @Id
    private int id;

    @OneToMany(mappedBy = "shop", cascade = CascadeType.ALL)
    private List<Product> products = new ArrayList<Product>();

    @NotEmpty
    private String name;

    @NotNull
    private boolean infiniteFunding;

    @NotNull
    private double funds;

    @NotNull
    private int maxTransactionSize;

    // Constructors.
    public Shop() {

    }

    public Shop(String name, boolean infiniteFunding, double funds,
	    int maxTransactionSize) {
	this.name = name;
	this.infiniteFunding = infiniteFunding;
	this.funds = funds;
	this.maxTransactionSize = maxTransactionSize;
    }

    // Gets & sets.
    public int getId() {
	return this.id;
    }

    public void setId(int id) {
	this.id = id;
    }

    public List<Product> getProducts() {
	return this.products;
    }

    public void setProducts(List<Product> products) {
	this.products = products;
    }

    public String getName() {
	return this.name;
    }

    public void setName(String name) {
	this.name = name;
    }

    public boolean isInfiniteFunding() {
	return this.infiniteFunding;
    }

    public void setInfiniteFunding(boolean infiniteFunding) {
	this.infiniteFunding = infiniteFunding;
    }

    public double getFunds() {
	return this.funds;
    }

    public void setFunds(double funds) {
	this.funds = funds;
    }

    public int getMaxTransactionSize() {
	return this.maxTransactionSize;
    }

    public void setMaxTransactionSize(int maxTransactionSize) {
	this.maxTransactionSize = maxTransactionSize;
    }

    // Methods
    public void addProduct(Product product) {
	product.setShop(this);
	this.products.add(product);
    }

    public Product getProduct(int type, byte data)
	    throws IllegalArgumentException {
	for (Product product : this.products) {
	    if (product.equals(type, data)) {
		return product;
	    }
	}
	throw new IllegalArgumentException(this.name + " doesn't stock that!");
    }

    public Product getProduct(MaterialData data) {
	return getProduct(data.getItemTypeId(), data.getData());
    }

    @SuppressWarnings("boxing")
    public static Shop parseShop(String... args)
	    throws IllegalArgumentException {
	try {
	    return new Shop(args[0], Format.parseBoolean(args[1]),
		    Format.parseDouble(args[2]), Format.parseInteger(args[3]));
	} catch (NumberFormatException e) {
	    // TODO: catch exception
	    e.printStackTrace();

	} catch (IndexOutOfBoundsException e) {
	    // TODO: catch exception
	    e.printStackTrace();
	}
	throw new IllegalArgumentException("That is not a valid Shop.");
    }

    public static Shop parseShop(String line) throws IllegalArgumentException {
	return parseShop(line.split(","));
    }

    // TODO 2. location
    public static Shop parseShop(CommandContext args)
	    throws IllegalArgumentException {
	return null;
	// TODO: Add parseShop command.
    }

    public void remove(Product product) {
	this.products.remove(product);
    }

    @SuppressWarnings("boxing")
    public String toCSV() {
	return Message.combine(
		",", // This is the separator
		"'" + this.name + "'", this.infiniteFunding, this.funds,
		Format.parseString(this.maxTransactionSize));
    }

    @Override
    public String toString() // TODO: Add proper spacing between columns.
    {
	String line = "";
	for (Product product : this.products) {
	    byte data = product.getData();
	    String subtype = (data == 0 ? "" : "{BKT}:{PRM}" + data);
	    line += "{CMD}" + product.getType() + subtype + " {CMD}"
		    + product.getName() + subtype + " {}Bundle: {PRM}"
		    + product.getBundleSize() + "{} Buy: {PRM}"
		    + product.getBuyPrice() + " {}Sell: {PRM}"
		    + product.getSellPrice() + "\n";
	}
	return line.substring(0, line.length() - 1); // Remove the extra '\n'.
    }
}
