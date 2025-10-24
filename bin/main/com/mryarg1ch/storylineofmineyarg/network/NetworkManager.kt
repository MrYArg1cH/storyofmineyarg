package com.mryarg1ch.storylineofmineyarg.network

import com.mryarg1ch.storylineofmineyarg.StorylineOfMineyarg
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraftforge.network.NetworkDirection
import net.minecraftforge.network.NetworkRegistry
import net.minecraftforge.network.PacketDistributor
import net.minecraftforge.network.simple.SimpleChannel

/**
 * Менеджер сетевых пакетов
 */
object NetworkManager {
    
    private const val PROTOCOL_VERSION = "1"
    
    val INSTANCE: SimpleChannel = NetworkRegistry.newSimpleChannel(
        ResourceLocation(StorylineOfMineyarg.MODID, "main"),
        { PROTOCOL_VERSION },
        PROTOCOL_VERSION::equals,
        PROTOCOL_VERSION::equals
    )
    
    private var messageId = 0
    
    fun registerPackets() {
        INSTANCE.messageBuilder(OpenSettingsPacket::class.java, messageId++)
            .decoder(::OpenSettingsPacket)
            .encoder(OpenSettingsPacket::encode)
            .consumerMainThread(OpenSettingsPacket::handle)
            .add()
    }
    
    fun sendToPlayer(packet: Any, player: ServerPlayer) {
        INSTANCE.send(PacketDistributor.PLAYER.with { player }, packet)
    }
}