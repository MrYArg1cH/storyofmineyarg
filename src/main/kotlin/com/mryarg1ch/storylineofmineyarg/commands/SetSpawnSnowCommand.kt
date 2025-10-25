package com.mryarg1ch.storylineofmineyarg.commands

import com.mryarg1ch.storylineofmineyarg.StorylineOfMineyarg
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer

object SetSpawnSnowCommand {
    
    fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(
            Commands.literal("apocalypse")
                .then(
                    Commands.literal("setspawnsnow")
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
            // Получаем текущую позицию игрока
            val playerPos = player.blockPosition()
            val snowSpawnPos = BlockPos(playerPos.x, playerPos.y, playerPos.z)
            
            // Сохраняем координаты в конфигурации
            StorylineOfMineyarg.config.snowSpawnPosition = snowSpawnPos
            
            // Сохраняем конфигурацию
            val configDir = player.server.serverDirectory.resolve("config").toPath().toFile()
            StorylineOfMineyarg.config.saveConfig(configDir)
            
            // Уведомляем игрока
            source.sendSuccess(
                {
                    Component.literal("§a§l[АПОКАЛИПСИС] §r§aКоординаты начала снегопада установлены: §f${snowSpawnPos.x}, ${snowSpawnPos.y}, ${snowSpawnPos.z}")
                },
                true
            )
            
            StorylineOfMineyarg.logAndMessage(
                "Игрок ${player.name.string} установил координаты снегопада: ${snowSpawnPos.x}, ${snowSpawnPos.y}, ${snowSpawnPos.z}"
            )
            
            return 1
            
        } catch (e: Exception) {
            System.err.println("[${StorylineOfMineyarg.MOD_NAME}] ERROR: Ошибка при установке координат снегопада: ${e.message}")
            source.sendFailure(Component.literal("Произошла ошибка при установке координат снегопада"))
            return 0
        }
    }
}