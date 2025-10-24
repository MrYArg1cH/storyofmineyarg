package com.mryarg1ch.storylineofmineyarg.apocalypse

import com.mryarg1ch.storylineofmineyarg.StorylineOfMineyarg
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.monster.*
import net.minecraft.world.item.Items
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.enchantment.Enchantments
import net.minecraft.world.item.enchantment.EnchantmentHelper
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.level.biome.Biomes
import net.minecraft.world.level.chunk.ChunkAccess
import net.minecraft.world.level.chunk.LevelChunk
import net.minecraft.core.Holder
import net.minecraft.world.level.biome.Biome
import net.minecraft.world.level.Level
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import kotlin.math.*

/**
 * Основной менеджер апокалипсиса
 */
class ApocalypseManager {
    
    private var server: MinecraftServer? = null
    private val scheduler: ScheduledExecutorService = Executors.newScheduledThreadPool(2)
    private var isRunning = false
    private val weatherManager = WeatherManager()
    
    /**
     * Инициализация при запуске сервера
     */
    fun onServerStart(server: MinecraftServer) {
        this.server = server
        weatherManager.onServerStart(server)
        
        // Если апокалипсис был активен, возобновляем его
        if (StorylineOfMineyarg.dataManager.isApocalypseActive) {
            startApocalypseLoop()
        }
    }
    
    /**
     * Очистка при остановке сервера
     */
    fun onServerStop() {
        stopApocalypseLoop()
        weatherManager.onServerStop()
        scheduler.shutdown()
    }
    
    /**
     * Проверяет, активен ли апокалипсис
     */
    fun isActive(): Boolean = StorylineOfMineyarg.dataManager.isApocalypseActive
    
    /**
     * Проверяет, на паузе ли апокалипсис
     */
    fun isPaused(): Boolean = StorylineOfMineyarg.dataManager.isApocalypsePaused
    
    /**
     * Запускает апокалипсис
     */
    fun startApocalypse() {
        StorylineOfMineyarg.dataManager.startApocalypse(false)
        startApocalypseLoop()
        server?.let { StorylineOfMineyarg.dataManager.saveData(it) }
    }
    
    /**
     * Запускает тестовый режим
     */
    fun startTestMode() {
        StorylineOfMineyarg.dataManager.startApocalypse(true)
        startApocalypseLoop()
        server?.let { StorylineOfMineyarg.dataManager.saveData(it) }
    }
    
    /**
     * Останавливает апокалипсис
     */
    fun stopApocalypse(resetProgress: Boolean = true) {
        StorylineOfMineyarg.dataManager.stopApocalypse(resetProgress)
        stopApocalypseLoop()
        server?.let { StorylineOfMineyarg.dataManager.saveData(it) }
    }
    
    /**
     * Приостанавливает апокалипсис
     */
    fun pauseApocalypse() {
        StorylineOfMineyarg.dataManager.pauseApocalypse()
        server?.let { StorylineOfMineyarg.dataManager.saveData(it) }
    }
    
    /**
     * Возобновляет апокалипсис
     */
    fun resumeApocalypse() {
        StorylineOfMineyarg.dataManager.resumeApocalypse()
        server?.let { StorylineOfMineyarg.dataManager.saveData(it) }
    }
    
    /**
     * Запускает основной цикл апокалипсиса
     */
    private fun startApocalypseLoop() {
        if (isRunning) return
        
        isRunning = true
        
        // Основной цикл проверки событий (каждые 5 секунд)
        scheduler.scheduleAtFixedRate({
            try {
                if (!StorylineOfMineyarg.dataManager.isApocalypsePaused) {
                    checkApocalypseEvents()
                }
            } catch (e: Exception) {
                StorylineOfMineyarg.LOGGER.error("Ошибка в цикле апокалипсиса: ${e.message}")
            }
        }, 0, 5, TimeUnit.SECONDS)
        
        // Цикл распространения снега (каждые 10 секунд)
        scheduler.scheduleAtFixedRate({
            try {
                if (!StorylineOfMineyarg.dataManager.isApocalypsePaused && 
                    StorylineOfMineyarg.dataManager.snowfallTriggered) {
                    expandSnowfall()
                }
            } catch (e: Exception) {
                StorylineOfMineyarg.LOGGER.error("Ошибка в распространении снега: ${e.message}")
            }
        }, 10, 10, TimeUnit.SECONDS)
        
        StorylineOfMineyarg.LOGGER.info("Цикл апокалипсиса запущен")
    }
    
    /**
     * Останавливает цикл апокалипсиса
     */
    private fun stopApocalypseLoop() {
        isRunning = false
        StorylineOfMineyarg.LOGGER.info("Цикл апокалипсиса остановлен")
    }
    
