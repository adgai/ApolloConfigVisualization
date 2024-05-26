package com.adgainai.apolloconfigvisualization.action

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.project.Project
import com.intellij.util.Alarm

fun showNotification(project: Project, content: String) {
    val notification = Notification(
        "ApolloConfigView",  // Notification group ID
        "加载Apollo配置",                     // Title
        content,                         // Content
        NotificationType.INFORMATION     // Type of notification
    )
    Notifications.Bus.notify(notification, project)

    // Show the notification as a balloon
    val balloon = notification.balloon

    // Set a timeout to hide the balloon
    Alarm().addRequest({ balloon?.hide() }, 5000)
}
