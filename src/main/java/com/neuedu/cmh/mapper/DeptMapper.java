package com.neuedu.cmh.mapper;

import org.apache.ibatis.annotations.*;
import com.neuedu.cmh.entity.Dept;

import java.util.List;

@Mapper
public interface DeptMapper {
    //增
    @Insert("insert into dept(dept_id,tenant_id, parent_id, dept_name,leader,email," +
            "contact_num,sort_order,status,create_time,update_time,is_deleted)  values " +
            "(#{dept_id},#{tenant_id},#{parent_id},#{dept_name},#{leader},#{email}," +
            "#{contact_num},#{sort_order},#{status},#{create_time},#{update_time},#{is_deleted})")
    int addStudent(Dept dept);
    //改,这里需要注意，因为id为自增主键，不需要初始化为空，否则会匹配错误
    @Update("update dept set dept_name=#{dept_name},parent_id=#{parent_id}," +
            "email=#{email},contact_num=#{contact_num},status=#{status}," +
            "sort_order=#{sort_order},leader=#{leader} where dept_id=#{dept_id}")
    int updateDept(Dept dept);
    //删
    @Delete("delete from dept where dept_id=#{dept_id} or parent_id=#{dept_id}")
    int deleteDept(String dept_id);
    //查(模糊查找)
    @Select("select * from dept")
    List<Dept> getAll();

    @Select("<script>" +
            "SELECT * FROM dept " +
            "<where>" +
            "   <if test='dept_name != null and dept_name != \"\"'>" +
            "       AND dept_name LIKE CONCAT('%', #{dept_name}, '%')" +
            "   </if>" +
            "   <if test='status != null and status != \"\"'>" +
            "       AND status=#{status} " +
            "   </if>" +
            "</where>" +
            "ORDER BY dept_id ASC" +
            "</script>")
    List<Dept> findDeptsByCondition(Dept dept);

    @Select("SELECT count(*) from dept")
    int createDept_idByOrder();
}
