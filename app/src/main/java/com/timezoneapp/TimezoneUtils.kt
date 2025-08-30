package com.timezoneapp

import java.util.*

/**
 * 时区工具类
 */
object TimezoneUtils {
    
    /**
     * 验证时区ID是否有效
     */
    fun isValidTimezoneId(timezoneId: String): Boolean {
        return try {
            TimeZone.getTimeZone(timezoneId)
            timezoneId.isNotEmpty() && !timezoneId.equals("GMT", ignoreCase = true)
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 获取时区的GMT偏移量
     */
    fun getTimezoneOffset(timezoneId: String): Int {
        return try {
            val timeZone = TimeZone.getTimeZone(timezoneId)
            timeZone.rawOffset / (1000 * 60 * 60) // 转换为小时
        } catch (e: Exception) {
            0
        }
    }
    
    /**
     * 格式化时区显示名称
     */
    fun formatTimezoneDisplay(timezoneId: String): String {
        return try {
            val timeZone = TimeZone.getTimeZone(timezoneId)
            val offset = timeZone.rawOffset / (1000 * 60 * 60)
            val sign = if (offset >= 0) "+" else ""
            "${timeZone.displayName} (GMT${sign}${offset})"
        } catch (e: Exception) {
            timezoneId
        }
    }
}