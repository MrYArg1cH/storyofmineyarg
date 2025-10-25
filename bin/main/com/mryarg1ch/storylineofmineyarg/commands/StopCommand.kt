package com.mryarg1ch.storylineofmineyarg.commands

import com.mryarg1ch.storylineofmineyarg.StorylineOfMineyarg
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer

object StopCommand {
    
    fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(
            Commands.literal("apocalypse")
                .then(
                    Commands.literal("stop")
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
            
            if (!apocalypseManager.isActive() && !apocalypseManager.isPaused()) {
                source.sendFailure(Component.literal("Апокалипсис не активен"))
                return 0
            }
            
            // Останавливаем апокалипсис с потерей прогресса
            apocalypseManager.stopApocalypse(resetProgress = true)
            
            // Уведомляем всех игроков
            val server = source.server
            val message = Component.literal("§a§l[АПОКАЛИПСИС] §r§aАпокалипсис остановлен! Игрок ${player.name.string} прекратил конец света. Весь прогресс сброшен.")
            
            server.playerList.players.forEach { serverPlayer ->
                serverPlayer.sendSystemMessage(message)
            }
            
            StorylineOfMineyarg.logAndMessage("Апокалипсис остановлен игроком ${player.name.string} с потерей прогресса")
            
            return 1
            
        } catch (e: Exception) {
            System.err.println("[${StorylineOfMineyarg.MOD_NAME}] ERROR: Ошибка при остановке апокалипсиса: ${e.message}")
            source.sendFailure(Component.literal("Произошла ошибка при остановке апокалипсиса"))
            return 0
        }
    }
}