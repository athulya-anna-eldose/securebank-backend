
package com.securebank.controller;

import com.securebank.dto.ProfileResponse;
import com.securebank.dto.ProfileUpdateRequest;
import com.securebank.service.ProfileService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@CrossOrigin(origins = "http://localhost:5173")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    public ResponseEntity<ProfileResponse> getProfile(
            @AuthenticationPrincipal String email) {

        return ResponseEntity.ok(
                profileService.getProfile(email)
        );
    }

    @PutMapping
    public ResponseEntity<ProfileResponse> updateProfile(
            @AuthenticationPrincipal String email,
            @Valid @RequestBody ProfileUpdateRequest request) {

        return ResponseEntity.ok(
                profileService.updateProfile(email, request)
        );
    }
}
