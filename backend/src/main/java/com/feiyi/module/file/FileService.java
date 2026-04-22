package com.feiyi.module.file;

import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * 文件上传服务（MinIO）
 *
 * @author system
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucket;

    @Value("${minio.endpoint}")
    private String endpoint;

    /**
     * 初始化：确保bucket存在
     */
    public void ensureBucket() {
        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucket).build()
            );
            if (!exists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(bucket).build()
                );
                // 设置bucket为公开读
                String policy = """
                    {
                        "Version": "2012-10-17",
                        "Statement": [
                            {
                                "Effect": "Allow",
                                "Principal": {"AWS": ["*"]},
                                "Action": ["s3:GetObject"],
                                "Resource": ["arn:aws:s3:::%s/*"]
                            }
                        ]
                    }
                    """.formatted(bucket);
                minioClient.setBucketPolicy(
                        SetBucketPolicyArgs.builder()
                                .bucket(bucket)
                                .config(policy)
                                .build()
                );
                log.info("MinIO bucket '{}' 创建成功并设置为公开读", bucket);
            }
        } catch (Exception e) {
            log.error("MinIO bucket初始化失败", e);
        }
    }

    /**
     * 上传文件
     *
     * @param file   上传的文件
     * @param folder 存储子目录（如 image、video）
     * @return 文件访问URL
     */
    public String upload(MultipartFile file, String folder) throws Exception {
        ensureBucket();

        // 生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        String ext = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            ext = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String objectName = folder + "/" + UUID.randomUUID().toString().replace("-", "") + ext;

        // 上传到MinIO
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucket)
                        .object(objectName)
                        .stream(file.getInputStream(), file.getSize(), -1)
                        .contentType(file.getContentType())
                        .build()
        );

        // 返回访问URL
        String url = endpoint + "/" + bucket + "/" + objectName;
        log.info("文件上传成功: {}", url);
        return url;
    }

    /**
     * 删除文件
     *
     * @param objectName 对象名称（从URL中提取）
     */
    public void delete(String objectName) throws Exception {
        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(bucket)
                        .object(objectName)
                        .build()
        );
        log.info("文件删除成功: {}", objectName);
    }
}
