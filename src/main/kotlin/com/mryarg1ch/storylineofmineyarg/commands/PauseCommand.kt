package com.mryarg1ch.storylineofmineyarg.commands

import com.mryarg1ch.storylineofmineyarg.StorylineOfMineyarg
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer

object PauseCommand {
    
    fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(
            Commands.literal("apocalypse")
                .then(
                    Commands.literal("pause")
                        .requires { source -> 
                            source.entity is ServerPlayer && 
                            StorylineOfMineyarg.isOperator(source.entity as ServerPlayer)
                        }
                        .executes(::execute)
                )
                .then(
                    Commands.literal("resume")
                        .requires { source -> 
                            source.entity is ServerPlayer && 
                            StorylineOfMineyarg.isOperator(source.entity as ServerPlayer)
                        }
                        .executes(::executeResume)
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
            
            if (!apocalypseManager.isActive()) {
                source.sendFailure(Component.literal("Апокалипсис не активен"))
                return 0
            }
            
            if (apocalypseManager.isPaused()) {
                source.sendFailure(Component.literal("Апокалипсис уже на паузе"))
                return 0
            }
            
            // Ставим апокалипсис на паузу
            apocalypseManager.pauseApocalypse()
            
            // Уведомляем всех игроков
            val server = source.server
            val message = Component.literal("§e§l[АПОКАЛИПСИС] §r§eАпокалипсис приостановлен игроком ${player.name.string}. Прогресс сохранен.")
            
            server.playerList.players.forEach { serverPlayer ->
                serverPlayer.sendSystemMessage(message)
            }
            
            StorylineOfMineyarg.logAndMessage("Апокалипсис приостановлен игроком ${player.name.string}")
            
            return 1
            
        } catch (e: Exception) {
            System.err.println("[${StorylineOfMineyarg.MOD_NAME}] ERROR: Ошибка при приостановке апокалипсиса: ${e.message}")
            source.sendFailure(Component.literal("Произошла ошибка при приостановке апокалипсиса"))
            return 0
        }
    }
    
    private fun executeResume(context: CommandContext<CommandSourceStack>): Int {
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
            
            if (!apocalypseManager.isPaused()) {
                source.sendFailure(Component.literal("Апокалипсис не на паузе"))
                return 0
            }
            
            // Возобновляем апокалипсис
            apocalypseManager.resumeApocalypse()
            
            // Уведомляем всех игроков
            val server = source.server
            val message = Component.literal("§c§l[АПОКАЛИПСИС] §r§cАпокалипсис возобновлен игроком ${player.name.string}!")
            
            server.playerList.players.forEach { serverPlayer ->
                serverPlayer.sendSystemMessage(message)
            }
            
            StorylineOfMineyarg.logAndMessage("Апокалипсис возобновлен игроком ${player.name.string}")
            
            return 1
            
        } catch (e: Exception) {
            System.err.println("[${StorylineOfMineyarg.MOD_NAME}] ERROR: Ошибка при возобновлении апокалипсиса: ${e.message}")
            source.sendFailure(Component.literal("Произошла ошибка при возобновлении апокалипсиса"))
            return 0
        }
    }
}