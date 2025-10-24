package com.mryarg1ch.storylineofmineyarg.apocalypse

import com.mryarg1ch.storylineofmineyarg.StorylineOfMineyarg
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.Level
import net.minecraft.world.level.biome.Biomes
import net.minecraft.core.BlockPos
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import kotlin.math.sqrt

/**
 * Менеджер переменной погоды в снежных биомах
 */
class WeatherManager {
    
    private var server: MinecraftServer? = null
    private val scheduler: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
    private val random = Random()
    private var isRunning = false
    
    /**
     * Инициализация при запуске сервера
     */
    fun onServerStart(server: MinecraftServer) {
        this.server = server
        startWeatherLoop()
    }
    
    /**
     * Очистка при остановке сервера
     */
    fun onServerStop() {
        stopWeatherLoop()
        scheduler.shutdown()
    }
    
    /**
     * Запускает цикл переменной погоды
     */
    private fun startWeatherLoop() {
        if (isRunning) return
        isRunning = true
        
        // Каждые 10 минут меняем погоду
        scheduler.scheduleAtFixedRate({
            try {
                updateWeatherInSnowBiomes()
            } catch (e: Exception) {
                StorylineOfMineyarg.LOGGER.error("Ошибка при обновлении погоды: ${e.message}")
            }
        }, 0, 10, TimeUnit.MINUTES)
    }
    
    /**
     * Останавливает цикл погоды
     */
    private fun stopWeatherLoop() {
        isRunning = false
    }
    
    /**
     * Обновляет погоду в снежных биомах
     */
    private fun updateWeatherInSnowBiomes() {
        val server = this.server ?: return
        val dataManager = StorylineOfMineyarg.dataManager
        val snowCenter = dataManager.snowCenter ?: return
        
        // Проверяем, активен ли снегопад
        if (!dataManager.snowfallTriggered) return
        
        // Рандомно выбираем: снегопад или ясная погода
        val shouldSnow = random.nextBoolean()
        
        server.allLevels.forEach { level ->
            if (level.dimension() == Level.OVERWORLD) {
                updateWeatherInLevel(level, snowCenter, dataManager.currentSnowRadius, shouldSnow)
            }
        }
        
        if (StorylineOfMineyarg.config.enableProgressLogging) {
            val weatherType = if (shouldSnow) "снегопад" else "ясная погода"
            StorylineOfMineyarg.LOGGER.info("Погода в снежной зоне изменена на: $weatherType")
        }
    }
    
    /**
     * Обновляет погоду в конкретном уровне
     */
    private fun updateWeatherInLevel(level: ServerLevel, center: BlockPos, radius: Int, shouldSnow: Boolean) {
        try {
            // Используем reflection для доступа к serverLevelData
            val serverLevelDataField = level.javaClass.getDeclaredField("serverLevelData")
            serverLevelDataField.isAccessible = true
            val serverLevelData = serverLevelDataField.get(level)
            
            if (shouldSnow) {
                // Включаем дождь/снег
                serverLevelData.javaClass.getMethod("setRaining", Boolean::class.java).invoke(serverLevelData, true)
                serverLevelData.javaClass.getMethod("setThundering", Boolean::class.java).invoke(serverLevelData, false)
                serverLevelData.javaClass.getDeclaredField("rainTime").apply { isAccessible = true }.set(serverLevelData, 12000)
                serverLevelData.javaClass.getDeclaredField("clearWeatherTime").apply { isAccessible = true }.set(serverLevelData, 0)
            } else {
                // Выключаем дождь/снег
                serverLevelData.javaClass.getMethod("setRaining", Boolean::class.java).invoke(serverLevelData, false)
                serverLevelData.javaClass.getMethod("setThundering", Boolean::class.java).invoke(serverLevelData, false)
                serverLevelData.javaClass.getDeclaredField("clearWeatherTime").apply { isAccessible = true }.set(serverLevelData, 12000)
                serverLevelData.javaClass.getDeclaredField("rainTime").apply { isAccessible = true }.set(serverLevelData, 0)
            }
        } catch (e: Exception) {
            // Игнорируем ошибки установки погоды
            StorylineOfMineyarg.LOGGER.warn("Не удалось установить погоду: ${e.message}")
        }
        
        // Уведомляем игроков в снежной зоне
        notifyPlayersInSnowZone(level, center, radius, shouldSnow)
    }
    
    /**
     * Уведомляет игроков в снежной зоне об изменении погоды
     */
    private fun notifyPlayersInSnowZone(level: ServerLevel, center: BlockPos, radius: Int, shouldSnow: Boolean) {
        val message = if (shouldSnow) {
            "§f❄ В снежной зоне начался снегопад!"
        } else {
            "§7☀ В снежной зоне прояснилось."
        }
        
        level.players().forEach { player ->
            val playerPos = player.blockPosition()
            val distance = sqrt(
                ((playerPos.x - center.x) * (playerPos.x - center.x) + 
                 (playerPos.z - center.z) * (playerPos.z - center.z)).toDouble()
            )
            
            // Уведомляем только игроков в снежной зоне
            if (distance <= radius) {
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal(message))
            }
        }
    }
    
    /**
     * Проверяет, находится ли позиция в снежном биоме
     */
    private fun isInSnowBiome(level: ServerLevel, pos: BlockPos): Boolean {
        val biome = level.getBiome(pos)
        return biome.`is`(Biomes.SNOWY_PLAINS) || 
               biome.`is`(Biomes.SNOWY_TAIGA) || 
               biome.`is`(Biomes.SNOWY_SLOPES) ||
               biome.`is`(Biomes.FROZEN_RIVER) ||
               biome.`is`(Biomes.ICE_SPIKES)
    }
}