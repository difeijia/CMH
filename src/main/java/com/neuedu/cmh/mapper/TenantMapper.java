package com.neuedu.cmh.mapper;

import com.neuedu.cmh.entity.Tenant;
import org.apache.ibatis.annotations.*;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

@Mapper
public interface TenantMapper {
    @Select("SELECT tenant_name, tenant_pwd,tenant_id FROM tenant WHERE tenant_name = #{tenantName} AND tenant_pwd = #{tenantPwd}")
    Map<String, String> login(@Param("tenantName") String tenantName, @Param("tenantPwd") String tenantPwd);
    // 插入用户
    @Insert("INSERT INTO tenant (tenant_name, tenant_pwd, tenant_id, tenant_email,admin_id,contact_phonenum) VALUES (#{tenant_name}, #{tenant_pwd}, #{tenant_id}, #{tenant_email},#{admin_id},#{contact_phonenum})")
    int insertTenant(@Param("tenant_id") String tenant_id,
                    @Param("tenant_name") String tenant_name,
                    @Param("tenant_pwd") String tenant_pwd,
                    @Param("tenant_email") String tenant_email,
                    @Param("admin_id") String admin_id,
                     @Param("contact_phonenum") String contact_phonenum);

    @Update({
            "UPDATE tenant",
            "SET tenant_logo = #{tenantLogo},",
            "tenant_name = #{tenantName},",
            "tenant_contact = #{tenantContact},",
            "contact_phonenum = #{contactPhonenum},",
            "tenant_comment = #{tenantComment},",
            "admin_id = #{adminId},",
            "tenant_email = #{tenantEmail},",
            "content = #{content}",
            "WHERE tenant_id = #{tenantId}"
    })
    int updateTenant(@Param("tenantId") String tenantId,
                     @Param("tenantLogo") String tenantLogo,
                     @Param("tenantName") String tenantName,
                     @Param("tenantContact") String tenantContact,
                     @Param("contactPhonenum") String contactPhonenum,
                     @Param("tenantComment") String tenantComment,
                     @Param("adminId") String adminId,
                     @Param("tenantEmail") String tenantEmail,
                     @Param("content") String content);


    //mysql
    @Select("""
    SELECT
        CONCAT(
            'T',
            LPAD(
                CAST(SUBSTRING(MAX(tenant_id), 2) AS UNSIGNED) + 1,
                3,
                '0'
            )
        )
    FROM tenant
    WHERE tenant_id LIKE 'T%'
    """)
    String findNextTenant_id();

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
//@Select("""
//    SELECT
//        CONCAT(
//            'T',
//            LPAD(
//                CAST(SUBSTRING(MAX(tenant_id), 2) AS UNSIGNED) + 1,
//                3,
//                '0'
//            )
//        )
//    FROM `tenant` t
//    WHERE t.tenant_id LIKE 'T%'
//    """)
//String findNextTenant_id();
//
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

    @Select("SELECT DISTINCT tenant_id FROM tenant WHERE tenant_id IS NOT NULL AND tenant_id <> ''")
    List<Integer> getAllTenant_ids();

    @Select("SELECT tenant_name, tenant_logo, tenant_contact, contact_phonenum, tenant_comment, tenant_pwd, admin_id, tenant_email,content FROM tenant WHERE tenant_id = #{tenantId}")
    Map<String, Object> getTenantInfoById(@Param("tenantId") String tenantId);

    @Select("<script>" +
            "SELECT * FROM tenant " +
            "<where>" +
            "   <if test='tenant_id != null'>" +
            "       (tenant_id LIKE CONCAT('%', #{tenant_id}, '%') OR tenant_id IS NULL)" +
            "   </if>" +
            "   <if test='tenant_contact != null'>" +
            "       AND (tenant_contact LIKE CONCAT('%', #{tenant_contact}, '%') OR tenant_contact IS NULL)" +
            "   </if>" +
            "   <if test='contact_phonenum != null'>" +
            "       AND (contact_phonenum LIKE CONCAT('%', #{contact_phonenum}, '%') OR contact_phonenum IS NULL)" +
            "   </if>" +
            "   <if test='tenant_name != null'>" +
            "       AND (tenant_name LIKE CONCAT('%', #{tenant_name}, '%') OR tenant_name IS NULL)" +
            "   </if>" +
            "</where>" +
            " ORDER BY tenant_id ASC" +
            "</script>")
    List<Tenant> searchTenants(Map<String, String> searchParams);

    @Select("SELECT * FROM tenant ORDER BY tenant_id ASC")
    List<Tenant> getAllTenants();

    @Delete("DELETE FROM tenant WHERE tenant_id = #{tenant_id}")
    Integer deleteTenant(String tenant_id);

    @Select("SELECT * FROM tenant WHERE tenant_id = #{tenant_id}")
    Tenant getTenantById(@Param("tenant_id") String tenant_id);

    @Insert({
            "INSERT INTO tenant (tenant_id, tenant_logo, tenant_name, tenant_contact, ",
            "contact_phonenum, tenant_comment, admin_id, tenant_email, content)",
            "VALUES (#{tenantId}, #{tenantLogo}, #{tenantName}, #{tenantContact}, ",
            "#{contactPhonenum}, #{tenantComment}, #{adminId}, #{tenantEmail}, #{content})"
    })
    int insertTenant2(@Param("tenantId") String tenantId,
                     @Param("tenantLogo") String tenantLogo,
                     @Param("tenantName") String tenantName,
                     @Param("tenantContact") String tenantContact,
                     @Param("contactPhonenum") String contactPhonenum,
                     @Param("tenantComment") String tenantComment,
                     @Param("adminId") String adminId,
                     @Param("tenantEmail") String tenantEmail,
                     @Param("content") String content);
}
