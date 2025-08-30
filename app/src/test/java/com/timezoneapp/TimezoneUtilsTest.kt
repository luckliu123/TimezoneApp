package com.timezoneapp

import org.junit.Assert.*
import org.junit.Test

/**
 * 时区工具类测试
 */
class TimezoneUtilsTest {

    @Test
    fun testValidTimezoneIds() {
        // 测试有效的时区ID
        assertTrue("Asia/Shanghai应该是有效时区", TimezoneUtils.isValidTimezoneId("Asia/Shanghai"))
        assertTrue("Asia/Tokyo应该是有效时区", TimezoneUtils.isValidTimezoneId("Asia/Tokyo"))
        assertTrue("Europe/London应该是有效时区", TimezoneUtils.isValidTimezoneId("Europe/London"))
        assertTrue("America/New_York应该是有效时区", TimezoneUtils.isValidTimezoneId("America/New_York"))
    }

    @Test
    fun testInvalidTimezoneIds() {
        // 测试无效的时区ID
        assertFalse("空字符串应该是无效时区", TimezoneUtils.isValidTimezoneId(""))
        assertFalse("GMT应该被认为是无效时区", TimezoneUtils.isValidTimezoneId("GMT"))
        assertFalse("无效时区ID应该返回false", TimezoneUtils.isValidTimezoneId("Invalid/Timezone"))
    }

    @Test
    fun testTimezoneOffsets() {
        // 测试时区偏移量
        assertEquals("北京时间应该是GMT+8", 8, TimezoneUtils.getTimezoneOffset("Asia/Shanghai"))
        assertEquals("东京时间应该是GMT+9", 9, TimezoneUtils.getTimezoneOffset("Asia/Tokyo"))
        assertEquals("纽约时间应该是GMT-5", -5, TimezoneUtils.getTimezoneOffset("America/New_York"))
        assertEquals("洛杉矶时间应该是GMT-8", -8, TimezoneUtils.getTimezoneOffset("America/Los_Angeles"))
    }

    @Test
    fun testTimezoneDisplayFormatting() {
        // 测试时区显示格式化
        val shanghaiDisplay = TimezoneUtils.formatTimezoneDisplay("Asia/Shanghai")
        val tokyoDisplay = TimezoneUtils.formatTimezoneDisplay("Asia/Tokyo")
        val newYorkDisplay = TimezoneUtils.formatTimezoneDisplay("America/New_York")

        assertNotNull("上海时区显示不能为null", shanghaiDisplay)
        assertNotNull("东京时区显示不能为null", tokyoDisplay)
        assertNotNull("纽约时区显示不能为null", newYorkDisplay)

        assertTrue("上海时区显示应包含GMT+8", shanghaiDisplay.contains("GMT+8"))
        assertTrue("东京时区显示应包含GMT+9", tokyoDisplay.contains("GMT+9"))
        assertTrue("纽约时区显示应包含GMT-5", newYorkDisplay.contains("GMT-5"))
    }

    @Test
    fun testEdgeCases() {
        // 测试边界情况
        assertEquals("无效时区的偏移量应该是0", 0, TimezoneUtils.getTimezoneOffset("Invalid/Timezone"))
        
        val invalidDisplay = TimezoneUtils.formatTimezoneDisplay("Invalid/Timezone")
        assertEquals("无效时区应该返回原始ID", "Invalid/Timezone", invalidDisplay)
    }
}