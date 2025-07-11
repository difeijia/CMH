package com.neuedu.cmh.entity;

import java.util.Date;

public class User {
    private String user_id;
    private String user_name;
    private String user_nickname;
    private String dept_id;
    private String tenant_id;
    private String phonenum;
    private String mailbox;
    private String user_gender;
    private String user_job_id;
    private Date createdate;
    private String user_comment;
    private String user_pwd;
    private String user_role;
    private String user_state;

    public User() {
    }

    // 构造函数
    public User(String user_id, String user_name, String user_nickname, String dept_id,
                String tenant_id, String phonenum, String mailbox, String user_gender, String user_job_id,
                Date createdate, String user_comment, String user_pwd ,String user_role,String user_state) {
        this.user_id = user_id;
        this.user_name = user_name;
        this.user_nickname = user_nickname;
        this.dept_id = dept_id;
        this.tenant_id = tenant_id;
        this.phonenum = phonenum;
        this.mailbox = mailbox;
        this.user_gender = user_gender;
        this.user_job_id = user_job_id;
        this.createdate = createdate;
        this.user_comment = user_comment;
        this.user_state = user_state;
        this.user_pwd = user_pwd;
        this.user_role = user_role;

    }

    // Getter 和 Setter 方法
    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getUser_nickname() {
        return user_nickname;
    }

    public void setUser_nickname(String user_nickname) {
        this.user_nickname = user_nickname;
    }

    public String getDept_id() {
        return dept_id;
    }

    public void setDept_id(String dept_id) {
        this.dept_id = dept_id;
    }

    public String getPhonenum() {
        return phonenum;
    }

    public void setPhonenum(String phonenum) {
        this.phonenum = phonenum;
    }

    public String getMailbox() {
        return mailbox;
    }

    public void setMailbox(String mailbox) {
        this.mailbox = mailbox;
    }

    public String getUser_gender() {
        return user_gender;
    }

    public void setUser_gender(String user_gender) {
        this.user_gender = user_gender;
    }

    public String getUser_job_id() {
        return user_job_id;
    }

    public void setUser_job_id(String user_job_id) {
        this.user_job_id = user_job_id;
    }

    public Date getCreatedate() {
        return createdate;
    }

    public void setCreatedate(Date createdate) {
        this.createdate = createdate;
    }

    public String getUser_comment() {
        return user_comment;
    }

    public void setUser_comment(String user_comment) {
        this.user_comment = user_comment;
    }

    public String getUser_state() {
        return user_state;
    }

    public void setUser_state(String user_state) {
        this.user_state = user_state;
    }

    public String getTenant_id() {
        return tenant_id;
    }

    public void setTenant_id(String tenant_id) {
        this.tenant_id = tenant_id;
    }

    public String getUser_pwd() {
        return user_pwd;
    }

    public void setUser_pwd(String user_pwd) {
        this.user_pwd = user_pwd;
    }

    public String getUser_role() {
        return user_role;
    }

    public void setUser_role(String user_role) {
        this.user_role = user_role;
    }

    // toString 方法
    @Override
    public String toString() {
        return "User{" +
                "user_id='" + user_id + '\'' +
                ", user_name='" + user_name + '\'' +
                ", user_nickname='" + user_nickname + '\'' +
                ", dept_id='" + dept_id + '\'' +
                ", phonenum='" + phonenum + '\'' +
                ", mailbox='" + mailbox + '\'' +
                ", user_gender=" + user_gender +
                ", user_job_id='" + user_job_id + '\'' +
                ", createdate=" + createdate +
                ", user_comment='" + user_comment + '\'' +
                ", user_state=" + user_state +'\'' +
                ", user_pwd=" + user_pwd +'\'' +
                ", user_role=" + user_role +'\'' +
                ", tenant_id=" + tenant_id +
                '}';
    }
}