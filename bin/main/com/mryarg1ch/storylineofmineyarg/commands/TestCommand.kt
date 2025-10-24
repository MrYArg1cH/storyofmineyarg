package com.mryarg1ch.storylineofmineyarg.commands

import com.mryarg1ch.storylineofmineyarg.StorylineOfMineyarg
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer

object TestCommand {
    
    fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(
            Commands.literal("apocalypse")
                .then(
                    Commands.literal("test")
                        .requires { source -> 
                            source.entity is ServerPlayer && 
                            StorylineOfMineyarg.isOperator(source.entity as ServerPlayer)
                        }
                        .executes(::execute)
                )
        )
    }
    
    private fun execute(context: CommandContext<CommandSourceStack>): Int {
        val source = context.source
        val player = source.entity as? ServerPlayer
        
        if (player == null) {
            source.sendFailure(Component.literal("Эта команда может быть выполнена только игроком"))
            return 0
        }
        
        if (!StorylineOfMineyarg.isOperator(player)) {
            source.sendFailure(Component.literal("У вас нет прав для выполнения этой команды"))
            return 0
        }
        
        try {
            val apocalypseManager = StorylineOfMineyarg.apocalypseManager
            
            // Проверяем, установлены ли координаты снегопада
            if (StorylineOfMineyarg.config.snowSpawnPosition == null) {
                source.sendFailure(Component.literal("Сначала установите координаты снегопада командой /apocalypse setspawnsnow"))
                return 0
            }
            
            // Запускаем тестовый режим
            apocalypseManager.startTestMode()
            
            // Уведомляем игрока
            source.sendSuccess(
                {
                    Component.literal("§e§l[ТЕСТ] §r§eЗапущен тестовый режим апокалипсиса! Все события будут происходить в ускоренном режиме.")
                },
                true
            )
            
            // Отправляем подробную информацию о тестовом режиме
            player.sendSystemMessage(Component.literal("§e§l[ТЕСТ] §r§eВ тестовом режиме:"))
            player.sendSystemMessage(Component.literal("§e- Увеличение спавна мобов: через 30 секунд"))
            player.sendSystemMessage(Component.literal("§e- Начало снегопада: через 60 секунд"))
            player.sendSystemMessage(Component.literal("§e- Эволюция мобов: через 90 секунд"))
            player.sendSystemMessage(Component.literal("§e- Все коэффициенты увеличены в 10 раз"))
            player.sendSystemMessage(Component.literal("§e§l[ТЕСТ] §r§eДля остановки используйте /apocalypse stop"))
            
            StorylineOfMineyarg.logAndMessage("Игрок ${player.name.string} запустил тестовый режим апокалипсиса")
            
            return 1
            
        } catch (e: Exception) {
            StorylineOfMineyarg.LOGGER.error("Ошибка при запуске тестового режима: ${e.message}")
            source.sendFailure(Component.literal("Произошла ошибка при запуске тестового режима"))
            return 0
        }
    }
}