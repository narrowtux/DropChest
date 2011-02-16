package com.narrowtux.DropChest;

public enum DropChestMinecartAction {
	IGNORE, //DropChest will ignore StorageMinecarts
	PUSH_TO_MINECART, //DropChest will move its inventory to the Minecart
	PULL_FROM_MINECART, //DropChest will move the inventory of the Minecart to itself
}
