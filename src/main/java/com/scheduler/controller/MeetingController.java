package com.scheduler.controller;

import com.scheduler.model.Meeting;
import com.scheduler.model.User;
import com.scheduler.service.MeetingService;
import com.scheduler.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Controller
public class MeetingController {

    private final MeetingService meetingService;
    private final UserService userService;

    public MeetingController(MeetingService meetingService, UserService userService) {
        this.meetingService = meetingService;
        this.userService = userService;
    }

    private User getCurrentUser(Authentication auth) {
        return userService.findByEmail(auth.getName()).orElseThrow();
    }

    private void addCommonAttributes(Model model, User user) {
        model.addAttribute("user", user);
        model.addAttribute("meetings", meetingService.getScheduledMeetings(user));
        model.addAttribute("cancelledMeetings", meetingService.getCancelledMeetings(user));
        model.addAttribute("notifications", meetingService.getNotifications(user));
        model.addAttribute("stats", meetingService.getStats(user));
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication auth) {
        User user = getCurrentUser(auth);
        addCommonAttributes(model, user);
        return "dashboard";
    }

    @PostMapping("/meetings/add")
    public String addMeeting(@ModelAttribute Meeting meeting,
                              Model model,
                              Authentication auth) {
        User user = getCurrentUser(auth);
        meeting.setUser(user);

        if (meetingService.hasConflict(meeting, user)) {
            model.addAttribute("error", "Time slot already booked!");
            addCommonAttributes(model, user);
            return "dashboard";
        }

        meetingService.saveMeeting(meeting);
        return "redirect:/dashboard";
    }

    @PostMapping("/meetings/edit/{id}")
    public String editMeeting(@PathVariable Long id,
                               @RequestParam String title,
                               @RequestParam LocalDate date,
                               @RequestParam LocalTime startTime,
                               @RequestParam int durationMinutes,
                               Model model,
                               Authentication auth) {
        User user = getCurrentUser(auth);

        Meeting candidate = new Meeting();
        candidate.setDate(date);
        candidate.setStartTime(startTime);
        candidate.setDurationMinutes(durationMinutes);

        if (meetingService.hasConflict(candidate, user, id)) {
            model.addAttribute("error", "Time slot already booked!");
            addCommonAttributes(model, user);
            return "dashboard";
        }

        meetingService.updateMeeting(id, title, date, startTime, durationMinutes, user);
        return "redirect:/dashboard";
    }

    @PostMapping("/meetings/cancel/{id}")
    public String cancelMeeting(@PathVariable Long id,
                                 @RequestParam(required = false) String reason,
                                 Authentication auth) {
        User user = getCurrentUser(auth);
        meetingService.cancelMeeting(id, reason, user);
        return "redirect:/dashboard";
    }

    @ExceptionHandler(SecurityException.class)
    public String handleUnauthorizedMeetingAccess(SecurityException ex,
                                                   Model model,
                                                   Authentication auth) {
        User user = getCurrentUser(auth);
        model.addAttribute("error", "You're not authorized to modify that meeting.");
        addCommonAttributes(model, user);
        return "dashboard";
    }
}