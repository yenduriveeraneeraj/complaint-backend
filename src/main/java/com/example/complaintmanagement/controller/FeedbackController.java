package com.example.complaintmanagement.controller;

import com.example.complaintmanagement.dto.*;
import com.example.complaintmanagement.entity.User;
import com.example.complaintmanagement.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Feedback controller — users submit feedback for resolved complaints */
@RestController
@RequestMapping("/api/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<FeedbackResponse> submitFeedback(
            @Valid @RequestBody FeedbackRequest request, Authentication auth) {
        User user = userService.getUserByEmail(auth.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(feedbackService.submitFeedback(request, user.getId()));
    }
}
