package com.cart.auth_service.dto;

public class PasswordUpdateDto {
    private String oldPassword;
    private String newPassword;

    // Getters y Setters
    public String getOldPassword() {
        return oldPassword;
    }
    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }
    public String getNewPassword() {
        return newPassword;
    }
    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
