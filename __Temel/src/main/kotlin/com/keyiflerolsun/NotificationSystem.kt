package com.keyiflerolsun

import android.util.Log
import java.util.*

/**
 * Bildirim ve bildirim sistemi
 */
object NotificationSystem {
    
    private const val TAG = "NotificationSystem"
    
    /**
     * Bildirim türü
     */
    enum class NotificationType {
        NEW_CONTENT,        // Yeni içerik
        RECOMMENDATION,     // Öneri
        SYSTEM_UPDATE,      // Sistem güncellemesi
        MAINTENANCE,        // Bakım
        PROMOTION,          // Promosyon
        PERSONAL           // Kişisel bildirim
    }
    
    /**
     * Bildirim önceliği
     */
    enum class NotificationPriority {
        LOW,        // Düşük
        NORMAL,     // Normal
        HIGH,       // Yüksek
        URGENT      // Acil
    }
    
    /**
     * Bildirim
     */
    data class Notification(
        val id: String,
        val type: NotificationType,
        val priority: NotificationPriority,
        val title: String,
        val message: String,
        val userId: String? = null, // null ise genel bildirim
        val contentId: String? = null,
        val actionUrl: String? = null,
        val imageUrl: String? = null,
        val createdAt: Long = System.currentTimeMillis(),
        val readAt: Long? = null,
        val expiresAt: Long? = null
    )
    
    /**
     * Bildirim ayarları
     */
    data class NotificationSettings(
        val userId: String,
        val enabled: Boolean = true,
        val newContent: Boolean = true,
        val recommendations: Boolean = true,
        val systemUpdates: Boolean = true,
        val maintenance: Boolean = true,
        val promotions: Boolean = false,
        val personal: Boolean = true,
        val quietHours: Boolean = false,
        val quietHoursStart: Int = 22, // 22:00
        val quietHoursEnd: Int = 8,    // 08:00
        val maxNotifications: Int = 100
    )
    
    // Bildirimler (gerçek uygulamada veritabanında saklanır)
    private val notifications = mutableMapOf<String, Notification>()
    
    // Bildirim ayarları
    private val notificationSettings = mutableMapOf<String, NotificationSettings>()
    
    /**
     * Bildirim oluşturur
     */
    fun createNotification(
        type: NotificationType,
        priority: NotificationPriority,
        title: String,
        message: String,
        userId: String? = null,
        contentId: String? = null,
        actionUrl: String? = null,
        imageUrl: String? = null,
        expiresAt: Long? = null
    ): Notification {
        val notification = Notification(
            id = generateNotificationId(),
            type = type,
            priority = priority,
            title = title,
            message = message,
            userId = userId,
            contentId = contentId,
            actionUrl = actionUrl,
            imageUrl = imageUrl,
            expiresAt = expiresAt
        )
        
        notifications[notification.id] = notification
        Log.d(TAG, "Bildirim oluşturuldu: ${notification.title}")
        
        return notification
    }
    
    /**
     * Yeni içerik bildirimi
     */
    fun notifyNewContent(contentItem: ContentItem, userIds: List<String>? = null) {
        val title = "Yeni İçerik: ${contentItem.title}"
        val message = "${contentItem.type.name} kategorisinde yeni içerik eklendi"
        
        if (userIds != null) {
            // Belirli kullanıcılara bildirim
            userIds.forEach { userId ->
                if (shouldSendNotification(userId, NotificationType.NEW_CONTENT)) {
                    createNotification(
                        type = NotificationType.NEW_CONTENT,
                        priority = NotificationPriority.NORMAL,
                        title = title,
                        message = message,
                        userId = userId,
                        contentId = contentItem.id,
                        actionUrl = "unified://${contentItem.id}"
                    )
                }
            }
        } else {
            // Genel bildirim
            createNotification(
                type = NotificationType.NEW_CONTENT,
                priority = NotificationPriority.NORMAL,
                title = title,
                message = message,
                contentId = contentItem.id,
                actionUrl = "unified://${contentItem.id}"
            )
        }
    }
    
    /**
     * Öneri bildirimi
     */
    fun notifyRecommendation(userId: String, contentItem: ContentItem) {
        if (!shouldSendNotification(userId, NotificationType.RECOMMENDATION)) return
        
        val title = "Sizin için öneri: ${contentItem.title}"
        val message = "Bu içeriği beğenebilirsiniz"
        
        createNotification(
            type = NotificationType.RECOMMENDATION,
            priority = NotificationPriority.LOW,
            title = title,
            message = message,
            userId = userId,
            contentId = contentItem.id,
            actionUrl = "unified://${contentItem.id}"
        )
    }
    
    /**
     * Sistem güncellemesi bildirimi
     */
    fun notifySystemUpdate(title: String, message: String, userIds: List<String>? = null) {
        if (userIds != null) {
            userIds.forEach { userId ->
                if (shouldSendNotification(userId, NotificationType.SYSTEM_UPDATE)) {
                    createNotification(
                        type = NotificationType.SYSTEM_UPDATE,
                        priority = NotificationPriority.HIGH,
                        title = title,
                        message = message,
                        userId = userId
                    )
                }
            }
        } else {
            createNotification(
                type = NotificationType.SYSTEM_UPDATE,
                priority = NotificationPriority.HIGH,
                title = title,
                message = message
            )
        }
    }
    
    /**
     * Bakım bildirimi
     */
    fun notifyMaintenance(title: String, message: String, startTime: Long, endTime: Long) {
        createNotification(
            type = NotificationType.MAINTENANCE,
            priority = NotificationPriority.URGENT,
            title = title,
            message = message,
            expiresAt = endTime
        )
    }
    
