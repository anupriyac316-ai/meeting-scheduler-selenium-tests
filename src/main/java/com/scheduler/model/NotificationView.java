package com.scheduler.model;

import java.time.LocalDateTime;

public class NotificationView {

    private String iconColor;
    private String title;
    private String description;
    private String timeAgo;
    private LocalDateTime timestamp;

    public NotificationView(String iconColor, String title, String description,
                             String timeAgo, LocalDateTime timestamp) {
        this.iconColor = iconColor;
        this.title = title;
        this.description = description;
        this.timeAgo = timeAgo;
        this.timestamp = timestamp;
    }

    public String getIconColor() { return iconColor; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getTimeAgo() { return timeAgo; }
    public LocalDateTime getTimestamp() { return timestamp; }
}