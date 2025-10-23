package com.mryarg1ch.storylineofmineyarg.config

import com.mryarg1ch.storylineofmineyarg.StorylineOfMineyarg
import net.minecraft.core.BlockPos
import java.io.File
import java.util.*

/**
 * Класс для управления конфигурацией апокалипсиса
 */
class ApocalypseConfig {
    
    // Настройки времени (в реальных днях)
    var daysUntilMobSpawnIncrease: Int = 3
        set(value) { field = value.coerceIn(1, 100) }
    
    var daysUntilSnowfall: Int = 2
        set(value) { field = value.coerceIn(2, 100) }
    
    var daysUntilMobEvolution: Int = 7
        set(value) { field = value.coerceIn(1, 100) }
    
    // Коэффициенты (в процентах)
    var mobSpawnCoefficient: Int = 50
        set(value) { field = value.coerceIn(1, 100) }
    
    var mobEvolutionSpeedCoefficient: Int = 75
        set(value) { field = value.coerceIn(1, 100) }
    
    var snowfallSpeedCoefficient: Int = 25
        set(value) { field = value.coerceIn(1, 100) }
    
    // Настройки логирования
    var enableProgressLogging: Boolean = true
    
    // Координаты начала снегопада
    var snowSpawnPosition: BlockPos? = null
    
    /**
     * Загружает конфигурацию из файла
     */
    fun loadConfig(configDir: File) {
        val configFile = File(configDir, "storylineofmineyarg-config.properties")
        
        if (!configFile.exists()) {
            saveConfig(configDir)
            return
        }
        
        try {
            val properties = Properties()
            configFile.inputStream().use { properties.load(it) }
            
            daysUntilMobSpawnIncrease = properties.getProperty("daysUntilMobSpawnIncrease", "3").toInt()
            daysUntilSnowfall = properties.getProperty("daysUntilSnowfall", "2").toInt()
            daysUntilMobEvolution = properties.getProperty("daysUntilMobEvolution", "7").toInt()
            mobSpawnCoefficient = properties.getProperty("mobSpawnCoefficient", "50").toInt()
            mobEvolutionSpeedCoefficient = properties.getProperty("mobEvolutionSpeedCoefficient", "75").toInt()
            snowfallSpeedCoefficient = properties.getProperty("snowfallSpeedCoefficient", "25").toInt()
            enableProgressLogging = properties.getProperty("enableProgressLogging", "true").toBoolean()
            
            // Загружаем координаты снегопада
            val snowX = properties.getProperty("snowSpawnX")
            val snowY = properties.getProperty("snowSpawnY")
            val snowZ = properties.getProperty("snowSpawnZ")
            
            if (snowX != null && snowY != null && snowZ != null) {
                snowSpawnPosition = BlockPos(snowX.toInt(), snowY.toInt(), snowZ.toInt())
            }
            
            StorylineOfMineyarg.LOGGER.info("Configuration loaded successfully")
            
        } catch (e: Exception) {
            StorylineOfMineyarg.LOGGER.error("Failed to load configuration: ${e.message}")
            saveConfig(configDir) // Создаем новый файл конфигурации
        }
    }
    
    /**
     * Сохраняет конфигурацию в файл
     */
    fun saveConfig(configDir: File) {
        if (!configDir.exists()) {
            configDir.mkdirs()
        }
        
        val configFile = File(configDir, "storylineofmineyarg-config.properties")
        
        try {
            val properties = Properties()
            
            properties.setProperty("daysUntilMobSpawnIncrease", daysUntilMobSpawnIncrease.toString())
            properties.setProperty("daysUntilSnowfall", daysUntilSnowfall.toString())
            properties.setProperty("daysUntilMobEvolution", daysUntilMobEvolution.toString())
            properties.setProperty("mobSpawnCoefficient", mobSpawnCoefficient.toString())
            properties.setProperty("mobEvolutionSpeedCoefficient", mobEvolutionSpeedCoefficient.toString())
            properties.setProperty("snowfallSpeedCoefficient", snowfallSpeedCoefficient.toString())
            properties.setProperty("enableProgressLogging", enableProgressLogging.toString())
            
            // Сохраняем координаты снегопада
            snowSpawnPosition?.let { pos ->
                properties.setProperty("snowSpawnX", pos.x.toString())
                properties.setProperty("snowSpawnY", pos.y.toString())
                properties.setProperty("snowSpawnZ", pos.z.toString())
            }
            
            configFile.outputStream().use { 
                properties.store(it, "Storyline Of Mineyarg Configuration")
            }
            
            StorylineOfMineyarg.LOGGER.info("Configuration saved successfully")
            
        } catch (e: Exception) {
            StorylineOfMineyarg.LOGGER.error("Failed to save configuration: ${e.message}")
        }
    }
    
    /**
     * Сбрасывает конфигурацию к значениям по умолчанию
     */
    fun resetToDefaults() {
        daysUntilMobSpawnIncrease = 3
        daysUntilSnowfall = 2
        daysUntilMobEvolution = 7
        mobSpawnCoefficient = 50
        mobEvolutionSpeedCoefficient = 75
        snowfallSpeedCoefficient = 25
        enableProgressLogging = true
        snowSpawnPosition = null
        
        StorylineOfMineyarg.LOGGER.info("Configuration reset to defaults")
    }
    
    /**
     * Возвращает время в миллисекундах для указанного количества дней
     */
    fun daysToMilliseconds(days: Int): Long {
        return days * 24L * 60L * 60L * 1000L
    }
    
    /**
     * Применяет коэффициент к времени
     */
    fun applyCoefficient(timeMs: Long, coefficient: Int): Long {
        return (timeMs * coefficient / 100.0).toLong()
    }
}