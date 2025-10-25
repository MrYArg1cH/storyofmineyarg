package com.mryarg1ch.storylineofmineyarg.events

import com.mryarg1ch.storylineofmineyarg.StorylineOfMineyarg
import net.minecraft.core.BlockPos
import net.minecraft.world.entity.monster.Zombie
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.block.*
import net.minecraft.world.level.block.state.BlockState
import net.minecraftforge.event.entity.living.LivingEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import kotlin.random.Random

/**
 * Обработчик ломания блоков зомби с эволюцией
 */
object ZombieBlockBreakingHandler {
    
    // Блоки, которые зомби могут ломать
    private val breakableBlocks = setOf(
        Blocks.OAK_DOOR,
        Blocks.SPRUCE_DOOR,
        Blocks.BIRCH_DOOR,
        Blocks.JUNGLE_DOOR,
        Blocks.ACACIA_DOOR,
        Blocks.DARK_OAK_DOOR,
        Blocks.CRIMSON_DOOR,
        Blocks.WARPED_DOOR,
        Blocks.IRON_DOOR,
        Blocks.OAK_TRAPDOOR,
        Blocks.SPRUCE_TRAPDOOR,
        Blocks.BIRCH_TRAPDOOR,
        Blocks.JUNGLE_TRAPDOOR,
        Blocks.ACACIA_TRAPDOOR,
        Blocks.DARK_OAK_TRAPDOOR,
        Blocks.CRIMSON_TRAPDOOR,
        Blocks.WARPED_TRAPDOOR,
        Blocks.IRON_TRAPDOOR,
        Blocks.OAK_BUTTON,
        Blocks.SPRUCE_BUTTON,
        Blocks.BIRCH_BUTTON,
        Blocks.JUNGLE_BUTTON,
        Blocks.ACACIA_BUTTON,
        Blocks.DARK_OAK_BUTTON,
        Blocks.CRIMSON_BUTTON,
        Blocks.WARPED_BUTTON,
        Blocks.STONE_BUTTON,
        Blocks.POLISHED_BLACKSTONE_BUTTON,
        Blocks.OAK_PRESSURE_PLATE,
        Blocks.SPRUCE_PRESSURE_PLATE,
        Blocks.BIRCH_PRESSURE_PLATE,
        Blocks.JUNGLE_PRESSURE_PLATE,
        Blocks.ACACIA_PRESSURE_PLATE,
        Blocks.DARK_OAK_PRESSURE_PLATE,
        Blocks.CRIMSON_PRESSURE_PLATE,
        Blocks.WARPED_PRESSURE_PLATE,
        Blocks.STONE_PRESSURE_PLATE,
        Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE,
        Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE,
        Blocks.POLISHED_BLACKSTONE_PRESSURE_PLATE,
        Blocks.DIRT,
        Blocks.COARSE_DIRT,
        Blocks.PODZOL,
        Blocks.GRASS_BLOCK,
        Blocks.SAND,
        Blocks.RED_SAND,
        Blocks.GRAVEL,
        Blocks.CLAY,
        Blocks.FARMLAND,
        Blocks.DIRT_PATH,
        Blocks.MYCELIUM,
        Blocks.SOUL_SAND,
        Blocks.SOUL_SOIL
    )
    
    // Карта для отслеживания времени последнего ломания блоков зомби
    private val lastBreakTime = mutableMapOf<Zombie, Long>()
    
    @SubscribeEvent
    fun onZombieTick(event: LivingEvent.LivingTickEvent) {
        val entity = event.entity
        
        // Проверяем только зомби на сервере
        if (entity !is Zombie || entity.level().isClientSide) {
            return
        }
        
        // Проверяем, активен ли апокалипсис
        if (!StorylineOfMineyarg.dataManager.isApocalypseActive || 
            StorylineOfMineyarg.dataManager.isApocalypsePaused) {
            return
        }
        
        // Проверяем, есть ли эволюция ломания блоков
        if (!hasBlockBreakingEvolution(entity)) {
            return
        }
        
        // Ограничиваем частоту попыток ломания (раз в 10 секунд)
        val currentTime = System.currentTimeMillis()
        val lastTime = lastBreakTime[entity] ?: 0
        if (currentTime - lastTime < 10000) {
            return
        }
        
        // Ищем ближайшего игрока
        val nearestPlayer = entity.level().getNearestPlayer(entity, 16.0)
        if (nearestPlayer == null || !entity.hasLineOfSight(nearestPlayer)) {
            return
        }
        
        // Проверяем, может ли зомби добраться до игрока без ломания блоков
        if (!canReachPlayerDirectly(entity, nearestPlayer)) {
            // Пытаемся сломать блоки на пути к игроку только если не может добраться напрямую
            if (tryBreakBlocksTowardsPlayer(entity, nearestPlayer)) {
                lastBreakTime[entity] = currentTime
            }
        }
    }
    
