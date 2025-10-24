package com.mryarg1ch.storylineofmineyarg.utils

import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent

/**
 * Помощник для работы с локализацией
 */
object LocalizationHelper {
    
    /**
     * Создает локализованный компонент
     */
    fun translatable(key: String, vararg args: Any): MutableComponent {
        return Component.translatable(key, *args)
    }
    
    /**
     * Создает локализованный компонент с форматированием
     */
    fun translatableFormatted(key: String, vararg args: Any): MutableComponent {
        return Component.translatable(key, *args)
    }
    
    /**
     * Получает локализованную строку для команд
     */
    fun getCommandMessage(key: String, vararg args: Any): Component {
        return translatable("commands.apocalypse.$key", *args)
    }
    
    /**
     * Получает локализованную строку для GUI
     */
    fun getGuiMessage(key: String, vararg args: Any): Component {
        return translatable("gui.apocalypse.$key", *args)
    }
    
    /**
     * Получает локализованную строку для событий апокалипсиса
     */
    fun getEventMessage(key: String, vararg args: Any): Component {
        return translatable("apocalypse.event.$key", *args)
    }
    
    /**
     * Получает локализованную строку для погоды
     */
    fun getWeatherMessage(key: String, vararg args: Any): Component {
        return translatable("weather.$key", *args)
    }
    
    /**
     * Получает локализованную строку для логов
     */
    fun getLogMessage(key: String, vararg args: Any): String {
        // Для логов возвращаем простую строку без форматирования
        return when (key) {
            "started" -> "Apocalypse started"
            "stopped" -> "Apocalypse stopped"
            "paused" -> "Apocalypse paused"
            "resumed" -> "Apocalypse resumed"
            "mob_spawn_triggered" -> "Mob spawn increase triggered"
            "snowfall_triggered" -> "Snowfall triggered"
            "mob_evolution_triggered" -> "Mob evolution triggered: level ${args.getOrNull(0) ?: "unknown"}"
            "snowfall_expanded" -> "Snowfall expanded to radius ${args.getOrNull(0) ?: "unknown"}"
            "weather_changed" -> "Weather in snow zone changed to: ${args.getOrNull(0) ?: "unknown"}"
            "settings_opened" -> "Player ${args.getOrNull(0) ?: "unknown"} opened apocalypse settings"
            "mob_spawn_blocked" -> "Blocked spawn of banned mob: ${args.getOrNull(0) ?: "unknown"}"
            else -> "Unknown log message: $key"
        }
    }
}