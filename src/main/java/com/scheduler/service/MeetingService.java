package com.scheduler.service;

import com.scheduler.model.Meeting;
import com.scheduler.model.NotificationView;
import com.scheduler.model.User;
import com.scheduler.repository.MeetingRepository;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MeetingService {

    private final MeetingRepository repo;

    public MeetingService(MeetingRepository repo) {
        this.repo = repo;
    }

    public List<Meeting> getScheduledMeetings(User user) {
        return repo.findByUserAndStatus(user, Meeting.Status.SCHEDULED);
    }

    public List<Meeting> getCancelledMeetings(User user) {
        return repo.findByUserAndStatus(user, Meeting.Status.CANCELLED);
    }

    public boolean hasConflict(Meeting newMeeting, User user) {
        return hasConflict(newMeeting, user, null);
    }

    public boolean hasConflict(Meeting newMeeting, User user, Long excludeMeetingId) {
        List<Meeting> existing = repo.findByUserAndDateAndStatus(
                user, newMeeting.getDate(), Meeting.Status.SCHEDULED);

        LocalTime newStart = newMeeting.getStartTime();
        LocalTime newEnd = newStart.plusMinutes(newMeeting.getDurationMinutes());

        for (Meeting m : existing) {
            if (excludeMeetingId != null && m.getId().equals(excludeMeetingId)) {
                continue; // skip the meeting being edited
            }
            LocalTime existStart = m.getStartTime();
            LocalTime existEnd = existStart.plusMinutes(m.getDurationMinutes());
            if (newStart.isBefore(existEnd) && newEnd.isAfter(existStart)) {
                return true;
            }
        }
        return false;
    }

    public void saveMeeting(Meeting meeting) {
        meeting.setStatus(Meeting.Status.SCHEDULED);
        meeting.setCreatedAt(LocalDateTime.now());
        repo.save(meeting);
    }

    public Meeting getOwnedMeeting(Long id, User user) {
        Meeting meeting = repo.findById(id).orElseThrow();
        if (!meeting.getUser().getId().equals(user.getId())) {
            throw new SecurityException("Not authorized to modify this meeting");
        }
        return meeting;
    }

    public void updateMeeting(Long id, String title, LocalDate date, LocalTime startTime,
                               int durationMinutes, User user) {
        Meeting meeting = getOwnedMeeting(id, user);
        meeting.setTitle(title);
        meeting.setDate(date);
        meeting.setStartTime(startTime);
        meeting.setDurationMinutes(durationMinutes);
        repo.save(meeting);
    }

    public void cancelMeeting(Long id, String reason, User user) {
        Meeting meeting = getOwnedMeeting(id, user);
        meeting.setStatus(Meeting.Status.CANCELLED);
        meeting.setCancelReason((reason == null || reason.isBlank()) ? "No reason given" : reason);
        meeting.setCancelledAt(LocalDateTime.now());
        repo.save(meeting);
    }

    public List<NotificationView> getNotifications(User user) {
        List<NotificationView> notifications = new ArrayList<>();

        for (Meeting m : getScheduledMeetings(user)) {
            LocalDateTime ts = m.getCreatedAt() != null ? m.getCreatedAt() : LocalDateTime.now();
            notifications.add(new NotificationView(
                    "teal",
                    "Meeting confirmed",
                    "\"" + m.getTitle() + "\" was scheduled for " + m.getDate() + " at " + m.getStartTime() + ".",
                    formatTimeAgo(ts),
                    ts
            ));
        }

        for (Meeting m : getCancelledMeetings(user)) {
            LocalDateTime ts = m.getCancelledAt() != null ? m.getCancelledAt() : LocalDateTime.now();
            String reason = (m.getCancelReason() != null && !m.getCancelReason().isBlank())
                    ? m.getCancelReason() : "No reason given";
            notifications.add(new NotificationView(
                    "red",
                    "Meeting cancelled",
                    "\"" + m.getTitle() + "\" was cancelled. Reason: " + reason,
                    formatTimeAgo(ts),
                    ts
            ));
        }

        return notifications.stream()
                .sorted(Comparator.comparing(NotificationView::getTimestamp).reversed())
                .limit(10)
                .collect(Collectors.toList());
    }

    public MeetingStats getStats(User user) {
        List<Meeting> scheduled = getScheduledMeetings(user);
        List<Meeting> cancelled = getCancelledMeetings(user);

        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        long thisWeekCount = scheduled.stream()
                .filter(m -> !m.getDate().isBefore(weekStart) && !m.getDate().isAfter(weekEnd))
                .count();

        double avgDuration = scheduled.stream()
                .mapToInt(Meeting::getDurationMinutes)
                .average()
                .orElse(0);

        return new MeetingStats(scheduled.size(), cancelled.size(), thisWeekCount, Math.round(avgDuration));
    }

    private String formatTimeAgo(LocalDateTime ts) {
        Duration diff = Duration.between(ts, LocalDateTime.now());
        long minutes = diff.toMinutes();
        long hours = diff.toHours();
        long days = diff.toDays();

        if (minutes < 1) return "Just now";
        if (minutes < 60) return minutes + " min ago";
        if (hours < 24) return hours + " hour" + (hours == 1 ? "" : "s") + " ago";
        if (days == 1) return "Yesterday";
        if (days < 7) return days + " days ago";
        return ts.toLocalDate().toString();
    }

    public static class MeetingStats {
        private final int totalScheduled;
        private final int totalCancelled;
        private final long thisWeekCount;
        private final long avgDurationMinutes;

        public MeetingStats(int totalScheduled, int totalCancelled, long thisWeekCount, long avgDurationMinutes) {
            this.totalScheduled = totalScheduled;
            this.totalCancelled = totalCancelled;
            this.thisWeekCount = thisWeekCount;
            this.avgDurationMinutes = avgDurationMinutes;
        }

        public int getTotalScheduled() { return totalScheduled; }
        public int getTotalCancelled() { return totalCancelled; }
        public long getThisWeekCount() { return thisWeekCount; }
        public long getAvgDurationMinutes() { return avgDurationMinutes; }
    }
}