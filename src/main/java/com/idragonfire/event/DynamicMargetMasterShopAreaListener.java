package com.idragonfire.event;

import java.util.Vector;

import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;

/**
 * needed because, unregister event doesn't exists :-/ <br>
 * https://github.com/Bukkit/Bukkit/pull/108
 * 
 * @author IDragonfire
 */
public class DynamicMargetMasterShopAreaListener extends BlockListener {
    public static final DynamicMargetMasterShopAreaListener INSTANCE = new DynamicMargetMasterShopAreaListener();
    private Vector<DynamicCreateShopAreaListener> listeners;
    private boolean active;

    private DynamicMargetMasterShopAreaListener() {
	this.listeners = new Vector<DynamicCreateShopAreaListener>();
    }

    public void addListener(Player newCreator) {
	System.out.println("add Listener");
	for (int i = 0; i < this.listeners.size(); i++) {
	    if (this.listeners.get(i).samePlayer(newCreator)) {
		this.listeners.get(i).reset(true);
		return;
	    }
	}
	this.listeners.add(new DynamicCreateShopAreaListener(newCreator));
	this.active = true;
	System.out.println("Listener added");
    }

    public void removeListener(DynamicCreateShopAreaListener oldListener) {
	this.listeners.remove(oldListener);
	if (this.listeners.size() <= 0) {
	    this.active = false;
	}
    }

    @Override
    public void onBlockBreak(BlockBreakEvent event) {
	if (this.active) {
	    for (int i = 0; i < this.listeners.size(); i++) {
		this.listeners.get(i).onBlockBreak(event);
	    }
	}
    }

}
