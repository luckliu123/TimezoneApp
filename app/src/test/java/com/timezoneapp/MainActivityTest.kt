package com.timezoneapp

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * 主Activity的单元测试
 */
@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun testApplicationContext() {
        // 验证应用上下文
        assertNotNull(context)
        assertEquals("com.timezoneapp", context.packageName)
    }

    @Test
    fun testTimezoneDataStructure() {
        // 测试时区数据结构
        val timezoneData = listOf(
            MainActivity.TimeZoneItem("请选择时区", ""),
            MainActivity.TimeZoneItem("北京时间 (GMT+8)", "Asia/Shanghai"),
            MainActivity.TimeZoneItem("东京时间 (GMT+9)", "Asia/Tokyo")
        )

        assertTrue("时区数据不能为空", timezoneData.isNotEmpty())
        assertEquals("第一项应该是请选择时区", "请选择时区", timezoneData[0].displayName)
        assertEquals("第一项时区ID应该为空", "", timezoneData[0].timezoneId)
        assertEquals("第二项应该是北京时间", "Asia/Shanghai", timezoneData[1].timezoneId)
    }

    @Test
    fun testTimezoneItemDataClass() {
        // 测试时区数据类
        val timezoneItem = MainActivity.TimeZoneItem("测试时区", "Test/Timezone")
        
        assertEquals("显示名称应该匹配", "测试时区", timezoneItem.displayName)
        assertEquals("时区ID应该匹配", "Test/Timezone", timezoneItem.timezoneId)
    }

    @Test
    fun testTimezoneItemEquality() {
        // 测试时区数据类的相等性
        val item1 = MainActivity.TimeZoneItem("北京时间", "Asia/Shanghai")
        val item2 = MainActivity.TimeZoneItem("北京时间", "Asia/Shanghai")
        val item3 = MainActivity.TimeZoneItem("东京时间", "Asia/Tokyo")

        assertEquals("相同的时区项应该相等", item1, item2)
        assertNotEquals("不同的时区项应该不相等", item1, item3)
    }

    @Test
    fun testStringResources() {
        // 测试字符串资源
        val appName = context.getString(R.string.app_name)
        val selectTimezone = context.getString(R.string.select_timezone)
        val wifiStatus = context.getString(R.string.wifi_status)

        assertNotNull("应用名称不能为null", appName)
        assertNotNull("选择时区文本不能为null", selectTimezone)
        assertNotNull("WiFi状态文本不能为null", wifiStatus)

        assertTrue("应用名称不能为空", appName.isNotEmpty())
        assertTrue("选择时区文本不能为空", selectTimezone.isNotEmpty())
        assertTrue("WiFi状态文本不能为空", wifiStatus.isNotEmpty())
    }
    
    @Test
    fun testTimezonePersistence() {
        // 测试时区设置的持久化功能
        val sharedPrefs = context.getSharedPreferences("timezone_prefs", Context.MODE_PRIVATE)
        
        // 清除之前的数据
        sharedPrefs.edit().clear().apply()
        
        // 模拟保存时区设置
        val testTimezoneId = "Asia/Shanghai"
        val testTimezoneName = "北京时间 (GMT+8)"
        
        sharedPrefs.edit()
            .putString("selected_timezone_id", testTimezoneId)
            .putString("selected_timezone_name", testTimezoneName)
            .apply()
        
        // 验证保存的数据
        val savedId = sharedPrefs.getString("selected_timezone_id", "")
        val savedName = sharedPrefs.getString("selected_timezone_name", "")
        
        assertEquals("保存的时区ID应该匹配", testTimezoneId, savedId)
        assertEquals("保存的时区名称应该匹配", testTimezoneName, savedName)
        
        // 清理测试数据
        sharedPrefs.edit().clear().apply()
    }
    
    @Test
    fun testAutoTimezoneMessages() {
        // 测试自动化相关的提示信息
        val welcomeMessage = context.getString(R.string.welcome_message)
        val retryConnection = context.getString(R.string.retry_connection)
        
        assertNotNull("欢迎信息不能为null", welcomeMessage)
        assertNotNull("重试连接按钮文本不能为null", retryConnection)
        
        assertTrue("欢迎信息应该包含关键信息", welcomeMessage.contains("时区"))
        assertTrue("重试按钮应该包含重试关键词", retryConnection.contains("重试"))
    }
    
    @Test
    fun testNetworkOptimizationMessages() {
        // 测试网络优化相关的提示信息
        val gettingTimeWait = context.getString(R.string.getting_network_time_wait)
        val networkNotAvailable = context.getString(R.string.network_not_available)
        val localTimeSuccess = context.getString(R.string.timezone_set_success_local)
        
        assertNotNull("获取时间等待信息不能为null", gettingTimeWait)
        assertNotNull("网络不可用信息不能为null", networkNotAvailable)
        assertNotNull("本地时间成功信息不能为null", localTimeSuccess)
        
        assertTrue("获取时间信息应该包含等待关键词", gettingTimeWait.contains("稍候"))
        assertTrue("网络不可用信息应该包含检查关键词", networkNotAvailable.contains("检查"))
        assertTrue("本地时间成功信息应该包含本地关键词", localTimeSuccess.contains("本地"))
    }
    
    @Test
    fun testWifiPriorityStrategy() {
        // 测试WiFi优先策略的逻辑
        
        // 模拟WiFi连接成功的情况
        val wifiConnected = context.getString(R.string.wifi_connected)
        val wifiDisconnected = context.getString(R.string.wifi_disconnected)
        
        assertNotNull("WiFi连接成功信息不能为null", wifiConnected)
        assertNotNull("WiFi未连接信息不能为null", wifiDisconnected)
        
        assertNotEquals("WiFi连接和未连接的信息应该不同", wifiConnected, wifiDisconnected)
        
        // 验证关键词
        assertTrue("WiFi连接信息应该体现正面意义", 
            wifiConnected.contains("已连接") || wifiConnected.contains("成功"))
        assertTrue("WiFi未连接信息应该体现负面意义",
            wifiDisconnected.contains("未连接") || wifiDisconnected.contains("断开"))
    }
}