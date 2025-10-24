package com.mryarg1ch.storylineofmineyarg.gui

import com.mryarg1ch.storylineofmineyarg.StorylineOfMineyarg
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn

/**
 * Упрощенный GUI экран для настроек апокалипсиса
 */
@OnlyIn(Dist.CLIENT)
class ApocalypseSettingsScreen : Screen(Component.literal("Настройки Апокалипсиса")) {
    
    private val config = StorylineOfMineyarg.config
    
    override fun init() {
        super.init()
        
        val centerX = width / 2
        val startY = 60
        val spacing = 25
        
        // Кнопки управления
        addRenderableWidget(Button.builder(Component.literal("Сохранить")) { saveSettings() }
            .bounds(centerX - 155, startY + spacing * 8, 100, 20)
            .build())
        
        addRenderableWidget(Button.builder(Component.literal("Закрыть")) { onClose() }
            .bounds(centerX + 55, startY + spacing * 8, 100, 20)
            .build())
        
        // Кнопки статуса апокалипсиса
        val dataManager = StorylineOfMineyarg.dataManager
        if (dataManager.isApocalypseActive) {
            if (dataManager.isApocalypsePaused) {
                addRenderableWidget(Button.builder(Component.literal("Возобновить")) { resumeApocalypse() }
                    .bounds(centerX - 100, startY + spacing * 10, 200, 20)
                    .build())
            } else {
                addRenderableWidget(Button.builder(Component.literal("Приостановить")) { pauseApocalypse() }
                    .bounds(centerX - 100, startY + spacing * 10, 200, 20)
                    .build())
            }
            
            addRenderableWidget(Button.builder(Component.literal("Остановить апокалипсис")) { stopApocalypse() }
                .bounds(centerX - 100, startY + spacing * 11, 200, 20)
                .build())
        } else {
            addRenderableWidget(Button.builder(Component.literal("Начать апокалипсис")) { startApocalypse() }
                .bounds(centerX - 100, startY + spacing * 10, 200, 20)
                .build())
        }
    }
    
    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        renderBackground(guiGraphics)
        super.render(guiGraphics, mouseX, mouseY, partialTick)
        
        val centerX = width / 2
        val startY = 60
        val spacing = 15
        
        // Отрисовываем заголовок и информацию
        guiGraphics.drawCenteredString(font, "§c§lНастройки Апокалипсиса", centerX, 20, 0xFFFFFF)
        
        // Текущие настройки
        guiGraphics.drawCenteredString(font, "§e§lТекущие настройки:", centerX, startY, 0xFFFFFF)
        guiGraphics.drawCenteredString(font, "Дни до спавна мобов: ${config.daysUntilMobSpawnIncrease}", centerX, startY + spacing, 0xFFFFFF)
        guiGraphics.drawCenteredString(font, "Дни до снегопада: ${config.daysUntilSnowfall}", centerX, startY + spacing * 2, 0xFFFFFF)
        guiGraphics.drawCenteredString(font, "Дни до эволюции: ${config.daysUntilMobEvolution}", centerX, startY + spacing * 3, 0xFFFFFF)
        guiGraphics.drawCenteredString(font, "Коэффициент спавна: ${config.mobSpawnCoefficient}%", centerX, startY + spacing * 4, 0xFFFFFF)
        guiGraphics.drawCenteredString(font, "Коэффициент эволюции: ${config.mobEvolutionSpeedCoefficient}%", centerX, startY + spacing * 5, 0xFFFFFF)
        guiGraphics.drawCenteredString(font, "Скорость снегопада: ${config.snowfallSpeedCoefficient}%", centerX, startY + spacing * 6, 0xFFFFFF)
        guiGraphics.drawCenteredString(font, "Логирование: ${if (config.enableProgressLogging) "Включено" else "Выключено"}", centerX, startY + spacing * 7, 0xFFFFFF)
        
        // Статус апокалипсиса
        val dataManager = StorylineOfMineyarg.dataManager
        val statusY = startY + spacing * 12
        guiGraphics.drawCenteredString(font, "§e§lСтатус апокалипсиса:", centerX, statusY, 0xFFFFFF)
        
        val status = when {
            !dataManager.isApocalypseActive -> "§7Неактивен"
            dataManager.isApocalypsePaused -> "§6Приостановлен"
            dataManager.isTestMode -> "§b§lТестовый режим"
            else -> "§a§lАктивен"
        }
        guiGraphics.drawCenteredString(font, status, centerX, statusY + 12, 0xFFFFFF)
        
        if (dataManager.isApocalypseActive) {
            val progress = "Прогресс: Мобы=${if (dataManager.mobSpawnIncreaseTriggered) "✓" else "✗"} " +
                          "Снег=${if (dataManager.snowfallTriggered) "✓" else "✗"} " +
                          "Эволюция=${if (dataManager.mobEvolutionLevel > 0) "✓" else "✗"}"
            guiGraphics.drawCenteredString(font, progress, centerX, statusY + 24, 0xFFFFFF)
        }
        
        // Инструкции
        guiGraphics.drawCenteredString(font, "§7Для изменения настроек используйте конфигурационный файл", centerX, height - 40, 0xFFFFFF)
        guiGraphics.drawCenteredString(font, "§7config/storylineofmineyarg/storylineofmineyarg-config.properties", centerX, height - 25, 0xFFFFFF)
    }
    
    private fun saveSettings() {
        try {
            // Уведомляем о сохранении (конфигурация сохраняется автоматически)
            minecraft?.player?.sendSystemMessage(Component.literal("§a§lНастройки отображены!"))
            
        } catch (e: Exception) {
            minecraft?.player?.sendSystemMessage(Component.literal("§c§lОшибка!"))
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
    
    override fun isPauseScreen(): Boolean {
        return false
    }
}