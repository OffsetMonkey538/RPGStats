package mc.rpgstats.mixin_logic;

import mc.rpgstats.main.CustomComponents;
import mc.rpgstats.main.RPGStats;
import net.minecraft.block.BlockState;
import net.minecraft.block.Fertilizable;
import net.minecraft.item.HoeItem;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

public class OnSneakLogic {
    public static void doLogic(boolean isSneaking, ServerPlayerEntity playerEntity) {
        Random random = new Random();
    
        // Check if all conditions met
        if (isSneaking && playerEntity.getMainHandStack().getItem() instanceof HoeItem && new Random().nextBoolean()) {
            int level = RPGStats.getComponentLevel(CustomComponents.FARMING, playerEntity);
            int amount = 0;

            if (level >= 50 && RPGStats.getConfig().toggles.farming.enableLv50Buff) {
                amount = 5;
            } else if (level >= 25 && RPGStats.getConfig().toggles.farming.enableLv25Buff) {
                amount = 3;
            }
        
            // If we should do stuff
            // In a 3x3 or 5x5 area, there is a 90% chance to grow any Fertilizable blocks
            if (amount > 0) {
                World world = playerEntity.world;
                BlockPos blockPos = playerEntity.getBlockPos();
                for (int y = -1; y <= 1; y++) {
                    for (int x = -amount; x <= amount; x++) {
                        for (int z = -amount; z <= amount; z++) {
                            BlockPos nextPos = blockPos.add(x, y, z);
                            BlockState bs = world.getBlockState(nextPos);
                            if (bs.getBlock() instanceof Fertilizable) {
                                if (random.nextDouble() > 0.9) {
                                    ((Fertilizable)bs.getBlock()).grow((ServerWorld)world, world.random, nextPos, bs);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}