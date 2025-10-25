package com.mryarg1ch.storylineofmineyarg.gui

import com.mryarg1ch.storylineofmineyarg.StorylineOfMineyarg
import com.mryarg1ch.storylineofmineyarg.network.NetworkManager
import com.mryarg1ch.storylineofmineyarg.network.SaveSettingsPacket
import net.minecraft.client.gui.components.AbstractSliderButton
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn
import java.io.File
import java.io.FileWriter
import java.util.*

/**
 * GUI экран для настроек апокалипсиса со слайдерами
 */
@OnlyIn(Dist.CLIENT)
class ApocalypseSettingsScreen : Screen(Component.literal("Настройки Апокалипсиса")) {
    
    private val config = StorylineOfMineyarg.config
    
    // Временные значения для слайдеров
    private var tempDaysUntilMobSpawn = config.daysUntilMobSpawnIncrease
    private var tempDaysUntilSnowfall = config.daysUntilSnowfall
    private var tempDaysUntilEvolution = config.daysUntilMobEvolution
    private var tempMobSpawnCoeff = config.mobSpawnCoefficient
    private var tempSnowfallCoeff = config.snowfallSpeedCoefficient
    private var tempEvolutionCoeff = config.mobEvolutionSpeedCoefficient
    
    // Слайдеры
    private lateinit var mobSpawnDaysSlider: AbstractSliderButton
    private lateinit var snowfallDaysSlider: AbstractSliderButton
    private lateinit var evolutionDaysSlider: AbstractSliderButton
    private lateinit var mobSpawnCoeffSlider: AbstractSliderButton
    private lateinit var snowfallCoeffSlider: AbstractSliderButton
    private lateinit var evolutionCoeffSlider: AbstractSliderButton
    
