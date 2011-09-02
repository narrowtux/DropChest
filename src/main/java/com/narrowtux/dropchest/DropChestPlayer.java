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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.bukkit.entity.Player;

public class DropChestPlayer {
	@SuppressWarnings("unused")
	private String playerName = "";
	private FilterType editingFilterType = FilterType.SUCK;
	private int requestedRadius = 2;
	private ChestRequestType chestRequestType = ChestRequestType.NONE;
	private List<DropChestItem> dropChests = new ArrayList<DropChestItem>();
	private boolean editingFilter = false;

	public boolean isEditingFilter() {
		return editingFilter;
	}


	public void setEditingFilter(boolean editingFilter) {
		this.editingFilter = editingFilter;
	}

	private static HashMap<String, DropChestPlayer> players = new HashMap<String, DropChestPlayer>();
	public static DropChest plugin;


	private DropChestPlayer(String p){
		playerName = p;
	}


	public FilterType getEditingFilterType() {
		return editingFilterType;
	}

	public void setEditingFilterType(FilterType editingFilterType) {
		this.editingFilterType = editingFilterType;
	}

	public int getRequestedRadius() {
		return requestedRadius;
	}

	public void setRequestedRadius(int requestedRadius) {
		this.requestedRadius = requestedRadius;
	}

	public ChestRequestType getChestRequestType() {
		return chestRequestType;
	}

	public void setChestRequestType(ChestRequestType chestRequestType) {
		this.chestRequestType = chestRequestType;
	}
	public Player getPlayer() {
		return plugin.getServer().getPlayer(playerName);
	}

	public static DropChestPlayer getPlayerByName(String name){
		if(players.containsKey(name)){
			return players.get(name);
		} else {
			DropChestPlayer p = new DropChestPlayer(name);
			players.put(name, p);
			return p;
		}
	}

	public void sendMessage(String message){
		for(String line:message.split("\n")){
			getPlayer().sendMessage(line);
		}
	}

	public void addChest(DropChestItem item){
		dropChests.add(item);
	}

	public void removeChest(DropChestItem item){
		dropChests.remove(item);
	}

	public List<DropChestItem> getChests(){
		return Collections.unmodifiableList(dropChests);
	}
}
