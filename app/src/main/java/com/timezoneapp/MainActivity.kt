package com.timezoneapp

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.timezoneapp.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var wifiManager: WifiManager
    private lateinit var connectivityManager: ConnectivityManager
    
    // 时区数据
    private val timezoneData = listOf(
        TimeZoneItem("请选择时区", ""),
        TimeZoneItem("北京时间 (GMT+8)", "Asia/Shanghai"),
        TimeZoneItem("东京时间 (GMT+9)", "Asia/Tokyo"),
        TimeZoneItem("首尔时间 (GMT+9)", "Asia/Seoul"),
        TimeZoneItem("香港时间 (GMT+8)", "Asia/Hong_Kong"),
        TimeZoneItem("新加坡时间 (GMT+8)", "Asia/Singapore"),
        TimeZoneItem("伦敦时间 (GMT+0)", "Europe/London"),
        TimeZoneItem("巴黎时间 (GMT+1)", "Europe/Paris"),
        TimeZoneItem("纽约时间 (GMT-5)", "America/New_York"),
        TimeZoneItem("洛杉矶时间 (GMT-8)", "America/Los_Angeles"),
        TimeZoneItem("悉尼时间 (GMT+11)", "Australia/Sydney")
    )
    
    private var selectedTimezoneId: String = ""
    
    // 权限请求器
    private val writeSettingsPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (Settings.System.canWrite(this)) {
            Toast.makeText(this, "权限已授予", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "需要系统设置权限才能修改时区", Toast.LENGTH_LONG).show()
        }
    }
    
    // 存储权限请求器
    private val storagePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            Toast.makeText(this, "存储权限已授予", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "需要存储权限才能清理相册", Toast.LENGTH_LONG).show()
        }
    }
    
    // 管理所有文件权限请求器 (Android 11+)
    private val manageStoragePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                Toast.makeText(this, "文件管理权限已授予", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "需要文件管理权限才能完全清理相册", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        initViews()
        setupTimezoneSpinner()
        setupClickListeners()
        loadSavedTimezone()
        
        // 启动时自动检测WiFi并开始自动化流程
        startAutomaticFlow()
    }
    
    private fun initViews() {
        sharedPreferences = getSharedPreferences("timezone_prefs", Context.MODE_PRIVATE)
        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }
    
    private fun setupTimezoneSpinner() {
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            timezoneData.map { it.displayName }
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerTimezone.adapter = adapter
        
        binding.spinnerTimezone.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position > 0) {
                    selectedTimezoneId = timezoneData[position].timezoneId
                    updateCurrentTimezoneDisplay(timezoneData[position].displayName)
                    saveSelectedTimezone(selectedTimezoneId, timezoneData[position].displayName)
                }
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }
    
    private fun setupClickListeners() {
        binding.btnCheckWifi.setOnClickListener {
            checkWifiStatus()
        }
        
        // 重试按钮 - 重新开始自动化流程
        binding.btnRetryConnection.setOnClickListener {
            startAutomaticFlow()
        }
        
        binding.btnGoToWifiSettings.setOnClickListener {
            openWifiSettings()
        }
        
        binding.btnSetTimezone.setOnClickListener {
            if (selectedTimezoneId.isEmpty()) {
                Toast.makeText(this, getString(R.string.please_select_timezone), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            if (!isWifiConnected()) {
                showWifiRequiredDialog()
                return@setOnClickListener
            }
            
            if (!Settings.System.canWrite(this)) {
                requestWriteSettingsPermission()
                return@setOnClickListener
            }
            
            // 使用优化的设置方法
            setTimezoneWithWifiPriority()
        }
        
        // 相册清理按钮点击事件
        binding.btnCleanupGallery.setOnClickListener {
            if (checkStoragePermissions()) {
                showGalleryCleanupConfirmDialog()
            } else {
                requestStoragePermissions()
            }
        }
    }
    
    private fun checkWifiStatus() {
        val isConnected = isWifiConnected()
        updateWifiStatusDisplay(isConnected)
    }
    
    private fun isWifiConnected(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }
    
    private fun updateWifiStatusDisplay(isConnected: Boolean) {
        binding.tvWifiStatus.text = if (isConnected) {
            getString(R.string.wifi_connected)
        } else {
            getString(R.string.wifi_disconnected)
        }
        
        binding.tvWifiStatus.setTextColor(
            ContextCompat.getColor(
                this,
                if (isConnected) R.color.success_green else R.color.error_red
            )
        )
        
        // 根据WiFi状态显示或隐藏连接选项
        if (isConnected) {
            hideConnectionOptions()
        } else {
            showConnectionOptions()
        }
    }
    
    private fun openWifiSettings() {
        try {
            val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "无法打开WiFi设置", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun showWifiRequiredDialog() {
        AlertDialog.Builder(this)
            .setTitle("需要WiFi连接")
            .setMessage(getString(R.string.wifi_not_connected))
            .setPositiveButton("前往设置") { _, _ ->
                openWifiSettings()
            }
            .setNegativeButton("取消", null)
            .show()
    }
    
    private fun requestWriteSettingsPermission() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.permission_required))
            .setMessage(getString(R.string.permission_description))
            .setPositiveButton(getString(R.string.grant_permission)) { _, _ ->
                val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
                intent.data = android.net.Uri.parse("package:$packageName")
                writeSettingsPermissionLauncher.launch(intent)
            }
            .setNegativeButton("取消", null)
            .show()
    }
    
    private fun setTimezone() {
        showStatus(getString(R.string.getting_network_time))
        
        lifecycleScope.launch {
            try {
                // 获取网络时间
                val networkTime = getNetworkTime()
                
                withContext(Dispatchers.Main) {
                    if (networkTime != null) {
                        // 设置时区
                        val success = setSystemTimezone(selectedTimezoneId)
                        if (success) {
                            val currentTimezoneName = getCurrentTimezoneDisplayName()
                            showStatus("时区设置成功！当前时区：${currentTimezoneName}")
                            Toast.makeText(this@MainActivity, "时区已自动设置为：${currentTimezoneName}", Toast.LENGTH_LONG).show()
                            hideConnectionOptions() // 设置成功后隐藏重试按钮
                        } else {
                            showStatus(getString(R.string.timezone_set_failed))
                            Toast.makeText(this@MainActivity, getString(R.string.timezone_set_failed), Toast.LENGTH_LONG).show()
                        }
                    } else {
                        showStatus(getString(R.string.network_time_failed))
                        Toast.makeText(this@MainActivity, getString(R.string.network_time_failed), Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showStatus("设置失败: ${e.message}")
                    Toast.makeText(this@MainActivity, "设置失败: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    
    private suspend fun getNetworkTime(): Date? = withContext(Dispatchers.IO) {
        try {
            // 通过访问世界时钟API获取网络时间
            val url = URL("http://worldtimeapi.org/api/timezone/UTC")
            val connection = url.openConnection()
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            connection.connect()
            
            // 简单的备选方案：使用系统当前时间
            Date()
        } catch (e: IOException) {
            null
        }
    }
    
    private fun setSystemTimezone(timezoneId: String): Boolean {
        return try {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            
            // 尝试通过AlarmManager设置时区
            alarmManager.setTimeZone(timezoneId)
            
            // 更新系统属性（需要root权限）
            try {
                val process = Runtime.getRuntime().exec("su -c 'setprop persist.sys.timezone $timezoneId'")
                process.waitFor()
            } catch (e: Exception) {
                // 如果没有root权限，忽略这个错误
            }
            
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    private fun updateCurrentTimezoneDisplay(timezoneName: String) {
        binding.tvCurrentTimezone.text = timezoneName
    }
    
    private fun saveSelectedTimezone(timezoneId: String, displayName: String) {
        sharedPreferences.edit()
            .putString("selected_timezone_id", timezoneId)
            .putString("selected_timezone_name", displayName)
            .apply()
    }
    
    private fun loadSavedTimezone() {
        val savedTimezoneId = sharedPreferences.getString("selected_timezone_id", "")
        val savedTimezoneName = sharedPreferences.getString("selected_timezone_name", "")
        
        if (!savedTimezoneId.isNullOrEmpty() && !savedTimezoneName.isNullOrEmpty()) {
            selectedTimezoneId = savedTimezoneId
            updateCurrentTimezoneDisplay(savedTimezoneName)
            
            // 设置spinner选中项
            val index = timezoneData.indexOfFirst { it.timezoneId == savedTimezoneId }
            if (index > 0) {
                binding.spinnerTimezone.setSelection(index)
            }
            
            // 显示已保存的设置信息
            showStatus("已加载保存的时区设置：${savedTimezoneName}")
        } else {
            showStatus("欢迎使用时区设置助手，请选择您的目标时区")
        }
    }
    
    private fun showStatus(message: String) {
        binding.tvStatus.text = message
        binding.tvStatus.visibility = View.VISIBLE
    }
    
    override fun onResume() {
        super.onResume()
        // 从设置页面返回时重新检查WiFi状态
        checkWifiStatus()
    }
    
    // 自动化流程相关功能
    
    /**
     * 启动自动化流程
     * 1. 检测WiFi连接（优先检测WiFi而非网络连接）
     * 2. 如果WiFi连接成功，直接尝试获取时间并设置时区
     * 3. 只有在获取时间失败时才检测网络连接状态
     * 4. 如果有保存的时区设置，自动使用保存的设置
     */
    private fun startAutomaticFlow() {
        showStatus("正在检测WiFi连接...")
        
        lifecycleScope.launch {
            delay(500) // 稍等一下让用户看到状态
            
            withContext(Dispatchers.Main) {
                val isWifiConnected = isWifiConnected()
                updateWifiStatusDisplay(isWifiConnected)
                
                if (isWifiConnected) {
                    // WiFi已连接，检查是否有保存的时区设置
                    val savedTimezone = getSavedTimezoneForAutoSetup()
                    if (savedTimezone.isNotEmpty()) {
                        showStatus("检测到WiFi连接，正在使用保存的时区设置自动配置...")
                        selectedTimezoneId = savedTimezone
                        autoSetupTimezoneWithWifi()
                    } else {
                        showStatus("检测到WiFi连接，请选择时区后点击重试进行自动设置")
                        showConnectionOptions()
                    }
                } else {
                    // WiFi未连接，显示重试和设置选项
                    showStatus("未检测到WiFi连接，请连接WiFi后点击重试")
                    showConnectionOptions()
                }
            }
        }
    }
    
    /**
     * 自动设置时区流程（原有方法）
     */
    private fun autoSetupTimezone() {
        if (selectedTimezoneId.isEmpty()) {
            showStatus("请先选择目标时区，然后点击重试")
            showConnectionOptions()
            return
        }
        
        if (!Settings.System.canWrite(this)) {
            showStatus("需要系统设置权限，请授予权限后点击重试")
            requestWriteSettingsPermission()
            return
        }
        
        // 开始自动设置流程
        setTimezone()
    }
    
    /**
     * WiFi连接后的自动设置时区流程（优化版本）
     * 优先尝试获取时间，只有在失败时才检测网络连接
     */
    private fun autoSetupTimezoneWithWifi() {
        if (selectedTimezoneId.isEmpty()) {
            showStatus("请先选择目标时区，然后点击重试")
            showConnectionOptions()
            return
        }
        
        if (!Settings.System.canWrite(this)) {
            showStatus("需要系统设置权限，请授予权限后点击重试")
            requestWriteSettingsPermission()
            return
        }
        
        // 开始优化的设置流程
        setTimezoneWithWifiPriority()
    }
    
    /**
     * 显示连接选项（重试和设置按钮）
     */
    private fun showConnectionOptions() {
        binding.btnRetryConnection.visibility = View.VISIBLE
        binding.btnGoToWifiSettings.visibility = View.VISIBLE
    }
    
    /**
     * 隐藏连接选项
     */
    private fun hideConnectionOptions() {
        binding.btnRetryConnection.visibility = View.GONE
        binding.btnGoToWifiSettings.visibility = View.GONE
    }
    
    /**
     * 获取保存的时区设置用于自动配置
     */
    private fun getSavedTimezoneForAutoSetup(): String {
        return sharedPreferences.getString("selected_timezone_id", "") ?: ""
    }
    
    /**
     * 检查是否已经有保存的时区设置
     */
    private fun hasSavedTimezone(): Boolean {
        val savedTimezoneId = getSavedTimezoneForAutoSetup()
        return savedTimezoneId.isNotEmpty()
    }
    
    /**
     * 获取当前选中的时区显示名称
     */
    private fun getCurrentTimezoneDisplayName(): String {
        val currentItem = timezoneData.find { it.timezoneId == selectedTimezoneId }
        return currentItem?.displayName ?: "未知时区"
    }
    
    /**
     * 获取保存的时区显示名称
     */
    private fun getSavedTimezoneDisplayName(): String {
        return sharedPreferences.getString("selected_timezone_name", "") ?: ""
    }
    
    // 相册清理相关功能
    
    /**
     * 检查存储权限
     */
    private fun checkStoragePermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ 使用新的媒体权限
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11-12 需要管理所有文件权限
            Environment.isExternalStorageManager()
        } else {
            // Android 10 及以下
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    /**
     * 请求存储权限
     */
    private fun requestStoragePermissions() {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                // Android 13+ 请求新的媒体权限
                storagePermissionLauncher.launch(arrayOf(
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO
                ))
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                // Android 11+ 请求管理所有文件权限
                showManageStoragePermissionDialog()
            }
            else -> {
                // Android 10 及以下
                storagePermissionLauncher.launch(arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ))
            }
        }
    }
    
    /**
     * 显示管理存储权限对话框
     */
    private fun showManageStoragePermissionDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.storage_permission_required))
            .setMessage(getString(R.string.storage_permission_description))
            .setPositiveButton(getString(R.string.grant_storage_permission)) { _, _ ->
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.data = Uri.parse("package:$packageName")
                manageStoragePermissionLauncher.launch(intent)
            }
            .setNegativeButton("取消", null)
            .show()
    }
    
    /**
     * 显示相册清理确认对话框
     */
    private fun showGalleryCleanupConfirmDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.confirm_gallery_cleanup))
            .setMessage(getString(R.string.gallery_cleanup_warning))
            .setPositiveButton(getString(R.string.confirm_delete)) { _, _ ->
                cleanupGallery()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }
    
    /**
     * 清理相册功能
     */
    private fun cleanupGallery() {
        showStatus(getString(R.string.scanning_gallery))
        
        lifecycleScope.launch {
            try {
                val deletedCount = withContext(Dispatchers.IO) {
                    scanAndDeleteMediaFiles()
                }
                
                withContext(Dispatchers.Main) {
                    if (deletedCount > 0) {
                        showStatus(getString(R.string.gallery_cleanup_success))
                        Toast.makeText(this@MainActivity, getString(R.string.gallery_cleanup_success), Toast.LENGTH_LONG).show()
                    } else {
                        showStatus(getString(R.string.gallery_empty))
                        Toast.makeText(this@MainActivity, getString(R.string.gallery_empty), Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showStatus(getString(R.string.gallery_cleanup_failed))
                    Toast.makeText(this@MainActivity, "清理失败: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    
    /**
     * 扫描并删除媒体文件
     */
    private fun scanAndDeleteMediaFiles(): Int {
        var deletedCount = 0
        
        try {
            // 删除图片文件
            deletedCount += deleteMediaFiles(MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            
            // 删除视频文件
            deletedCount += deleteMediaFiles(MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            
            // 删除外部存储中的DCIM文件夹
            deletedCount += deleteDCIMFolder()
            
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        return deletedCount
    }
    
    /**
     * 删除指定类型的媒体文件
     */
    private fun deleteMediaFiles(uri: Uri): Int {
        var deletedCount = 0
        val projection = arrayOf(
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.DATA
        )
        
        contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
            
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val filePath = cursor.getString(dataColumn)
                
                try {
                    // 通过MediaStore删除
                    val deleteUri = Uri.withAppendedPath(uri, id.toString())
                    val rowsDeleted = contentResolver.delete(deleteUri, null, null)
                    
                    if (rowsDeleted > 0) {
                        deletedCount++
                    } else {
                        // 如果MediaStore删除失败，尝试直接删除文件
                        val file = File(filePath)
                        if (file.exists() && file.delete()) {
                            deletedCount++
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        
        return deletedCount
    }
    
    /**
     * 删除DCIM文件夹中的文件
     */
    private fun deleteDCIMFolder(): Int {
        var deletedCount = 0
        
        try {
            val dcimPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
            if (dcimPath.exists()) {
                deletedCount += deleteFilesRecursively(dcimPath)
            }
            
            // 也检查Pictures文件夹
            val picturesPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            if (picturesPath.exists()) {
                deletedCount += deleteFilesRecursively(picturesPath)
            }
            
            // 检查Movies文件夹
            val moviesPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
            if (moviesPath.exists()) {
                deletedCount += deleteFilesRecursively(moviesPath)
            }
            
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        return deletedCount
    }
    
    /**
     * 递归删除文件夹中的文件
     */
    private fun deleteFilesRecursively(directory: File): Int {
        var deletedCount = 0
        
        if (!directory.exists() || !directory.isDirectory) {
            return 0
        }
        
        directory.listFiles()?.forEach { file ->
            try {
                if (file.isDirectory) {
                    deletedCount += deleteFilesRecursively(file)
                    // 尝试删除空文件夹
                    if (file.listFiles()?.isEmpty() == true) {
                        if (file.delete()) {
                            deletedCount++
                        }
                    }
                } else if (isMediaFile(file)) {
                    if (file.delete()) {
                        deletedCount++
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
        return deletedCount
    }
    
    /**
     * 判断是否为媒体文件
     */
    private fun isMediaFile(file: File): Boolean {
        val fileName = file.name.lowercase()
        val imageExtensions = listOf(".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp", ".heic", ".heif")
        val videoExtensions = listOf(".mp4", ".avi", ".mov", ".wmv", ".flv", ".mkv", ".webm", ".3gp")
        
        return imageExtensions.any { fileName.endsWith(it) } || 
               videoExtensions.any { fileName.endsWith(it) }
    }
    
    // 优化的时区设置方法
    
    /**
     * WiFi优先的时区设置方法
     * 1. 先尝试获取网络时间（即使WiFi已连接但网络可能未完全可用）
     * 2. 只有在获取时间失败时才检测网络连接
     * 3. 增加更长的超时时间以应对获取时间的延迟
     */
    private fun setTimezoneWithWifiPriority() {
        showStatus("正在获取网络时间...")
        
        lifecycleScope.launch {
            try {
                // 尝试获取网络时间（增加超时时间）
                val networkTime = getNetworkTimeWithExtendedTimeout()
                
                withContext(Dispatchers.Main) {
                    if (networkTime != null) {
                        // 成功获取网络时间，设置时区
                        val success = setSystemTimezone(selectedTimezoneId)
                        if (success) {
                            val currentTimezoneName = getCurrentTimezoneDisplayName()
                            showStatus("时区设置成功！当前时区：${currentTimezoneName}")
                            Toast.makeText(this@MainActivity, "时区已自动设置为：${currentTimezoneName}", Toast.LENGTH_LONG).show()
                            hideConnectionOptions() // 设置成功后隐藏重试按钮
                        } else {
                            showStatus("时区设置失败，请检查权限")
                            Toast.makeText(this@MainActivity, "时区设置失败，请检查权限", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        // 获取网络时间失败，检查网络连接状态
                        handleNetworkTimeFailure()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showStatus("设置失败: ${e.message}")
                    Toast.makeText(this@MainActivity, "设置失败: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    
    /**
     * 带扩展超时的网络时间获取
     */
    private suspend fun getNetworkTimeWithExtendedTimeout(): Date? = withContext(Dispatchers.IO) {
        try {
            withContext(Dispatchers.Main) {
                showStatus("正在获取网络时间，请稍候...")
            }
            
            // 增加超时时间以应对网络延迟
            val url = URL("http://worldtimeapi.org/api/timezone/UTC")
            val connection = url.openConnection()
            connection.connectTimeout = 15000 // 增加到15秒
            connection.readTimeout = 15000 // 增加到15秒
            connection.connect()
            
            // 简单的备选方案：使用系统当前时间
            Date()
        } catch (e: IOException) {
            null
        }
    }
    
    /**
     * 处理网络时间获取失败的情况
     */
    private fun handleNetworkTimeFailure() {
        // 现在才检查网络连接状态
        val isNetworkConnected = isNetworkConnected()
        
        if (isNetworkConnected) {
            // 网络已连接但获取时间失败，可能是网络延迟或服务问题
            showStatus("网络已连接但获取时间失败，尝试使用本地时间设置时区")
            
            // 使用本地时间设置时区
            val success = setSystemTimezone(selectedTimezoneId)
            if (success) {
                val currentTimezoneName = getCurrentTimezoneDisplayName()
                showStatus("使用本地时间设置成功！当前时区：${currentTimezoneName}")
                Toast.makeText(this, "时区设置成功（使用本地时间）", Toast.LENGTH_LONG).show()
                hideConnectionOptions()
            } else {
                showStatus("时区设置失败，请检查权限")
                Toast.makeText(this, "时区设置失败", Toast.LENGTH_LONG).show()
            }
        } else {
            // 网络未连接
            showStatus("网络连接不可用，请检查网络设置")
            Toast.makeText(this, "网络连接不可用，请检查网络设置", Toast.LENGTH_LONG).show()
            showConnectionOptions()
        }
    }
    
    /**
     * 检查网络连接状态（不仅仅是WiFi）
     */
    private fun isNetworkConnected(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
               networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
    
    // 时区数据类
    data class TimeZoneItem(
        val displayName: String,
        val timezoneId: String
    )
}