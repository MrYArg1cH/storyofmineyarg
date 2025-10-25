package com.mryarg1ch.storylineofmineyarg.utils

import com.mryarg1ch.storylineofmineyarg.StorylineOfMineyarg
import net.minecraft.server.MinecraftServer
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.text.SimpleDateFormat
import java.util.*

/**
 * Система логирования прогресса апокалипсиса
 */
object ApocalypseLogger {
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    private var logFile: File? = null
    private var isServerMode = false
    
    /**
     * Инициализирует логгер
     */
    fun initialize(server: MinecraftServer?) {
        if (!StorylineOfMineyarg.config.enableProgressLogging) {
            return
        }
        
        isServerMode = server != null
        
        if (isServerMode && server != null) {
            // Серверный режим - сохраняем в файл
            val logDir = File(server.serverDirectory, "config/storylineofmineyarg/logs")
            if (!logDir.exists()) {
                logDir.mkdirs()
            }
            
            val logFileName = "apocalypse-${SimpleDateFormat("yyyy-MM-dd").format(Date())}.log"
            logFile = File(logDir, logFileName)
            
            logProgress("=== Логирование апокалипсиса инициализировано ===")
            logProgress("Режим: Сервер")
            logProgress("Файл лога: ${logFile!!.absolutePath}")
        } else {
            // Одиночный режим - только в консоль
            logProgress("=== Логирование апокалипсиса инициализировано ===")
            logProgress("Режим: Одиночная игра (только консоль)")
        }
    }
    
    /**
     * Логирует прогресс апокалипсиса
     */
    fun logProgress(message: String) {
        if (!StorylineOfMineyarg.config.enableProgressLogging) {
            return
        }
        
        val timestamp = dateFormat.format(Date())
        val logMessage = "[$timestamp] [APOCALYPSE] $message"
        
        // Всегда логируем в консоль мода
        println(logMessage)
        
        // В серверном режиме также сохраняем в файл
        if (isServerMode && logFile != null) {
            try {
                FileWriter(logFile, true).use { writer ->
                    PrintWriter(writer).use { printer ->
                        printer.println(logMessage)
                    }
                }
            } catch (e: Exception) {
                System.err.println("[${StorylineOfMineyarg.MOD_NAME}] ERROR: Ошибка при записи в лог файл: ${e.message}")
            }
        }
    }
    
    /**
     * Логирует начало апокалипсиса
     */
    fun logApocalypseStart(testMode: Boolean = false) {
        logProgress("=== АПОКАЛИПСИС НАЧАЛСЯ ===")
        logProgress("Тестовый режим: ${if (testMode) "ДА" else "НЕТ"}")
        logProgress("Настройки:")
        logProgress("  - Дни до увеличения спавна зомби: ${StorylineOfMineyarg.config.daysUntilMobSpawnIncrease}")
        logProgress("  - Дни до снегопада: ${StorylineOfMineyarg.config.daysUntilSnowfall}")
        logProgress("  - Дни до эволюции зомби: ${StorylineOfMineyarg.config.daysUntilMobEvolution}")
        logProgress("  - Коэффициент спавна зомби: ${StorylineOfMineyarg.config.mobSpawnCoefficient}%")
        logProgress("  - Коэффициент эволюции зомби: ${StorylineOfMineyarg.config.mobEvolutionSpeedCoefficient}%")
        logProgress("  - Коэффициент скорости снегопада: ${StorylineOfMineyarg.config.snowfallSpeedCoefficient}%")
        
        val snowPos = StorylineOfMineyarg.config.snowSpawnPosition
        if (snowPos != null) {
            logProgress("  - Координаты начала снегопада: ${snowPos.x}, ${snowPos.y}, ${snowPos.z}")
        } else {
            logProgress("  - Координаты начала снегопада: НЕ УСТАНОВЛЕНЫ")
        }
    }
    
    /**
     * Логирует остановку апокалипсиса
     */
    fun logApocalypseStop(resetProgress: Boolean) {
        logProgress("=== АПОКАЛИПСИС ОСТАНОВЛЕН ===")
        logProgress("Сброс прогресса: ${if (resetProgress) "ДА" else "НЕТ"}")
        
        if (!resetProgress) {
            logCurrentProgress()
        }
    }
    
    /**
     * Логирует паузу апокалипсиса
     */
    fun logApocalypsePause() {
        logProgress("=== АПОКАЛИПСИС ПРИОСТАНОВЛЕН ===")
        logCurrentProgress()
    }
    
    /**
     * Логирует возобновление апокалипсиса
     */
    fun logApocalypseResume() {
        logProgress("=== АПОКАЛИПСИС ВОЗОБНОВЛЕН ===")
        logCurrentProgress()
    }
    
    /**
     * Логирует событие апокалипсиса
     */
    fun logApocalypseEvent(eventName: String, details: String = "") {
        logProgress("СОБЫТИЕ: $eventName")
        if (details.isNotEmpty()) {
            logProgress("  Детали: $details")
        }
        logCurrentProgress()
    }
    
