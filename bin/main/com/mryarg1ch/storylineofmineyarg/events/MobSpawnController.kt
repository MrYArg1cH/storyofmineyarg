package com.mryarg1ch.storylineofmineyarg.events

import com.mryarg1ch.storylineofmineyarg.StorylineOfMineyarg
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.monster.*
import net.minecraft.world.level.Level
import net.minecraftforge.event.entity.EntityJoinLevelEvent
import net.minecraftforge.eventbus.api.Event
import net.minecraftforge.eventbus.api.SubscribeEvent

/**
 * Контроллер спавна мобов - запрещает спавн нежелательных агрессивных мобов
 */
object MobSpawnController {
    
    // Список разрешенных агрессивных мобов
    private val allowedHostileMobs = setOf(
        EntityType.ZOMBIE,           // Зомби
        EntityType.BLAZE,            // Ифриты
        EntityType.ENDERMAN,         // Эндермены
        EntityType.GHAST,            // Гасты
        EntityType.WITHER,           // Визер
        EntityType.WITHER_SKELETON,  // Визер скелет
        EntityType.ENDER_DRAGON,     // Эндердракон
        EntityType.MAGMA_CUBE,       // Лава слайм
        EntityType.PIGLIN,           // Пиглины
        EntityType.ZOMBIFIED_PIGLIN, // Зомбифицированные пиглины
        EntityType.SHULKER,          // Шалкер
        EntityType.SILVERFISH,       // Чешуйница
        EntityType.ENDERMITE         // Эндер чешуйница
    )
    
    // Список запрещенных мобов (которые будут удалены из спавна)
    private val bannedHostileMobs = setOf(
        EntityType.SKELETON,         // Скелеты
        EntityType.SPIDER,           // Пауки
        EntityType.CAVE_SPIDER,      // Пещерные пауки
        EntityType.CREEPER,          // Криперы
        EntityType.WITCH,            // Ведьмы
        EntityType.PHANTOM,          // Фантомы
        EntityType.SLIME,            // Слаймы
        EntityType.VINDICATOR,       // Поборники
        EntityType.EVOKER,           // Заклинатели
        EntityType.PILLAGER,         // Разбойники
        EntityType.RAVAGER,          // Разоритель
        EntityType.VEX,              // Досаждатель
        EntityType.GUARDIAN,         // Стражи
        EntityType.ELDER_GUARDIAN,   // Древние стражи
        EntityType.DROWNED,          // Утопленники
        EntityType.HUSK,             // Кадавры
        EntityType.STRAY,            // Зимогоры
        EntityType.ZOMBIE_VILLAGER,  // Зомби-жители
        EntityType.PIGLIN_BRUTE,     // Пиглин-громила
        EntityType.HOGLIN,           // Хоглин
        EntityType.ZOGLIN           // Зоглин
    )
    
    @SubscribeEvent
    fun onEntityJoinLevel(event: EntityJoinLevelEvent) {
        val entity = event.entity
        val level = event.level
        
        // Работаем только на сервере и только в верхнем мире
        if (level.isClientSide || level.dimension() != Level.OVERWORLD) {
            return
        }
        
        // Проверяем, является ли сущность запрещенным агрессивным мобом
        if (entity.type in bannedHostileMobs) {
            // Отменяем спавн
            event.isCanceled = true
            
            if (StorylineOfMineyarg.config.enableProgressLogging) {
                StorylineOfMineyarg.LOGGER.debug("Заблокирован спавн запрещенного моба: ${entity.type.description.string}")
            }
            return
        }
        
        // Дополнительная проверка для агрессивных мобов
        if (entity is Monster && entity.type !in allowedHostileMobs) {
            // Если это агрессивный моб, которого нет в списке разрешенных, блокируем его
            event.isCanceled = true
            
            if (StorylineOfMineyarg.config.enableProgressLogging) {
                StorylineOfMineyarg.LOGGER.debug("Заблокирован спавн неразрешенного агрессивного моба: ${entity.type.description.string}")
            }
        }
    }
    
    /**
     * Проверяет, разрешен ли спавн данного типа моба
     */
    fun isMobAllowed(entityType: EntityType<*>): Boolean {
        return entityType !in bannedHostileMobs
    }
    
    /**
     * Получает список разрешенных агрессивных мобов
     */
    fun getAllowedHostileMobs(): Set<EntityType<*>> {
        return allowedHostileMobs.toSet()
    }
    
    /**
     * Получает список запрещенных мобов
     */
    fun getBannedHostileMobs(): Set<EntityType<*>> {
        return bannedHostileMobs.toSet()
    }
}