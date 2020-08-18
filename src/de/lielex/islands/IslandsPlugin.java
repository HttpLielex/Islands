package de.lielex.islands;

import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class IslandsPlugin extends JavaPlugin{

    public static IslandsPlugin instance=null;
    public static final String prefix="§8[§aIslands§8] §7";

    @Override
    public void onEnable(){
        instance=this;
        if(!IslandsManager.schematicFile.exists()){
            Bukkit.getLogger().warning(" ");
            Bukkit.getLogger().warning("[Islands] Cannot find island.schem file in plugins/Islands! "+
                                               "Plugin needs Schematic file to work! Shutting down Plugin.");
            Bukkit.getLogger().warning(" ");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        new IslandsCommand(this);
        new IslandsListener(this);
        Island.loadAll(IslandsManager.dataConfig);
    }

    public void pasteSchematic(final Location loc,final File schematic){
        Bukkit.getLogger().info("Starting generating "+schematic+" in "+loc.getWorld()+" at x:"+loc.getX()+", y:"+loc.getY()+", z:"+loc.getZ());
        final com.sk89q.worldedit.world.World adaptedWorld=BukkitAdapter.adapt(loc.getWorld());
        final ClipboardFormat format=ClipboardFormats.findByFile(schematic);
        try(ClipboardReader reader=format.getReader(new FileInputStream(schematic))){
            Clipboard clipboard=reader.read();
            try(EditSession editSession=WorldEdit.getInstance().getEditSessionFactory().getEditSession(adaptedWorld,-1)){
                final Operation operation=new ClipboardHolder(clipboard).createPaste(editSession)
                        .to(BlockVector3.at(loc.getBlockX(),loc.getBlockY(),loc.getBlockZ())).ignoreAirBlocks(true).build();
                try{
                    Operations.complete(operation);
                    editSession.flushSession();
                }catch(WorldEditException e){
                    e.printStackTrace();
                    Bukkit.getLogger().warning("System generated a WorldEdit Error while pasting a Schematic!");
                }
            }
        }catch(IOException e){
            e.printStackTrace();

        }
    }

    public static List<String> uuidToStringList(final List<UUID> list){
        final List<String> out=new ArrayList<>();
        list.forEach(uuid->out.add(uuid.toString()));
        return out;
    }

    public static List<UUID> stringToStringUUID(final List<String> list){
        final List<UUID> out=new ArrayList<>();
        list.forEach(uuid->out.add(UUID.fromString(uuid)));
        return out;
    }
}