    /**
     * Promosyon bildirimi
     */
    fun notifyPromotion(title: String, message: String, actionUrl: String, userIds: List<String>? = null) {
        if (userIds != null) {
            userIds.forEach { userId ->
                if (shouldSendNotification(userId, NotificationType.PROMOTION)) {
                    createNotification(
                        type = NotificationType.PROMOTION,
                        priority = NotificationPriority.NORMAL,
                        title = title,
                        message = message,
                        userId = userId,
                        actionUrl = actionUrl
                    )
                }
            }
        } else {
            createNotification(
                type = NotificationType.PROMOTION,
                priority = NotificationPriority.NORMAL,
                title = title,
                message = message,
                actionUrl = actionUrl
            )
        }
    }
    
    /**
     * Kişisel bildirim
     */
    fun notifyPersonal(userId: String, title: String, message: String, contentId: String? = null) {
        if (!shouldSendNotification(userId, NotificationType.PERSONAL)) return
        
        createNotification(
            type = NotificationType.PERSONAL,
            priority = NotificationPriority.NORMAL,
            title = title,
            message = message,
            userId = userId,
            contentId = contentId
        )
    }
    
    /**
     * Kullanıcının bildirimlerini alır
     */
    fun getUserNotifications(userId: String, limit: Int = 50): List<Notification> {
        val currentTime = System.currentTimeMillis()
        
        return notifications.values
            .filter { notification ->
                (notification.userId == null || notification.userId == userId) &&
                (notification.expiresAt == null || notification.expiresAt > currentTime)
            }
            .sortedByDescending { it.createdAt }
            .take(limit)
    }
    
    /**
     * Okunmamış bildirimleri alır
     */
    fun getUnreadNotifications(userId: String): List<Notification> {
        return getUserNotifications(userId).filter { it.readAt == null }
    }
    
    /**
     * Bildirimi okundu olarak işaretler
     */
    fun markAsRead(notificationId: String): Boolean {
        val notification = notifications[notificationId] ?: return false
        
        val updatedNotification = notification.copy(readAt = System.currentTimeMillis())
        notifications[notificationId] = updatedNotification
        
        Log.d(TAG, "Bildirim okundu olarak işaretlendi: $notificationId")
        return true
    }
    
    /**
     * Tüm bildirimleri okundu olarak işaretler
     */
    fun markAllAsRead(userId: String): Int {
        val currentTime = System.currentTimeMillis()
        var count = 0
        
        notifications.forEach { (id, notification) ->
            if ((notification.userId == null || notification.userId == userId) && notification.readAt == null) {
                notifications[id] = notification.copy(readAt = currentTime)
                count++
            }
        }
        
        Log.d(TAG, "$count bildirim okundu olarak işaretlendi")
        return count
    }
    
    /**
     * Bildirimi siler
     */
    fun deleteNotification(notificationId: String): Boolean {
        return notifications.remove(notificationId) != null
    }
    
    /**
     * Eski bildirimleri temizler
     */
    fun cleanupOldNotifications(daysToKeep: Int = 30) {
        val cutoffTime = System.currentTimeMillis() - (daysToKeep * 24 * 60 * 60 * 1000L)
        val oldNotifications = notifications.filter { it.value.createdAt < cutoffTime }
        
        oldNotifications.forEach { (id, _) ->
            notifications.remove(id)
        }
        
        Log.d(TAG, "${oldNotifications.size} eski bildirim temizlendi")
    }
    
    /**
     * Bildirim ayarlarını oluşturur
     */
    fun createNotificationSettings(userId: String): NotificationSettings {
        val settings = NotificationSettings(userId = userId)
        notificationSettings[userId] = settings
        return settings
    }
    
    /**
     * Bildirim ayarlarını alır
     */
    fun getNotificationSettings(userId: String): NotificationSettings? {
        return notificationSettings[userId]
    }
    
    /**
     * Bildirim ayarlarını günceller
     */
    fun updateNotificationSettings(userId: String, settings: NotificationSettings): Boolean {
        notificationSettings[userId] = settings
        Log.d(TAG, "Bildirim ayarları güncellendi: $userId")
        return true
    }
    
    /**
     * Bildirim gönderilip gönderilmeyeceğini kontrol eder
     */
    private fun shouldSendNotification(userId: String, type: NotificationType): Boolean {
        val settings = notificationSettings[userId] ?: return true
        
        if (!settings.enabled) return false
        
        // Sessiz saatler kontrolü
        if (settings.quietHours) {
            val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            if (currentHour >= settings.quietHoursStart || currentHour < settings.quietHoursEnd) {
                return false
            }
        }
        
        // Bildirim türü kontrolü
        return when (type) {
            NotificationType.NEW_CONTENT -> settings.newContent
            NotificationType.RECOMMENDATION -> settings.recommendations
            NotificationType.SYSTEM_UPDATE -> settings.systemUpdates
            NotificationType.MAINTENANCE -> settings.maintenance
            NotificationType.PROMOTION -> settings.promotions
            NotificationType.PERSONAL -> settings.personal
        }
    }
    
    /**
     * Bildirim ID'si oluşturur
     */
    private fun generateNotificationId(): String {
        return "notification_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(8)}"
    }
    
    /**
     * Bildirim istatistikleri
     */
    fun getNotificationStats(userId: String): Map<String, Any> {
        val userNotifications = getUserNotifications(userId)
        val unreadCount = userNotifications.count { it.readAt == null }
        
        val typeStats = userNotifications
            .groupBy { it.type }
            .mapValues { it.value.size }
        
        return mapOf(
            "totalNotifications" to userNotifications.size,
            "unreadCount" to unreadCount,
            "typeStats" to typeStats,
            "lastNotification" to userNotifications.maxByOrNull { it.createdAt }?.createdAt
        )
    }
} 