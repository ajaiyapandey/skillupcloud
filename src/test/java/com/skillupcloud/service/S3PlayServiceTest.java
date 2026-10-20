package com.skillupcloud.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.core.sync.RequestBody;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class S3PlayServiceTest {

    private final S3Client s3Client = mock(S3Client.class);
    private final S3PlayService service = new S3PlayService(s3Client, new ObjectMapper(), "test-bucket");

    @Test
    void uploadsValidJsonUsingOriginalFileNameAsKey() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "payload.json",
                "application/json",
                "{\"message\":\"hello\"}".getBytes());

        assertEquals("payload.json", service.uploadJsonFile(file));
        verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void rejectsMalformedJsonWithoutCallingS3() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "payload.json",
                "application/json",
                "{invalid}".getBytes());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.uploadJsonFile(file));

        assertEquals(400, exception.getStatusCode().value());
        verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }
}
