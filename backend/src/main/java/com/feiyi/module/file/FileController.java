package com.feiyi.module.file;

import com.feiyi.common.domain.ResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 文件上传控制器
 *
 * @author system
 */
@Slf4j
@Tag(name = "文件上传接口")
@RestController
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @Operation(summary = "上传图片")
    @PostMapping("/admin/file/upload/image")
    public ResponseDTO<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseDTO.error("文件不能为空");
        }
        // 校验文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ResponseDTO.error("只能上传图片文件");
        }
        try {
            String url = fileService.upload(file, "image");
            Map<String, String> result = new HashMap<>();
            result.put("url", url);
            result.put("name", file.getOriginalFilename());
            return ResponseDTO.succ(result);
        } catch (Exception e) {
            log.error("图片上传失败", e);
            return ResponseDTO.error("上传失败: " + e.getMessage());
        }
    }

    @Operation(summary = "上传通用文件")
    @PostMapping("/admin/file/upload")
    public ResponseDTO<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file,
                                                        @RequestParam(value = "folder", defaultValue = "file") String folder) {
        if (file.isEmpty()) {
            return ResponseDTO.error("文件不能为空");
        }
        try {
            String url = fileService.upload(file, folder);
            Map<String, String> result = new HashMap<>();
            result.put("url", url);
            result.put("name", file.getOriginalFilename());
            return ResponseDTO.succ(result);
        } catch (Exception e) {
            log.error("文件上传失败", e);
            return ResponseDTO.error("上传失败: " + e.getMessage());
        }
    }
}
