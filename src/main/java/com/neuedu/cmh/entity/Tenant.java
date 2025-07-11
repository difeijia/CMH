package com.neuedu.cmh.entity;

public class Tenant {
    private String tenant_id;
    private String tenant_logo;
    private String tenant_name;
    private String tenant_contact;
    private String contact_phonenum;
    private String tenant_comment;
    private String tenant_pwd;
    private String admin_id;
    private String tenant_email;
    private String tenant_emailCode; // 用户的邮箱验证码
    private String role;
    private String content;

    // 构造函数
    public Tenant(String tenant_id, String tenant_logo, String tenant_name, String tenant_contact,
                  String contact_phonenum,  String tenant_comment,
                  String tenant_pwd, String admin_id, String tenant_email, String tenant_emailCode, String role, String content) {
        this.tenant_id = tenant_id;
        this.tenant_logo = tenant_logo;
        this.tenant_name = tenant_name;
        this.tenant_contact = tenant_contact;
        this.contact_phonenum = contact_phonenum;
        this.tenant_comment = tenant_comment;
        this.tenant_pwd = tenant_pwd;
        this.admin_id = admin_id;
        this.tenant_email = tenant_email;
        this.tenant_emailCode = tenant_emailCode;
        this.role = role;
        this.content = content;
    }

    public Tenant() {
    }

    // Getter 和 Setter 方法
    public String getTenant_id() {
        return tenant_id;
    }

    public void setTenant_id(String tenant_id) {
        this.tenant_id = tenant_id;
    }

    public String getTenant_logo() {
        return tenant_logo;
    }

    public void setTenant_logo(String tenant_logo) {
        this.tenant_logo = tenant_logo;
    }

    public String getTenant_name() {
        return tenant_name;
    }

    public void setTenant_name(String tenant_name) {
        this.tenant_name = tenant_name;
    }

    public String getTenant_contact() {
        return tenant_contact;
    }

    public void setTenant_contact(String tenant_contact) {
        this.tenant_contact = tenant_contact;
    }

    public String getContact_phonenum() {
        return contact_phonenum;
    }

    public void setContact_phonenum(String contact_phonenum) {
        this.contact_phonenum = contact_phonenum;
    }

    public String getTenant_comment() {
        return tenant_comment;
    }

    public void setTenant_comment(String tenant_comment) {
        this.tenant_comment = tenant_comment;
    }

    public String getTenant_pwd() {
        return tenant_pwd;
    }

    public void setTenant_pwd(String tenant_pwd) {
        this.tenant_pwd = tenant_pwd;
    }

    public String getAdmin_id() {
        return admin_id;
    }

    public void setAdmin_id(String admin_id) {
        this.admin_id = admin_id;
    }

    public String getTenant_email() {
        return tenant_email;
    }

    public void setTenant_email(String tenant_email) {
        this.tenant_email = tenant_email;
    }

    public String getTenant_emailCode() {
        return tenant_emailCode;
    }
    public void setTenant_emailCode(String tenant_emailCode) {
        this.tenant_emailCode = tenant_emailCode;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }


    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
    // toString 方法

    @Override
    public String toString() {
        return "Tenant{" +
                "tenant_id='" + tenant_id + '\'' +
                ", tenant_logo='" + tenant_logo + '\'' +
                ", tenant_name='" + tenant_name + '\'' +
                ", tenant_contact='" + tenant_contact + '\'' +
                ", contact_phonenum='" + contact_phonenum + '\'' +
                ", tenant_comment='" + tenant_comment + '\'' +
                ", tenant_pwd='" + tenant_pwd + '\'' +
                ", admin_id='" + admin_id + '\'' +
                ", tenant_email='" + tenant_email + '\'' +
                ", tenant_emailCode='" + tenant_emailCode + '\'' +
                ", role='" + role + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}