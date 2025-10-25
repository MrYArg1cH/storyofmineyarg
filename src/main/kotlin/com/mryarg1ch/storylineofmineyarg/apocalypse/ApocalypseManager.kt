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
import net.minecraft.world.level.Level
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.network.chat.Component
import java.io.File
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import kotlin.math.*

/**
 * Основной менеджер апокалипсиса (упрощенная версия без оптимизаций)
 */
class ApocalypseManager {
    
    private var server: MinecraftServer? = null
    private val scheduler = Executors.newScheduledThreadPool(2)
    private var isRunning = false
    private val weatherManager = WeatherManager()
    private var lastWeatherChangeTime = 0L // Время последней смены погоды
    private var lastSnowExpansionTime = 0L // Время последнего расширения снега и биома
    
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
        server?.let { 
            StorylineOfMineyarg.dataManager.saveData(it)
            
            // Воспроизводим звук рычания эндердракона для всех игроков
            it.allLevels.forEach { level ->
                level.players().forEach { player ->
                    player.playNotifySound(SoundEvents.ENDER_DRAGON_GROWL, SoundSource.MASTER, 1.0f, 1.0f)
                }
            }
        }
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
     * Запускает основной цикл апокалипсиса (упрощенная версия)
     */
    private fun startApocalypseLoop() {
        if (isRunning) return
        
        isRunning = true
        
        // Основной цикл проверки событий (каждые 30 секунд)
        scheduler.scheduleAtFixedRate({
            try {
                if (isRunning && !StorylineOfMineyarg.dataManager.isApocalypsePaused) {
                    checkApocalypseEvents()
                    server?.let { StorylineOfMineyarg.dataManager.saveData(it) }
                }
            } catch (e: Exception) {
                StorylineOfMineyarg.LOGGER.error("Ошибка в цикле апокалипсиса: ${e.message}")
            }
        }, 30, 30, TimeUnit.SECONDS)
        
        // Отдельный цикл для расширения снега (каждые 10 секунд проверяем)
        scheduler.scheduleAtFixedRate({
            try {
                if (isRunning && !StorylineOfMineyarg.dataManager.isApocalypsePaused) {
                    if (StorylineOfMineyarg.dataManager.snowfallTriggered) {
                        checkSnowExpansion()
                    }
                }
            } catch (e: Exception) {
                StorylineOfMineyarg.LOGGER.error("Ошибка в цикле расширения снега: ${e.message}")
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
        
        // Проверяем увеличение спавна зомби
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
        
        // Проверяем эволюцию зомби
        val evolutionThreshold = config.daysUntilMobEvolution + (dataManager.mobEvolutionLevel * 3)
        if (dataManager.shouldTriggerEvent(evolutionThreshold, config.mobEvolutionSpeedCoefficient)) {
            triggerMobEvolution()
            dataManager.mobEvolutionLevel++
            server?.let { dataManager.saveData(it) }
        }
    }
    
    /**
     * Запускает увеличение спавна зомби
     */
    private fun triggerMobSpawnIncrease() {
        server?.let { server ->
            val message = "§c§l[АПОКАЛИПСИС] §r§cСпавн зомби увеличен!"
            
            server.playerList.players.forEach { player ->
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal(message))
            }
            
            StorylineOfMineyarg.logAndMessage("Увеличен спавн зомби")
        }
    }
    
    /**
     * Запускает снегопад
     */
    private fun triggerSnowfall() {
        server?.let { server ->
            val dataManager = StorylineOfMineyarg.dataManager
            
            // Устанавливаем центр снегопада, если он не установлен
            if (dataManager.snowCenter == null) {
                // Используем спавн мира как центр
                val overworld = server.getLevel(Level.OVERWORLD)
                if (overworld != null) {
                    val spawnPos = overworld.sharedSpawnPos
                    dataManager.snowCenter = spawnPos
                    dataManager.currentSnowRadius = 1 // Начальный радиус - 1 блок
                    dataManager.currentBiomeRadius = 1 // Начальный радиус биома - 1 блок
                    
                    // Применяем снег к начальному кругу (радиус 1)
                    applySnowToNewRing(overworld, spawnPos, 0, 1)
                    
                    StorylineOfMineyarg.LOGGER.info("Установлен центр снегопада: ${spawnPos.x}, ${spawnPos.z}")
                }
            }
            
            val message = "§f§l[АПОКАЛИПСИС] §r§fНачался вечный снегопад! Холод распространяется по миру..."
            
            server.playerList.players.forEach { player ->
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal(message))
            }
            
            StorylineOfMineyarg.logAndMessage("Начался снегопад")
        }
    }
    
    /**
     * Запускает эволюцию зомби
     */
    private fun triggerMobEvolution() {
        val evolutionLevel = StorylineOfMineyarg.dataManager.mobEvolutionLevel
        
        // Плавное увеличение спавна зомби при каждой эволюции (1-3 зомби)
        val spawnIncrease = Random().nextInt(3) + 1 // 1-3 зомби
        val config = StorylineOfMineyarg.config
        val newSpawnCoeff = (config.mobSpawnCoefficient + spawnIncrease).coerceAtMost(100)
        config.mobSpawnCoefficient = newSpawnCoeff
        // Сохраняем конфигурацию (требует доступа к configDir)
        try {
            val configDir = File("config")
            config.saveConfig(configDir)
        } catch (e: Exception) {
            System.err.println("[${StorylineOfMineyarg.MOD_NAME}] WARNING: Не удалось сохранить конфигурацию: ${e.message}")
        }
        
        server?.let { server ->
            val message = when (evolutionLevel) {
                0 -> "§c§l[ЭВОЛЮЦИЯ] §r§cЗомби больше не горят на солнце и начинают носить кожаную броню! Спавн увеличен на $spawnIncrease зомби."
                1 -> "§c§l[ЭВОЛЮЦИЯ] §r§cЗомби стали быстрее и получили лучшую броню с оружием! Спавн увеличен на $spawnIncrease зомби."
                2 -> "§c§l[ЭВОЛЮЦИЯ] §r§cЗомби прыгают выше, носят железную броню и владеют железными мечами! Спавн увеличен на $spawnIncrease зомби."
                3 -> "§c§l[ЭВОЛЮЦИЯ] §r§cЗомби стали сильнее в бою, их оружие может быть зачаровано! Спавн увеличен на $spawnIncrease зомби."
                4 -> "§c§l[ЭВОЛЮЦИЯ] §r§cЗомби достигли высокого уровня и могут носить алмазные мечи! Спавн увеличен на $spawnIncrease зомби."
                else -> "§c§l[ЭВОЛЮЦИЯ] §r§cЗомби достигли максимальной эволюции с алмазной броней и зачарованными мечами! Спавн увеличен на $spawnIncrease зомби."
            }
            
            server.playerList.players.forEach { player ->
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal(message))
            }
            
            StorylineOfMineyarg.logAndMessage("Эволюция зомби: уровень $evolutionLevel, спавн увеличен на $spawnIncrease (новый коэффициент: $newSpawnCoeff%)")
        }
    }
    
