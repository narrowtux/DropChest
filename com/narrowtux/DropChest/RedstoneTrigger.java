package com.narrowtux.DropChest;

import java.util.TimerTask;

public class RedstoneTrigger extends TimerTask {
	private DropChestItem item;
	
	public RedstoneTrigger(DropChestItem item){
		this.item = item;
	}

	@Override
	public void run() {
		item.setRedstone(false);
	}
	
}
