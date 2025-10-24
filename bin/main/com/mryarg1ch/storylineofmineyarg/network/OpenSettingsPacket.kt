package com.mryarg1ch.storylineofmineyarg.network

import com.mryarg1ch.storylineofmineyarg.gui.ApocalypseSettingsScreen
import net.minecraft.client.Minecraft
import net.minecraft.network.FriendlyByteBuf
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.fml.DistExecutor
import net.minecraftforge.network.NetworkEvent
import java.util.function.Supplier

/**
 * Пакет для открытия GUI настроек на клиенте
 */
class OpenSettingsPacket {
    
    constructor() {
        // Пустой конструктор для создания пакета
    }
    
    constructor(buf: FriendlyByteBuf) {
        // Конструктор для чтения из буфера (пакет пустой, ничего не читаем)
    }
    
    fun encode(buf: FriendlyByteBuf) {
        // Пакет пустой, ничего не записываем
    }
    
    fun handle(ctx: Supplier<NetworkEvent.Context>): Boolean {
        ctx.get().enqueueWork {
            // Выполняем на клиенте
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT) {
                Runnable {
                    val minecraft = Minecraft.getInstance()
                    minecraft.setScreen(ApocalypseSettingsScreen())
                }
            }
        }
        return true
    }
}