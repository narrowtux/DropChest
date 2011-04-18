package com.narrowtux.DropChest;

import java.io.File;

public class Configuration {
	private File file;
	private FlatFileReader reader;
	private boolean dropItemsOnRedstone = false;
	private boolean letUsersProtectChests = false;
	private int defaultRadius = 2;
	private int fallbackRadius = 20;

	public Configuration(File file){
		this.file = file;
		load();
	}

	private void load() {
		
	}
}
