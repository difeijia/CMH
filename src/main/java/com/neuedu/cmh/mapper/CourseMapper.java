package com.neuedu.cmh.mapper;

import com.neuedu.cmh.entity.Course;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface CourseMapper {
    @Select("<script>" +
            "SELECT * FROM course " +
            "<where>" +
            "   <if test='courseName != null and courseName != \"\"'> AND course_name LIKE CONCAT('%', #{courseName}, '%')</if>" +
            "   <if test='courseOrder != null and courseOrder != \"\"'> AND course_order = #{courseOrder}</if>" +
            "</where>" +
            "ORDER BY course_order ASC " +
            "LIMIT #{offset}, #{pageSize}" +
            "</script>")
    List<Course> searchCourses(@Param("courseName") String courseName,
                               @Param("courseOrder") String courseOrder,
                               @Param("offset") int offset,
                               @Param("pageSize") int pageSize);

    @Select("<script>" +
            "SELECT COUNT(*) FROM course " +
            "<where>" +
            "   <if test='courseName != null and courseName != \"\"'> AND course_name LIKE CONCAT('%', #{courseName}, '%')</if>" +
            "   <if test='courseOrder != null and courseOrder != \"\"'> AND course_order = #{courseOrder}</if>" +
            "</where>" +
            "</script>")
    int countCourses(@Param("courseName") String courseName,
                     @Param("courseOrder") String courseOrder);

    @Insert("INSERT INTO course (course_id, tenant_id, course_name, course_cover, course_introduction, course_order, course_videos, course_author) " +
            "VALUES (#{courseId}, #{tenantId}, #{courseName}, #{courseCover}, #{courseIntroduction}, #{courseOrder}, #{courseVideos}, #{courseAuthor})")
    int addCourse(Course course);

    @Update("UPDATE course SET " +
            "course_name = #{courseName}, " +
            "course_cover = #{courseCover}, " +
            "course_introduction = #{courseIntroduction}, " +
            "course_order = #{courseOrder}, " +
            "course_videos = #{courseVideos}, " +
            "course_author = #{courseAuthor} " +
            "WHERE course_id = #{courseId}")
    int updateCourse(Course course);

    @Delete("DELETE FROM course WHERE course_id = #{courseId}")
    int deleteCourse(String courseId);

    @Select("SELECT * FROM course WHERE course_id = #{courseId}")
    Course getCourseById(String courseId);

    @Select("SELECT * FROM course ORDER BY course_order ASC")
    List<Course> getAllCourses();

    @Select("SELECT course_id as courseId, course_name as courseName, course_order as courseOrder, course_author as courseAuthor FROM course ORDER BY course_order ASC")
    List<Map<String, String>> getCourseListForExport();
}