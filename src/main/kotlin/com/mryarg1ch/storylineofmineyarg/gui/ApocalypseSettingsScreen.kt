package com.mryarg1ch.storylineofmineyarg.gui

import com.mryarg1ch.storylineofmineyarg.StorylineOfMineyarg
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.MenuProvider
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.ChestMenu
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.SimpleContainer
import net.minecraft.world.item.enchantment.EnchantmentHelper
import net.minecraft.world.item.enchantment.Enchantments

/**
 * GUI экран для настроек апокалипсиса
 */
object ApocalypseSettingsScreen {
    
    /**
     * Создает контейнер для GUI настроек
     */
    fun createContainer(player: ServerPlayer): MenuProvider {
        return object : MenuProvider {
            override fun createMenu(containerId: Int, inventory: Inventory, player: Player): AbstractContainerMenu {
                val container = SimpleContainer(54) // 6 рядов по 9 слотов
                setupSettingsItems(container, player as ServerPlayer)
                return ChestMenu(MenuType.GENERIC_9x6, containerId, inventory, container, 6)
            }
            
            override fun getDisplayName(): Component {
                return Component.literal("§c§lНастройки Апокалипсиса")
            }
        }
    }
    
    /**
     * Настраивает предметы в GUI
     */
    private fun setupSettingsItems(container: SimpleContainer, player: ServerPlayer) {
        val config = StorylineOfMineyarg.config
        
        // Заголовок
        container.setItem(4, createItem(
            Items.NETHER_STAR,
            "§c§lНастройки Апокалипсиса",
            listOf(
                "§7Настройте параметры зомби-апокалипсиса",
                "§7Все изменения сохраняются автоматически"
            )
        ))
        
        // Настройки времени
        container.setItem(10, createItem(
            Items.CLOCK,
            "§e§lДни до увеличения спавна мобов",
            listOf(
                "§7Текущее значение: §f${config.daysUntilMobSpawnIncrease} дней",
                "§7Диапазон: 1-100 дней",
                "§a§lЛКМ: §a+1 день",
                "§c§lПКМ: §c-1 день"
            )
        ))
        
        container.setItem(11, createItem(
            Items.SNOWBALL,
            "§f§lДни до начала снегопада",
            listOf(
                "§7Текущее значение: §f${config.daysUntilSnowfall} дней",
                "§7Диапазон: 2-100 дней",
                "§a§lЛКМ: §a+1 день",
                "§c§lПКМ: §c-1 день"
            )
        ))
        
        container.setItem(12, createItem(
            Items.ZOMBIE_HEAD,
            "§c§lДни до эволюции мобов",
            listOf(
                "§7Текущее значение: §f${config.daysUntilMobEvolution} дней",
                "§7Диапазон: 1-100 дней",
                "§7Первая эволюция - мобы не горят на солнце",
                "§7Вторая - улучшенное снаряжение",
                "§7Далее - баффы на скорость и урон",
                "§a§lЛКМ: §a+1 день",
                "§c§lПКМ: §c-1 день"
            )
        ))
        
        // Коэффициенты
        container.setItem(19, createItem(
            Items.SPIDER_EYE,
            "§c§lКоэффициент спавна мобов",
            listOf(
                "§7Текущее значение: §f${config.mobSpawnCoefficient}%",
                "§7Диапазон: 1-100%",
                "§7Влияет на скорость увеличения спавна",
                "§a§lЛКМ: §a+5%",
                "§c§lПКМ: §c-5%"
            )
        ))
        
        container.setItem(20, createItem(
            Items.FERMENTED_SPIDER_EYE,
            "§c§lКоэффициент эволюции мобов",
            listOf(
                "§7Текущее значение: §f${config.mobEvolutionSpeedCoefficient}%",
                "§7Диапазон: 1-100%",
                "§7Влияет на скорость эволюции мобов",
                "§a§lЛКМ: §a+5%",
                "§c§lПКМ: §c-5%"
            )
        ))
        
        container.setItem(21, createItem(
            Items.ICE,
            "§f§lКоэффициент скорости снегопада",
            listOf(
                "§7Текущее значение: §f${config.snowfallSpeedCoefficient}%",
                "§7Диапазон: 1-100%",
                "§7Влияет на скорость распространения снега",
                "§a§lЛКМ: §a+5%",
                "§c§lПКМ: §c-5%"
            )
        ))
        
        // Настройки логирования
        container.setItem(28, createItem(
            if (config.enableProgressLogging) Items.WRITABLE_BOOK else Items.BOOK,
            "§e§lЛогирование прогресса",
            listOf(
                "§7Состояние: ${if (config.enableProgressLogging) "§aВключено" else "§cВыключено"}",
                "§7Сохраняет логи прогресса апокалипсиса",
                "§7На сервере - в папку config/storylineofmineyarg",
                "§7В одиночке - на текущий сеанс",
                "§e§lКлик: §eПереключить"
            )
        ))
        
        // Информация о текущем состоянии
        container.setItem(31, createItem(
            Items.COMPASS,
            "§b§lТекущее состояние",
            listOf(
                "§7Апокалипсис: ${if (StorylineOfMineyarg.dataManager.isApocalypseActive) "§aАктивен" else "§cНеактивен"}",
                "§7Пауза: ${if (StorylineOfMineyarg.dataManager.isApocalypsePaused) "§eДа" else "§aНет"}",
                "§7Тестовый режим: ${if (StorylineOfMineyarg.dataManager.isTestMode) "§eДа" else "§aНет"}",
                "§7Уровень эволюции: §f${StorylineOfMineyarg.dataManager.mobEvolutionLevel}",
                "§7Радиус снега: §f${StorylineOfMineyarg.dataManager.currentSnowRadius}",
                "§7Координаты снега: ${config.snowSpawnPosition?.let { "§f${it.x}, ${it.y}, ${it.z}" } ?: "§cНе установлены"}"
            )
        ))
        
        // Кнопки управления
        container.setItem(37, createItem(
            Items.EMERALD,
            "§a§lСохранить настройки",
            listOf(
                "§7Сохраняет все изменения в файл конфигурации",
                "§e§lКлик: §eСохранить"
            )
        ))
        
        container.setItem(38, createItem(
            Items.REDSTONE,
            "§c§lСбросить к умолчанию",
            listOf(
                "§7Сбрасывает все настройки к значениям по умолчанию",
                "§c§lВнимание: §cВсе изменения будут потеряны!",
                "§e§lКлик: §eСбросить"
            )
        ))
        
        container.setItem(40, createItem(
            Items.BARRIER,
            "§c§lЗакрыть меню",
            listOf(
                "§7Закрывает меню настроек",
                "§e§lКлик: §eЗакрыть"
            )
        ))
        
        // Заполняем пустые слоты стеклом
        for (i in 0 until 54) {
            if (container.getItem(i).isEmpty) {
                container.setItem(i, createItem(Items.GRAY_STAINED_GLASS_PANE, " ", emptyList()))
            }
        }
    }
    
    /**
     * Создает предмет с названием и описанием
     */
    private fun createItem(item: net.minecraft.world.item.Item, name: String, lore: List<String>): ItemStack {
        val stack = ItemStack(item)
        val tag = stack.orCreateTag
        
        // Устанавливаем название
        tag.putString("display", "{\"Name\":\"\\\"$name\\\"\"}")
        
        // Устанавливаем описание
        if (lore.isNotEmpty()) {
            val loreArray = lore.joinToString("\",\"", "[\"", "\"]") { it }
            tag.putString("display", "{\"Name\":\"\\\"$name\\\"\",\"Lore\":$loreArray}")
        }
        
        // Добавляем свечение для важных предметов
        if (name.contains("§l")) {
            EnchantmentHelper.setEnchantments(mapOf(Enchantments.UNBREAKING to 1), stack)
            tag.putBoolean("HideFlags", true)
        }
        
        return stack
    }
}