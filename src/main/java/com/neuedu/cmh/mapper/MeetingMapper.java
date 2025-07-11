package com.neuedu.cmh.mapper;

import org.apache.ibatis.annotations.*;
import com.neuedu.cmh.entity.Meeting;

import java.util.List;
import java.util.Map;

@Mapper
public interface MeetingMapper {
    @Select("<script>" +
            "SELECT * FROM meeting " +
            "<where>" +
            "   <if test='meetingName != null'>AND meeting_name LIKE CONCAT('%', #{meetingName}, '%')</if>" +
            "   <if test='creator != null'>AND creator LIKE CONCAT('%', #{creator}, '%')</if>" +
            "   <if test='startTime != null'>AND start_time &gt;= STR_TO_DATE(#{startTime}, '%Y-%m-%d %H:%i:%s')</if>" +
            "</where>" +
            "ORDER BY start_time DESC" +
            "</script>")
    List<Meeting> findMeetings(Map<String, Object> params);

    @Select("SELECT * FROM meeting WHERE meeting_id = #{id}")
    Meeting findById(String id);

    @Insert("INSERT INTO meeting (meeting_id, meeting_name, meeting_cover, meeting_content, " +
            "creator, start_time, end_time, tenant_id) " +
            "VALUES (#{meetingId}, #{meetingName}, #{meetingCover}, #{meetingContent}, " +
            "#{creator}, #{startTime}, #{endTime}, #{tenantId})")
    int insert(Meeting meeting);

    @Update("UPDATE meeting SET " +
            "meeting_name = #{meetingName}, " +
            "meeting_cover = #{meetingCover}, " +
            "meeting_content = #{meetingContent}, " +
            "creator = #{creator}, " +
            "start_time = #{startTime}, " +
            "end_time = #{endTime} " +
            "WHERE meeting_id = #{meetingId}")
    int update(Meeting meeting);

    @Delete("DELETE FROM meeting WHERE meeting_id = #{id}")
    int delete(String id);
}