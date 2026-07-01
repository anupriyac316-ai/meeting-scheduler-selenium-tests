package com.scheduler.service;

import com.scheduler.model.Meeting;
import com.scheduler.model.User;
import com.scheduler.repository.MeetingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MeetingServiceTest {

    @Mock
    private MeetingRepository repo;

    @InjectMocks
    private MeetingService meetingService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("raj@example.com");
    }

    private Meeting buildMeeting(Long id, LocalDate date, LocalTime start, int duration) {
        Meeting m = new Meeting();
        m.setId(id);
        m.setDate(date);
        m.setStartTime(start);
        m.setDurationMinutes(duration);
        m.setUser(user);
        m.setStatus(Meeting.Status.SCHEDULED);
        return m;
    }

    @Test
    void hasConflict_returnsTrue_whenTimesOverlap() {
        Meeting existing = buildMeeting(1L, LocalDate.of(2026, 7, 10), LocalTime.of(10, 0), 30);
        when(repo.findByUserAndDateAndStatus(user, existing.getDate(), Meeting.Status.SCHEDULED))
                .thenReturn(List.of(existing));

        Meeting newMeeting = buildMeeting(null, existing.getDate(), LocalTime.of(10, 15), 30);

        assertTrue(meetingService.hasConflict(newMeeting, user));
    }

    @Test
    void hasConflict_returnsFalse_whenTimesDontOverlap() {
        Meeting existing = buildMeeting(1L, LocalDate.of(2026, 7, 10), LocalTime.of(10, 0), 30);
        when(repo.findByUserAndDateAndStatus(user, existing.getDate(), Meeting.Status.SCHEDULED))
                .thenReturn(List.of(existing));

        Meeting newMeeting = buildMeeting(null, existing.getDate(), LocalTime.of(11, 0), 30);

        assertFalse(meetingService.hasConflict(newMeeting, user));
    }

    @Test
    void hasConflict_withExcludeId_ignoresOwnMeetingWhenEditing() {
        Meeting existing = buildMeeting(5L, LocalDate.of(2026, 7, 10), LocalTime.of(10, 0), 30);
        when(repo.findByUserAndDateAndStatus(user, existing.getDate(), Meeting.Status.SCHEDULED))
                .thenReturn(List.of(existing));

        // Editing meeting #5 to the same slot should NOT be flagged as a conflict with itself
        Meeting candidate = buildMeeting(5L, existing.getDate(), LocalTime.of(10, 0), 30);

        assertFalse(meetingService.hasConflict(candidate, user, 5L));
    }

    @Test
    void hasConflict_withExcludeId_stillDetectsConflictWithOtherMeetings() {
        Meeting other = buildMeeting(9L, LocalDate.of(2026, 7, 10), LocalTime.of(10, 0), 30);
        when(repo.findByUserAndDateAndStatus(user, other.getDate(), Meeting.Status.SCHEDULED))
                .thenReturn(List.of(other));

        // Editing meeting #5 into meeting #9's slot should still conflict
        Meeting candidate = buildMeeting(5L, other.getDate(), LocalTime.of(10, 15), 30);

        assertTrue(meetingService.hasConflict(candidate, user, 5L));
    }

    @Test
    void updateMeeting_updatesFieldsAndSaves() {
        Meeting existing = buildMeeting(1L, LocalDate.of(2026, 7, 10), LocalTime.of(10, 0), 30);
        when(repo.findById(1L)).thenReturn(Optional.of(existing));
        when(repo.save(any(Meeting.class))).thenAnswer(inv -> inv.getArgument(0));

        meetingService.updateMeeting(1L, "New Title", LocalDate.of(2026, 7, 12), LocalTime.of(14, 0), 45, user);

        assertEquals("New Title", existing.getTitle());
        assertEquals(LocalDate.of(2026, 7, 12), existing.getDate());
        assertEquals(LocalTime.of(14, 0), existing.getStartTime());
        assertEquals(45, existing.getDurationMinutes());
        verify(repo).save(existing);
    }

    @Test
    void updateMeeting_throwsSecurityException_whenNotOwner() {
        Meeting existing = buildMeeting(1L, LocalDate.of(2026, 7, 10), LocalTime.of(10, 0), 30);
        User otherUser = new User();
        otherUser.setId(99L);
        when(repo.findById(1L)).thenReturn(Optional.of(existing));

        assertThrows(SecurityException.class, () ->
                meetingService.updateMeeting(1L, "Hack", LocalDate.now(), LocalTime.NOON, 30, otherUser));

        verify(repo, never()).save(any());
    }

    @Test
    void cancelMeeting_setsCancelledStatusAndReason() {
        Meeting existing = buildMeeting(1L, LocalDate.of(2026, 7, 10), LocalTime.of(10, 0), 30);
        when(repo.findById(1L)).thenReturn(Optional.of(existing));
        when(repo.save(any(Meeting.class))).thenAnswer(inv -> inv.getArgument(0));

        meetingService.cancelMeeting(1L, "No longer needed", user);

        assertEquals(Meeting.Status.CANCELLED, existing.getStatus());
        assertEquals("No longer needed", existing.getCancelReason());
        assertNotNull(existing.getCancelledAt());
    }

    @Test
    void cancelMeeting_usesDefaultReason_whenBlank() {
        Meeting existing = buildMeeting(1L, LocalDate.of(2026, 7, 10), LocalTime.of(10, 0), 30);
        when(repo.findById(1L)).thenReturn(Optional.of(existing));
        when(repo.save(any(Meeting.class))).thenAnswer(inv -> inv.getArgument(0));

        meetingService.cancelMeeting(1L, "  ", user);

        assertEquals("No reason given", existing.getCancelReason());
    }

    @Test
    void getStats_calculatesAverageDurationCorrectly() {
        Meeting m1 = buildMeeting(1L, LocalDate.now(), LocalTime.NOON, 30);
        Meeting m2 = buildMeeting(2L, LocalDate.now(), LocalTime.NOON, 60);
        when(repo.findByUserAndStatus(user, Meeting.Status.SCHEDULED)).thenReturn(List.of(m1, m2));
        when(repo.findByUserAndStatus(user, Meeting.Status.CANCELLED)).thenReturn(List.of());

        MeetingService.MeetingStats stats = meetingService.getStats(user);

        assertEquals(2, stats.getTotalScheduled());
        assertEquals(0, stats.getTotalCancelled());
        assertEquals(45, stats.getAvgDurationMinutes());
    }
}