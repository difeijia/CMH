package com.neuedu.cmh.entity;

import java.util.Date;

public class Dept {
    private String dept_id;
    private String tenant_id;
    private String parent_id;
    private String dept_name;
    private String leader;
    private String email;
    private String contact_num;
    private int sort_order;
    private int status;
    private Date create_time;
    private Date update_time;
    private int is_deleted;

    // 构造函数
    public Dept(String dept_id, String tenant_id, String parent_id, String dept_name,
                String leader, String email, String contact_num, int sort_order,
                int status, Date create_time, Date update_time, int is_deleted) {
        this.dept_id = dept_id;
        this.tenant_id = tenant_id;
        this.parent_id = parent_id;
        this.dept_name = dept_name;
        this.leader = leader;
        this.email = email;
        this.contact_num = contact_num;
        this.sort_order = sort_order;
        this.status = status;
        this.create_time = create_time;
        this.update_time = update_time;
        this.is_deleted = is_deleted;
    }

    // Getter 和 Setter 方法
    public String getDept_id() {
        return dept_id;
    }

    public void setDept_id(String dept_id) {
        this.dept_id = dept_id;
    }

    public String getTenant_id() {
        return tenant_id;
    }

    public void setTenant_id(String tenant_id) {
        this.tenant_id = tenant_id;
    }

    public String getParent_id() {
        return parent_id;
    }

    public void setParent_id(String parent_id) {
        this.parent_id = parent_id;
    }

    public String getDept_name() {
        return dept_name;
    }

    public void setDept_name(String dept_name) {
        this.dept_name = dept_name;
    }

    public String getLeader() {
        return leader;
    }

    public void setLeader(String leader) {
        this.leader = leader;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContact_num() {
        return contact_num;
    }

    public void setContact_num(String contact_num) {
        this.contact_num = contact_num;
    }

    public int getSort_order() {
        return sort_order;
    }

    public void setSort_order(int sort_order) {
        this.sort_order = sort_order;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Date getCreate_time() {
        return create_time;
    }

    public void setCreate_time(Date create_time) {
        this.create_time = create_time;
    }

    public Date getUpdate_time() {
        return update_time;
    }

    public void setUpdate_time(Date update_time) {
        this.update_time = update_time;
    }

    public int getIs_deleted() {
        return is_deleted;
    }

    public void setIs_deleted(int is_deleted) {
        this.is_deleted = is_deleted;
    }

    // toString 方法
    @Override
    public String toString() {
        return "Dept{" +
                "dept_id='" + dept_id + '\'' +
                ", tenant_id='" + tenant_id + '\'' +
                ", parent_id='" + parent_id + '\'' +
                ", dept_name='" + dept_name + '\'' +
                ", leader='" + leader + '\'' +
                ", email='" + email + '\'' +
                ", contact_num='" + contact_num + '\'' +
                ", sort_order=" + sort_order +
                ", status=" + status +
                ", create_time=" + create_time +
                ", update_time=" + update_time +
                ", is_deleted=" + is_deleted +
                '}';
    }
}