    /**
     * Логирует текущий прогресс
     */
    private fun logCurrentProgress() {
        val dataManager = StorylineOfMineyarg.dataManager
        val effectiveTime = dataManager.getEffectiveApocalypseTime()
        val hours = effectiveTime / (1000 * 60 * 60)
        val minutes = (effectiveTime % (1000 * 60 * 60)) / (1000 * 60)
        
        logProgress("Текущий прогресс:")
        logProgress("  - Эффективное время: ${hours}ч ${minutes}м")
        logProgress("  - Увеличение спавна зомби: ${if (dataManager.mobSpawnIncreaseTriggered) "АКТИВИРОВАНО" else "НЕ АКТИВИРОВАНО"}")
        logProgress("  - Снегопад: ${if (dataManager.snowfallTriggered) "АКТИВИРОВАН" else "НЕ АКТИВИРОВАН"}")
        logProgress("  - Уровень эволюции зомби: ${dataManager.mobEvolutionLevel}")
        logProgress("  - Радиус снегопада: ${dataManager.currentSnowRadius} блоков")
    }
    
    /**
     * Логирует изменение настроек
     */
    fun logSettingsChange(settingName: String, oldValue: Any, newValue: Any) {
        logProgress("ИЗМЕНЕНИЕ НАСТРОЕК: $settingName")
        logProgress("  Старое значение: $oldValue")
        logProgress("  Новое значение: $newValue")
    }
    
    /**
     * Логирует ошибку
     */
    fun logError(error: String, exception: Exception? = null) {
        logProgress("ОШИБКА: $error")
        if (exception != null) {
            logProgress("  Исключение: ${exception.javaClass.simpleName}: ${exception.message}")
        }
    }
    
    /**
     * Создает отчет о состоянии апокалипсиса
     */
    fun generateStatusReport(): List<String> {
        val report = mutableListOf<String>()
        val dataManager = StorylineOfMineyarg.dataManager
        val config = StorylineOfMineyarg.config
        
        report.add("=== ОТЧЕТ О СОСТОЯНИИ АПОКАЛИПСИСА ===")
        report.add("Время создания: ${dateFormat.format(Date())}")
        report.add("")
        
        report.add("СОСТОЯНИЕ:")
        report.add("  Активен: ${if (dataManager.isApocalypseActive) "ДА" else "НЕТ"}")
        report.add("  На паузе: ${if (dataManager.isApocalypsePaused) "ДА" else "НЕТ"}")
        report.add("  Тестовый режим: ${if (dataManager.isTestMode) "ДА" else "НЕТ"}")
        report.add("")
        
        if (dataManager.isApocalypseActive) {
            val effectiveTime = dataManager.getEffectiveApocalypseTime()
            val hours = effectiveTime / (1000 * 60 * 60)
            val minutes = (effectiveTime % (1000 * 60 * 60)) / (1000 * 60)
            
            report.add("ПРОГРЕСС:")
            report.add("  Эффективное время: ${hours}ч ${minutes}м")
            report.add("  Увеличение спавна зомби: ${if (dataManager.mobSpawnIncreaseTriggered) "АКТИВИРОВАНО" else "НЕ АКТИВИРОВАНО"}")
            report.add("  Снегопад: ${if (dataManager.snowfallTriggered) "АКТИВИРОВАН" else "НЕ АКТИВИРОВАН"}")
            report.add("  Уровень эволюции зомби: ${dataManager.mobEvolutionLevel}")
            report.add("  Радиус снегопада: ${dataManager.currentSnowRadius} блоков")
            report.add("")
        }
        
        report.add("НАСТРОЙКИ:")
        report.add("  Дни до увеличения спавна зомби: ${config.daysUntilMobSpawnIncrease}")
        report.add("  Дни до снегопада: ${config.daysUntilSnowfall}")
        report.add("  Дни до эволюции зомби: ${config.daysUntilMobEvolution}")
        report.add("  Коэффициент спавна зомби: ${config.mobSpawnCoefficient}%")
        report.add("  Коэффициент эволюции зомби: ${config.mobEvolutionSpeedCoefficient}%")
        report.add("  Коэффициент скорости снегопада: ${config.snowfallSpeedCoefficient}%")
        report.add("  Логирование: ${if (config.enableProgressLogging) "ВКЛЮЧЕНО" else "ВЫКЛЮЧЕНО"}")
        
        val snowPos = config.snowSpawnPosition
        if (snowPos != null) {
            report.add("  Координаты снегопада: ${snowPos.x}, ${snowPos.y}, ${snowPos.z}")
        } else {
            report.add("  Координаты снегопада: НЕ УСТАНОВЛЕНЫ")
        }
        
        report.add("")
        report.add("=== КОНЕЦ ОТЧЕТА ===")
        
        return report
    }
}