package com.mryarg1ch.storylineofmineyarg.network

import com.mryarg1ch.storylineofmineyarg.StorylineOfMineyarg
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraftforge.network.NetworkEvent
import java.io.File
import java.util.function.Supplier

/**
 * Пакет для отправки настроек с клиента на сервер
 */
class SaveSettingsPacket {
    
    val daysUntilMobSpawn: Int
    val daysUntilSnowfall: Int
    val daysUntilEvolution: Int
    val mobSpawnCoeff: Double
    val snowfallCoeff: Double
    val evolutionCoeff: Double
    
    constructor(
        daysUntilMobSpawn: Int,
        daysUntilSnowfall: Int,
        daysUntilEvolution: Int,
        mobSpawnCoeff: Double,
        snowfallCoeff: Double,
        evolutionCoeff: Double
    ) {
        this.daysUntilMobSpawn = daysUntilMobSpawn
        this.daysUntilSnowfall = daysUntilSnowfall
        this.daysUntilEvolution = daysUntilEvolution
        this.mobSpawnCoeff = mobSpawnCoeff
        this.snowfallCoeff = snowfallCoeff
        this.evolutionCoeff = evolutionCoeff
    }
    
    constructor(buf: FriendlyByteBuf) {
        this.daysUntilMobSpawn = buf.readInt()
        this.daysUntilSnowfall = buf.readInt()
        this.daysUntilEvolution = buf.readInt()
        this.mobSpawnCoeff = buf.readDouble()
        this.snowfallCoeff = buf.readDouble()
        this.evolutionCoeff = buf.readDouble()
    }
    
    fun encode(buf: FriendlyByteBuf) {
        buf.writeInt(daysUntilMobSpawn)
        buf.writeInt(daysUntilSnowfall)
        buf.writeInt(daysUntilEvolution)
        buf.writeDouble(mobSpawnCoeff)
        buf.writeDouble(snowfallCoeff)
        buf.writeDouble(evolutionCoeff)
    }
    
    fun handle(ctx: Supplier<NetworkEvent.Context>): Boolean {
        ctx.get().enqueueWork {
            val player = ctx.get().sender
            if (player != null && StorylineOfMineyarg.isOperator(player)) {
                try {
                    // Обновляем серверную конфигурацию
                    val config = StorylineOfMineyarg.config
                    config.daysUntilMobSpawnIncrease = daysUntilMobSpawn
                    config.daysUntilSnowfall = daysUntilSnowfall
                    config.daysUntilMobEvolution = daysUntilEvolution
                    config.mobSpawnCoefficient = mobSpawnCoeff.toInt()
                    config.snowfallSpeedCoefficient = snowfallCoeff.toInt()
                    config.mobEvolutionSpeedCoefficient = evolutionCoeff.toInt()
                    
                    // Сохраняем конфигурацию на сервере
                    val server = player.server!!
                    config.saveConfig(File(server.serverDirectory, "config"))
                    
                    // Сохраняем данные апокалипсиса
                    StorylineOfMineyarg.dataManager.saveData(server)
                    
                    // Отправляем подтверждение игроку
                    player.sendSystemMessage(Component.literal("§a§lНастройки сохранены на сервере!"))
                    
                    // Логируем изменения
                    StorylineOfMineyarg.logAndMessage("Игрок ${player.name.string} обновил настройки апокалипсиса")
                    
                } catch (e: Exception) {
                    player.sendSystemMessage(Component.literal("§c§lОшибка сохранения на сервере: ${e.message}"))
                    System.err.println("[${StorylineOfMineyarg.MOD_NAME}] ERROR: Ошибка сохранения настроек: ${e.message}")
                }
            } else {
                player?.sendSystemMessage(Component.literal("§c§lУ вас нет прав для изменения настроек!"))
            }
        }
        ctx.get().packetHandled = true
        return true
    }
}