    /**
     * Проверяет и запускает события апокалипсиса
     */
    private fun checkApocalypseEvents() {
        val dataManager = StorylineOfMineyarg.dataManager
        val config = StorylineOfMineyarg.config
        
        // Проверяем увеличение спавна мобов
        if (!dataManager.mobSpawnIncreaseTriggered && 
            dataManager.shouldTriggerEvent(config.daysUntilMobSpawnIncrease, config.mobSpawnCoefficient)) {
            
            triggerMobSpawnIncrease()
            dataManager.mobSpawnIncreaseTriggered = true
            server?.let { dataManager.saveData(it) }
        }
        
        // Проверяем начало снегопада
        if (!dataManager.snowfallTriggered && 
            dataManager.shouldTriggerEvent(config.daysUntilSnowfall, config.snowfallSpeedCoefficient)) {
            
            triggerSnowfall()
            dataManager.snowfallTriggered = true
            server?.let { dataManager.saveData(it) }
        }
        
        // Проверяем эволюцию мобов
        val evolutionThreshold = config.daysUntilMobEvolution + (dataManager.mobEvolutionLevel * 3)
        if (dataManager.shouldTriggerEvent(evolutionThreshold, config.mobEvolutionSpeedCoefficient)) {
            triggerMobEvolution()
            dataManager.mobEvolutionLevel++
            server?.let { dataManager.saveData(it) }
        }
    }
    