    /**
     * Проверяет, нужно ли расширить снегопад и биом одновременно
     */
    private fun checkSnowExpansion() {
        val currentTime = System.currentTimeMillis()
        val dataManager = StorylineOfMineyarg.dataManager
        
        // Определяем интервал расширения в зависимости от режима
        val expansionInterval = if (dataManager.isTestMode) {
            1000L // 1 секунда в тестовом режиме
        } else {
            60000L // 1 минута в обычном режиме
        }
        
        // Проверяем расширение снега и биома одновременно
        if (currentTime - lastSnowExpansionTime >= expansionInterval) {
            expandSnowfallAndBiome()
            lastSnowExpansionTime = currentTime
        }
    }
    
    /**
     * Расширяет область снегопада и биом одновременно для синхронного распространения
     */
    private fun expandSnowfallAndBiome() {
        val dataManager = StorylineOfMineyarg.dataManager
        val snowCenter = dataManager.snowCenter ?: return
        val server = this.server ?: return
        
        // Увеличиваем радиусы снега и биома на 1 блок одновременно
        val oldSnowRadius = dataManager.currentSnowRadius
        val oldBiomeRadius = dataManager.currentBiomeRadius
        dataManager.currentSnowRadius += 1
        dataManager.currentBiomeRadius += 1
        
        // Применяем изменения только к overworld
        val overworld = server.getLevel(Level.OVERWORLD)
        if (overworld != null) {
            // Сначала расширяем биом к новому кольцу (чтобы он был готов для снега)
            expandBiomeArea(overworld, snowCenter, oldBiomeRadius, dataManager.currentBiomeRadius)
            
            // Затем применяем снег к новому кольцу
            applySnowToNewRing(overworld, snowCenter, oldSnowRadius, dataManager.currentSnowRadius)
            
            // Управляем погодой
            manageWeatherForSnowfall(overworld)
        }
        
        if (StorylineOfMineyarg.config.enableProgressLogging) {
            StorylineOfMineyarg.LOGGER.info("Снег и биом расширились синхронно до радиуса: ${dataManager.currentSnowRadius}")
        }
    }
    
