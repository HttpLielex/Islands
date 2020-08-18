package de.lielex.islands;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.ChunkGenerator;

import java.util.Random;

public class IslandsWorldGenerator extends ChunkGenerator{

    public static final IslandsWorldGenerator instance=new IslandsWorldGenerator();

    @Override
    public ChunkGenerator.ChunkData generateChunkData(final World world,final Random random,final int chunkX,final int chunkZ,
                                                      final ChunkGenerator.BiomeGrid biome){
        final ChunkData data=createChunkData(world);
        for(int x=0;x<16;x++){
            for(int z=0;z<16;z++){
                for(int y=0;y<50;y++){
                    biome.setBiome(x,y,z,Biome.FOREST);
                    if(y==0){
                        data.setBlock(x,y,z,Material.BEDROCK);
                        continue;
                    }
                    data.setBlock(x,y,z,Material.WATER);
                }
            }
        }
        return data;
    }
}
