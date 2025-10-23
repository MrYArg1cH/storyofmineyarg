package com.mryarg1ch.storylineofmineyarg.data

import com.mryarg1ch.storylineofmineyarg.StorylineOfMineyarg
import net.minecraft.core.BlockPos
import net.minecraft.server.MinecraftServer
import java.io.File
import java.util.*

/**
 * Класс для управления сохранением и загрузкой данных апокалипсиса
 */
class ApocalypseDataManager {
    
    // Данные о состоянии апокалипсиса
    var isApocalypseActive: Boolean = false
    var isApocalypsePaused: Boolean = false
    var isTestMode: Boolean = false
    
    // Временные метки
    var apocalypseStartTime: Long = 0L
    var pausedTime: Long = 0L
    var totalPausedDuration: Long = 0L
    
    // Прогресс апокалипсиса
    var mobSpawnIncreaseTriggered: Boolean = false
    var snowfallTriggered: Boolean = false
    var mobEvolutionLevel: Int = 0
    var currentSnowRadius: Int = 0
    
    // Координаты снегопада
    var snowCenter: BlockPos? = null
    
    /**
     * Загружает данные из файла
     */
    fun loadData(server: MinecraftServer) {
        val dataDir = getDataDirectory(server)
        val dataFile = File(dataDir, "apocalypse-data.properties")
        
        if (!dataFile.exists()) {
            StorylineOfMineyarg.LOGGER.info("Файл данных не найден, создается новый")
            saveData(server)
            return
        }
        
        try {
            val properties = Properties()
            dataFile.inputStream().use { properties.load(it) }
            
            // Загружаем состояние апокалипсиса
            isApocalypseActive = properties.getProperty("isApocalypseActive", "false").toBoolean()
            isApocalypsePaused = properties.getProperty("isApocalypsePaused", "false").toBoolean()
            isTestMode = properties.getProperty("isTestMode", "false").toBoolean()
            
            // Загружаем временные метки
            apocalypseStartTime = properties.getProperty("apocalypseStartTime", "0").toLong()
            pausedTime = properties.getProperty("pausedTime", "0").toLong()
            totalPausedDuration = properties.getProperty("totalPausedDuration", "0").toLong()
            
            // Загружаем прогресс
            mobSpawnIncreaseTriggered = properties.getProperty("mobSpawnIncreaseTriggered", "false").toBoolean()
            snowfallTriggered = properties.getProperty("snowfallTriggered", "false").toBoolean()
            mobEvolutionLevel = properties.getProperty("mobEvolutionLevel", "0").toInt()
            currentSnowRadius = properties.getProperty("currentSnowRadius", "0").toInt()
            
            // Загружаем координаты снегопада
            val snowX = properties.getProperty("snowCenterX")
            val snowY = properties.getProperty("snowCenterY")
            val snowZ = properties.getProperty("snowCenterZ")
            
            if (snowX != null && snowY != null && snowZ != null) {
                snowCenter = BlockPos(snowX.toInt(), snowY.toInt(), snowZ.toInt())
            }
            
            // Загружаем конфигурацию
            StorylineOfMineyarg.config.loadConfig(File(server.serverDirectory, "config"))
            
            StorylineOfMineyarg.LOGGER.info("Данные апокалипсиса загружены успешно")
            
        } catch (e: Exception) {
            StorylineOfMineyarg.LOGGER.error("Ошибка при загрузке данных: ${e.message}")
            resetData()
        }
    }
    
    /**
     * Сохраняет данные в файл
     */
    fun saveData(server: MinecraftServer) {
        val dataDir = getDataDirectory(server)
        if (!dataDir.exists()) {
            dataDir.mkdirs()
        }
        
        val dataFile = File(dataDir, "apocalypse-data.properties")
        
        try {
            val properties = Properties()
            
            // Сохраняем состояние апокалипсиса
            properties.setProperty("isApocalypseActive", isApocalypseActive.toString())
            properties.setProperty("isApocalypsePaused", isApocalypsePaused.toString())
            properties.setProperty("isTestMode", isTestMode.toString())
            
            // Сохраняем временные метки
            properties.setProperty("apocalypseStartTime", apocalypseStartTime.toString())
            properties.setProperty("pausedTime", pausedTime.toString())
            properties.setProperty("totalPausedDuration", totalPausedDuration.toString())
            
            // Сохраняем прогресс
            properties.setProperty("mobSpawnIncreaseTriggered", mobSpawnIncreaseTriggered.toString())
            properties.setProperty("snowfallTriggered", snowfallTriggered.toString())
            properties.setProperty("mobEvolutionLevel", mobEvolutionLevel.toString())
            properties.setProperty("currentSnowRadius", currentSnowRadius.toString())
            
            // Сохраняем координаты снегопада
            snowCenter?.let { pos ->
                properties.setProperty("snowCenterX", pos.x.toString())
                properties.setProperty("snowCenterY", pos.y.toString())
                properties.setProperty("snowCenterZ", pos.z.toString())
            }
            
            dataFile.outputStream().use { 
                properties.store(it, "Storyline Of Mineyarg Apocalypse Data")
            }
            
            // Сохраняем конфигурацию
            StorylineOfMineyarg.config.saveConfig(File(server.serverDirectory, "config"))
            
            StorylineOfMineyarg.LOGGER.info("Данные апокалипсиса сохранены успешно")
            
        } catch (e: Exception) {
            StorylineOfMineyarg.LOGGER.error("Ошибка при сохранении данных: ${e.message}")
        }
    }
    
