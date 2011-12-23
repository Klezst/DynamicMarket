/*
	Shopaholic
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

import java.util.HashMap;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import com.avaje.ebean.validation.NotEmpty;
import com.avaje.ebean.validation.NotNull;
import com.gmail.haloinverse.DynamicMarket.util.Economy;
import com.gmail.haloinverse.DynamicMarket.util.Message;
import com.gmail.haloinverse.DynamicMarket.util.Util;
import com.idragonfire.data.DMsg;
import com.idragonfire.data.DMsg.MsgKey;
import com.idragonfire.event.DynamicMarketException;

@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    private int id;

    @NotNull
    private long time;

    @NotEmpty
    private String who;

    @NotEmpty
    private String shop;

    @NotEmpty
    private String item;

    @NotNull
    private int volume;

    @NotNull
    private double price;

    public Transaction() {
	// TODO: ? - IDragonfire
    }

    public Transaction(DynamicMarket plugin, int amount, Player player,
	    String matrialid) {
	if (!Economy.isLoaded()) {
	    Message.send(player, "{ERR}The economy isn't loaded!");
	} else {
	    commitTransaction(plugin, amount, player, matrialid);
	}
    }

    // TODO: remove items by Exceptions
    @SuppressWarnings("boxing")
    private void commitTransaction(DynamicMarket plugin, int amount,
	    Player player, String matrialid) {
	MaterialData data;
	Product product;
	try {
	    // throwsIllegalArgumentException, if no shop at the player'slocation.
	    Shop newShop = plugin.getMarket().getShop(player.getLocation());
	    // throws IllegalArgumentException, if id is not a valid MaterialData.
	    data = Util.getMaterialData(matrialid);
	    // throws IllegalArgumentException, if shop doesn't sell data.
	    product = newShop.getProduct(data);

	    int newVolume = amount * product.getBundleSize();
	    if (Math.abs(newVolume) > newShop.getMaxTransactionSize()) {
		throw new DynamicMarketException(
			DMsg.INSTANCE.get(MsgKey.BUY_TOMUCH));
	    }

	    if (!product.hasStock(amount)) {
		if (newVolume < 0) {
		    throw new DynamicMarketException(
			    DMsg.INSTANCE.get(
				    MsgKey.BUY_NOSPACE,
				    new String[][] { { "shop",
					    newShop.getName() } }));
		}
		throw new DynamicMarketException(DMsg.INSTANCE.get(
			MsgKey.SELL_NOSTOCK, new String[][] { { "shop",
				newShop.getName() } }));
	    }

	    double newPrice, newBundles;
	    HashMap<Integer, ItemStack> overflow;
	    if (newVolume > 0) {
		if (!product.isBuyable()) {
		    throw new DynamicMarketException("{ERR}"
			    + newShop.getName() + " refuses to sell "
			    + matrialid + " to you!");
		}

		newPrice = amount * product.getBuyPrice();
		if (Economy.getBalance(player.getName()) < newPrice) {
		    throw new DynamicMarketException(
			    "{ERR}You don't have enough money!");
		}

		overflow = player.getInventory().addItem(
			data.toItemStack(newVolume));

		for (int i = 0; overflow.containsKey(i); i++) {
		    newVolume -= overflow.get(i).getAmount();
		}

		if (newVolume == 0) {
		    throw new DynamicMarketException(
			    "{ERR}You don't have enough space in your inventory!");
		}

		newBundles = 1.0 * newVolume / product.getBundleSize();
		newPrice = product.getBuyPrice() * newBundles;
	    } else {
		if (!product.isSellable()) {
		    throw new DynamicMarketException("{ERR}"
			    + newShop.getName() + "Refuses to sell "
			    + matrialid + " to you!");
		}

		newPrice = product.getSellPrice();
		if (!newShop.isInfiniteFunding()
			&& newShop.getFunds() < -newPrice * amount) {
		    throw new DynamicMarketException("{ERR}"
			    + newShop.getName() + " doesn't have enough money!");
		}

		overflow = player.getInventory().removeItem(
			data.toItemStack(-newVolume));

		for (int i = 0; overflow.containsKey(i); i++) {
		    newVolume += overflow.get(i).getAmount();
		}

		if (newVolume == 0) {
		    throw new DynamicMarketException(
			    "{ERR}You don't have enough of that in your inventory!");
		}

		newBundles = 1.0 * newVolume / product.getBundleSize();
		newPrice *= newBundles;
	    }

	    Economy.deltaBalance(-newPrice, player.getName());

	    product.setStock(product.getStock() - newVolume
		    / product.getBundleSize());

	    newShop.setFunds(Util.round(newShop.getFunds() + newPrice, 2));
	    plugin.getDatabase().update(product);

	    // Log transaction.
	    Message.send(player,
		    "{}You " + (newVolume > 0 ? "bought " : "sold ") + "{PRM}"
			    + Math.abs(newVolume) + " {}" + matrialid
			    + " for {PRM}" + Math.abs(newPrice));

	    this.time = System.currentTimeMillis() / 1000;
	    this.who = player.getName();
	    this.shop = newShop.getName();
	    this.item = data.getItemType().toString();
	    this.volume = newVolume;
	    this.price = newPrice;

	    if (plugin.getSetting(Setting.TRANSACTION_LOGGING, Boolean.class)) {
		plugin.getDatabase().save(this);
	    }

	} // TODO: 1.add better exception handling
	catch (IllegalArgumentException e) {
	    player.sendMessage(Message.parseColor("{ERR}" + e.getMessage()));
	} catch (DynamicMarketException e) {
	    player.sendMessage(Message.parseColor("{ERR}" + e.getMessage()));
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public int getId() {
	return this.id;
    }

    public void setId(int id) {
	this.id = id;
    }

    public long getTime() {
	return this.time;
    }

    public void setTime(long time) {
	this.time = time;
    }

    public String getWho() {
	return this.who;
    }

    public void setWho(String who) {
	this.who = who;
    }

    public String getShop() {
	return this.shop;
    }

    public void setShop(String shop) {
	this.shop = shop;
    }

    public String getItem() {
	return this.item;
    }

    public void setItem(String item) {
	this.item = item;
    }

    public int getVolume() {
	return this.volume;
    }

    public void setVolume(int volume) {
	this.volume = volume;
    }

    public double getPrice() {
	return this.price;
    }

    public void setPrice(double price) {
	this.price = price;
    }
}
