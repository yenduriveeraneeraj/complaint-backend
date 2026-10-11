package com.example.complaintmanagement.controller;

import com.example.complaintmanagement.dto.PublicComplaintTrackResponse;
import com.example.complaintmanagement.service.ComplaintService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/complaints")
@RequiredArgsConstructor
public class PublicComplaintController {

    private final ComplaintService complaintService;

    @GetMapping("/track/{complaintNumber}")
    public ResponseEntity<PublicComplaintTrackResponse> trackComplaint(@PathVariable String complaintNumber) {
        PublicComplaintTrackResponse response = complaintService.getPublicComplaintTrack(complaintNumber);
        return ResponseEntity.ok(response);
    }
}
