package com.narrowtux.DropChest;
import java.util.HashMap;
import java.util.logging.Level;

import org.bukkit.entity.Player;

public class DropChestPlayer {
	private Player player = null;
	private FilterType editingFilterType = FilterType.SUCK;
	private int requestedRadius = 2;
	private ChestRequestType chestRequestType = ChestRequestType.NONE;
	private boolean editingFilter = false;
	public boolean isEditingFilter() {
		return editingFilter;
	}


	public void setEditingFilter(boolean editingFilter) {
		this.editingFilter = editingFilter;
	}

	private static HashMap<String, DropChestPlayer> players = new HashMap<String, DropChestPlayer>();
	public static DropChest plugin;
	

	public DropChestPlayer(Player p){
		player = p;
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
		return player;
	}
	
	public void setPlayer(Player player) {
		this.player = player;
	}
	
	public static DropChestPlayer getPlayerByName(String name){
		if(players.containsKey(name)){
			return players.get(name);
		} else {
			Player pl = plugin.getServer().getPlayer(name);
			if(pl!=null){
				DropChestPlayer p = new DropChestPlayer(pl);
				players.put(name, p);
				return p;
			}
		}
		plugin.log.log(Level.WARNING,"Player "+name+" not found.");
		return null;
	}
}
