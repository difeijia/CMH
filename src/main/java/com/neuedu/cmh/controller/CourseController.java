package com.neuedu.cmh.controller;
import com.neuedu.cmh.service.*;
import com.neuedu.cmh.entity.Course;
import com.neuedu.cmh.mapper.CourseMapper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import com.neuedu.cmh.service.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/course")
@CrossOrigin
public class CourseController {
    @Autowired
    private CourseMapper courseMapper;

    @Value("${file.upload-dir:uploads}")
    private String uploadBaseDir;


    @GetMapping("/list")
    public Map<String, Object> getCourseList(
            @RequestParam(required = false) String courseName,
            @RequestParam(required = false) String courseOrder,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {

        int offset = (page - 1) * pageSize;
        List<Course> data = courseMapper.searchCourses(courseName, courseOrder, offset, pageSize);
        int total = courseMapper.countCourses(courseName, courseOrder);

        Map<String, Object> result = new HashMap<>();
        result.put("data", data);
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        return result;
    }

    @GetMapping("/all")
    public List<Course> getAllCourses() {
        return courseMapper.getAllCourses();
    }

    @PostMapping("/add")
    public Map<String, Object> addCourse(@RequestBody Course course) {
        Map<String, Object> result = new HashMap<>();

        if (course.getCourseName() == null || course.getCourseName().isEmpty() ||
                course.getCourseIntroduction() == null || course.getCourseIntroduction().isEmpty() ||
                course.getCourseOrder() == null || course.getCourseOrder().isEmpty() ||
                course.getCourseAuthor() == null || course.getCourseAuthor().isEmpty()) {
            result.put("success", false);
            result.put("message", "课程名称、简介、排序和作者不能为空");
            return result;
        }

        course.setCourseId("C" + UUID.randomUUID().toString().replace("-", "").substring(0, 7));
        course.setTenantId("T001");

        int rows = courseMapper.addCourse(course);
        result.put("success", rows > 0);
        result.put("message", rows > 0 ? "课程添加成功" : "课程添加失败");
        return result;
    }

    @PostMapping("/update")
    public Map<String, Object> updateCourse(@RequestBody Course course) {
        Map<String, Object> result = new HashMap<>();

        if (course.getCourseName() == null || course.getCourseName().isEmpty() ||
                course.getCourseIntroduction() == null || course.getCourseIntroduction().isEmpty() ||
                course.getCourseOrder() == null || course.getCourseOrder().isEmpty() ||
                course.getCourseAuthor() == null || course.getCourseAuthor().isEmpty()) {
            result.put("success", false);
            result.put("message", "课程名称、简介、排序和作者不能为空");
            return result;
        }

        int rows = courseMapper.updateCourse(course);
        result.put("success", rows > 0);
        result.put("message", rows > 0 ? "课程修改成功" : "课程修改失败");
        return result;
    }

    @DeleteMapping("/delete/{courseId}")
    public Map<String, Object> deleteCourse(@PathVariable String courseId) {
        Map<String, Object> result = new HashMap<>();
        int rows = courseMapper.deleteCourse(courseId);
        result.put("success", rows > 0);
        result.put("message", rows > 0 ? "课程删除成功" : "课程删除失败");
        return result;
    }

    @GetMapping("/detail/{courseId}")
    public Course getCourseDetail(@PathVariable String courseId) {
        return courseMapper.getCourseById(courseId);
    }

    @PostMapping("/upload")
    public Map<String, Object> uploadFile(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 确保上传目录存在
            String subDir = determineSubDirectory(file.getContentType());
            Path uploadDir = Paths.get(uploadBaseDir, subDir);
            Files.createDirectories(uploadDir);

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String uniqueFileName = UUID.randomUUID() + fileExtension;

            // 保存文件
            Path filePath = uploadDir.resolve(uniqueFileName);
            file.transferTo(filePath);

            // 修正URL生成逻辑
            String accessUrl = "/uploads/" + subDir + "/" + uniqueFileName;
            result.put("success", true);
            result.put("url", accessUrl);
        } catch (IOException e) {
            result.put("success", false);
            result.put("message", "文件上传失败: " + e.getMessage());
        }

        return result;
    }

    private String determineSubDirectory(String contentType) {
        if (contentType.startsWith("image/")) {
            return "images";
        } else if (contentType.startsWith("video/")) {
            return "videos";
        }
        return "files";
    }

