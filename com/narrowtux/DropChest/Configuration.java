package com.narrowtux.DropChest;

import java.io.File;

public class Configuration {
	private File file;
	private FlatFileReader reader;
	private boolean dropItemsOnRedstone;
	private boolean letUsersProtectChests;
	private int defaultRadius;
	private int fallbackRadius;
	private int idleTimeAfterRedstone;

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
}
