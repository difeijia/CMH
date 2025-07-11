package com.neuedu.cmh.mapper;

import com.neuedu.cmh.entity.Admin;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface AdminMapper {

@Select("SELECT admin_name, admin_pwd FROM admin WHERE admin_name = #{adminName} AND admin_pwd = #{adminPwd}")
Map<String, String> login(@Param("adminName") String adminName, @Param("adminPwd") String adminPwd);
    // 插入用户
    @Insert("INSERT INTO admin (admin_name, admin_pwd, admin_id, admin_email) VALUES (#{admin_name}, #{admin_pwd}, #{admin_id}, #{admin_email})")
    int insertAdmin(@Param("admin_id") String admin_id,
                    @Param("admin_name") String admin_name,
                    @Param("admin_pwd") String admin_pwd,
                    @Param("admin_email") String admin_email);


    //mysql
    @Select("""
    SELECT
        CONCAT(
            'A',
            LPAD(
                CAST(SUBSTRING(MAX(admin_id), 2) AS UNSIGNED) + 1,
                3,
                '0'
            )
        )
    FROM admin
    WHERE admin_id LIKE 'A%'
    """)
    String findNextAdmin_id();


    //金仓
//    @Select("""
//    SELECT
//        CONCAT(
//            'A',
//            LPAD(
//                CAST(SUBSTRING(MAX(admin_id), 2) AS UNSIGNED) + 1,
//                3,
//                '0'
//            )
//        )
//    FROM `admin` a
//    WHERE a.admin_id LIKE 'A%'
//    """)
//    String findNextAdmin_id();

    @Select("SELECT DISTINCT admin_id FROM admin WHERE admin_id IS NOT NULL AND admin_id <> ''")
    List<Integer> getAllAdmin_ids();
}
