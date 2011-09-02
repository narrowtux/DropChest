/*
 * Copyright (C) 2011 Moritz Schmale <narrow.m@gmail.com>
 *
 * DropChest is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/gpl.html>.
 */

package com.narrowtux.dropchest;

import java.io.File;

public class Configuration {
	private File file;
	private FlatFileReader reader;
	private boolean dropItemsOnRedstone;
	private boolean letUsersProtectChests;
	private int defaultRadius;
	private int fallbackRadius;
	private int idleTimeAfterRedstone;
	private double warnFillStatus;
	private String warnString;

	public Configuration(File file){
		this.file = file;
		reader = new FlatFileReader(this.file, false);
		load();
	}

	private void load() {
		dropItemsOnRedstone = reader.getBoolean("droponredstone", false);
		letUsersProtectChests = reader.getBoolean("usercanprotectchest", true);
		defaultRadius = reader.getInteger("defaultradius", 2);
		fallbackRadius = reader.getInteger("fallbackradius", 20);
		idleTimeAfterRedstone = reader.getInteger("waitafterdrop", 10);
		warnFillStatus = reader.getDouble("warnfillstatus", 80);
		warnString = reader.getString("warnmessage", "Your chest $name is nearly full($fill%)");
	}

	/**
	 * @return the idleTimeAfterRedstone
	 */
	public int getIdleTimeAfterRedstone() {
		return idleTimeAfterRedstone;
	}

	/**
	 * @return the dropItemsOnRedstone
	 */
	public boolean isDropItemsOnRedstone() {
		return dropItemsOnRedstone;
	}

	/**
	 * @return the letUsersProtectChests
	 */
	public boolean isLetUsersProtectChests() {
		return letUsersProtectChests;
	}

	/**
	 * @return the defaultRadius
	 */
	public int getDefaultRadius() {
		return defaultRadius;
	}

	/**
	 * @return the fallbackRadius
	 */
	public int getFallbackRadius() {
		return fallbackRadius;
	}

	/**
	 * @return the warnFillStatus
	 */
	public double getWarnFillStatus() {
		return warnFillStatus;
	}

	/**
	 * @return the warnString
	 */
	public String getWarnString() {
		return warnString;
	}
}