    /**
     * Расширяет биом зимняя тайга (кольцевая область на всю высоту блока)
     */
    private fun expandBiomeArea(overworld: ServerLevel, snowCenter: BlockPos, innerRadius: Int, outerRadius: Int) {
        // Применяем биом только к новому кольцу между старым и новым радиусом
        applyBiomeToNewRing(overworld, snowCenter, innerRadius, outerRadius)
    }
    
    /**
     * Применяет снег только к новому кольцу между старым и новым радиусом
     */
    private fun applySnowToNewRing(level: ServerLevel, center: BlockPos, innerRadius: Int, outerRadius: Int) {
        // Собираем все координаты в кольце
        val ringCoordinates = mutableListOf<Pair<Int, Int>>()
        
        for (x in (center.x - outerRadius)..(center.x + outerRadius)) {
            for (z in (center.z - outerRadius)..(center.z + outerRadius)) {
                val distance = sqrt(((x - center.x) * (x - center.x) + (z - center.z) * (z - center.z)).toDouble()).toInt()
                
                // Проверяем, находится ли блок в кольце между радиусами
                if (distance > innerRadius && distance <= outerRadius) {
                    ringCoordinates.add(Pair(x, z))
                }
            }
        }
        
        // Применяем снежные эффекты к поверхности только для координат в кольце (круговая область)
        ringCoordinates.forEach { (x, z) ->
            applySnowEffectsToSurface(level, x, z)
        }
    }
    
    /**
     * Применяет биом только к новому кольцу между старым и новым радиусом на всю высоту блока
     */
    private fun applyBiomeToNewRing(level: ServerLevel, center: BlockPos, innerRadius: Int, outerRadius: Int) {
        // Собираем все координаты в кольце
        val ringCoordinates = mutableListOf<Pair<Int, Int>>()
        
        for (x in (center.x - outerRadius)..(center.x + outerRadius)) {
            for (z in (center.z - outerRadius)..(center.z + outerRadius)) {
                val distance = sqrt(((x - center.x) * (x - center.x) + (z - center.z) * (z - center.z)).toDouble()).toInt()
                
                // Проверяем, находится ли блок в кольце между радиусами
                if (distance > innerRadius && distance <= outerRadius) {
                    ringCoordinates.add(Pair(x, z))
                }
            }
        }
        
        // Применяем биом к каждой координате в кольце на всю высоту
        ringCoordinates.forEach { (x, z) ->
            applyBiomeToColumn(level, x, z)
        }
    }

    /**
     * Применяет биом к колонке блоков на всю высоту мира
     */
    private fun applyBiomeToColumn(level: ServerLevel, x: Int, z: Int) {
        // Получаем минимальную и максимальную высоту мира
        val minY = level.minBuildHeight
        val maxY = level.maxBuildHeight - 1
        
        // Создаем тихий источник команд, чтобы подавить вывод сообщений
        val silentCommandSource = level.server.createCommandSourceStack()
            .withPosition(net.minecraft.world.phys.Vec3(x.toDouble(), 0.0, z.toDouble()))
            .withSuppressedOutput()
        
        // Применяем биом к одной колонке блоков
        level.server.commands.performPrefixedCommand(
            silentCommandSource,
            "fillbiome $x $minY $z $x $maxY $z minecraft:snowy_taiga"
        )
    }

    /**
     * Применяет снежные эффекты к поверхности в указанных координатах
     */
    private fun applySnowEffectsToSurface(level: ServerLevel, x: Int, z: Int) {
        val surfaceY = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING, x, z)
        val pos = BlockPos(x, surfaceY, z)
        
        // Размещаем снег на поверхности
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
    
