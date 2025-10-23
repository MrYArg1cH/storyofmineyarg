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
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

/**
 * Основной менеджер апокалипсиса
 */
class ApocalypseManager {
    
    private var server: MinecraftServer? = null
    private val scheduler: ScheduledExecutorService = Executors.newScheduledThreadPool(2)
    private var isRunning = false
    
    /**
     * Инициализация при запуске сервера
     */
    fun onServerStart(server: MinecraftServer) {
        this.server = server
        
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
        
        dataManager.currentSnowRadius += radiusIncrease
        
        // Применяем снег в области
        server.allLevels.forEach { level ->
            if (level.dimension() == net.minecraft.world.level.Level.OVERWORLD) {
                applySnowInRadius(level, snowCenter, dataManager.currentSnowRadius)
            }
        }
        
        // Сохраняем прогресс
        server.let { dataManager.saveData(it) }
        
        if (StorylineOfMineyarg.config.enableProgressLogging) {
            StorylineOfMineyarg.LOGGER.info("Снегопад расширился до радиуса ${dataManager.currentSnowRadius}")
        }
    }
    
    /**
     * Применяет снег в указанном радиусе
     */
    private fun applySnowInRadius(level: ServerLevel, center: BlockPos, radius: Int) {
        val random = Random()
        val blocksToProcess = (radius * radius * 0.1).toInt().coerceAtLeast(10)
        
        repeat(blocksToProcess) {
            val x = center.x + random.nextInt(radius * 2) - radius
            val z = center.z + random.nextInt(radius * 2) - radius
            
            // Проверяем, находится ли блок в радиусе
            val distance = kotlin.math.sqrt(((x - center.x) * (x - center.x) + (z - center.z) * (z - center.z)).toDouble())
            if (distance <= radius) {
                val pos = BlockPos(x, level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING, x, z), z)
                
                // Размещаем снег
                if (level.getBlockState(pos).isAir && level.getBlockState(pos.below()).isSolidRender(level, pos.below())) {
                    level.setBlock(pos, Blocks.SNOW.defaultBlockState(), 3)
                }
                
                // Замораживаем воду
                if (level.getBlockState(pos.below()).block == Blocks.WATER) {
                    level.setBlock(pos.below(), Blocks.ICE.defaultBlockState(), 3)
                }
            }
        }
    }
    
    /**
     * Применяет эволюцию к мобу
     */
    fun applyMobEvolution(mob: Monster) {
        val evolutionLevel = StorylineOfMineyarg.dataManager.mobEvolutionLevel
        
        when (evolutionLevel) {
            0 -> {
                // Мобы не горят на солнце
                mob.addEffect(MobEffectInstance(MobEffects.FIRE_RESISTANCE, Int.MAX_VALUE, 0, false, false))
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
                mob.addEffect(MobEffectInstance(MobEffects.MOVEMENT_SPEED, Int.MAX_VALUE, evolutionLevel - 2, false, false))
                mob.addEffect(MobEffectInstance(MobEffects.JUMP, Int.MAX_VALUE, evolutionLevel - 2, false, false))
                mob.addEffect(MobEffectInstance(MobEffects.DAMAGE_BOOST, Int.MAX_VALUE, evolutionLevel - 2, false, false))
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