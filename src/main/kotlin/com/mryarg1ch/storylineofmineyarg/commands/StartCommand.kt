package com.mryarg1ch.storylineofmineyarg.commands

import com.mryarg1ch.storylineofmineyarg.StorylineOfMineyarg
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer

object StartCommand {
    
    fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(
            Commands.literal("apocalypse")
                .then(
                    Commands.literal("start")
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
            
            if (apocalypseManager.isActive()) {
                source.sendFailure(Component.literal("Апокалипсис уже активен!"))
                return 0
            }
            
            // Проверяем, установлены ли координаты снегопада
            if (StorylineOfMineyarg.config.snowSpawnPosition == null) {
                source.sendFailure(Component.literal("Сначала установите координаты снегопада командой /apocalypse setspawnsnow"))
                return 0
            }
            
            // Запускаем апокалипсис
            apocalypseManager.startApocalypse()
            
            // Уведомляем всех игроков
            val server = source.server
            val message = Component.literal("§c§l[АПОКАЛИПСИС] §r§cАпокалипсис начался! Игрок ${player.name.string} запустил конец света...")
            
            server.playerList.players.forEach { serverPlayer ->
                serverPlayer.sendSystemMessage(message)
            }
            
            StorylineOfMineyarg.logAndMessage("Апокалипсис запущен игроком ${player.name.string}")
            
            return 1
            
        } catch (e: Exception) {
            StorylineOfMineyarg.LOGGER.error("Ошибка при запуске апокалипсиса: ${e.message}")
            source.sendFailure(Component.literal("Произошла ошибка при запуске апокалипсиса"))
            return 0
        }
    }
}