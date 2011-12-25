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

import com.gmail.klezst.util.settings.Validatable;

public enum Setting implements Validatable {
    // Do not use primitive data types or null.
    VERSION("version", Double.class), // Double.class is the class of the type of data you expect to get from the config.yml.
    DRIVER("database.driver", String.class),
    URL("database.url", String.class),
    USERNAME("database.username", String.class),
    PASSWORD("database.password", String.class),
    ISOLATION("database.isolation", String.class),
    LOGGING("database.logging", Boolean.class),
    IMPORT_EXPORT_PATH("import-export-path", String.class),
    TRANSACTION_LOGGING("transaction-logging", Boolean.class);

    private String key;
    private Class<?> type;

    private Setting(String key, Class<?> type) {
	this.key = key;
	this.type = type;
    }

    @Override
    public String getKey() {
	return this.key;
    }

    @Override
    public Class<?> getType() {
	return this.type;
    }

    @Override
    public Object validate(Object value) {
	return value;
    }
}
