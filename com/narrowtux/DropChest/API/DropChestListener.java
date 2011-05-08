package com.narrowtux.DropChest.API;

import org.bukkit.event.CustomEventListener;
import org.bukkit.event.Event;

public class DropChestListener extends CustomEventListener {
	public void onDropChestSuck(DropChestSuckEvent event){
		
	}
	
	@Override
	public void onCustomEvent(Event e){
		if(e.getEventName().equals("DropChestSuckEvent")){
			DropChestSuckEvent event = (DropChestSuckEvent)e;
			onDropChestSuck(event);
		}
	}
}
