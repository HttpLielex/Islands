package de.lielex.islands;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;

public class IslandsListener implements Listener{

    private IslandsPlugin plugin;

    public IslandsListener(final IslandsPlugin plugin){
        this.plugin=plugin;
        this.plugin.getServer().getPluginManager().registerEvents(this,plugin);
    }

    @EventHandler
    public void onJoin(final PlayerJoinEvent e){
        final Player p=e.getPlayer();
        final Island island=Island.loadedIslands.get(p.getUniqueId());
    }

    @EventHandler
    public void onQuit(final PlayerQuitEvent e){
        final Player p=e.getPlayer();
        final Island island=Island.loadedIslands.get(p.getUniqueId());
    }


    @EventHandler
    public void onInteract(final PlayerInteractEvent e){
        final Player p=e.getPlayer();
        if(p.isOp()||p.hasPermission("*")) return;
        if(!IslandsManager.canInteract(p)){
            p.sendMessage("§7You are not allowed to interact here!");
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onWalk(final PlayerMoveEvent e){
        final Player p=e.getPlayer();
        if(!IslandsManager.isInIslandWorld(p)) return;
        if(!e.getFrom().equals(e.getTo())){
            if(p.getLocation().distance(p.getWorld().getSpawnLocation()) >= IslandsManager.maxIslandDistance){
                p.playSound(p.getLocation(),Sound.ENTITY_ENDERMAN_TELEPORT,1f,1f);
                p.sendMessage(IslandsPlugin.prefix+"§7You were too far from your island! Teleport home ...");
                p.teleport(p.getWorld().getSpawnLocation());
            }
        }
    }

    @EventHandler
    public void onTeleport(final PlayerTeleportEvent e){
        final Player p=e.getPlayer();
        if(!p.getWorld().equals(e.getTo().getWorld())) return;
        if(!IslandsManager.isInIslandWorld(p)) return;
        if(e.getTo().distanceSquared(p.getWorld().getSpawnLocation()) >= IslandsManager.maxIslandDistance){
            p.playSound(p.getLocation(),Sound.ENTITY_ENDERMAN_TELEPORT,1f,1f);
            p.sendMessage(IslandsPlugin.prefix+"§7You were too far from your island! Teleport home ...");
            p.teleport(p.getWorld().getSpawnLocation());
        }
    }
}
