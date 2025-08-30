package com.timezoneapp

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/**
 * 相册清理功能测试
 */
@RunWith(AndroidJUnit4::class)
class GalleryCleanupTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun testMediaFileDetection() {
        // 测试图片文件检测
        assertTrue("JPG文件应该被识别为媒体文件", isMediaFile("test.jpg"))
        assertTrue("JPEG文件应该被识别为媒体文件", isMediaFile("test.jpeg"))
        assertTrue("PNG文件应该被识别为媒体文件", isMediaFile("test.png"))
        assertTrue("GIF文件应该被识别为媒体文件", isMediaFile("test.gif"))
        assertTrue("WEBP文件应该被识别为媒体文件", isMediaFile("test.webp"))
        assertTrue("HEIC文件应该被识别为媒体文件", isMediaFile("test.heic"))

        // 测试视频文件检测
        assertTrue("MP4文件应该被识别为媒体文件", isMediaFile("test.mp4"))
        assertTrue("AVI文件应该被识别为媒体文件", isMediaFile("test.avi"))
        assertTrue("MOV文件应该被识别为媒体文件", isMediaFile("test.mov"))
        assertTrue("MKV文件应该被识别为媒体文件", isMediaFile("test.mkv"))

        // 测试非媒体文件
        assertFalse("TXT文件不应该被识别为媒体文件", isMediaFile("test.txt"))
        assertFalse("PDF文件不应该被识别为媒体文件", isMediaFile("test.pdf"))
        assertFalse("DOC文件不应该被识别为媒体文件", isMediaFile("test.doc"))
    }

    @Test
    fun testFileExtensionCaseInsensitive() {
        // 测试大小写不敏感
        assertTrue("大写JPG应该被识别", isMediaFile("test.JPG"))
        assertTrue("混合大小写Jpg应该被识别", isMediaFile("test.Jpg"))
        assertTrue("大写MP4应该被识别", isMediaFile("test.MP4"))
        assertTrue("混合大小写Mp4应该被识别", isMediaFile("test.Mp4"))
    }

    @Test
    fun testPermissionConstants() {
        // 测试权限常量
        assertNotNull("READ_EXTERNAL_STORAGE权限常量不能为null", 
            android.Manifest.permission.READ_EXTERNAL_STORAGE)
        assertNotNull("WRITE_EXTERNAL_STORAGE权限常量不能为null", 
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    @Test
    fun testStringResources() {
        // 测试相册清理相关的字符串资源
        val galleryCleanup = context.getString(R.string.gallery_cleanup)
        val cleanupGallery = context.getString(R.string.cleanup_gallery)
        val confirmCleanup = context.getString(R.string.confirm_gallery_cleanup)

        assertNotNull("相册清理标题不能为null", galleryCleanup)
        assertNotNull("清理按钮文本不能为null", cleanupGallery)
        assertNotNull("确认清理文本不能为null", confirmCleanup)

        assertTrue("相册清理标题不能为空", galleryCleanup.isNotEmpty())
        assertTrue("清理按钮文本不能为空", cleanupGallery.isNotEmpty())
        assertTrue("确认清理文本不能为空", confirmCleanup.isNotEmpty())
    }

    @Test
    fun testWarningMessages() {
        // 测试警告信息
        val warningMessage = context.getString(R.string.gallery_cleanup_warning)
        val storagePermission = context.getString(R.string.storage_permission_required)

        assertNotNull("警告信息不能为null", warningMessage)
        assertNotNull("权限提示不能为null", storagePermission)

        assertTrue("警告信息应该包含警告内容", warningMessage.contains("删除"))
        assertTrue("警告信息应该包含不可恢复提示", warningMessage.contains("无法恢复"))
    }

    /**
     * 测试用的媒体文件检测方法（复制自MainActivity的逻辑）
     */
    private fun isMediaFile(fileName: String): Boolean {
        val lowerFileName = fileName.lowercase()
        val imageExtensions = listOf(".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp", ".heic", ".heif")
        val videoExtensions = listOf(".mp4", ".avi", ".mov", ".wmv", ".flv", ".mkv", ".webm", ".3gp")
        
        return imageExtensions.any { lowerFileName.endsWith(it) } || 
               videoExtensions.any { lowerFileName.endsWith(it) }
    }
}