    /**
     * Управляет погодой во время снегопада
     */
    private fun manageWeatherForSnowfall(level: ServerLevel) {
        val currentTime = System.currentTimeMillis()
        val weatherChangeInterval = 30000L // 30 секунд между сменами погоды
        
        // Проверяем, прошло ли достаточно времени с последней смены погоды
        if (currentTime - lastWeatherChangeTime < weatherChangeInterval) {
            return
        }
        
        try {
            val commandSource = level.server.createCommandSourceStack()
                .withSuppressedOutput()
                .withMaximumPermission(4)
            
            // Случайно выбираем тип погоды
            val random = Random()
            val weatherType = random.nextFloat()
            
            val command = when {
                weatherType < 0.33f -> "weather clear"
                weatherType < 0.66f -> "weather rain"
                else -> "weather thunder"
            }
            
            level.server.commands.performPrefixedCommand(commandSource, command)
            lastWeatherChangeTime = currentTime // Обновляем время последней смены погоды
            
            if (StorylineOfMineyarg.config.enableProgressLogging) {
                StorylineOfMineyarg.LOGGER.info("Установлена погода: $command")
            }
        } catch (e: Exception) {
            StorylineOfMineyarg.LOGGER.warn("Не удалось установить погоду: ${e.message}")
        }
    }
    
    /**
     * Применяет эволюцию к зомби
     */
    fun applyMobEvolution(mob: Monster) {
        val evolutionLevel = StorylineOfMineyarg.dataManager.mobEvolutionLevel
        
        // Только эволюционировавшие зомби (уровень >= 0) получают улучшения
        if (evolutionLevel < 0 || mob !is Zombie) {
            return // Никаких улучшений для неэволюционировавших мобов или не-зомби
        }
        
        // Все эволюционировавшие зомби не горят на солнце
        mob.addEffect(MobEffectInstance(MobEffects.FIRE_RESISTANCE, Int.MAX_VALUE, 0, false, false))
        
        // Плавное улучшение характеристик с каждым уровнем эволюции
        if (evolutionLevel > 0) {
            val buffLevel = (evolutionLevel - 1).coerceAtLeast(0)
            mob.addEffect(MobEffectInstance(MobEffects.MOVEMENT_SPEED, Int.MAX_VALUE, buffLevel, false, false))
            mob.addEffect(MobEffectInstance(MobEffects.JUMP, Int.MAX_VALUE, buffLevel, false, false))
            mob.addEffect(MobEffectInstance(MobEffects.DAMAGE_BOOST, Int.MAX_VALUE, buffLevel, false, false))
        }
        
        // Плавное улучшение брони и оружия
        equipProgressiveArmor(mob, evolutionLevel)
        equipProgressiveWeapon(mob, evolutionLevel)
    }
    
    /**
     * Плавно улучшает броню зомби в зависимости от уровня эволюции
     */
    private fun equipProgressiveArmor(mob: Monster, evolutionLevel: Int) {
        val random = Random()
        
        // Определяем вероятность получения брони в зависимости от уровня
        val armorChance = when (evolutionLevel) {
            0 -> 0.3f  // 30% шанс на кожаную броню
            1 -> 0.5f  // 50% шанс на кожаную/железную броню
            2 -> 0.7f  // 70% шанс на железную броню
            3 -> 0.8f  // 80% шанс на железную броню
            4 -> 0.9f  // 90% шанс на железную броню + возможность алмазного элемента
            else -> 1.0f // 100% шанс на полную железную броню + алмазный элемент
        }
        
        val armorSlots = listOf(
            net.minecraft.world.entity.EquipmentSlot.HEAD,
            net.minecraft.world.entity.EquipmentSlot.CHEST,
            net.minecraft.world.entity.EquipmentSlot.LEGS,
            net.minecraft.world.entity.EquipmentSlot.FEET
        )
        
        armorSlots.forEach { slot ->
            if (random.nextFloat() < armorChance) {
                val armorItem = when (evolutionLevel) {
                    0 -> getLeatherArmor(slot)
                    1 -> if (random.nextFloat() < 0.6f) getLeatherArmor(slot) else getIronArmor(slot)
                    2 -> if (random.nextFloat() < 0.3f) getLeatherArmor(slot) else getIronArmor(slot)
                    3 -> getIronArmor(slot)
                    4 -> getIronArmor(slot)
                    else -> {
                        // Уровень 5+: железная броня + возможность одного алмазного элемента
                        if (evolutionLevel >= 5 && random.nextFloat() < 0.25f) {
                            getDiamondArmor(slot)
                        } else {
                            getIronArmor(slot)
                        }
                    }
                }
                mob.setItemSlot(slot, armorItem)
            }
        }
    }
    
