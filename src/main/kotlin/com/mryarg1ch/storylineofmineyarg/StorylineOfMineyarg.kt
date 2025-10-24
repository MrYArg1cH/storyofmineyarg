package com.mryarg1ch.storylineofmineyarg

import com.mryarg1ch.storylineofmineyarg.commands.*
import com.mryarg1ch.storylineofmineyarg.config.ApocalypseConfig
import com.mryarg1ch.storylineofmineyarg.data.ApocalypseDataManager
import com.mryarg1ch.storylineofmineyarg.events.ApocalypseEventHandler
import com.mryarg1ch.storylineofmineyarg.events.MobSpawnController
import com.mryarg1ch.storylineofmineyarg.apocalypse.ApocalypseManager
import com.mryarg1ch.storylineofmineyarg.network.NetworkManager
import net.minecraft.server.level.ServerPlayer
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.RegisterCommandsEvent
import net.minecraftforge.event.server.ServerStartingEvent
import net.minecraftforge.event.server.ServerStoppingEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import thedarkcolour.kotlinforforge.forge.MOD_BUS

@Mod(StorylineOfMineyarg.MOD_ID)
object StorylineOfMineyarg {
    const val MOD_ID = "storylineofmineyarg"
    const val MODID = MOD_ID // Для совместимости
    const val MOD_NAME = "Storyline Of Mineyarg"
    const val VERSION = "1.0.0"
    const val AUTHOR = "MrYArg1cH"
    
    val LOGGER: Logger = LogManager.getLogger(MOD_NAME)
    
    lateinit var config: ApocalypseConfig
    lateinit var dataManager: ApocalypseDataManager
    lateinit var apocalypseManager: ApocalypseManager
    
    init {
        LOGGER.info("Initializing $MOD_NAME v$VERSION by $AUTHOR")
        
        // Регистрируем обработчики событий
        MOD_BUS.addListener(::onCommonSetup)
        MinecraftForge.EVENT_BUS.register(this)
        MinecraftForge.EVENT_BUS.register(ApocalypseEventHandler)
        MinecraftForge.EVENT_BUS.register(MobSpawnController)
        
        LOGGER.info("$MOD_NAME initialization completed")
    }
    
    private fun onCommonSetup(event: FMLCommonSetupEvent) {
        LOGGER.info("Common setup for $MOD_NAME")
        
        // Регистрация сетевых пакетов
        NetworkManager.registerPackets()
        
        // Инициализация конфигурации
        config = ApocalypseConfig()
        
        // Инициализация менеджера данных
        dataManager = ApocalypseDataManager()
        
        // Инициализация менеджера апокалипсиса
        apocalypseManager = ApocalypseManager()
        
        LOGGER.info("Common setup completed for $MOD_NAME")
    }
    
    @SubscribeEvent
    fun onServerStarting(event: ServerStartingEvent) {
        LOGGER.info("Server starting - loading $MOD_NAME data")
        
        // Загружаем данные при запуске сервера
        dataManager.loadData(event.server)
        apocalypseManager.onServerStart(event.server)
        
        LOGGER.info("$MOD_NAME data loaded successfully")
    }
    
    @SubscribeEvent
    fun onServerStopping(event: ServerStoppingEvent) {
        LOGGER.info("Server stopping - saving $MOD_NAME data")
        
        // Сохраняем данные при остановке сервера
        dataManager.saveData(event.server)
        apocalypseManager.onServerStop()
        
        LOGGER.info("$MOD_NAME data saved successfully")
    }
    
    @SubscribeEvent
    fun onRegisterCommands(event: RegisterCommandsEvent) {
        LOGGER.info("Registering commands for $MOD_NAME")
        
        // Регистрируем все команды мода
        StartCommand.register(event.dispatcher)
        StopCommand.register(event.dispatcher)
        PauseCommand.register(event.dispatcher)
        SettingsCommand.register(event.dispatcher)
        SetSpawnSnowCommand.register(event.dispatcher)
        TestCommand.register(event.dispatcher)
        
        LOGGER.info("Commands registered for $MOD_NAME")
    }
    
    /**
     * Проверяет, является ли игрок оператором
     */
    fun isOperator(player: ServerPlayer): Boolean {
        return player.server.playerList.isOp(player.gameProfile)
    }
    
    /**
     * Отправляет сообщение в лог и игроку (если указан)
     */
    fun logAndMessage(message: String, player: ServerPlayer? = null) {
        LOGGER.info(message)
        player?.sendSystemMessage(net.minecraft.network.chat.Component.literal(message))
    }
}