    /**
     * Проверяет, есть ли у зомби эволюция ломания блоков
     */
    private fun hasBlockBreakingEvolution(zombie: Zombie): Boolean {
        // Ломание блоков отключено
        return false
    }
    
    /**
     * Проверяет, может ли зомби добраться до игрока напрямую (без ломания блоков)
     */
    private fun canReachPlayerDirectly(zombie: Zombie, player: Player): Boolean {
        val zombiePos = zombie.blockPosition()
        val playerPos = player.blockPosition()
        
        // Простая проверка: есть ли прямая видимость между зомби и игроком
        val level = zombie.level()
        val rayTrace = level.clip(
            net.minecraft.world.level.ClipContext(
                zombie.position(),
                player.position(),
                net.minecraft.world.level.ClipContext.Block.COLLIDER,
                net.minecraft.world.level.ClipContext.Fluid.NONE,
                zombie
            )
        )
        
        // Если луч не попал в блок, значит путь свободен
        return rayTrace.type == net.minecraft.world.phys.HitResult.Type.MISS
    }
    
    /**
     * Пытается сломать блоки на пути к игроку
     */
    private fun tryBreakBlocksTowardsPlayer(zombie: Zombie, player: Player): Boolean {
        val zombiePos = zombie.blockPosition()
        val playerPos = player.blockPosition()
        
        // Ищем блоки в радиусе 3 блоков от зомби (только на уровне роста зомби - 2 блока)
        for (x in -2..2) {
            for (y in 0..1) { // Только на уровне ног и головы зомби
                for (z in -2..2) {
                    val checkPos = zombiePos.offset(x, y, z)
                    val blockState = zombie.level().getBlockState(checkPos)
                    
                    // Проверяем, можно ли сломать этот блок
                    if (canBreakBlock(blockState) && isBlockObstructingPath(zombiePos, playerPos, checkPos)) {
                        // Ломаем блок с шансом 15% (уменьшено для баланса)
                        if (Random.nextFloat() < 0.15f) {
                            breakBlock(zombie, checkPos, blockState)
                            return true
                        }
                    }
                }
            }
        }
        
        return false
    }
    
    /**
     * Проверяет, можно ли сломать данный блок
     */
    private fun canBreakBlock(blockState: BlockState): Boolean {
        return blockState.block in breakableBlocks && !blockState.isAir
    }
    
    /**
     * Проверяет, мешает ли блок пути к игроку
     */
    private fun isBlockObstructingPath(zombiePos: BlockPos, playerPos: BlockPos, blockPos: BlockPos): Boolean {
        // Простая проверка: блок находится между зомби и игроком
        val zombieToPlayer = playerPos.subtract(zombiePos)
        val zombieToBlock = blockPos.subtract(zombiePos)
        
        // Если блок в направлении игрока
        return (zombieToBlock.x * zombieToPlayer.x >= 0 && 
                zombieToBlock.z * zombieToPlayer.z >= 0 &&
                (Math.abs(zombieToBlock.x) <= Math.abs(zombieToPlayer.x) || 
                 Math.abs(zombieToBlock.z) <= Math.abs(zombieToPlayer.z)))
    }
    
    /**
     * Ломает блок
     */
    private fun breakBlock(zombie: Zombie, pos: BlockPos, blockState: BlockState) {
        try {
            val level = zombie.level()
            
            // Ломаем блок
            level.destroyBlock(pos, true)
            
            if (StorylineOfMineyarg.config.enableProgressLogging) {
                println("[${StorylineOfMineyarg.MOD_NAME}] DEBUG: Зомби сломал блок ${blockState.block.name.string} в позиции $pos")
            }
            
        } catch (e: Exception) {
            System.err.println("[${StorylineOfMineyarg.MOD_NAME}] ERROR: Ошибка при ломании блока: ${e.message}")
        }
    }
    
    /**
     * Очищает данные о зомби при его удалении
     */
    fun cleanupZombie(zombie: Zombie) {
        lastBreakTime.remove(zombie)
    }
}