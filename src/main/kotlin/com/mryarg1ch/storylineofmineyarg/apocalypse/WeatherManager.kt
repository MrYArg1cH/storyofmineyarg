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
 * Менеджер переменной погоды во всех зимних биомах
 */
class WeatherManager {
    
    private var server: MinecraftServer? = null
    private var scheduler: ScheduledExecutorService? = null
    private val random = Random()
    private var isRunning = false
    
    // Список всех зимних биомов
    private val winterBiomes = listOf(
        Biomes.SNOWY_PLAINS,
        Biomes.SNOWY_TAIGA,
        Biomes.SNOWY_SLOPES,
        Biomes.FROZEN_RIVER,
        Biomes.ICE_SPIKES,
        Biomes.FROZEN_OCEAN,
        Biomes.DEEP_FROZEN_OCEAN,
        Biomes.SNOWY_BEACH,
        Biomes.GROVE,
        Biomes.FROZEN_PEAKS,
        Biomes.JAGGED_PEAKS
    )
    
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
        scheduler?.shutdown()
        scheduler = null
    }
    
    /**
     * Запускает цикл переменной погоды
     */
    private fun startWeatherLoop() {
        if (isRunning) return
        isRunning = true
        
        // Создаем новый scheduler если он null или завершен
        if (scheduler == null || scheduler!!.isShutdown) {
            scheduler = Executors.newSingleThreadScheduledExecutor()
        }
        
        // Каждые 10 минут меняем погоду во всех зимних биомах
        scheduler!!.scheduleAtFixedRate({
            try {
                updateWeatherInAllWinterBiomes()
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
     * Обновляет погоду во всех зимних биомах
     */
    private fun updateWeatherInAllWinterBiomes() {
        val server = this.server ?: return
        
        // Рандомно выбираем: снегопад или ясная погода для всех зимних биомов
        val shouldSnow = random.nextBoolean()
        
        server.allLevels.forEach { level ->
            if (level.dimension() == Level.OVERWORLD) {
                updateWeatherInLevel(level, shouldSnow)
            }
        }
        
        if (StorylineOfMineyarg.config.enableProgressLogging) {
            val weatherType = if (shouldSnow) "снегопад" else "ясная погода"
            StorylineOfMineyarg.LOGGER.info("Погода во всех зимних биомах изменена на: $weatherType")
        }
    }
    
    /**
     * Обновляет погоду в конкретном уровне для всех зимних биомов
     */
    private fun updateWeatherInLevel(level: ServerLevel, shouldSnow: Boolean) {
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
        
        // Уведомляем игроков в зимних биомах
        notifyPlayersInWinterBiomes(level, shouldSnow)
    }
    
    /**
     * Уведомляет игроков в зимних биомах об изменении погоды
     */
    private fun notifyPlayersInWinterBiomes(level: ServerLevel, shouldSnow: Boolean) {
        val message = if (shouldSnow) {
            "§f❄ В зимних биомах начался снегопад!"
        } else {
            "§7☀ В зимних биомах прояснилось."
        }
        
        level.players().forEach { player ->
            val playerPos = player.blockPosition()
            
            // Уведомляем только игроков в зимних биомах
            if (isInWinterBiome(level, playerPos)) {
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal(message))
            }
        }
    }
    
    /**
     * Проверяет, находится ли позиция в зимнем биоме
     */
    private fun isInWinterBiome(level: ServerLevel, pos: BlockPos): Boolean {
        val biome = level.getBiome(pos)
        return winterBiomes.any { biome.`is`(it) }
    }
    
    /**
     * Принудительно устанавливает снегопад во всех зимних биомах
     */
    fun forceSnowInWinterBiomes() {
        val server = this.server ?: return
        
        server.allLevels.forEach { level ->
            if (level.dimension() == Level.OVERWORLD) {
                updateWeatherInLevel(level, true)
            }
        }
        
        StorylineOfMineyarg.LOGGER.info("Принудительно установлен снегопад во всех зимних биомах")
    }
    
    /**
     * Принудительно убирает снегопад во всех зимних биомах
     */
    fun clearWeatherInWinterBiomes() {
        val server = this.server ?: return
        
        server.allLevels.forEach { level ->
            if (level.dimension() == Level.OVERWORLD) {
                updateWeatherInLevel(level, false)
            }
        }
        
        StorylineOfMineyarg.LOGGER.info("Убран снегопад во всех зимних биомах")
    }
}