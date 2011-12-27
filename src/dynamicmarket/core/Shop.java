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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.bukkit.Location;
import org.bukkit.material.MaterialData;

import com.avaje.ebean.validation.NotEmpty;
import com.avaje.ebean.validation.NotNull;
import com.sk89q.minecraft.util.commands.CommandContext;

import dynamicmarket.data.Messaging;
import dynamicmarket.event.DynamicMarketException;
import dynamicmarket.util.Format;

@Entity
@Table(name = "dm_shops")
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

    // TODO: own Location class
    private int pos1x;
    private int pos1y;
    private int pos1z;
    private int pos2x;
    private int pos2y;
    private int pos2z;

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
	    throws DynamicMarketException {
	for (Product product : this.products) {
	    if (product.equals(type, data)) {
		return product;
	    }
	}
	throw new DynamicMarketException(this.name + " doesn't stock that!");
    }

    public Product getProduct(MaterialData data) throws DynamicMarketException {
	return getProduct(data.getItemTypeId(), data.getData());
    }

    @SuppressWarnings("boxing")
    public static Shop parseShop(String... args) throws DynamicMarketException {
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
	throw new DynamicMarketException("That is not a valid Shop.");
    }

    public static Shop parseShop(String line) throws DynamicMarketException {
	return parseShop(line.split(","));
    }

    // TODO 2. location
    @Deprecated
    public static Shop parseShop(CommandContext args)
	    throws DynamicMarketException {
	return null;
	// TODO: Add parseShop command.
    }

    public void remove(Product product) {
	this.products.remove(product);
    }

    @SuppressWarnings("boxing")
    public String toCSV() {
	return Messaging.combine(
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

    // TODO: later save min, max if loaded !!
    public boolean isShopInLocation(Location loc) {
	int minX = Math.min(this.pos1x, this.pos2x), maxX = Math.max(
		this.pos1x, this.pos2x);
	int minY = Math.min(this.pos1y, this.pos2y), maxY = Math.max(
		this.pos1y, this.pos2y);
	int minZ = Math.min(this.pos1z, this.pos2z), maxZ = Math.max(
		this.pos1z, this.pos2z);
	return minX <= loc.getX() && loc.getX() <= maxX && minY <= loc.getY()
		&& loc.getY() <= maxY && minZ <= loc.getZ()
		&& loc.getZ() <= maxZ;
    }

    // dirty Location stuff

    public int getPos1x() {
	return this.pos1x;
    }

    public void setPos1x(int pos1x) {
	this.pos1x = pos1x;
    }

    public int getPos1y() {
	return this.pos1y;
    }

    public void setPos1y(int pos1y) {
	this.pos1y = pos1y;
    }

    public int getPos1z() {
	return this.pos1z;
    }

    public void setPos1z(int pos1z) {
	this.pos1z = pos1z;
    }

    public int getPos2x() {
	return this.pos2x;
    }

    public void setPos2x(int pos2x) {
	this.pos2x = pos2x;
    }

    public int getPos2y() {
	return this.pos2y;
    }

    public void setPos2y(int pos2y) {
	this.pos2y = pos2y;
    }

    public int getPos2z() {
	return this.pos2z;
    }

    public void setPos2z(int pos2z) {
	this.pos2z = pos2z;
    }
}
