
package com.securebank.service;

import com.securebank.dto.ProfileResponse;
import com.securebank.dto.ProfileUpdateRequest;

public interface ProfileService {

    ProfileResponse getProfile(String email);

    ProfileResponse updateProfile(
            String email,
            ProfileUpdateRequest request);
}
