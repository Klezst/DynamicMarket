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

package dynamicmarket;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;

public class Market {
    private List<Shop> shops;

    public Market() {
	this(new ArrayList<Shop>());
    }

    public Market(List<Shop> shops) {
	this.shops = shops;
    }

    public List<Shop> getShops() {
	return this.shops;
    }

    public void setShops(List<Shop> shops) {
	this.shops = shops;
    }

    public void addShop(Shop shop) {
	this.shops.add(shop);
    }

    public Shop getShop(Location loc) throws IllegalArgumentException {
	return this.shops.get(0);
    }
}
