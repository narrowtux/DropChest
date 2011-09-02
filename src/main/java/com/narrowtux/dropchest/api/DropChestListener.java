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

package com.narrowtux.dropchest.api;

import org.bukkit.event.CustomEventListener;
import org.bukkit.event.Event;

public class DropChestListener extends CustomEventListener {
	public void onDropChestSuck(DropChestSuckEvent event){

	}

	public void onDropChestFill(DropChestFillEvent event){

	}

	@Override
	public void onCustomEvent(Event e){
		if(e.getEventName().equals("DropChestSuckEvent")){
			DropChestSuckEvent event = (DropChestSuckEvent)e;
			onDropChestSuck(event);
		}
		if(e.getEventName().equals("DropChestFillEvent")){
			DropChestFillEvent event = (DropChestFillEvent)e;
			onDropChestFill(event);
		}
	}
}
