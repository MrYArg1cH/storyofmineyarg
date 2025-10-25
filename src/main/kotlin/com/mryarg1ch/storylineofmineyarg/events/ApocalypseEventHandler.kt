package com.mryarg1ch.storylineofmineyarg.events

import com.mryarg1ch.storylineofmineyarg.StorylineOfMineyarg
import net.minecraft.world.entity.monster.*
import net.minecraft.world.entity.MobSpawnType
import net.minecraftforge.event.entity.EntityJoinLevelEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import java.util.*

/**
 * Обработчик событий для апокалипсиса
 */
@Mod.EventBusSubscriber(modid = StorylineOfMineyarg.MOD_ID)
object ApocalypseEventHandler {
    
    /**
     * Обрабатывает присоединение зомби к миру для увеличения количества агрессивных мобов
     */
    @SubscribeEvent
    fun onMobSpawn(event: EntityJoinLevelEvent) {
        if (!StorylineOfMineyarg.dataManager.isApocalypseActive || 
            StorylineOfMineyarg.dataManager.isApocalypsePaused) {
            return
        }
        
        val entity = event.entity
        
        // Применяем эффекты к агрессивным мобам, если это событие уже активировано
        if (StorylineOfMineyarg.dataManager.mobSpawnIncreaseTriggered && 
            entity is Monster) {
            
            // В тестовом режиме спавним дополнительных мобов
            if (StorylineOfMineyarg.dataManager.isTestMode) {
                val config = StorylineOfMineyarg.config
                val spawnMultiplier = config.mobSpawnCoefficient / 100.0
                
                // Увеличиваем шанс спавна дополнительных мобов
                if (Random().nextDouble() < spawnMultiplier) {
                    spawnAdditionalMobs(entity)
                }
            }
        }
        
        // Применяем эволюцию к агрессивным мобам
        if (entity is Monster && StorylineOfMineyarg.dataManager.mobEvolutionLevel > 0) {
            try {
                StorylineOfMineyarg.apocalypseManager.applyMobEvolution(entity)
                
                if (StorylineOfMineyarg.config.enableProgressLogging) {
                    println("[${StorylineOfMineyarg.MOD_NAME}] DEBUG: Применена эволюция к мобу: ${entity.javaClass.simpleName}")
                }
            } catch (e: Exception) {
                System.err.println("[${StorylineOfMineyarg.MOD_NAME}] ERROR: Ошибка при применении эволюции к мобу: ${e.message}")
            }
        }
    }
    
    /**
     * Спавнит дополнительных зомби рядом с исходным (для тестового режима)
     */
    private fun spawnAdditionalMobs(originalMob: Monster) {
        try {
            val level = originalMob.level()
            val pos = originalMob.blockPosition()
            val random = Random()
            
            // Спавним 1-2 дополнительных зомби
            val additionalMobCount = random.nextInt(2) + 1
            
            repeat(additionalMobCount) {
                val offsetX = random.nextInt(6) - 3
                val offsetZ = random.nextInt(6) - 3
                val spawnPos = pos.offset(offsetX, 0, offsetZ)
                
                // Создаем копию зомби
                val newMob = when (originalMob) {
                    is Zombie -> Zombie(net.minecraft.world.entity.EntityType.ZOMBIE, level)
                    is Skeleton -> Skeleton(net.minecraft.world.entity.EntityType.SKELETON, level)
                    is Creeper -> Creeper(net.minecraft.world.entity.EntityType.CREEPER, level)
                    is Spider -> Spider(net.minecraft.world.entity.EntityType.SPIDER, level)
                    else -> return@repeat
                }
                
                // Устанавливаем позицию
                newMob.setPos(spawnPos.x.toDouble(), spawnPos.y.toDouble(), spawnPos.z.toDouble())
                
                // Добавляем в мир
                if (level.addFreshEntity(newMob)) {
                    if (StorylineOfMineyarg.config.enableProgressLogging) {
                        println("[${StorylineOfMineyarg.MOD_NAME}] DEBUG: Заспавнен дополнительный зомби в тестовом режиме")
                    }
                }
            }
        } catch (e: Exception) {
            System.err.println("[${StorylineOfMineyarg.MOD_NAME}] ERROR: Ошибка при спавне дополнительных зомби: ${e.message}")
        }
    }
}