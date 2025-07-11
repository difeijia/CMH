package com.neuedu.cmh.dto;

public class AdminDTO {
    private String admin_id;
    private String admin_name;
    private String admin_pwd;
    private String admin_email;
    private String role;
    private String email_code;

    public AdminDTO(String admin_id, String admin_name, String admin_pwd, String admin_email, String role, String email_code) {
        this.admin_id = admin_id;
        this.admin_name = admin_name;
        this.admin_pwd = admin_pwd;
        this.admin_email = admin_email;
        this.role = role;
        this.email_code = email_code;
    }

    public String getAdmin_id() {
        return admin_id;
    }

    public void setAdmin_id(String admin_id) {
        this.admin_id = admin_id;
    }

    public String getAdmin_name() {
        return admin_name;
    }

    public void setAdmin_name(String admin_name) {
        this.admin_name = admin_name;
    }

    public String getAdmin_pwd() {
        return admin_pwd;
    }

    public void setAdmin_pwd(String admin_pwd) {
        this.admin_pwd = admin_pwd;
    }

    public String getAdmin_email() {
        return admin_email;
    }

    public void setAdmin_email(String admin_email) {
        this.admin_email = admin_email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getEmail_code() {
        return email_code;
    }

    public void setEmail_code(String email_code) {
        this.email_code = email_code;
    }
}
