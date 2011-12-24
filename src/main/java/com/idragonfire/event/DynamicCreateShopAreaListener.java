package com.idragonfire.event;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;

public class DynamicCreateShopAreaListener extends BlockListener {
    private Player creator;
    private Location pos1;
    private Location pos2;
    private static final Material edit = Material.GLOWSTONE;
    private static final int terminateTime = 10000;
    private Thread abortThread;

    public DynamicCreateShopAreaListener(Player creator) {
	this.creator = creator;
	initThread();
	reset(true);
    }

    private void initThread() {
	this.abortThread = new Thread() {

	    @SuppressWarnings("synthetic-access")
	    @Override
	    public void run() {
		try {
		    Thread.sleep(terminateTime);
		} catch (Exception e) {
		    // reset interupt and remove Listener
		    return;
		}
		DynamicCreateShopAreaListener.this.creator
			.sendMessage("Area abort");
		reset(false);
	    }
	};
    }

    public boolean samePlayer(Player player) {
	return this.creator.equals(player);
    }

    // TODO: add plugin
    public void reset(boolean restart) {
	if (this.pos1 != null) {
	    this.creator.sendBlockChange(this.pos1, this.creator.getWorld()
		    .getBlockTypeIdAt(this.pos1), (byte) 0);
	}
	this.pos1 = null;
	if (this.pos2 != null) {
	    this.creator.sendBlockChange(this.pos2, this.creator.getWorld()
		    .getBlockTypeIdAt(this.pos2), (byte) 0);
	}
	this.pos2 = null;
	DynamicMarketMasterShopAreaListener.INSTANCE.removeListener(this);
	if (restart) {
	    initThread();
	    this.abortThread.start();
	}
    }

    @Override
    public void onBlockBreak(BlockBreakEvent event) {
	if (samePlayer(event.getPlayer())) {
	    event.setCancelled(true);
	    if (this.pos1 == null) {
		this.pos1 = event.getBlock().getLocation();
		this.creator.sendBlockChange(this.pos1, edit, (byte) 0);
		this.creator.sendMessage("Save pos1");
	    } else if (this.pos2 == null) {
		this.pos2 = event.getBlock().getLocation();
		this.creator.sendBlockChange(this.pos2, edit, (byte) 0);
		this.creator
			.sendMessage("Save pos2 ... break block to confirm type new area");
	    } else {
		this.abortThread.interrupt();
		this.creator.sendMessage("save all (dummy method)");
		reset(false);
	    }
	}
    }
}