    /**
     * Плавно улучшает оружие зомби в зависимости от уровня эволюции
     */
    private fun equipProgressiveWeapon(mob: Monster, evolutionLevel: Int) {
        val random = Random()
        
        when (evolutionLevel) {
            0 -> {
                // Базовый уровень - без оружия или деревянный меч
                if (random.nextFloat() < 0.2f) {
                    mob.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, ItemStack(Items.WOODEN_SWORD))
                }
            }
            1 -> {
                // Каменный или железный меч
                if (random.nextFloat() < 0.4f) {
                    val weapon = if (random.nextFloat() < 0.7f) Items.STONE_SWORD else Items.IRON_SWORD
                    mob.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, ItemStack(weapon))
                }
            }
            2 -> {
                // Железный меч
                if (random.nextFloat() < 0.6f) {
                    mob.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, ItemStack(Items.IRON_SWORD))
                }
            }
            3 -> {
                // Железный меч с возможностью зачарования
                if (random.nextFloat() < 0.7f) {
                    val sword = ItemStack(Items.IRON_SWORD)
                    if (random.nextFloat() < 0.3f) {
                        EnchantmentHelper.setEnchantments(mapOf(Enchantments.SHARPNESS to 1), sword)
                    }
                    mob.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, sword)
                }
            }
            4 -> {
                // Железный меч с зачарованием или алмазный меч
                if (random.nextFloat() < 0.8f) {
                    val weapon = if (random.nextFloat() < 0.7f) Items.IRON_SWORD else Items.DIAMOND_SWORD
                    val sword = ItemStack(weapon)
                    if (random.nextFloat() < 0.5f) {
                        val enchantLevel = if (weapon == Items.DIAMOND_SWORD) 2 else 1
                        EnchantmentHelper.setEnchantments(mapOf(Enchantments.SHARPNESS to enchantLevel), sword)
                    }
                    mob.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, sword)
                }
            }
            else -> {
                // Уровень 5+: алмазный меч с зачарованием
                if (random.nextFloat() < 0.9f) {
                    val sword = ItemStack(Items.DIAMOND_SWORD)
                    val enchantLevel = minOf(evolutionLevel - 3, 3) // Максимум Sharpness III
                    EnchantmentHelper.setEnchantments(mapOf(Enchantments.SHARPNESS to enchantLevel), sword)
                    mob.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, sword)
                }
            }
        }
        
        // Эндер-жемчуг в оффхенде для высоких уровней
        if (evolutionLevel >= 2 && random.nextFloat() < 0.1f + (evolutionLevel * 0.05f)) {
            mob.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND, ItemStack(Items.ENDER_PEARL))
        }
    }
    
    /**
     * Возвращает кожаную броню для указанного слота
     */
    private fun getLeatherArmor(slot: net.minecraft.world.entity.EquipmentSlot): ItemStack {
        return when (slot) {
            net.minecraft.world.entity.EquipmentSlot.HEAD -> ItemStack(Items.LEATHER_HELMET)
            net.minecraft.world.entity.EquipmentSlot.CHEST -> ItemStack(Items.LEATHER_CHESTPLATE)
            net.minecraft.world.entity.EquipmentSlot.LEGS -> ItemStack(Items.LEATHER_LEGGINGS)
            net.minecraft.world.entity.EquipmentSlot.FEET -> ItemStack(Items.LEATHER_BOOTS)
            else -> ItemStack.EMPTY
        }
    }
    
    /**
     * Возвращает железную броню для указанного слота
     */
    private fun getIronArmor(slot: net.minecraft.world.entity.EquipmentSlot): ItemStack {
        return when (slot) {
            net.minecraft.world.entity.EquipmentSlot.HEAD -> ItemStack(Items.IRON_HELMET)
            net.minecraft.world.entity.EquipmentSlot.CHEST -> ItemStack(Items.IRON_CHESTPLATE)
            net.minecraft.world.entity.EquipmentSlot.LEGS -> ItemStack(Items.IRON_LEGGINGS)
            net.minecraft.world.entity.EquipmentSlot.FEET -> ItemStack(Items.IRON_BOOTS)
            else -> ItemStack.EMPTY
        }
    }
    
    /**
     * Возвращает алмазную броню для указанного слота
     */
    private fun getDiamondArmor(slot: net.minecraft.world.entity.EquipmentSlot): ItemStack {
        return when (slot) {
            net.minecraft.world.entity.EquipmentSlot.HEAD -> ItemStack(Items.DIAMOND_HELMET)
            net.minecraft.world.entity.EquipmentSlot.CHEST -> ItemStack(Items.DIAMOND_CHESTPLATE)
            net.minecraft.world.entity.EquipmentSlot.LEGS -> ItemStack(Items.DIAMOND_LEGGINGS)
            net.minecraft.world.entity.EquipmentSlot.FEET -> ItemStack(Items.DIAMOND_BOOTS)
            else -> ItemStack.EMPTY
        }
    }
}