    /**
     * Запускает увеличение спавна агрессивных мобов
     */
    private fun triggerMobSpawnIncrease() {
        server?.let { server ->
            val message = "§c§l[АПОКАЛИПСИС] §r§cСпавн агрессивных мобов увеличен!"
            
            server.playerList.players.forEach { player ->
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal(message))
            }
            
            StorylineOfMineyarg.logAndMessage("Увеличен спавн агрессивных мобов")
        }
    }
    
    /**
     * Запускает снегопад
     */
    private fun triggerSnowfall() {
        server?.let { server ->
            val message = "§f§l[АПОКАЛИПСИС] §r§fНачался вечный снегопад! Холод распространяется по миру..."
            
            server.playerList.players.forEach { player ->
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal(message))
            }
            
            StorylineOfMineyarg.logAndMessage("Начался снегопад")
        }
    }
    
    /**
     * Запускает эволюцию мобов
     */
    private fun triggerMobEvolution() {
        val evolutionLevel = StorylineOfMineyarg.dataManager.mobEvolutionLevel
        
        server?.let { server ->
            val message = when (evolutionLevel) {
                0 -> "§c§l[ЭВОЛЮЦИЯ] §r§cМобы больше не горят на солнце!"
                1 -> "§c§l[ЭВОЛЮЦИЯ] §r§cМобы получили улучшенное снаряжение!"
                else -> "§c§l[ЭВОЛЮЦИЯ] §r§cМобы стали сильнее, быстрее и опаснее!"
            }
            
            server.playerList.players.forEach { player ->
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal(message))
            }
            
            StorylineOfMineyarg.logAndMessage("Эволюция мобов: уровень $evolutionLevel")
        }
    }
    
    /**
     * Расширяет область снегопада
     */
    private fun expandSnowfall() {
        val dataManager = StorylineOfMineyarg.dataManager
        val snowCenter = dataManager.snowCenter ?: return
        val server = this.server ?: return
        
        // Увеличиваем радиус снега
        val speedCoeff = StorylineOfMineyarg.config.snowfallSpeedCoefficient
        val radiusIncrease = if (dataManager.isTestMode) {
            (speedCoeff / 10).coerceAtLeast(1) // В тестовом режиме быстрее
        } else {
            (speedCoeff / 50).coerceAtLeast(1)
        }
        
        val oldRadius = dataManager.currentSnowRadius
        dataManager.currentSnowRadius += radiusIncrease
        
        // Применяем снег равномерно по окружности
        server.allLevels.forEach { level ->
            if (level.dimension() == Level.OVERWORLD) {
                applySnowCircle(level, snowCenter, oldRadius, dataManager.currentSnowRadius)
            }
        }
        
        // Сохраняем прогресс
        server.let { dataManager.saveData(it) }
        
        if (StorylineOfMineyarg.config.enableProgressLogging) {
            StorylineOfMineyarg.LOGGER.info("Снегопад расширился до радиуса ${dataManager.currentSnowRadius}")
        }
    }
    
    /**
     * Применяет снег равномерно по окружности между двумя радиусами
     */
    private fun applySnowCircle(level: ServerLevel, center: BlockPos, innerRadius: Int, outerRadius: Int) {
        val chunkRadius = (outerRadius / 16) + 2
        val centerChunkX = center.x shr 4
        val centerChunkZ = center.z shr 4
        
        // Обрабатываем чанки в области
        for (chunkX in (centerChunkX - chunkRadius)..(centerChunkX + chunkRadius)) {
            for (chunkZ in (centerChunkZ - chunkRadius)..(centerChunkZ + chunkRadius)) {
                val chunk = level.getChunk(chunkX, chunkZ)
                
                // Применяем снег и биом к чанку
                applySnowToChunk(level, chunk, center, innerRadius, outerRadius)
            }
        }
    }
    
    /**
     * Применяет снег и снежный биом к чанку
     */
    private fun applySnowToChunk(level: ServerLevel, chunk: ChunkAccess, center: BlockPos, innerRadius: Int, outerRadius: Int) {
        val chunkPos = chunk.pos
        val startX = chunkPos.minBlockX
        val startZ = chunkPos.minBlockZ
        
        // Получаем снежный биом
        val snowyBiome = level.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.BIOME)
            .getHolderOrThrow(Biomes.SNOWY_PLAINS)
        
        for (x in 0..15) {
            for (z in 0..15) {
                val worldX = startX + x
                val worldZ = startZ + z
                
                val distance = sqrt(((worldX - center.x) * (worldX - center.x) + (worldZ - center.z) * (worldZ - center.z)).toDouble())
                
                // Проверяем, находится ли блок в кольце между радиусами
                if (distance > innerRadius && distance <= outerRadius) {
                    val y = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING, worldX, worldZ)
                    val pos = BlockPos(worldX, y, worldZ)
                    
                    // Устанавливаем снежный биом
                    if (chunk is LevelChunk) {
                        for (ySection in level.minSection until level.maxSection) {
                            try {
                                // Используем reflection для доступа к setBiome
                                val setBiomeMethod = chunk.javaClass.getDeclaredMethod("setBiome", Int::class.java, Int::class.java, Int::class.java, Holder::class.java)
                                setBiomeMethod.isAccessible = true
                                setBiomeMethod.invoke(chunk, x, ySection, z, snowyBiome)
                            } catch (e: Exception) {
                                // Игнорируем ошибки установки биома
                            }
                        }
                    }
                    
                    // Размещаем снег
                    if (level.getBlockState(pos).isAir && level.getBlockState(pos.below()).isSolidRender(level, pos.below())) {
                        level.setBlock(pos, Blocks.SNOW.defaultBlockState(), 3)
                    }
                    
                    // Замораживаем воду
                    val belowPos = pos.below()
                    val belowState = level.getBlockState(belowPos)
                    if (belowState.block == Blocks.WATER) {
                        level.setBlock(belowPos, Blocks.ICE.defaultBlockState(), 3)
                    }
                    
                    // Добавляем снежные слои на траву
                    if (belowState.block == Blocks.GRASS_BLOCK) {
                        level.setBlock(belowPos, Blocks.SNOW_BLOCK.defaultBlockState(), 3)
                    }
                }
            }
        }
        
        // Помечаем чанк как измененный
        if (chunk is LevelChunk) {
            chunk.setUnsaved(true)
        }
    }
    
    /**
     * Применяет эволюцию к мобу
     */
    fun applyMobEvolution(mob: Monster) {
        val evolutionLevel = StorylineOfMineyarg.dataManager.mobEvolutionLevel
        
        // Все эволюционировавшие мобы не горят на солнце
        if (evolutionLevel >= 0) {
            mob.addEffect(MobEffectInstance(MobEffects.FIRE_RESISTANCE, Int.MAX_VALUE, 0, false, false))
        }
        
        when (evolutionLevel) {
            0 -> {
                // Базовая эволюция - только огнестойкость (уже применена выше)
            }
            1 -> {
                // Улучшенное снаряжение
                when (mob) {
                    is Zombie -> {
                        // Удочка в руке
                        if (Random().nextFloat() < 0.3f) {
                            mob.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, ItemStack(Items.FISHING_ROD))
                        }
                        // Эндер-жемчуг в оффхенде
                        if (Random().nextFloat() < 0.2f) {
                            mob.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND, ItemStack(Items.ENDER_PEARL))
                        }
                        // Улучшенная броня
                        equipBetterArmor(mob)
                    }
                    is Skeleton -> {
                        // Улучшенный лук
                        val bow = ItemStack(Items.BOW)
                        EnchantmentHelper.setEnchantments(mapOf(Enchantments.POWER_ARROWS to 2), bow)
                        mob.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, bow)
                        equipBetterArmor(mob)
                    }
                }
            }
            else -> {
                // Баффы на скорость, прыжки и урон
                val buffLevel = (evolutionLevel - 2).coerceAtLeast(0)
                mob.addEffect(MobEffectInstance(MobEffects.MOVEMENT_SPEED, Int.MAX_VALUE, buffLevel, false, false))
                mob.addEffect(MobEffectInstance(MobEffects.JUMP, Int.MAX_VALUE, buffLevel, false, false))
                mob.addEffect(MobEffectInstance(MobEffects.DAMAGE_BOOST, Int.MAX_VALUE, buffLevel, false, false))
                
                // Продвинутое снаряжение для высоких уровней
                if (evolutionLevel >= 3) {
                    equipBetterArmor(mob)
                }
            }
        }
    }
    
    /**
     * Экипирует моба лучшей броней
     */
    private fun equipBetterArmor(mob: Monster) {
        val armorItems = listOf(
            ItemStack(Items.IRON_HELMET),
            ItemStack(Items.IRON_CHESTPLATE),
            ItemStack(Items.IRON_LEGGINGS),
            ItemStack(Items.IRON_BOOTS)
        )
        
        armorItems.forEachIndexed { index, armor ->
            if (Random().nextFloat() < 0.5f) {
                mob.setItemSlot(net.minecraft.world.entity.EquipmentSlot.values()[index + 2], armor)
            }
        }
    }
}