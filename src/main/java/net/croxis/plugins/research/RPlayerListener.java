package net.croxis.plugins.research;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.HashSet;

@SuppressWarnings("unused")
public class RPlayerListener implements Listener{
	@EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
		if(event.getPlayer().hasPermission("research")){
			TechManager.initPlayer(event.getPlayer());
			Player player = event.getPlayer();
			Tech t = TechManager.getCurrentResearch(player);
			if(t == null){
				t = new Tech();
				t.name = "None";
			}
			if(player.hasPermission("research.logininfo"))
				event.getPlayer().sendMessage("You currently know " + TechManager.getResearched(event.getPlayer()).size() + " technologies" +
        		" and are currently researching " + t.name + ".");
		}
    }
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event){
		if (event.getPlayer().getGameMode().equals(GameMode.CREATIVE))
			return;
		if(event.getPlayer().hasPermission("research") && event.hasItem()){
			Research.logDebug("Interact Event: " + event.getPlayer().getName() + "|" + Integer.toString(event.getItem().getTypeId()));
			if(TechManager.players.get(event.getPlayer()).cantUse.contains(event.getItem().getTypeId())){
				Research.logDebug("Canceling Interaction: " + event.getPlayer().getName() + "|" + event.getMaterial().toString());
				event.setCancelled(true);
			}
		}
	}
}
