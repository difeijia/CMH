package com.neuedu.cmh.entity;

import java.util.Date;

public class News {
    private String news_id;
    private String news_title;
    private String news_image_path;
    private String news_content;
    private String author;
    private String news_summary;
    private String tenant_id;

    public News() {
    }

    public News(String news_id, String news_title, String news_image_path, String news_content, String author, String news_summary, String tenant_id, Date create_time) {
        this.news_id = news_id;
        this.news_title = news_title;
        this.news_image_path = news_image_path;
        this.news_content = news_content;
        this.author = author;
        this.news_summary = news_summary;
        this.tenant_id = tenant_id;
        this.create_time = create_time;
    }

    private Date create_time;

    public Date getCreate_time() {
        return create_time;
    }

    public void setCreate_time(Date create_time) {
        this.create_time = create_time;
    }

    // 构造函数
    public News(String news_id, String news_title, String news_image, String news_text,
                String author, String news_sum, String tenant_id) {
        this.news_id = news_id;
        this.news_title = news_title;
        this.news_image_path = news_image;
        this.news_content = news_text;
        this.author = author;
        this.news_summary = news_sum;
        this.tenant_id = tenant_id;
    }

    // Getter 和 Setter 方法
    public String getNews_id() {
        return news_id;
    }

    public void setNews_id(String news_id) {
        this.news_id = news_id;
    }

    public String getNews_title() {
        return news_title;
    }

    public void setNews_title(String news_title) {
        this.news_title = news_title;
    }

    public String getNews_image_path() {
        return news_image_path;
    }

    public void setNews_image_path(String news_image_path) {
        this.news_image_path = news_image_path;
    }

    public String getNews_content() {
        return news_content;
    }

    public void setNews_content(String news_content) {
        this.news_content = news_content;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getNews_summary() {
        return news_summary;
    }

    public void setNews_summary(String news_summary) {
        this.news_summary = news_summary;
    }

    public String getTenant_id() {
        return tenant_id;
    }

    public void setTenant_id(String tenant_id) {
        this.tenant_id = tenant_id;
    }

    // toString 方法
    @Override
    public String toString() {
        return "News{" +
                "news_id='" + news_id + '\'' +
                ", news_title='" + news_title + '\'' +
                ", news_image='" + news_image_path + '\'' +
                ", news_text='" + news_content + '\'' +
                ", author='" + author + '\'' +
                ", news_sum='" + news_summary + '\'' +
                ", tenant_id='" + tenant_id + '\'' +
                '}';
    }
}