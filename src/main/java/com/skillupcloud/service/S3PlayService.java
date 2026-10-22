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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class S3PlayService {

    private static final Logger log = LoggerFactory.getLogger(S3PlayService.class);

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
        log.info("Starting upload for multipart file: {}", file != null ? file.getOriginalFilename() : "null");
        if (file == null || file.isEmpty()) {
            log.warn("Upload rejected: file is null or empty");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A non-empty JSON file is required");
        }

        String fileName = safeFileName(file.getOriginalFilename());
        log.info("Validated upload file name: {} using bucket: {}", fileName, bucketName);

        byte[] content;
        try {
            content = file.getBytes();
            if (objectMapper.readTree(content) == null) {
                log.warn("Upload rejected: invalid JSON payload for file {}", fileName);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Uploaded file must contain valid JSON");
            }
        } catch (JsonProcessingException exception) {
            log.error("Upload rejected: JSON parsing failed for file {}", fileName, exception);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Uploaded file must contain valid JSON", exception);
        } catch (IOException exception) {
            log.error("Upload rejected: unable to read multipart contents for file {}", fileName, exception);
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
            log.info("Successfully uploaded file {} to S3 bucket {}", fileName, bucketName);
            return fileName;
        } catch (S3Exception exception) {
            log.error("S3 upload failed for file {} in bucket {}", fileName, bucketName, exception);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Unable to upload file to S3", exception);
        }
    }

    public byte[] readJsonFile(String fileName) {
        log.info("Attempting to read JSON file from S3: {}", fileName);
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
                log.error("File {} exists in S3 but contains invalid JSON", key);
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "S3 object does not contain valid JSON");
            }
            log.info("Successfully read JSON file {} from bucket {}", key, bucketName);
            return content;
        } catch (NoSuchKeyException exception) {
            log.warn("S3 read failed: file {} not found in bucket {}", key, bucketName, exception);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found in S3", exception);
        } catch (JsonProcessingException exception) {
            log.error("S3 read failed: JSON parse error for file {} in bucket {}", key, bucketName, exception);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "S3 object does not contain valid JSON", exception);
        } catch (IOException exception) {
            log.error("S3 read failed: unable to parse content for file {} in bucket {}", key, bucketName, exception);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Unable to parse JSON object from S3", exception);
        } catch (S3Exception exception) {
            log.error("S3 read failed: AWS SDK error for file {} in bucket {}", key, bucketName, exception);
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
            log.error("S3 configuration missing: app.s3.bucket is blank");
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "S3 bucket is not configured");
        }
    }
}
