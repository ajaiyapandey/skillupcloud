package com.skillupcloud.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.util.Objects;

@Service
public class S3PlayService {

    private final S3Client s3Client;
    private final ObjectMapper objectMapper;
    private final String bucketName;

    public S3PlayService(
            S3Client s3Client,
            ObjectMapper objectMapper,
            @Value("${app.s3.bucket:${S3_BUCKET:}}") String bucketName) {
        this.s3Client = s3Client;
        this.objectMapper = objectMapper;
        this.bucketName = bucketName;
    }

    public String uploadJsonFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A non-empty JSON file is required");
        }

        String fileName = safeFileName(file.getOriginalFilename());
        byte[] content;
        try {
            content = file.getBytes();
            if (objectMapper.readTree(content) == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Uploaded file must contain valid JSON");
            }
        } catch (JsonProcessingException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Uploaded file must contain valid JSON", exception);
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unable to read uploaded file", exception);
        }

        requireBucket();
        try {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(fileName)
                            .contentType("application/json")
                            .build(),
                    RequestBody.fromBytes(content));
            return fileName;
        } catch (S3Exception exception) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Unable to upload file to S3", exception);
        }
    }

    public byte[] readJsonFile(String fileName) {
        String key = safeFileName(fileName);
        requireBucket();
        try {
            ResponseBytes<GetObjectResponse> response = s3Client.getObjectAsBytes(
                    GetObjectRequest.builder()
                            .bucket(bucketName)
                            .key(key)
                            .build());
            byte[] content = response.asByteArray();
            JsonNode json = objectMapper.readTree(content);
            if (json == null) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "S3 object does not contain valid JSON");
            }
            return content;
        } catch (NoSuchKeyException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found in S3", exception);
        } catch (JsonProcessingException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "S3 object does not contain valid JSON", exception);
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Unable to parse JSON object from S3", exception);
        } catch (S3Exception exception) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Unable to read file from S3", exception);
        }
    }

    private String safeFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "fileName is required");
        }

        String normalized = fileName.replace('\\', '/');
        String key = normalized.substring(normalized.lastIndexOf('/') + 1);
        if (key.isBlank() || !key.toLowerCase().endsWith(".json")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "fileName must identify a JSON file");
        }
        return key;
    }

    private void requireBucket() {
        if (Objects.requireNonNullElse(bucketName, "").isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "S3 bucket is not configured");
        }
    }
}