    override fun init() {
        super.init()
        
        val centerX = width / 2
        val startY = 50
        val spacing = 35
        val sliderWidth = 200
        
        // Слайдер для дней до спавна зомби (1-30 дней)
        mobSpawnDaysSlider = object : AbstractSliderButton(centerX - sliderWidth/2, startY, sliderWidth, 20, 
            Component.literal("Дни до спавна зомби: $tempDaysUntilMobSpawn"), 
            (tempDaysUntilMobSpawn - 1.0) / 29.0) {
            
            override fun updateMessage() {
                tempDaysUntilMobSpawn = (1 + value * 29).toInt()
                setMessage(Component.literal("Дни до спавна зомби: $tempDaysUntilMobSpawn"))
            }
            
            override fun applyValue() {
                updateMessage()
            }
        }
        addRenderableWidget(mobSpawnDaysSlider)
        
        // Слайдер для дней до снегопада (1-30 дней)
        snowfallDaysSlider = object : AbstractSliderButton(centerX - sliderWidth/2, startY + spacing, sliderWidth, 20, 
            Component.literal("Дни до снегопада: $tempDaysUntilSnowfall"), 
            (tempDaysUntilSnowfall - 1.0) / 29.0) {
            
            override fun updateMessage() {
                tempDaysUntilSnowfall = (1 + value * 29).toInt()
                setMessage(Component.literal("Дни до снегопада: $tempDaysUntilSnowfall"))
            }
            
            override fun applyValue() {
                updateMessage()
            }
        }
        addRenderableWidget(snowfallDaysSlider)
        
        // Слайдер для дней до эволюции (1-20 дней)
        evolutionDaysSlider = object : AbstractSliderButton(centerX - sliderWidth/2, startY + spacing * 2, sliderWidth, 20,
            Component.literal("Дни до эволюции: $tempDaysUntilEvolution"),
            (tempDaysUntilEvolution - 1.0) / 19.0) {
            
            override fun updateMessage() {
                tempDaysUntilEvolution = (1 + value * 19).toInt()
                setMessage(Component.literal("Дни до эволюции: $tempDaysUntilEvolution"))
            }
            
            override fun applyValue() {
                updateMessage()
            }
        }
        addRenderableWidget(evolutionDaysSlider)
        
        // Слайдер для коэффициента спавна (1-100%)
        mobSpawnCoeffSlider = object : AbstractSliderButton(centerX - sliderWidth/2, startY + spacing * 3, sliderWidth, 20,
            Component.literal("Коэффициент спавна: ${tempMobSpawnCoeff}%"),
            (tempMobSpawnCoeff - 1.0) / 99.0) {
            
            override fun updateMessage() {
                tempMobSpawnCoeff = (1 + value * 99).toInt()
                setMessage(Component.literal("Коэффициент спавна: ${tempMobSpawnCoeff}%"))
            }
            
            override fun applyValue() {
                updateMessage()
            }
        }
        addRenderableWidget(mobSpawnCoeffSlider)
        
        // Слайдер для коэффициента скорости снегопада (1-100%)
        snowfallCoeffSlider = object : AbstractSliderButton(centerX - sliderWidth/2, startY + spacing * 4, sliderWidth, 20,
            Component.literal("Коэффициент скорости снега: ${tempSnowfallCoeff}%"),
            (tempSnowfallCoeff - 1.0) / 99.0) {
            
            override fun updateMessage() {
                tempSnowfallCoeff = (1 + value * 99).toInt()
                setMessage(Component.literal("Коэффициент скорости снега: ${tempSnowfallCoeff}%"))
            }
            
            override fun applyValue() {
                updateMessage()
            }
        }
        addRenderableWidget(snowfallCoeffSlider)
        
        // Слайдер для коэффициента эволюции (1-100%)
        evolutionCoeffSlider = object : AbstractSliderButton(centerX - sliderWidth/2, startY + spacing * 5, sliderWidth, 20,
            Component.literal("Коэффициент эволюции: ${tempEvolutionCoeff}%"),
            (tempEvolutionCoeff - 1.0) / 99.0) {
            
            override fun updateMessage() {
                tempEvolutionCoeff = (1 + value * 99).toInt()
                setMessage(Component.literal("Коэффициент эволюции: ${tempEvolutionCoeff}%"))
            }
            
            override fun applyValue() {
                updateMessage()
            }
        }
        addRenderableWidget(evolutionCoeffSlider)
        
        // Кнопки управления
        addRenderableWidget(Button.builder(Component.literal("Сохранить настройки")) { saveSettings() }
            .bounds(centerX - 155, startY + spacing * 7, 150, 20)
            .build())
        
        addRenderableWidget(Button.builder(Component.literal("Закрыть")) { onClose() }
            .bounds(centerX + 5, startY + spacing * 7, 150, 20)
            .build())
        
        // Кнопки статуса апокалипсиса
        val dataManager = StorylineOfMineyarg.dataManager
        if (dataManager.isApocalypseActive) {
            if (dataManager.isApocalypsePaused) {
                addRenderableWidget(Button.builder(Component.literal("Возобновить")) { resumeApocalypse() }
                    .bounds(centerX - 100, startY + spacing * 8, 200, 20)
                    .build())
            } else {
                addRenderableWidget(Button.builder(Component.literal("Приостановить")) { pauseApocalypse() }
                    .bounds(centerX - 100, startY + spacing * 8, 200, 20)
                    .build())
            }
            
            addRenderableWidget(Button.builder(Component.literal("Остановить апокалипсис")) { stopApocalypse() }
                .bounds(centerX - 100, startY + spacing * 9, 200, 20)
                .build())
        } else {
            addRenderableWidget(Button.builder(Component.literal("Начать апокалипсис")) { startApocalypse() }
                .bounds(centerX - 100, startY + spacing * 8, 200, 20)
                .build())
        }
    }
    
    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        renderBackground(guiGraphics)
        super.render(guiGraphics, mouseX, mouseY, partialTick)
        
        val centerX = width / 2
        
        // Отрисовываем заголовок
        guiGraphics.drawCenteredString(font, "§c§lНастройки Апокалипсиса", centerX, 20, 0xFFFFFF)
        
        // Статус апокалипсиса
        val dataManager = StorylineOfMineyarg.dataManager
        val statusY = height - 80
        guiGraphics.drawCenteredString(font, "§e§lСтатус апокалипсиса:", centerX, statusY, 0xFFFFFF)
        
        val status = when {
            !dataManager.isApocalypseActive -> "§7Неактивен"
            dataManager.isApocalypsePaused -> "§6Приостановлен"
            dataManager.isTestMode -> "§b§lТестовый режим"
            else -> "§a§lАктивен"
        }
        guiGraphics.drawCenteredString(font, status, centerX, statusY + 12, 0xFFFFFF)
        
