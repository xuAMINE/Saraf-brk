package com.saraf.security.admin;

import lombok.Data;

@Data
public class ApiResponse {

    private Boolean success;
    private String message;
    private String data;

    // Constructors
    public ApiResponse(Boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public ApiResponse(Boolean success, String message, String data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

}
