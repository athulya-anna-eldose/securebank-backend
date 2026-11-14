package com.securebank.dto;

public class ProfileResponse {

    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private String accountNumber;

    public ProfileResponse(Long id, String fullName, String email,
                           String phone, String address,
                           String accountNumber) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.accountNumber = accountNumber;
    }

    public Long getId() { return id; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getAddress() { return address; }
    public String getAccountNumber() { return accountNumber; }
}