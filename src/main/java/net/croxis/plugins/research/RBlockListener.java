package net.croxis.plugins.research;

import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import java.util.HashSet;

public class RBlockListener implements Listener{
	@EventHandler()
	public void onBlockBreak(BlockBreakEvent event){
		if (event.getPlayer().getGameMode().equals(GameMode.CREATIVE))
			return;
		if (event.getPlayer().hasPermission("research")){
			if(TechManager.players.get(event.getPlayer()).cantBreak.contains(event.getBlock().getTypeId()))
				event.setCancelled(true);
		}
	}
	
	@EventHandler()
	public void onBlockPlace(BlockPlaceEvent event){
		if (event.getPlayer().getGameMode().equals(GameMode.CREATIVE))
			return;
		if (event.getPlayer().hasPermission("research")){
			if(TechManager.players.get(event.getPlayer()).cantPlace.contains(event.getBlock().getTypeId()))
				event.setCancelled(true);
		}
	}
}