    /**
     * Сбрасывает все данные к начальному состоянию
     */
    fun resetData() {
        isApocalypseActive = false
        isApocalypsePaused = false
        isTestMode = false
        
        apocalypseStartTime = 0L
        pausedTime = 0L
        totalPausedDuration = 0L
        
        mobSpawnIncreaseTriggered = false
        snowfallTriggered = false
        mobEvolutionLevel = 0
        currentSnowRadius = 0
        
        snowCenter = null
        
        StorylineOfMineyarg.LOGGER.info("Данные апокалипсиса сброшены")
    }
    
    /**
     * Возвращает эффективное время апокалипсиса (без учета пауз)
     */
    fun getEffectiveApocalypseTime(): Long {
        if (!isApocalypseActive) return 0L
        
        val currentTime = System.currentTimeMillis()
        val totalTime = if (isApocalypsePaused) {
            pausedTime - apocalypseStartTime
        } else {
            currentTime - apocalypseStartTime
        }
        
        return totalTime - totalPausedDuration
    }
    
    /**
     * Начинает апокалипсис
     */
    fun startApocalypse(testMode: Boolean = false) {
        isApocalypseActive = true
        isApocalypsePaused = false
        isTestMode = testMode
        apocalypseStartTime = System.currentTimeMillis()
        totalPausedDuration = 0L
        
        // Устанавливаем координаты снегопада из конфигурации
        snowCenter = StorylineOfMineyarg.config.snowSpawnPosition
        
        StorylineOfMineyarg.LOGGER.info("Апокалипсис запущен${if (testMode) " в тестовом режиме" else ""}")
    }
    
    /**
     * Останавливает апокалипсис
     */
    fun stopApocalypse(resetProgress: Boolean = true) {
        isApocalypseActive = false
        isApocalypsePaused = false
        
        if (resetProgress) {
            resetData()
        }
        
        StorylineOfMineyarg.LOGGER.info("Апокалипсис остановлен${if (resetProgress) " с сбросом прогресса" else ""}")
    }
    
    /**
     * Ставит апокалипсис на паузу
     */
    fun pauseApocalypse() {
        if (isApocalypseActive && !isApocalypsePaused) {
            isApocalypsePaused = true
            pausedTime = System.currentTimeMillis()
            StorylineOfMineyarg.LOGGER.info("Апокалипсис приостановлен")
        }
    }
    
    /**
     * Возобновляет апокалипсис
     */
    fun resumeApocalypse() {
        if (isApocalypsePaused) {
            val pauseDuration = System.currentTimeMillis() - pausedTime
            totalPausedDuration += pauseDuration
            isApocalypsePaused = false
            StorylineOfMineyarg.LOGGER.info("Апокалипсис возобновлен")
        }
    }
    
    /**
     * Возвращает папку для сохранения данных мода
     */
    private fun getDataDirectory(server: MinecraftServer): File {
        return File(server.serverDirectory, "config/storylineofmineyarg")
    }
    
    /**
     * Проверяет, должно ли произойти событие на основе времени
     */
    fun shouldTriggerEvent(daysThreshold: Int, coefficient: Int): Boolean {
        val effectiveTime = getEffectiveApocalypseTime()
        val thresholdTime = if (isTestMode) {
            // В тестовом режиме используем секунды вместо дней
            daysThreshold * 30L * 1000L // 30 секунд за день
        } else {
            StorylineOfMineyarg.config.daysToMilliseconds(daysThreshold)
        }
        
        val adjustedThreshold = StorylineOfMineyarg.config.applyCoefficient(thresholdTime, coefficient)
        return effectiveTime >= adjustedThreshold
    }
}