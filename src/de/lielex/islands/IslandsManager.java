package de.lielex.islands;

import org.bukkit.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.libs.org.apache.commons.io.FileUtils;
import org.bukkit.craftbukkit.v1_15_R1.CraftServer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

public class IslandsManager{

    public static final int maxIslandDistance=300;
    public static final File schematicFile=new File("Plugins"+File.separator+"Islands"+File.separator+"island.schem");
    public static final File dataFile=new File("plugins"+File.separator+"Islands"+File.separator+"islands.yml");
    public static final YamlConfiguration dataConfig=YamlConfiguration.loadConfiguration(dataFile);

    public static World getWorld(final String worldName){
        return Bukkit.getWorld(worldName)!=null?Bukkit.getWorld(worldName):new WorldCreator(worldName).generateStructures(false)
                .generator(IslandsWorldGenerator.instance).createWorld();
    }

    public static void onCreate(final Player p,final Island island){
        p.playSound(p.getLocation(),Sound.ENTITY_PLAYER_LEVELUP,1f,2f);
        p.sendMessage(IslandsPlugin.prefix+"§7Island was created!");
    }

    public static Island create(final UUID owner){
        if(!hasIsland(owner)){
            final World world=createWorld("island_"+owner.toString());
            final Island island=new Island(owner,new ArrayList<>());
            if(Bukkit.getPlayer(owner)!=null) onCreate(Bukkit.getPlayer(owner),island);
            island.toFile(dataConfig);
            save();
            return island;
        }else{
            return Island.loadedIslands.get(owner);
        }
    }

    @SuppressWarnings("unchecked")
    public static boolean isWorldLoaded(final String worldName){
        try{
            final Field field=CraftServer.class.getDeclaredField("worlds");
            field.setAccessible(true);
            return ((Map<String,World>)field.get(Bukkit.getServer())).containsKey(worldName);
        }catch(final NoSuchFieldException|IllegalAccessException e){
            e.printStackTrace();
        }
        return false;
    }

    public static boolean canInteract(final Player p){
        if(isInIslandWorld(p)){
            final Island island=Island.getByWorld(p.getWorld());
            if(island!=null){
                if(island.getOwner().equals(p.getUniqueId())) return true;
                return island.getMembers().contains(p.getUniqueId());
            }
        }
        return true;
    }

    public static void delete(final Island island){
        if(island!=null){
            Island.loadedIslands.remove(island.getOwner());
            dataConfig.set("Islands."+island.getOwner().toString(),null);
            save();
            final World world=getWorld("island_"+island.getOwner().toString());
            if(world!=null){
                for(Player p: world.getPlayers()){
                    p.teleport(Bukkit.getWorld("world").getSpawnLocation());
                    p.sendMessage(IslandsPlugin.prefix+"The world you were in has been deleted!");
                }
                Bukkit.unloadWorld(world,false);
                try{
                    FileUtils.deleteDirectory(new File("island_"+island.getOwner().toString()));
                }catch(final IOException e){
                    e.printStackTrace();
                }
                if(Bukkit.getPlayer(island.getOwner())!=null){
                    Bukkit.getPlayer(island.getOwner()).sendMessage(IslandsPlugin.prefix+"You have deleted your island!");
                }
            }else{
                Bukkit.getLogger().warning("World "+island.getWorld()+" is null!");
            }
        }
    }

    public static void loadWorld(final String name){
        new BukkitRunnable(){
            @Override
            public void run(){
                Bukkit.getLogger().info("Loading World "+name+"...");
                try{
                    new WorldCreator(name).createWorld();
                }catch(final IllegalStateException ignore){
                }
            }
        }.runTaskAsynchronously(IslandsPlugin.instance);
    }

    public static void unloadWorld(final World world){
        new BukkitRunnable(){
            @Override
            public void run(){
                Bukkit.getLogger().info("Unloading World "+world.getName()+"...");
                try{
                    Bukkit.unloadWorld(world,true);
                }catch(final NullPointerException|IllegalStateException ignore){
                }
            }
        }.runTaskAsynchronously(IslandsPlugin.instance);
    }

    public static int getOnlineMembers(final Island island){
        if(island!=null){
            int i=0;
            if(Bukkit.getPlayer(island.getOwner())!=null) i++;
            for(UUID uuid: island.getMembers()){
                if(Bukkit.getPlayer(uuid)!=null) i++;
            }
            return i;
        }
        return 0;
    }

    public static boolean worldExists(final String name){
        return new File(name).exists();
    }

    public static boolean hasIsland(final UUID owner){
        return worldExists("island_"+owner.toString());
    }

    public static boolean isInIslandWorld(final Player p){
        return p.getWorld().getName().startsWith("island_");
    }

    public static World createWorld(final String name){
        Bukkit.getLogger().info("Creating World "+name+"...");
        final World world=new WorldCreator(name).generateStructures(false).environment(World.Environment.NORMAL).type(WorldType.FLAT)
                .generator(new IslandsWorldGenerator()).createWorld();
        world.setSpawnLocation(0,55,0);
        IslandsPlugin.instance.pasteSchematic(world.getSpawnLocation(),schematicFile);
        world.getWorldBorder().setCenter(world.getSpawnLocation());
        world.getWorldBorder().setSize(maxIslandDistance*2);
        return world;
    }

    public static void save(){
        try{
            dataConfig.save(dataFile);
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}
