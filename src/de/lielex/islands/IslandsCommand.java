package de.lielex.islands;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class IslandsCommand implements CommandExecutor, TabCompleter{

    private IslandsPlugin plugin;
    private String prefix;

    public IslandsCommand(final IslandsPlugin plugin){
        this.plugin=plugin;
        this.prefix=IslandsPlugin.prefix;
        this.plugin.getCommand("islands").setExecutor(this);
        this.plugin.getCommand("island").setTabCompleter(this);
    }

    public static void sendHelp(final CommandSender p){
        p.sendMessage("§7=============[ §aIslands §7]=============");
        p.sendMessage(" ");
        p.sendMessage("§8> §7§lAlle Befehle:");
        p.sendMessage("§8- §e/islands§8: Shows this page");
        p.sendMessage("§8- §e/islands create§8: Create an island");
        p.sendMessage("§8- §e/islands delete§8: Delete an island");
        p.sendMessage("§8- §e/islands home [Player]§8: Teleport you to an island");
        p.sendMessage("§8- §e/islands add <Player>§8: Adds a player to your island");
        p.sendMessage("§8- §e/islands remove <Player>§8: Removes a player from your island");
        p.sendMessage("§8- §e/islands list [Player]§8: List all players on an island");
        p.sendMessage(" ");
        p.sendMessage("§7=============[ §aIslands §7]=============");
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean onCommand(final CommandSender sender,final Command cmd,final String label,final String[] args){
        if(sender instanceof Player){
            final Player p=(Player)sender;
            if(args.length==0){
                sendHelp(p);
            }else{
                switch(args[0]){
                    case "create":
                        if(!IslandsManager.hasIsland(p.getUniqueId())){
                            p.sendMessage(prefix+"§7Create island...");
                            IslandsManager.create(p.getUniqueId());
                        }else{
                            p.sendMessage(prefix+"§7You already have an island!");
                        }
                        return true;
                    case "delete":
                        if(args.length==1){
                            if(IslandsManager.hasIsland(p.getUniqueId())){
                                IslandsManager.delete(Island.loadedIslands.get(p.getUniqueId()));
                            }else{
                                p.sendMessage(prefix+"§7You don't have an island!");
                            }
                        }else{
                            if(p.hasPermission("islands.admin")){
                                if(args.length!=2){
                                    final OfflinePlayer player=Bukkit.getOfflinePlayer(args[2]);
                                    if(IslandsManager.hasIsland(player.getUniqueId())){
                                        IslandsManager.delete(Island.loadedIslands.get(player.getUniqueId()));
                                        p.sendMessage(prefix+"§7You deleted the island from §c"+args[2]+"§7!");
                                    }else{
                                        p.sendMessage(prefix+"§7The player §c"+args[2]+" §7has no island!");
                                    }
                                }else{
                                    p.sendMessage(prefix+"§7Usage: §c/islands delete [Player]");
                                }
                            }else{
                                p.sendMessage(prefix+"§7You don't have permission to execute this Sub-Command!");
                            }
                        }
                        return true;
                    case "home":
                        if(args.length==1){
                            if(IslandsManager.hasIsland(p.getUniqueId())){
                                World world=Bukkit.getWorld(Island.loadedIslands.get(p.getUniqueId()).getWorld());
                                if(world==null){
                                    world=IslandsManager.getWorld(Island.loadedIslands.get(p.getUniqueId()).getWorld());
                                }
                                if(world!=null){
                                    p.teleport(world.getSpawnLocation());
                                    p.playSound(p.getLocation(),Sound.ENTITY_ENDERMAN_TELEPORT,1f,1f);
                                    p.sendMessage(prefix+"§7You have been teleported!");
                                }else{
                                    p.sendMessage(prefix+"§cA ERROR OCCURRED! PLEASE CONTACT AN ADMIN!");
                                }
                            }else{
                                p.sendMessage(prefix+"§cYou don't have an island!");
                            }
                        }else{
                            final OfflinePlayer player=Bukkit.getOfflinePlayer(args[1]);
                            if(IslandsManager.hasIsland(player.getUniqueId())&&Island.loadedIslands.get(player.getUniqueId()).
                                    getMembers().contains(p.getUniqueId())){
                                final World world=IslandsManager.getWorld(Island.loadedIslands.get(player.getUniqueId()).getWorld());
                                if(world!=null){
                                    p.teleport(world.getSpawnLocation());
                                    p.playSound(p.getLocation(),Sound.ENTITY_ENDERMAN_TELEPORT,1f,1f);
                                    p.sendMessage(prefix+"§7You have been teleported!");
                                }else{
                                    p.sendMessage(prefix+"§cA ERROR OCCURRED! PLEASE CONTACT AN ADMIN!");
                                }
                            }else{
                                p.sendMessage(prefix+"§7This player doesn't have an island!");
                            }
                        }
                        return true;
                    case "add":
                        if(IslandsManager.hasIsland(p.getUniqueId())){
                            if(args.length!=1){
                                if(p.getName().equalsIgnoreCase(args[1])){
                                    p.sendMessage(prefix+"§7You can't add yourself!");
                                    return true;
                                }
                                final OfflinePlayer player=Bukkit.getOfflinePlayer(args[1]);
                                final Island island=Island.loadedIslands.get(p.getUniqueId());
                                if(!island.getMembers().contains(player.getUniqueId())){
                                    island.addMember(player.getUniqueId());
                                    p.sendMessage(prefix+"§7You have added §c"+args[1]+" §7to your island!!");
                                }else{
                                    p.sendMessage(prefix+"§7This player already belongs to your island!");
                                }
                            }else{
                                p.sendMessage(prefix+"§7Usage: §c/islands add <Player>");
                            }
                        }else{
                            p.sendMessage(prefix+"§7You don't have an island!");
                        }
                        return true;
                    case "remove":
                        if(IslandsManager.hasIsland(p.getUniqueId())){
                            if(args.length!=1){
                                if(p.getName().equalsIgnoreCase(args[1])){
                                    p.sendMessage(prefix+"§7You cannot remove yourself!");
                                    return true;
                                }
                                final OfflinePlayer player=Bukkit.getOfflinePlayer(args[1]);
                                final Island island=Island.loadedIslands.get(p.getUniqueId());
                                if(island.getMembers().contains(player.getUniqueId())){
                                    island.removeMember(player.getUniqueId());
                                    p.sendMessage(prefix+"§7You removed §c"+args[1]+" §7from your island!");
                                }else{
                                    p.sendMessage(prefix+"§7This player does not belong to your island!");
                                }
                            }else{
                                p.sendMessage(prefix+"§7Usage: §c/islands remove <Player>");
                            }
                        }else{
                            p.sendMessage(prefix+"§7You don't have an island!");
                        }
                        return true;
                    case "list":
                        Island island=null;
                        if(args.length==1){
                            if(IslandsManager.hasIsland(p.getUniqueId())){
                                island=Island.loadedIslands.get(p.getUniqueId());
                            }else{
                                p.sendMessage(prefix+"§7Usage: §c/islands list <Player>");
                                return true;
                            }
                        }else{
                            final OfflinePlayer player=Bukkit.getOfflinePlayer(args[1]);
                            if(IslandsManager.hasIsland(player.getUniqueId())){
                                island=Island.loadedIslands.get(p.getUniqueId());
                            }else{
                                p.sendMessage(prefix+"§7This player doesn't have an island!");
                                return true;
                            }
                        }
                        if(island!=null){
                            p.sendMessage("§7=============[ §aIslands §7]=============");
                            p.sendMessage(" ");
                            p.sendMessage("§8> §7Owner§8: §e"+Bukkit.getOfflinePlayer(island.getOwner()).getName());
                            String members="";
                            final List<UUID> list=island.getMembers();
                            for(UUID member: list){
                                if(!list.get(list.size()-1).equals(member)){
                                    members+=Bukkit.getOfflinePlayer(member).getName()+"§7, §e";
                                }else{
                                    members+=Bukkit.getOfflinePlayer(member).getName();
                                }
                            }
                            p.sendMessage("§8> §7Members§8: §e"+members);
                            p.sendMessage(" ");
                            p.sendMessage("§7=============[ §aIslands §7]=============");
                            p.playSound(p.getLocation(),Sound.UI_BUTTON_CLICK,1f,1f);
                        }else{
                            p.sendMessage(prefix+"§cA ERROR OCCURRED!");
                        }
                        return true;
                }
                sendHelp(p);
            }
        }else{
            sender.sendMessage("§cThis command can only be executed by a player!");
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender,Command cmd,String label,String[] args){
        if(sender instanceof Player)
            if(cmd.getName().equalsIgnoreCase("islands")||cmd.getName().equalsIgnoreCase("is")){
                List<String> completions=new ArrayList<>();
                if(args.length==1){
                    completions.add("create");
                    completions.add("delete");
                    completions.add("home");
                    completions.add("add");
                    completions.add("remove");
                    completions.add("list");
                    return (List<String>)StringUtil.copyPartialMatches(args[0],completions,new ArrayList());
                }
            }
        return null;
    }
}
