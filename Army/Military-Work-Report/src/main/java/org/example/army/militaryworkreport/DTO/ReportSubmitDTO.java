package org.example.army.militaryworkreport.DTO;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ReportSubmitDTO {
    private Long deptId;       // 营连ID
    private String title;      // 标题
    private String content;    // 内容
    // 注意：文件通常在Controller中作为 MultipartFile 单独接收，或者先上传文件拿到 filePath 后再传这个String
    // 这里假设前端先调文件上传接口拿到了路径，或者后端统一处理
    private String filePath;
    private Integer reportType; // 1=月计划, 2=月总结, 3=专项
    private String reportMonth; // 例如 "2025-12"
}

