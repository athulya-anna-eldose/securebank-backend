
package com.securebank.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ProfileUpdateRequest {

    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 60, message = "Full name must be between 2 and 60 characters")
    @Pattern(
            regexp = "^[\\p{L}][\\p{L} .'-]*$",
            message = "Full name contains invalid characters"
    )
    private String fullName;

    @NotBlank(message = "Phone number is required")
    @Pattern(
            regexp = "^\\+?[0-9]{10,15}$",
            message = "Phone number must contain 10 to 15 digits, optionally starting with +"
    )
    private String phone;

    @Size(max = 250, message = "Address cannot exceed 250 characters")
    @Pattern(
            regexp = "^[^<>]*$",
            message = "Address must not contain HTML tags"
    )
    private String address;

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
