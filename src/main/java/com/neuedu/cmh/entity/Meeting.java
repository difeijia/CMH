package com.neuedu.cmh.entity;

import lombok.Data;
import java.util.Date;

@Data
public class Meeting {
    private String meetingId;
    private String meetingName;
    private String meetingCover;
    private String meetingContent;
    private String creator;
    private Date startTime;
    private Date endTime;
    private String tenantId;

    public String getMeetingId() {
        return meetingId;
    }

    public void setMeetingId(String meetingId) {
        this.meetingId = meetingId;
    }

    public String getMeetingName() {
        return meetingName;
    }

    public void setMeetingName(String meetingName) {
        this.meetingName = meetingName;
    }

    public String getMeetingCover() {
        return meetingCover;
    }

    public void setMeetingCover(String meetingCover) {
        this.meetingCover = meetingCover;
    }

    public String getMeetingContent() {
        return meetingContent;
    }

    public void setMeetingContent(String meetingContent) {
        this.meetingContent = meetingContent;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    // 计算会议状态
    public String getStatus() {
        Date now = new Date();
        if (now.before(startTime)) return "未开始";
        if (now.after(endTime)) return "已结束";
        return "进行中";
    }
}