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

    }

    public Transaction(DynamicMarket plugin, int amount, Player player,
	    String id) {
	if (!Economy.isLoaded()) {
	    Message.send(player, "{ERR}The economy isn't loaded!");
	}

	Shop shop;
	MaterialData data;
	Product product;
	try {
	    // throwsIllegalArgumentException, if no shop at the player'slocation.
	    shop = plugin.getMarket().getShop(player.getLocation());
	    // throws IllegalArgumentException, if id is not a valid MaterialData.
	    data = Util.getMaterialData(id);
	    // throws IllegalArgumentException, if shop doesn't sell data.
	    product = shop.getProduct(data);
	} catch (IllegalArgumentException e) {
	    player.sendMessage(Message.parseColor("{ERR}" + e.getMessage()));
	    return;
	}

	int volume = amount * product.getBundleSize();
	if (Math.abs(volume) > shop.getMaxTransactionSize()) {
	    player.sendMessage(Message
		    .parseColor("{ERR}You can't buy that much at once!"));
	    return;
	}

	if (!product.hasStock(amount)) {
	    player.sendMessage(Message.parseColor("{ERR}" + shop.getName()
		    + " doesn't have enough "
		    + (volume < 0 ? "space" : "stock") + "."));
	    return;
	}

	double price;
	double bundles;
	HashMap<Integer, ItemStack> overflow;
	if (volume > 0) {
	    if (!product.isBuyable()) {
		player.sendMessage(Message.parseColor("{ERR}" + shop.getName()
			+ " refuses to sell " + id + " to you!"));
		return;
	    }

	    price = amount * product.getBuyPrice();
	    if (Economy.getBalance(player.getName()) < price) {
		player.sendMessage(Message
			.parseColor("{ERR}You don't have enough money!"));
		return;
	    }

	    overflow = player.getInventory().addItem(data.toItemStack(volume));

	    for (int i = 0; overflow.containsKey(i); i++) {
		volume -= overflow.get(i).getAmount();
	    }

	    if (volume == 0) {
		player.sendMessage(Message
			.parseColor("{ERR}You don't have enough space in your inventory!"));
		return;
	    }

	    bundles = 1.0 * volume / product.getBundleSize();
	    price = product.getBuyPrice() * bundles;
	} else {
	    if (!product.isSellable()) {
		player.sendMessage(Message.parseColor("{ERR}" + shop.getName()
			+ "Refuses to sell " + id + " to you!"));
		return;
	    }

	    price = product.getSellPrice();
	    if (!shop.isInfiniteFunding() && shop.getFunds() < -price * amount) {
		Message.send(player, "{ERR}" + shop.getName()
			+ " doesn't have enough money!");
		return;
	    }

	    overflow = player.getInventory().removeItem(
		    data.toItemStack(-volume));

	    for (int i = 0; overflow.containsKey(i); i++) {
		volume += overflow.get(i).getAmount();
	    }

	    if (volume == 0) {
		player.sendMessage(Message
			.parseColor("{ERR}You don't have enough of that in your inventory!"));
		return;
	    }

	    bundles = 1.0 * volume / product.getBundleSize();
	    price *= bundles;
	}

	Economy.deltaBalance(-price, player.getName());

	product.setStock(product.getStock() - volume / product.getBundleSize());

	shop.setFunds(Util.round(shop.getFunds() + price, 2));
	plugin.getDatabase().update(shop);

	// Log transaction.
	Message.send(
		player,
		"{}You " + (volume > 0 ? "bought " : "sold ") + "{PRM}"
			+ Math.abs(volume) + " {}" + id + " for {PRM}"
			+ Math.abs(price));

	this.time = System.currentTimeMillis() / 1000;
	this.who = player.getName();
	this.shop = shop.getName();
	this.item = data.getItemType().toString();
	this.volume = volume;
	this.price = price;

	if (plugin.getSetting(Setting.TRANSACTION_LOGGING, Boolean.class)) {
	    plugin.getDatabase().save(this);
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
