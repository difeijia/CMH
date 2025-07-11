package com.neuedu.cmh.mapper;
import com.neuedu.cmh.entity.Tenant;
import com.neuedu.cmh.entity.User;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.JdbcType;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Mapper
public interface UserMapper {

//    // 根据用户名和密码查询用户信息
//    @Select("SELECT user_id, user_name, user_nickname, dept_id, phonenum, mailbox, user_gender, user_job_id, createdate, user_comment, user_state, tenant_id FROM user WHERE user_name = #{user_name} AND user_pwd = #{userPwd}")
//    Map<String, Object> login(@Param("user_name") String user_name, @Param("userPwd") String userPwd);

    // 插入用户信息
    @Insert("INSERT INTO user (user_id, user_name, user_nickname, dept_id, tenant_id,phonenum, mailbox, user_gender, user_job_id, createdate, user_comment, user_pwd,user_role,user_state) VALUES (#{user_id}, #{user_name}, #{user_nickname}, #{dept_id},#{tenant_id}, #{phonenum}, #{mailbox}, #{user_gender}, #{user_job_id}, #{createdate}, #{user_comment},#{user_pwd},#{user_role}, #{user_state})")
    int insertUser(@Param("user_id") String user_id,
                   @Param("user_name") String user_name,
                   @Param("user_nickname") String user_nickname,
                   @Param("dept_id") String dept_id,
                   @Param("tenant_id") String tenant_id,
                   @Param("phonenum") String phonenum,
                   @Param("mailbox") String mailbox,
                   @Param("user_gender") String user_gender,
                   @Param("user_job_id") String user_job_id,
                   @Param("createdate") Date createdate,
                   @Param("user_comment") String user_comment,
                   @Param("user_pwd") String user_pwd,
                   @Param("user_role") String user_role,
                   @Param("user_state") String user_state
                   );

    // 更新用户信息
    @Update({
            "UPDATE user",
            "SET user_nickname = #{user_nickname},",
//            "user_name = #{user_name},",
            "dept_id = #{dept_id},",
            "tenant_id = #{tenant_id},",
            "phonenum = #{phonenum},",
            "mailbox = #{mailbox},",
            "user_gender = #{user_gender},",
            "user_job_id = #{user_job_id},",
            "user_comment = #{user_comment},",
//            "user_pwd = #{user_pwd},",
            "user_role = #{user_role},",
            "user_state = #{user_state}",
            "WHERE user_id = #{user_id}"
    })
    int updateUser(@Param("user_id") String user_id,
//                   @Param("user_name") String user_name,
                   @Param("user_nickname") String user_nickname,
                   @Param("dept_id") String dept_id,
                   @Param("tenant_id") String tenant_id,
                   @Param("phonenum") String phonenum,
                   @Param("mailbox") String mailbox,
                   @Param("user_gender") String user_gender,
                   @Param("user_job_id") String user_job_id,
                   @Param("user_comment") String user_comment,
//                   @Param("user_pwd") String user_pwd,
                   @Param("user_role") String user_role,
                   @Param("user_state") String user_state
                   );

    //mysql
    @Select("""
    SELECT
        CONCAT(
            'U',
            LPAD(
                CAST(SUBSTRING(MAX(user_id), 2) AS UNSIGNED) + 1,
                3,
                '0'
            )
        )
    FROM user
    WHERE user_id LIKE 'U%'
    """)
    String findNextUser_id();

//    //金仓
//@Select("""
//    SELECT
//        CONCAT(
//            'U',
//            LPAD(
//                CAST(SUBSTRING(MAX(user_id), 2) AS UNSIGNED) + 1,
//                3,
//                '0'
//            )
//        )
//    FROM user u
//    WHERE u.user_id LIKE 'U%'
//    """)
//String findNextUser_id();

    // 根据用户ID查询用户信息
    @Select("SELECT user_id, user_name, user_nickname, dept_id,tenant_id , phonenum, mailbox, user_gender, user_job_id, createdate, user_comment, user_state FROM user WHERE user_id = #{user_id}")
    Map<String, Object> getUserInfoById(@Param("user_id") String user_id);

    // 查询所有用户ID
    @Select("SELECT DISTINCT user_id FROM user WHERE user_id IS NOT NULL AND user_id <> ''")
    List<String> getAllUser_ids();


    @Select("<script>" +
            "SELECT * FROM user  " +
            "<where>" +
            "   <if test='user_name != null'>" +
            "       AND user_name LIKE CONCAT('%', #{user_name}, '%')" +
            "   </if>" +
            "   <if test='phonenum != null'>" +
            "       AND phonenum LIKE CONCAT('%', #{phonenum}, '%')" +
            "   </if>" +
            "   <if test='user_state != null'>" +
            "       AND user_state LIKE CONCAT('%', #{user_state}, '%')" +
            "   </if>" +
            "   <if test='startDate != null and endDate != null'>" +
            "       AND createdate BETWEEN #{startDate} AND #{endDate}" +
            "   </if>" +
            "</where>" +
            " ORDER BY user_id ASC" + // 添加排序语句
            "</script>")
    List<User> searchUsers(Map<String, Object> searchParams);

    // 删除用户
    @Delete("DELETE FROM user WHERE user_id = #{user_id}")
    int deleteUser(@Param("user_id") String user_id);

    @Select("SELECT * FROM user WHERE user_id = #{user_id}")
    User getUserById(@Param("user_id") String user_id);

    @Select("SELECT user_id, user_name, user_nickname, dept_id, tenant_id, phonenum, mailbox, user_gender, user_job_id, createdate, user_comment, user_state, user_pwd, user_role FROM user ORDER BY user_id ASC")
    List<User> getAllUsers();
}