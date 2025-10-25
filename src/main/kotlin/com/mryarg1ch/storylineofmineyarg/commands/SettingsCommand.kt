package com.mryarg1ch.storylineofmineyarg.commands

import com.mryarg1ch.storylineofmineyarg.StorylineOfMineyarg
import com.mryarg1ch.storylineofmineyarg.network.NetworkManager
import com.mryarg1ch.storylineofmineyarg.network.OpenSettingsPacket
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer

object SettingsCommand {
    
    fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(
            Commands.literal("apocalypse")
                .then(
                    Commands.literal("settings")
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
            // Отправляем пакет для открытия GUI на клиенте
            NetworkManager.sendToPlayer(OpenSettingsPacket(), player)
            
            StorylineOfMineyarg.logAndMessage("Игрок ${player.name.string} открыл настройки апокалипсиса")
            
            return 1
            
        } catch (e: Exception) {
            System.err.println("[${StorylineOfMineyarg.MOD_NAME}] ERROR: Ошибка при открытии настроек: ${e.message}")
            source.sendFailure(Component.literal("Произошла ошибка при открытии настроек"))
            return 0
        }
    }
}