    @Configuration
    public static class WebMvcConfig implements WebMvcConfigurer {
        @Value("${file.upload-dir:uploads}")
        private String uploadDir;

        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
            // 修正资源映射路径
            String resourcePath = Paths.get(uploadDir).toAbsolutePath().toString() + "/";
            registry.addResourceHandler("/uploads/**")
                    .addResourceLocations("file:" + resourcePath);
        }
    }


    @PostMapping("/uploadCover")
    public Map<String, Object> uploadCover(@RequestParam("file") MultipartFile file) {
        return handleSpecialUpload(file, "covers/", "image/");
    }

    @PostMapping("/uploadVideo")
    public Map<String, Object> uploadVideo(@RequestParam("file") MultipartFile file) {
        if (!"video/mp4".equals(file.getContentType())) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "只支持上传MP4格式的视频");
            return result;
        }
        return handleSpecialUpload(file, "videos/", "video/");
    }

    private Map<String, Object> handleSpecialUpload(MultipartFile file, String subDir, String expectedContentType) {
        Map<String, Object> result = new HashMap<>();

        try {
            if (!file.getContentType().startsWith(expectedContentType)) {
                result.put("success", false);
                result.put("message", "不支持的文件类型");
                return result;
            }

            Path uploadDir = Paths.get(uploadBaseDir, subDir);
            Files.createDirectories(uploadDir);

            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String uniqueFileName = UUID.randomUUID() + fileExtension;

            Path filePath = uploadDir.resolve(uniqueFileName);
            file.transferTo(filePath.toFile());

            String accessUrl = "/uploads/" + subDir + uniqueFileName;
            result.put("success", true);
            result.put("url", accessUrl);
        } catch (IOException e) {
            result.put("success", false);
            result.put("message", "文件上传失败: " + e.getMessage());
        }

        return result;
    }



    @GetMapping("/export")
    public ResponseEntity<byte[]> exportCourses() throws IOException {
        List<Map<String, String>> courses = courseMapper.getCourseListForExport();

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("课程列表");

        // 创建表头
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("课程ID");
        headerRow.createCell(1).setCellValue("课程名称");
        headerRow.createCell(2).setCellValue("课程排序");
        headerRow.createCell(3).setCellValue("课程作者");

        // 填充数据
        int rowNum = 1;
        for (Map<String, String> course : courses) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(course.get("courseId"));
            row.createCell(1).setCellValue(course.get("courseName"));
            row.createCell(2).setCellValue(course.get("courseOrder"));
            row.createCell(3).setCellValue(course.get("courseAuthor"));
        }

        // 自动调整列宽
        for (int i = 0; i < 4; i++) {
            sheet.autoSizeColumn(i);
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "courses.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .body(outputStream.toByteArray());
    }
    // 在CourseController类中添加以下内容

    @Autowired
    private DeepSeekService deepSeekService;

    @PostMapping("/generate-introduction")
    public Map<String, Object> generateCourseIntroduction(@RequestBody Map<String, String> request) {
        Map<String, Object> result = new HashMap<>();
        String courseName = request.get("courseName");

        if (courseName == null || courseName.isEmpty()) {
            result.put("success", false);
            result.put("message", "课程名称不能为空");
            return result;
        }

        String prompt = "请为课程《" + courseName + "》生成一段专业、简洁的课程简介，约200字左右。";
        String introduction = deepSeekService.generateCourseContent(prompt);

        result.put("success", true);
        result.put("introduction", introduction);
        return result;
    }

    @PostMapping("/generate-name")
    public Map<String, Object> generateCourseName(@RequestBody Map<String, String> request) {
        Map<String, Object> result = new HashMap<>();
        String keyword = request.get("keyword");

        if (keyword == null || keyword.isEmpty()) {
            result.put("success", false);
            result.put("message", "关键词不能为空");
            return result;
        }

        String prompt = "请根据关键词'" + keyword + "'生成5个专业、简洁的课程名称，每个名称不超过15个字。";
        String names = deepSeekService.generateCourseContent(prompt);

        result.put("success", true);
        result.put("names", names);
        return result;
    }
    @GetMapping("/summary")
    public Map<String, Object> generateCourseSummary() {
        Map<String, Object> result = new HashMap<>();
        List<Course> courses = courseMapper.getAllCourses();

        if (courses.isEmpty()) {
            result.put("success", false);
            result.put("message", "没有课程数据可供分析");
            return result;
        }

        String prompt = deepSeekService.generateSummaryPrompt(courses);
        String summary = deepSeekService.generateCourseContent(prompt);

        result.put("success", true);
        result.put("summary", summary);
        return result;
    }

    @GetMapping("/mindmap")
    public Map<String, Object> generateMindMap() {
        Map<String, Object> result = new HashMap<>();
        List<Course> courses = courseMapper.getAllCourses();

        if (courses.isEmpty()) {
            result.put("success", false);
            result.put("message", "没有课程数据可供生成思维导图");
            return result;
        }

        String prompt = deepSeekService.generateMindMapPrompt(courses);
        String mindMap = deepSeekService.generateCourseContent(prompt);

        result.put("success", true);
        result.put("mindmap", mindMap);
        return result;
    }

    @GetMapping("/search-links")
    public Map<String, Object> generateSearchLinks() {
        Map<String, Object> result = new HashMap<>();
        List<Course> courses = courseMapper.getAllCourses();

        if (courses.isEmpty()) {
            result.put("success", false);
            result.put("message", "没有课程数据可供生成搜索链接");
            return result;
        }

        String prompt = deepSeekService.generateSearchLinksPrompt(courses);
        String links = deepSeekService.generateCourseContent(prompt);

        result.put("success", true);
        result.put("links", links);
        return result;
    }
    @Autowired
    private MoonshotService moonshotService;
    @PostMapping("/generate-introduction-moonshot")
    public Map<String, Object> generateCourseIntroductionMoonshot(@RequestBody Map<String, String> request) {
        Map<String, Object> result = new HashMap<>();
        String courseName = request.get("courseName");

        if (courseName == null || courseName.isEmpty()) {
            result.put("success", false);
            result.put("message", "课程名称不能为空");
            return result;
        }

        String prompt = "请为课程《" + courseName + "》生成一段专业、简洁的课程简介，约200字左右。";

        String introduction = moonshotService.generateCourseContent(prompt);

        result.put("success", true);
        result.put("introduction", introduction);
        return result;
    }
    @Autowired
    private MinimaxService minimaxService;
    @PostMapping("/generate-introduction-minimax")
    public Map<String, Object> generateCourseIntroductionMinimax(@RequestBody Map<String, String> request) {
        Map<String, Object> result = new HashMap<>();
        String courseName = request.get("courseName");

        if (courseName == null || courseName.isEmpty()) {
            result.put("success", false);
            result.put("message", "课程名称不能为空");
            return result;
        }

        String prompt = "请为课程《" + courseName + "》生成一段专业、简洁的课程简介，约200字左右。";

        String introduction = minimaxService.generateCourseContent(prompt);

        result.put("success", true);
        result.put("introduction", introduction);
        return result;
    }
    @GetMapping("/analysis")
    public Map<String, Object> generateCourseAnalysis() {
        Map<String, Object> result = new HashMap<>();
        List<Course> courses = courseMapper.getAllCourses();

        if (courses.isEmpty()) {
            result.put("success", false);
            result.put("message", "没有课程数据可供分析");
            return result;
        }

        // 1. 基础统计信息
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCourses", courses.size());
        stats.put("coursesWithVideo", courses.stream().filter(c -> c.getCourseVideos() != null && !c.getCourseVideos().isEmpty()).count());
        stats.put("coursesWithoutVideo", courses.stream().filter(c -> c.getCourseVideos() == null || c.getCourseVideos().isEmpty()).count());
        stats.put("uniqueAuthors", courses.stream().map(Course::getCourseAuthor).distinct().count());

        // 2. 条形图数据 - 按作者统计课程数量
        Map<String, Long> authorCourseCount = courses.stream()
                .collect(Collectors.groupingBy(Course::getCourseAuthor, Collectors.counting()));
        stats.put("authorCourseCount", authorCourseCount);

        // 3. 饼图数据 - 课程分类统计
        Map<String, Long> categoryStats = new HashMap<>();
        courses.forEach(course -> {
            String name = course.getCourseName();
            if (name != null) {
                if (name.matches(".*(编程|代码|算法|人工智能|AI|大数据|区块链).*")) {
                    categoryStats.merge("技术类课程", 1L, Long::sum);
                } else if (name.matches(".*(商业|营销|市场|管理|金融|经济).*")) {
                    categoryStats.merge("商业类课程", 1L, Long::sum);
                } else if (name.matches(".*(设计|艺术|创意|美术|UI|UX).*")) {
                    categoryStats.merge("设计类课程", 1L, Long::sum);
                } else {
                    categoryStats.merge("其他课程", 1L, Long::sum);
                }
            }
        });
        stats.put("categoryStats", categoryStats);

        // 4. 热力图数据 - 作者与课程排序关系
        Map<String, Map<String, Integer>> heatmapData = new HashMap<>();
        courses.forEach(course -> {
            String author = course.getCourseAuthor();
            String orderRange = getOrderRange(course.getCourseOrder());

            heatmapData.computeIfAbsent(author, k -> new HashMap<>())
                    .merge(orderRange, 1, Integer::sum);
        });
        stats.put("heatmapData", heatmapData);

        result.put("success", true);
        result.put("stats", stats);
        return result;
    }

    private String getOrderRange(String order) {
        try {
            int num = Integer.parseInt(order);
            if (num <= 10) return "1-10";
            if (num <= 20) return "11-20";
            if (num <= 30) return "21-30";
            return "31+";
        } catch (NumberFormatException e) {
            return "未知";
        }
    }

    // 添加Moonshot版本的摘要生成
    @GetMapping("/summary-moonshot")
    public Map<String, Object> generateCourseSummaryMoonshot() {
        Map<String, Object> result = new HashMap<>();
        List<Course> courses = courseMapper.getAllCourses();

        if (courses.isEmpty()) {
            result.put("success", false);
            result.put("message", "没有课程数据可供分析");
            return result;
        }

        String prompt = deepSeekService.generateSummaryPrompt(courses);
        String summary = moonshotService.generateCourseContent(prompt);

        result.put("success", true);
        result.put("summary", summary);
        return result;
    }

    // 添加Minimax版本的摘要生成
    @GetMapping("/summary-minimax")
    public Map<String, Object> generateCourseSummaryMinimax() {
        Map<String, Object> result = new HashMap<>();
        List<Course> courses = courseMapper.getAllCourses();

        if (courses.isEmpty()) {
            result.put("success", false);
            result.put("message", "没有课程数据可供分析");
            return result;
        }

        String prompt = deepSeekService.generateSummaryPrompt(courses);
        String summary = minimaxService.generateCourseContent(prompt);

        result.put("success", true);
        result.put("summary", summary);
        return result;
    }

    // 添加Moonshot版本的学习资源生成
    @GetMapping("/search-links-moonshot")
    public Map<String, Object> generateSearchLinksMoonshot() {
        Map<String, Object> result = new HashMap<>();
        List<Course> courses = courseMapper.getAllCourses();

        if (courses.isEmpty()) {
            result.put("success", false);
            result.put("message", "没有课程数据可供生成搜索链接");
            return result;
        }

        String prompt = deepSeekService.generateSearchLinksPrompt(courses);
        String links = moonshotService.generateCourseContent(prompt);

        result.put("success", true);
        result.put("links", links);
        return result;
    }

    // 添加Minimax版本的学习资源生成
    @GetMapping("/search-links-minimax")
    public Map<String, Object> generateSearchLinksMinimax() {
        Map<String, Object> result = new HashMap<>();
        List<Course> courses = courseMapper.getAllCourses();

        if (courses.isEmpty()) {
            result.put("success", false);
            result.put("message", "没有课程数据可供生成搜索链接");
            return result;
        }

        String prompt = deepSeekService.generateSearchLinksPrompt(courses);
        String links = minimaxService.generateCourseContent(prompt);

        result.put("success", true);
        result.put("links", links);
        return result;
    }
    @PostMapping("/generate-image")
    public Map<String, Object> generateImage(@RequestBody Map<String, String> request) {
        Map<String, Object> result = new HashMap<>();
        String prompt = request.get("prompt");
        String aiType = request.get("aiType"); // 默认使用DeepSeek

        try {
            // 1. 调用AI服务生成图片
            byte[] imageBytes;
            switch (aiType) {
                case "moonshot":
                    imageBytes = moonshotService.generateImage(prompt);
                    break;
                case "minimax":
                    imageBytes = minimaxService.generateImage(prompt);
                    break;
                default:
                    imageBytes = deepSeekService.generateImage(prompt);
            }

            // 2. 保存图片到服务器
            String fileName = "ai-image-" + UUID.randomUUID() + ".png";
            Path uploadDir = Paths.get(uploadBaseDir, "ai-images");
            Files.createDirectories(uploadDir);
            Path filePath = uploadDir.resolve(fileName);
            Files.write(filePath, imageBytes);

            // 3. 返回访问URL
            String accessUrl = "/uploads/ai-images/" + fileName;
            result.put("success", true);
            result.put("url", accessUrl);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "生成图片失败: " + e.getMessage());
        }

        return result;
    }
    @Autowired
    private StableDiffusionService stableDiffusionService;

    @PostMapping("/generate-image-sd")
    public Map<String, Object> generateImageSD(@RequestBody Map<String, String> request) {
        Map<String, Object> result = new HashMap<>();
        String prompt = request.get("prompt");

        try {
            String imageUrl = stableDiffusionService.generateImage(prompt);
            result.put("success", true);
            result.put("url", imageUrl);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "图片生成失败: " + e.getMessage());
        }

        return result;
    }
    @PostMapping("/generate-analysis-image")
    public Map<String, Object> generateAnalysisImage(@RequestBody Map<String, Object> request) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 1. 获取当前课程数据
            List<Course> courses = courseMapper.getAllCourses();

            // 2. 生成分析提示词
            String prompt = generateAnalysisPrompt(courses);

            // 3. 调用Stable Diffusion生成图片
            String imageUrl = stableDiffusionService.generateImage(prompt);

            result.put("success", true);
            result.put("url", imageUrl);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "分析图片生成失败: " + e.getMessage());
        }

        return result;
    }

    private String generateAnalysisPrompt(List<Course> courses) {
        // 1. 基础统计信息
        int totalCourses = courses.size();
        long coursesWithVideo = courses.stream().filter(c -> c.getCourseVideos() != null && !c.getCourseVideos().isEmpty()).count();
        long uniqueAuthors = courses.stream().map(Course::getCourseAuthor).distinct().count();

        // 2. 按作者统计课程数量
        Map<String, Long> authorCourseCount = courses.stream()
                .collect(Collectors.groupingBy(Course::getCourseAuthor, Collectors.counting()));

        // 3. 课程分类统计
        Map<String, Long> categoryStats = new HashMap<>();
        courses.forEach(course -> {
            String name = course.getCourseName();
            if (name != null) {
                if (name.matches(".*(编程|代码|算法|人工智能|AI|大数据|区块链).*")) {
                    categoryStats.merge("技术类课程", 1L, Long::sum);
                } else if (name.matches(".*(商业|营销|市场|管理|金融|经济).*")) {
                    categoryStats.merge("商业类课程", 1L, Long::sum);
                } else if (name.matches(".*(设计|艺术|创意|美术|UI|UX).*")) {
                    categoryStats.merge("设计类课程", 1L, Long::sum);
                } else {
                    categoryStats.merge("其他课程", 1L, Long::sum);
                }
            }
        });

        // 4. 构建学术风格的提示词
        StringBuilder prompt = new StringBuilder();
        prompt.append("请生成一张学术风格的课程分析信息图表，包含以下内容：\n\n");
        prompt.append("【总体概况】\n");
        prompt.append("- 课程总数: ").append(totalCourses).append("\n");
        prompt.append("- 含视频课程占比: ").append(String.format("%.1f%%", (coursesWithVideo * 100.0 / totalCourses))).append("\n");
        prompt.append("- 作者数量: ").append(uniqueAuthors).append("\n\n");

        prompt.append("【课程分类分析】\n");
        categoryStats.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .forEach(entry -> {
                    prompt.append("- ").append(entry.getKey())
                            .append(": ").append(entry.getValue())
                            .append(" (").append(String.format("%.1f%%", entry.getValue() * 100.0 / totalCourses))
                            .append(")\n");
                });

        prompt.append("\n【作者贡献分析】\n");
        authorCourseCount.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .forEach(entry -> {
                    prompt.append("- ").append(entry.getKey())
                            .append(": ").append(entry.getValue())
                            .append(" (").append(String.format("%.1f%%", entry.getValue() * 100.0 / totalCourses))
                            .append(")\n");
                });

        prompt.append("\n【图表要求】\n");
        prompt.append("1. 使用学术海报风格，蓝灰主色调\n");
        prompt.append("2. 包含清晰的标题《课程分析报告》\n");
        prompt.append("3. 使用信息图表形式展示数据，包含柱状图、饼图等可视化元素\n");
        prompt.append("4. 添加数据来源和日期标注\n");
        prompt.append("5. 使用专业字体，避免卡通风格\n");
        prompt.append("6. 包含关键数据点和简要分析结论\n");

        return prompt.toString();
    }

}