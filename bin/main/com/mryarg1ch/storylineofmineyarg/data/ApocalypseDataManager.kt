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
    var currentBiomeRadius: Int = 0
    
    // Координаты снегопада
    var snowCenter: BlockPos? = null
    
    /**
     * Загружает данные из файла
     */
    fun loadData(server: MinecraftServer) {
        val dataDir = getDataDirectory(server)
        val dataFile = File(dataDir, "apocalypse-data.properties")
        
        // Проверяем, есть ли старые данные для миграции
        if (!dataFile.exists()) {
            migrateOldData(server, dataDir)
        }
        
        if (!dataFile.exists()) {
            println("[${StorylineOfMineyarg.MOD_NAME}] INFO: Файл данных не найден, создается новый для мира")
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
            currentBiomeRadius = properties.getProperty("currentBiomeRadius", "0").toInt()
            
            // Загружаем координаты снегопада
            val snowX = properties.getProperty("snowCenterX")
            val snowY = properties.getProperty("snowCenterY")
            val snowZ = properties.getProperty("snowCenterZ")
            
            if (snowX != null && snowY != null && snowZ != null) {
                snowCenter = BlockPos(snowX.toInt(), snowY.toInt(), snowZ.toInt())
            }
            
            // Загружаем конфигурацию (глобальную)
            StorylineOfMineyarg.config.loadConfig(File(server.serverDirectory, "config"))
            
            println("[${StorylineOfMineyarg.MOD_NAME}] INFO: Данные апокалипсиса загружены успешно для мира")
            
        } catch (e: Exception) {
            System.err.println("[${StorylineOfMineyarg.MOD_NAME}] ERROR: Ошибка при загрузке данных: ${e.message}")
            resetData()
        }
    }
    
    /**
     * Мигрирует старые данные из глобальной папки в папку мира
     */
    private fun migrateOldData(server: MinecraftServer, newDataDir: File) {
        try {
            val oldDataDir = File(server.serverDirectory, "config/storylineofmineyarg")
            val oldDataFile = File(oldDataDir, "apocalypse-data.properties")
            
            if (oldDataFile.exists()) {
                println("[${StorylineOfMineyarg.MOD_NAME}] INFO: Найдены старые данные, выполняется миграция...")
                
                // Копируем старый файл в новое место
                val newDataFile = File(newDataDir, "apocalypse-data.properties")
                oldDataFile.copyTo(newDataFile, overwrite = true)
                
                println("[${StorylineOfMineyarg.MOD_NAME}] INFO: Миграция данных завершена успешно")
                
                // Опционально: удаляем старый файл (закомментировано для безопасности)
                // oldDataFile.delete()
            }
        } catch (e: Exception) {
            System.err.println("[${StorylineOfMineyarg.MOD_NAME}] WARN: Не удалось выполнить миграцию старых данных: ${e.message}")
        }
    }
    
    /**
     * Сохраняет данные в файл для конкретного мира
     */
    fun saveData(server: MinecraftServer) {
        val dataDir = getDataDirectory(server)
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
            properties.setProperty("currentBiomeRadius", currentBiomeRadius.toString())
            
            // Сохраняем координаты снегопада
            snowCenter?.let { pos ->
                properties.setProperty("snowCenterX", pos.x.toString())
                properties.setProperty("snowCenterY", pos.y.toString())
                properties.setProperty("snowCenterZ", pos.z.toString())
            }
            
            // Добавляем метку времени сохранения
            properties.setProperty("lastSaved", System.currentTimeMillis().toString())
            
            dataFile.outputStream().use { 
                properties.store(it, "Storyline Of Mineyarg Apocalypse Data - Per World Save")
            }
            
            // Сохраняем конфигурацию (глобально)
            StorylineOfMineyarg.config.saveConfig(File(server.serverDirectory, "config"))
            
            println("[${StorylineOfMineyarg.MOD_NAME}] INFO: Данные апокалипсиса сохранены успешно для мира")
            
        } catch (e: Exception) {
            System.err.println("[${StorylineOfMineyarg.MOD_NAME}] ERROR: Ошибка при сохранении данных: ${e.message}")
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
        currentBiomeRadius = 0
        
        snowCenter = null
        
        println("[${StorylineOfMineyarg.MOD_NAME}] INFO: Данные апокалипсиса сброшены")
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
        
        println("[${StorylineOfMineyarg.MOD_NAME}] INFO: Апокалипсис запущен${if (testMode) " в тестовом режиме" else ""}")
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
        
        println("[${StorylineOfMineyarg.MOD_NAME}] INFO: Апокалипсис остановлен${if (resetProgress) " с сбросом прогресса" else ""}")
    }
    
    /**
     * Ставит апокалипсис на паузу
     */
    fun pauseApocalypse() {
        if (isApocalypseActive && !isApocalypsePaused) {
            isApocalypsePaused = true
            pausedTime = System.currentTimeMillis()
            println("[${StorylineOfMineyarg.MOD_NAME}] INFO: Апокалипсис приостановлен")
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
            println("[${StorylineOfMineyarg.MOD_NAME}] INFO: Апокалипсис возобновлен")
        }
    }
    
    /**
     * Возвращает папку для сохранения данных мода для конкретного мира
     */
    private fun getDataDirectory(server: MinecraftServer): File {
        // Получаем имя мира
        val worldName = try {
            // Используем более простой способ получения имени мира
            val serverDir = server.serverDirectory
            val worldsDir = File(serverDir, "saves")
            if (worldsDir.exists()) {
                // Ищем папку мира по времени создания (самая новая)
                val worldDirs = worldsDir.listFiles { file -> file.isDirectory }
                if (worldDirs != null && worldDirs.isNotEmpty()) {
                    worldDirs.maxByOrNull { it.lastModified() }?.name ?: "world"
                } else {
                    "world"
                }
            } else {
                "world"
            }
        } catch (e: Exception) {
            // Fallback для случаев, когда не удается получить имя мира
            "world"
        }
        
        // Создаем папку для данных мода в папке конкретного мира
        val worldDataDir = File(server.serverDirectory, "saves/$worldName/data/storylineofmineyarg")
        
        // Если папка не существует, создаем её
        if (!worldDataDir.exists()) {
            worldDataDir.mkdirs()
        }
        
        return worldDataDir
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