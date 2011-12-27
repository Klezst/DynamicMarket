package dynamicmarket.event;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;

import dynamicmarket.core.DynamicMarket;
import dynamicmarket.core.Shop;

public class DynamicCreateShopAreaListener extends BlockListener {
    private Shop shop;
    private Player creator;
    private Location pos1;
    private Location pos2;
    private static final Material edit = Material.GLOWSTONE;
    private static final int terminateTime = 10000;
    private Thread abortThread;
    private ArrayList<Thread> fakeThread;

    public DynamicCreateShopAreaListener(Player creator, Shop shop) {
	this.creator = creator;
	this.shop = shop;
	this.fakeThread = new ArrayList<Thread>();
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

    public boolean same(Player player, Shop shop2) {
	return samePlayer(player) && this.shop.getId() == shop2.getId();
    }

    public boolean samePlayer(Player player) {
	return this.creator.equals(player);
    }

    // TODO: use Bukkit sheduleManager
    public void reset(boolean restart) {
	for (int i = 0; i < this.fakeThread.size(); i++) {
	    this.fakeThread.get(i).interrupt();
	}
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
		// server lag fix
		sendFakePackageThread(this.pos1);
		this.creator.sendBlockChange(this.pos1, edit, (byte) 0);
		this.creator.sendMessage("Save pos1");
	    } else if (this.pos2 == null) {
		this.pos2 = event.getBlock().getLocation();
		// server lag fix
		sendFakePackageThread(this.pos2);
		this.creator.sendBlockChange(this.pos2, edit, (byte) 0);
		this.creator
			.sendMessage("Save pos2 ... break block to confirm type new area");
	    } else {
		this.abortThread.interrupt();
		this.creator.sendMessage("save all");
		savePosition();
		reset(false);
	    }
	}
    }

    private void savePosition() {
	this.shop.setPos1x(this.pos1.getBlockX());
	this.shop.setPos1y(this.pos1.getBlockY());
	this.shop.setPos1z(this.pos1.getBlockZ());
	this.shop.setPos2x(this.pos2.getBlockX());
	this.shop.setPos2y(this.pos2.getBlockY());
	this.shop.setPos2z(this.pos2.getBlockZ());
	DynamicMarket.INSTANCE.getDatabase().update(this.shop);
    }

    private void sendFakePackageThread(final Location pos) {
	Thread t = new Thread() {
	    @SuppressWarnings("synthetic-access")
	    @Override
	    public void run() {
		for (int i = 0; i < 5; i++) {
		    DynamicCreateShopAreaListener.this.creator.sendBlockChange(
			    pos, edit, (byte) 0);
		    try {
			Thread.sleep(1000);
		    } catch (Exception e) {
			// stop loop
			return;
		    }
		}
	    }
	};
	t.start();
	this.fakeThread.add(t);
    }
}