        if (dataManager.isApocalypseActive) {
            val progress = "Прогресс: Зомби=${if (dataManager.mobSpawnIncreaseTriggered) "✓" else "✗"} " +
                          "Снег=${if (dataManager.snowfallTriggered) "✓" else "✗"} " +
                          "Эволюция=${if (dataManager.mobEvolutionLevel > 0) "✓" else "✗"}"
            guiGraphics.drawCenteredString(font, progress, centerX, statusY + 24, 0xFFFFFF)
        }
        
        // Инструкции
        guiGraphics.drawCenteredString(font, "§7Используйте слайдеры для настройки параметров", centerX, height - 40, 0xFFFFFF)
        guiGraphics.drawCenteredString(font, "§7Нажмите 'Сохранить настройки' для применения изменений", centerX, height - 25, 0xFFFFFF)
    }
    
    private fun saveSettings() {
        try {
            // Обновляем значения в локальной конфигурации
            config.daysUntilMobSpawnIncrease = tempDaysUntilMobSpawn
            config.daysUntilSnowfall = tempDaysUntilSnowfall
            config.daysUntilMobEvolution = tempDaysUntilEvolution
            config.mobSpawnCoefficient = tempMobSpawnCoeff
            config.snowfallSpeedCoefficient = tempSnowfallCoeff
            config.mobEvolutionSpeedCoefficient = tempEvolutionCoeff
            
            // Сохраняем в локальный файл конфигурации
            saveConfigToFile()
            
            // Отправляем настройки на сервер
            sendSettingsToServer()
            
            minecraft?.player?.sendSystemMessage(Component.literal("§a§lНастройки отправлены на сервер!"))
            
        } catch (e: Exception) {
            minecraft?.player?.sendSystemMessage(Component.literal("§c§lОшибка сохранения: ${e.message}"))
        }
    }
    
    private fun saveConfigToFile() {
        try {
            val configDir = File("config/storylineofmineyarg")
            if (!configDir.exists()) {
                configDir.mkdirs()
            }
            
            val configFile = File(configDir, "storylineofmineyarg-config.properties")
            val properties = Properties()
            
            // Загружаем существующие настройки
            if (configFile.exists()) {
                configFile.inputStream().use { properties.load(it) }
            }
            
            // Обновляем значения
            properties.setProperty("daysUntilMobSpawnIncrease", tempDaysUntilMobSpawn.toString())
            properties.setProperty("daysUntilSnowfall", tempDaysUntilSnowfall.toString())
            properties.setProperty("daysUntilMobEvolution", tempDaysUntilEvolution.toString())
            properties.setProperty("mobSpawnCoefficient", tempMobSpawnCoeff.toString())
            properties.setProperty("snowfallSpeedCoefficient", tempSnowfallCoeff.toString())
            properties.setProperty("mobEvolutionSpeedCoefficient", tempEvolutionCoeff.toString())
            
            // Сохраняем в файл
            FileWriter(configFile).use { writer ->
                properties.store(writer, "Apocalypse Mod Configuration - Updated via GUI")
            }
            
        } catch (e: Exception) {
            throw RuntimeException("Не удалось сохранить конфигурацию: ${e.message}")
        }
    }
    
    private fun startApocalypse() {
        // Отправляем команду на сервер
        minecraft?.player?.connection?.sendCommand("apocalypse start")
        onClose()
    }
    
    private fun pauseApocalypse() {
        minecraft?.player?.connection?.sendCommand("apocalypse pause")
        onClose()
    }
    
    private fun resumeApocalypse() {
        minecraft?.player?.connection?.sendCommand("apocalypse resume")
        onClose()
    }
    
    private fun stopApocalypse() {
        minecraft?.player?.connection?.sendCommand("apocalypse stop")
        onClose()
    }
    
    private fun sendSettingsToServer() {
        try {
            val packet = SaveSettingsPacket(
                tempDaysUntilMobSpawn,
                tempDaysUntilSnowfall,
                tempDaysUntilEvolution,
                tempMobSpawnCoeff.toDouble(),
                tempSnowfallCoeff.toDouble(),
                tempEvolutionCoeff.toDouble()
            )
            
            NetworkManager.INSTANCE.sendToServer(packet)
            
        } catch (e: Exception) {
            minecraft?.player?.sendSystemMessage(Component.literal("§c§lОшибка отправки на сервер: ${e.message}"))
        }
    }
    
    override fun isPauseScreen(): Boolean {
        return false
    }
}