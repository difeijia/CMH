package com.neuedu.cmh.controller;

import com.neuedu.cmh.entity.Dept;
import com.neuedu.cmh.mapper.DeptMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

@RestController
@CrossOrigin
public class DeptController {

    @Autowired
    DeptMapper deptMapper;

    //添加
    @RequestMapping("/adddept")
    public int addDept(@RequestBody Dept dept){
        //这里需要完善新增组织的信息
        String num=String.format("%03d",deptMapper.createDept_idByOrder()+10);
        dept.setDept_id("D"+num);
        dept.setCreate_time(new Date());
        dept.setUpdate_time(new Date());

        // 必填字段校验
        if (dept == null ||
                dept.getDept_name() == null || dept.getDept_name().trim().isEmpty() ) {
            throw new IllegalArgumentException("部门名称和租户ID不能为空");
        }
        // ID生成逻辑
        int sequence = deptMapper.createDept_idByOrder();
        if (sequence < 0) {
            throw new RuntimeException("部门ID生成失败");
        }

        System.out.println(dept);
        return deptMapper.addStudent(dept);
    }
    //修改
    @RequestMapping("/updatedept")
    public int updateDept(@RequestBody Dept dept){
        // 参数校验
        if (dept == null || dept.getDept_id() == null || dept.getDept_name() == null) {
            throw new IllegalArgumentException("部门ID和名称不能为空");
        }
        return deptMapper.updateDept(dept);
    }
    //删除
    @RequestMapping("/deletedept")
    public int deleteDept(String dept_id){
        // 新增 null 检查
        if (dept_id == null || dept_id.trim().isEmpty()) {
            throw new IllegalArgumentException("部门ID不能为空");
        }

        return deptMapper.deleteDept(dept_id);
    }
    //查询
    @RequestMapping("/getdept")
    public List<Dept> getStudentsByCondition(Dept dept) {
        // 新增null检查
        if (dept == null) {
            throw new IllegalArgumentException("查询条件不能为null");
        }

        return deptMapper.findDeptsByCondition(dept);
    }

}
