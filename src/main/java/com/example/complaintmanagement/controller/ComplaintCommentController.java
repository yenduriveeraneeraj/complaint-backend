package com.example.complaintmanagement.controller;

import com.example.complaintmanagement.dto.CommentRequest;
import com.example.complaintmanagement.dto.CommentResponse;
import com.example.complaintmanagement.security.UserPrincipal;
import com.example.complaintmanagement.service.ComplaintCommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/complaints/{complaintId}/comments")
@RequiredArgsConstructor
public class ComplaintCommentController {

    private final ComplaintCommentService commentService;

    @GetMapping
    public ResponseEntity<List<CommentResponse>> getComments(@PathVariable Long complaintId) {
        List<CommentResponse> comments = commentService.getComments(complaintId);
        return ResponseEntity.ok(comments);
    }

    @PostMapping
    public ResponseEntity<CommentResponse> addComment(
            @PathVariable Long complaintId,
            @Valid @RequestBody CommentRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        CommentResponse comment = commentService.addComment(complaintId, userPrincipal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(comment);
    }
}
