package com.neuedu.cmh.controller;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import com.neuedu.cmh.entity.Meeting;
import com.neuedu.cmh.mapper.MeetingMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@RestController
@RequestMapping("/api/meetings")  // 修改为/api前缀避免冲突
public class MeetingController {

    private static final Logger logger = LoggerFactory.getLogger(MeetingController.class);

    @Autowired
    private MeetingMapper meetingMapper;

    @Value("${file.upload-dir:./uploads/}")
    private String uploadDir;

    @PostMapping("/upload")
    public Map<String, Object> uploadCover(@RequestParam("file") MultipartFile file) {
        // 检查文件是否为空
        if (file == null || file.isEmpty()) {
            return Map.of("code", 400, "message", "文件不能为空");
        }

        try {
            String fileName = UUID.randomUUID() + "." +
                    StringUtils.getFilenameExtension(file.getOriginalFilename());

            Files.copy(file.getInputStream(), Paths.get(uploadDir).resolve(fileName));

            return Map.of(
                    "code", 200,
                    "data", fileName // 只返回文件名，不带路径
            );
        } catch (Exception e) {
            return Map.of("code", 500, "message", "上传失败");
        }
    }

    // 1. 会议列表查询（带搜索）
    @GetMapping
    public Map<String, Object> getMeetings(
            @RequestParam(required = false) String meetingName,
            @RequestParam(required = false) String creator,
            @RequestParam(required = false) String startTime) {

        try {
            logger.info("请求会议列表参数 - meetingName: {}, creator: {}, startTime: {}", meetingName, creator, startTime);

            Map<String, Object> params = new HashMap<>();
            params.put("meetingName", meetingName);
            params.put("creator", creator);
            params.put("startTime", startTime);

            List<Meeting> meetings = meetingMapper.findMeetings(params);
            logger.info("查询到{}条会议记录", meetings.size());

            return Map.of(
                    "code", 200,
                    "data", meetings
            );
        } catch (Exception e) {
            logger.error("获取会议列表失败", e);
            return Map.of(
                    "code", 500,
                    "message", "服务器内部错误",
                    "detail", e.getMessage()
            );
        }
    }

    @PostMapping
    @Transactional
    public Map<String, Object> createMeeting(
            @RequestParam String meetingName,
            @RequestParam String meetingContent,
            @RequestParam String creator,
            @RequestParam String startTime,
            @RequestParam String endTime,
            @RequestParam String meetingCover // 接收前端传递的文件名（替代coverFile）
    ) {
        try {
            // 1. 时间戳转换
            Date startDate = new Date(Long.parseLong(startTime));
            Date endDate = new Date(Long.parseLong(endTime));

            // 2. 构建实体
            Meeting meeting = new Meeting();
            meeting.setMeetingId("M" + UUID.randomUUID().toString().replace("-", "").substring(0, 16));
            meeting.setMeetingName(meetingName);
            meeting.setMeetingCover(meetingCover); // 直接使用前端传递的文件名
            meeting.setMeetingContent(meetingContent);
            meeting.setCreator(creator);
            meeting.setStartTime(startDate);
            meeting.setEndTime(endDate);
            meeting.setTenantId("T001");

            // 3. 插入数据库
            int result = meetingMapper.insert(meeting);
            if (result <= 0) {
                throw new RuntimeException("插入数据库失败");
            }

            return Map.of("code", 200, "message", "创建成功");
        } catch (Exception e) {
            logger.error("创建会议失败", e);
            return Map.of("code", 500, "message", "创建失败: " + e.getMessage());
        }
    }

    // 3. 导出Excel
    @GetMapping("/export")
    public ResponseEntity<?> exportExcel() {
        try {
            List<Meeting> meetings = meetingMapper.findMeetings(new HashMap<>());
            logger.info("导出{}条会议记录", meetings.size());

            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("会议列表");

            // 表头
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("会议名称");
            headerRow.createCell(1).setCellValue("创建人");
            headerRow.createCell(2).setCellValue("开始时间");
            headerRow.createCell(3).setCellValue("状态");

            // 数据行
            int rowNum = 1;
            for (Meeting meeting : meetings) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(meeting.getMeetingName());
                row.createCell(1).setCellValue(meeting.getCreator());
                row.createCell(2).setCellValue(meeting.getStartTime().toString());
                row.createCell(3).setCellValue(
                        new Date().after(meeting.getEndTime()) ? "已结束" : "进行中"
                );
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            workbook.close();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=meetings.xlsx")
                    .body(new ByteArrayResource(out.toByteArray()));
        } catch (Exception e) {
            logger.error("导出Excel失败", e);
            return ResponseEntity.status(500)
                    .body(Map.of(
                            "code", 500,
                            "message", "导出失败",
                            "detail", e.getMessage()
                    ));
        }
    }

    // 4. 修改会议
    @PutMapping("/{id}")
    public Map<String, Object> updateMeeting(
            @PathVariable String id,
            @RequestParam String meetingName,
            @RequestParam String meetingContent,
            @RequestParam String creator,
            @RequestParam String startTime,
            @RequestParam String endTime,
            @RequestParam(required = false) String meetingCover // 接收前端传递的文件名
    ) {
        try {
            Meeting meeting = meetingMapper.findById(id);
            if (meeting == null) {
                return Map.of("code", 404, "message", "会议不存在");
            }

            // 更新基本字段
            meeting.setMeetingName(meetingName);
            meeting.setMeetingContent(meetingContent);
            meeting.setCreator(creator);
            meeting.setStartTime(new Date(Long.parseLong(startTime)));
            meeting.setEndTime(new Date(Long.parseLong(endTime)));

            // 处理封面（如果有新文件名）
            if (meetingCover != null && !meetingCover.isEmpty()) {
                // 可选：删除旧封面文件（根据业务需求决定是否保留）
                if (meeting.getMeetingCover() != null) {
                    Path oldFilePath = Paths.get(uploadDir, meeting.getMeetingCover());
                    if (Files.exists(oldFilePath)) {
                        Files.delete(oldFilePath);
                    }
                }
                meeting.setMeetingCover(meetingCover); // 设置新封面文件名
            }

            int result = meetingMapper.update(meeting);
            logger.info("更新会议结果：{}", result > 0 ? "成功" : "失败");

            return result > 0 ?
                    Map.of("code", 200, "message", "更新成功") :
                    Map.of("code", 500, "message", "更新失败");
        } catch (Exception e) {
            logger.error("更新会议失败", e);
            return Map.of(
                    "code", 500,
                    "message", "更新会议时发生错误",
                    "detail", e.getMessage()
            );
        }
    }
    // 5. 删除会议
    @DeleteMapping("/{id}")
    public Map<String, Object> deleteMeeting(@PathVariable String id) {
        try {
            int result = meetingMapper.delete(id);
            logger.info("删除会议结果：{}", result > 0 ? "成功" : "失败");

            return result > 0 ?
                    Map.of("code", 200, "message", "删除成功") :
                    Map.of("code", 500, "message", "删除失败");
        } catch (Exception e) {
            logger.error("删除会议失败", e);
            return Map.of(
                    "code", 500,
                    "message", "删除会议时发生错误",
                    "detail", e.getMessage()
            );
        }
    }


}