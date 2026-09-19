package com.example.demo.service;

import io.minio.BucketExistsArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.http.Method;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.util.concurrent.TimeUnit;

@Service
public class MinioStorageService {

    private final MinioClient minioClient;
    private final MinioClient publicMinioClient;

    private final String bucket;

    public MinioStorageService(
            MinioClient minioClient,
            @Value("${minio.bucket}") String bucket,
            @Value("${minio.access-key}") String accessKey,
            @Value("${minio.secret-key}") String secretKey,
            @Value("${minio.public-url}") String publicUrl) {

        this.minioClient = minioClient;
        this.bucket = bucket;

        this.publicMinioClient =
                MinioClient.builder()
                        .endpoint(publicUrl)
                        .credentials(accessKey, secretKey)
                        .build();
    }

    public void upload(
            String objectName,
            byte[] content,
            String contentType) {

        try {

            boolean bucketExists =
                    minioClient.bucketExists(
                            BucketExistsArgs.builder()
                                    .bucket(bucket)
                                    .build()
                    );

            if (!bucketExists) {

                minioClient.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(bucket)
                                .build()
                );
            }

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .stream(
                                    new ByteArrayInputStream(content),
                                    content.length,
                                    -1
                            )
                            .contentType(contentType)
                            .build()
            );

        } catch (Exception e) {

            System.err.println(
                    "========== MINIO ERROR =========="
            );

            e.printStackTrace();

            System.err.println(
                    "================================="
            );

            throw new RuntimeException(
                    "Failed to upload file to MinIO",
                    e
            );
        }
    }

    public String getPresignedUrl(String objectName) {

        try {

            return publicMinioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucket)
                            .object(objectName)
                            .expiry(
                                    10,
                                    TimeUnit.MINUTES
                            )
                            .build()
            );

        } catch (Exception e) {

            System.err.println(
                    "========== MINIO PRESIGN ERROR =========="
            );

            e.printStackTrace();

            System.err.println(
                    "========================================="
            );

            throw new RuntimeException(
                    "Failed to generate presigned URL",
                    e
            );
        }
    }
}