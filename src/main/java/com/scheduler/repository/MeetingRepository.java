package com.scheduler.repository;

import com.scheduler.model.Meeting;
import com.scheduler.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface MeetingRepository extends JpaRepository<Meeting, Long> {
    List<Meeting> findByUser(User user);
    List<Meeting> findByUserAndDate(User user, LocalDate date);
    List<Meeting> findByUserAndStatus(User user, Meeting.Status status);
    List<Meeting> findByUserAndDateAndStatus(User user, LocalDate date, Meeting.Status status);
}