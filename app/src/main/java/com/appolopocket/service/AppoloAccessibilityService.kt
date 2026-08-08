package com.appolopocket.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import kotlinx.coroutines.*

class AppoloAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var currentPackage: String? = null
    
    companion object {
        var instance: AppoloAccessibilityService? = null
            private set
        
        const val ACTION_LAUNCH_APP = "com.appolopocket.ACTION_LAUNCH_APP"
        const val ACTION_CLICK = "com.appolopocket.ACTION_CLICK"
        const val ACTION_TYPE_TEXT = "com.appolopocket.ACTION_TYPE_TEXT"
        const val ACTION_SCROLL = "com.appolopocket.ACTION_SCROLL"
        
        const val EXTRA_PACKAGE = "package"
        const val EXTRA_TEXT = "text"
        const val EXTRA_RESOURCE_ID = "resource_id"
        const val EXTRA_NODE_TEXT = "node_text"
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        serviceScope.cancel()
    }

    override fun onServiceConnected() {
        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or 
                         AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS or
                    AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                    AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
            notificationTimeout = 100
        }
        serviceInfo = info
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return
        
        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED,
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                currentPackage = event.packageName?.toString()
            }
        }
    }

    override fun onInterrupt() {
        // Service interrupted
    }

    fun handleIntent(intent: Intent) {
        intent ?: return
        
        when (intent.action) {
            ACTION_LAUNCH_APP -> {
                val packageName = intent.getStringExtra(EXTRA_PACKAGE)
                if (packageName != null) {
                    launchApp(packageName)
                }
            }
            ACTION_CLICK -> {
                val nodeText = intent.getStringExtra(EXTRA_NODE_TEXT)
                val resourceId = intent.getStringExtra(EXTRA_RESOURCE_ID)
                if (nodeText != null) {
                    clickByText(nodeText)
                } else if (resourceId != null) {
                    clickByResourceId(resourceId)
                }
            }
            ACTION_TYPE_TEXT -> {
                val text = intent.getStringExtra(EXTRA_TEXT)
                val resourceId = intent.getStringExtra(EXTRA_RESOURCE_ID)
                if (text != null && resourceId != null) {
                    typeText(resourceId, text)
                }
            }
            ACTION_SCROLL -> {
                val resourceId = intent.getStringExtra(EXTRA_RESOURCE_ID)
                val forward = intent.getBooleanExtra("forward", true)
                if (resourceId != null) {
                    scroll(resourceId, forward)
                }
            }
        }
    }

    private fun launchApp(packageName: String) {
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    private fun clickByText(text: String) {
        serviceScope.launch {
            findAndClick(textMatcher = { node ->
                node.text?.contains(text, ignoreCase = true) == true
            })
        }
    }

    private fun clickByResourceId(resourceId: String) {
        serviceScope.launch {
            val rootNode = rootInActiveWindow ?: return@launch
            try {
                val node = rootNode.findAccessibilityNodeInfosByViewId(resourceId)
                    .firstOrNull()
                node?.let {
                    it.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    it.recycle()
                }
            } finally {
                rootNode.recycle()
            }
        }
    }

    private fun typeText(resourceId: String, text: String) {
        serviceScope.launch {
            val rootNode = rootInActiveWindow ?: return@launch
            try {
                val node = rootNode.findAccessibilityNodeInfosByViewId(resourceId)
                    .firstOrNull()
                node?.let {
                    it.text = text
                    it.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT)
                    it.recycle()
                }
            } finally {
                rootNode.recycle()
            }
        }
    }

    private fun scroll(resourceId: String, forward: Boolean) {
        serviceScope.launch {
            val rootNode = rootInActiveWindow ?: return@launch
            try {
                val node = rootNode.findAccessibilityNodeInfosByViewId(resourceId)
                    .firstOrNull()
                node?.let {
                    val action = if (forward) {
                        AccessibilityNodeInfo.ACTION_SCROLL_FORWARD
                    } else {
                        AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD
                    }
                    it.performAction(action)
                    it.recycle()
                }
            } finally {
                rootNode.recycle()
            }
        }
    }

    private suspend fun findAndClick(
        textMatcher: (AccessibilityNodeInfo) -> Boolean,
        timeout: Long = 5000L
    ) {
        val startTime = System.currentTimeMillis()
        
        while (System.currentTimeMillis() - startTime < timeout) {
            val rootNode = rootInActiveWindow ?: run {
                delay(100)
                return
            }
            
            try {
                val nodes = rootNode.findAccessibilityNodeInfosByText(".*")
                val targetNode = nodes.find(textMatcher)
                
                if (targetNode != null) {
                    // Try to click the node or its parent
                    var clickableNode: AccessibilityNodeInfo? = targetNode
                    while (clickableNode != null && !clickableNode.isClickable) {
                        clickableNode = clickableNode.parent
                    }
                    
                    clickableNode?.let {
                        it.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        it.recycle()
                    }
                    targetNode.recycle()
                    nodes.forEach { it.recycle() }
                    return
                }
                
                nodes.forEach { it.recycle() }
            } finally {
                rootNode.recycle()
            }
            
            delay(100)
        }
    }

    fun getScreenContent(): String {
        val rootNode = rootInActiveWindow ?: return ""
        return try {
            buildString {
                fun traverse(node: AccessibilityNodeInfo, depth: Int = 0) {
                    val indent = "  ".repeat(depth)
                    val text = node.text?.toString() ?: ""
                    val contentDesc = node.contentDescription?.toString() ?: ""
                    
                    if (text.isNotEmpty() || contentDesc.isNotEmpty()) {
                        appendLine("$indent${node.className}: ${text.ifEmpty { contentDesc }}")
                    }
                    
                    for (i in 0 until node.childCount) {
                        node.getChild(i)?.let { child ->
                            traverse(child, depth + 1)
                            child.recycle()
                        }
                    }
                }
                traverse(rootNode)
            }
        } finally {
            rootNode.recycle()
        }
    }

    fun getCurrentPackage(): String? = currentPackage

    fun isAppInstalled(packageName: String): Boolean {
        return try {
            packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: Exception) {
            false
        }
    }
}
