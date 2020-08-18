package de.lielex.islands;

import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class Island{

    public static final HashMap<UUID,Island> loadedIslands=new HashMap<>();

    public static void loadAll(final YamlConfiguration config){
        if(config.getConfigurationSection("Islands")==null) return;
        for(String is: config.getConfigurationSection("Islands").getKeys(false)){
            loadedIslands.put(UUID.fromString(is),new Island(UUID.fromString(is),IslandsPlugin.stringToStringUUID(config.getStringList("Islands."+is+".Members"))));
        }
    }

    public static Island getByWorld(final World world){
        if(!world.getName().startsWith("island_")) return null;
        return loadedIslands.get(UUID.fromString(world.getName().replace("island_","")));
    }

    private UUID owner;
    private List<UUID> members;

    public Island(final UUID owner,final List<UUID> members){
        this.owner=owner;
        this.members=members;
        loadedIslands.put(owner,this);
    }

    public void toFile(final YamlConfiguration config){
        config.set("Islands."+owner.toString()+".Owner",owner.toString());
        config.set("Islands."+owner.toString()+".Members",IslandsPlugin.uuidToStringList(members));
    }

    public void addMember(final UUID member){
        if(!members.contains(member)){
            members.add(member);
            IslandsManager.dataConfig.set("Islands."+owner.toString()+".Members",IslandsPlugin.uuidToStringList(members));
            IslandsManager.save();
        }
    }

    public void removeMember(final UUID member){
        if(members.contains(member)){
            members.remove(member);
            IslandsManager.dataConfig.set("Islands."+owner.toString()+".Members",IslandsPlugin.uuidToStringList(members));
            IslandsManager.save();
        }
    }

    public UUID getOwner(){
        return owner;
    }

    public List<UUID> getMembers(){
        return members;
    }

    public String getWorld(){
        return "island_"+owner.toString();
    }
}

